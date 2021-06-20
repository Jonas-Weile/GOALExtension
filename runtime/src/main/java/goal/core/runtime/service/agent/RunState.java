/**
 * The GOAL Runtime Environment. Copyright (C) 2015 Koen Hindriks.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package goal.core.runtime.service.agent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.Sets;

import eis.PerceptUpdate;
import eis.exceptions.ActException;
import eis.exceptions.EnvironmentInterfaceException;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Percept;
import events.Channel;
import events.ExecutionEventGenerator;
import events.ExecutionEventGeneratorInterface;
import events.ExecutionEventListener;
import goal.core.agent.Agent;
import goal.core.agent.AgentRegistry;
import goal.core.agent.Controller;
import goal.core.agent.DefaultEnvironmentCapabilities;
import goal.core.agent.EnvironmentCapabilities;
import goal.core.agent.GOALInterpreter;
import goal.core.agent.LoggingCapabilities;
import goal.core.executors.actions.LogActionExecutor.LogOptions;
import goal.preferences.CorePreferences;
import goal.preferences.DebugPreferences;
import goal.preferences.LoggingPreferences;
import goal.tools.IDEGOALInterpreter;
import goal.tools.adapt.Learner;
import goal.tools.debugger.DebugEvent;
import goal.tools.debugger.DebuggerKilledException;
import goal.tools.debugger.NOPDebugger;
import goal.tools.debugger.SteppingDebugger;
import goal.tools.debugger.SteppingDebugger.RunMode;
import goal.tools.errorhandling.Resources;
import goal.tools.errorhandling.Warning;
import goal.tools.errorhandling.WarningStrings;
import goal.tools.errorhandling.exceptions.GOALActionFailedException;
import goal.tools.errorhandling.exceptions.GOALLaunchFailureException;
import goal.tools.planner.Planner;
import goal.tools.planner.PlannerFactory;
import krTools.KRInterface;
import krTools.language.DatabaseFormula;
import krTools.language.Term;
import krTools.parser.SourceInfo;
import languageTools.analyzer.FileRegistry;
import languageTools.program.ProgramMap;
import languageTools.program.actionspec.UserSpecAction;
import languageTools.program.agent.AgentId;
import languageTools.program.agent.Module;
import languageTools.program.agent.Module.ExitCondition;
import languageTools.program.agent.actions.Action;
import languageTools.program.agent.actions.ActionCombo;
import languageTools.program.agent.actions.CancelTimerAction;
import languageTools.program.agent.actions.LogAction;
import languageTools.program.agent.actions.MentalAction;
import languageTools.program.agent.actions.PrintAction;
import languageTools.program.agent.actions.SendAction;
import languageTools.program.agent.actions.SleepAction;
import languageTools.program.agent.actions.StartTimerAction;
import languageTools.program.agent.actions.SubscribeAction;
import languageTools.program.agent.actions.UnsubscribeAction;
import languageTools.program.agent.msc.MentalStateCondition;
import languageTools.program.agent.msg.Message;
import languageTools.program.agent.rules.IfThenRule;
import languageTools.program.mas.AgentDefinition;
import languageTools.program.mas.UseClause.UseCase;
import mentalState.BASETYPE;
import mentalState.MentalState;
import mentalState.MentalStateWithEvents;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;
import mentalState.error.MSTTranslationException;
import mentalState.translator.Translator;
import msFactory.InstantiationFailedException;
import msFactory.MentalStateFactory;
import msFactory.translator.TranslatorFactory;

/**
 * The run state of an {@link Agent}. Normally this is called by the Agent's
 * thread.
 *
 * @param <D> The debugger type
 */
public class RunState {
	private final Controller parent;
	/**
	 * The agent's name.
	 */
	private final AgentId agentName;
	/**
	 * The AgentDefinition associated with this RunState.
	 */
	private final AgentDefinition agentDf;
	/**
	 * The global agent registry.
	 */
	private final AgentRegistry<?> registry;
	/**
	 * Counter for the number of times the agent's reasoning cycle has been
	 * performed (i.e. event module has been entered).
	 */
	private int roundCounter = 0;
	/**
	 * The {@link MentalState} of the {@link Agent}.
	 */
	private MentalStateWithEvents mentalState = null;
	/**
	 * The port to the environment.
	 */
	private final EnvironmentCapabilities environment;
	/**
	 * Logging.
	 */
	private final LoggingCapabilities logActionsLogger;
	/**
	 * Records messages from the previous round.
	 */
	private Set<Message> previousMessages = new HashSet<>(0);
	/**
	 * The init module of the {@link Agent} (if any).
	 */
	private Module initModule = null;
	/**
	 * The event module of the {@link Agent} (if any).
	 */
	private Module eventModule = null;
	/**
	 * The main module of the {@link Agent} (if any).
	 */
	private Module mainModule = null;
	/**
	 * The shutdown module of the {@link Agent} (if any).
	 */
	private Module shutdownModule = null;
	/**
	 * Stack of (non-anonymous) modules that have been entered and not yet exited;
	 * last element on the list has been entered last.
	 */
	private final Stack<Module> activeStackOfModules = new Stack<>();
	/**
	 * Top level context in which we are running now; Each of three main built-in
	 * modules is considered a run context. We're assuming by default that we're in
	 * the main context.
	 */
	private UseCase topLevelRunContext = UseCase.MAIN;
	/**
	 * The {@link ExecutionEventGenerator} that reports events in the execution.
	 */
	private final ExecutionEventGeneratorInterface eventGenerator;
	/**
	 * Learner that allows agent to learn from repeated trials.
	 */
	private final Learner learner;
	/**
	 * The timestamp (in millisecond precision) at which the run should be
	 * terminated; 0 means run indefinately.
	 */
	private final long timeout;
	/**
	 * Keep track whether sleep condition held previous cycle.
	 */
	private boolean sleepConditionsHoldingPreviousCycle;
	/**
	 * Keep track of executed actions
	 */
	private Action<?> lastAction;
	private int actionCount = 0;
	private int messageCount = 0;
	/**
	 * Timer that keeps track of the actual used CPU time.
	 */
	private final NettoRunTime timer = new NettoRunTime();
	/**
	 * Timers started with the StarTimer action.
	 */
	private final Map<AgentTimer, Future<?>> timers = new HashMap<>();
	private final ScheduledExecutorService timerservice;
	private final BlockingQueue<Percept> timerqueue = new LinkedBlockingQueue<>();
		

	/**
	 * Creates a new {@link RunState}
	 *
	 * @throws GOALLaunchFailureException
	 */
	public RunState(Controller parent, AgentId agentId, EnvironmentCapabilities environment, LoggingCapabilities logger,
			AgentDefinition agentDf, AgentRegistry<?> registry, Learner learner, long timeout)
			throws GOALLaunchFailureException {
		this.parent = parent;
		this.agentName = agentId;
		this.environment = environment;
		this.logActionsLogger = logger;

		// Store reference to program for reset.
		this.agentDf = agentDf;
		this.registry = registry;

		this.eventGenerator = createEventGenerator();

		// Get the built-in modules from the agent's program, if present.
		this.initModule = this.agentDf.getInitModule();
		this.eventModule = this.agentDf.getEventModule();
		this.mainModule = this.agentDf.getMainModule();
		this.shutdownModule = this.agentDf.getShutdownModule();
		// Check there is a main module; create a "dummy" one if there is not.
		if (this.mainModule == null) {
			// program did not specify a main module;
			// insert a fake one to make sure event module is continually run.
			this.mainModule = new Module(new FileRegistry(), null);
			// give name to module because otherwise module is considered
			// anonymous
			this.mainModule.setName("main");
			this.mainModule.addRule( // need some rule to not terminate
					new IfThenRule(new MentalStateCondition(null, null), new ActionCombo(null), null));
			if (this.eventModule == null) {
				this.mainModule.setKRInterface(this.initModule.getKRInterface());
				this.mainModule.setDefinition(this.initModule.getDefinition());
			} else {
				this.mainModule.setKRInterface(this.eventModule.getKRInterface());
				this.mainModule.setDefinition(this.eventModule.getDefinition());
			}
		}
		// Set exit condition of a main module to NEVER;
		// only (!) set if not already done so in the module file.
		this.mainModule.setExitCondition(ExitCondition.NEVER);

		try { // Create a new mental state for the agent.
			this.mentalState = MentalStateFactory.getMentalState(this.agentDf, this.agentName);
		} catch (InstantiationFailedException e) {
			throw new GOALLaunchFailureException(
					"failed to create the initial mental state for agent '" + this.agentName + "'.", e);
		}

		this.eventGenerator.event(Channel.CLEARSTATE, null, null, "initialized mental state.");

		// Configure learner.
		this.learner = learner;

		// Set timeout.
		this.timeout = timeout;

		// Initialize timer service
		this.timerservice = Executors.newScheduledThreadPool(1, new ThreadFactory() {
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r, RunState.this.agentName + " timerservice");
				t.setPriority(Thread.MIN_PRIORITY);
				return t;
			}
		});
	}

	/**
	 * @return the {@link NettoRunTime} for this runstate.
	 */
	public NettoRunTime getTimer() {
		return this.timer;
	}

	/**
	 * Default method to create the Event Generator. Override this for testing
	 * purposes.
	 *
	 * @return default event generator.
	 */
	private ExecutionEventGeneratorInterface createEventGenerator() {
		ExecutionEventGenerator generator = new ExecutionEventGenerator();
		if (LoggingPreferences.getLogToFile()) {
			generator.addListener(new ExecutionEventListener() {
				@Override
				public void goalEvent(Channel channel, Object associateObject, SourceInfo associateSource,
						String message, Object... args) {
					if (DebugPreferences.getChannelState(channel).canView()) {
						DebugEvent event = new DebugEvent(null, RunState.this.agentName.toString(), channel,
								associateObject, associateSource, message, args);
						RunState.this.logActionsLogger.log(event.toString());
					}
				}
			});
		}
		return generator;
	}

	public Controller getParent() {
		return this.parent;
	}

	/**
	 * @return A string representing agent's name.
	 */
	public AgentId getId() {
		return this.agentName;
	}

	public KRInterface getKRI() {
		return this.agentDf.getKRInterface();
	}

	/**
	 * @return The number of rounds that have been executed so far.
	 */
	public int getRoundCounter() {
		return this.roundCounter;
	}

	/**
	 * Increase the round counter by one.
	 */
	public void incrementRoundCounter() {
		++this.roundCounter;
	}

	/**
	 * Returns the {@link MentalState} of the agent's {@link RunState}.
	 *
	 * @return The mental state of the agent.
	 */
	public MentalStateWithEvents getMentalState() {
		return this.mentalState;
	}

	/**
	 * @return The agent registry.
	 */
	public AgentRegistry<?> getRegistry() {
		return this.registry;
	}

	public ProgramMap getMap() {
		return this.agentDf.getMap();
	}

	public Stack<Module> getModuleStack() {
		return this.activeStackOfModules;
	}

	/**
	 * Resetting is same as soft kill and replacing mental state with new initial
	 * mental state.
	 *
	 * TODO: merge kill and reset functionality, basically we have: - reset which
	 * now replaces mental state with initial one (TODO: possibly in the middle of
	 * an agent run(!); things are not so simple here, if we kill the agent's
	 * thread, environment entities, if any, are also freed up again, and a new
	 * agent is MAY be (re-)launched immediately instead of using this agent... (but
	 * only if the launch rules would still allow for it, which is not what we
	 * want). - soft kill that only kills agent thread - hard kill which kills agent
	 * thread, cleans up mental state, and kills connection with messaging service
	 * and TODO: environment.
	 */
	public void reset() throws GOALLaunchFailureException {
		this.roundCounter = 0;
		try {
			// Clean up old and create new initial mental state.
			if (this.mentalState != null) {
				this.mentalState.cleanUp();
			}
			this.mentalState = MentalStateFactory.getMentalState(this.agentDf, this.agentName);
			this.eventGenerator.event(Channel.CLEARSTATE, null, null, "reinitialized mental state.");
		} catch (MSTDatabaseException | MSTQueryException | InstantiationFailedException e) {
			throw new GOALLaunchFailureException(
					"Failed to re-initiate the mental state for agent '" + this.agentName + "'.", e);
		}
		this.previousMessages = new HashSet<>(0);
		this.activeStackOfModules.clear();
		this.sleepConditionsHoldingPreviousCycle = false;
		this.topLevelRunContext = UseCase.MAIN;
		for (Future<?> timer : this.timers.values()) {
			timer.cancel(true);
		}
		this.timers.clear();
		this.timerqueue.clear();
	}

	/**
	 * Terminates all the runtime resources used by the run state, specifically
	 * agent's mental state.
	 *
	 * @throws MSTQueryException
	 * @throws MSTDatabaseException
	 */
	public void dispose() throws MSTDatabaseException, MSTQueryException {
		this.timerservice.shutdownNow();
		// Check whether we need to cleanup mental state.
		if (this.mentalState != null) {
			this.mentalState.cleanUp();
			this.mentalState = null;
		}
	}

	/**
	 * Processes EIS percepts received from the agent's environment. Converts EIS
	 * {@link Percept}s to {@link DatabaseFormula}s and inserts new and removes old
	 * percepts from the percept base.
	 *
	 * @param newPercepts      The percepts to be processed.
	 * @param previousPercepts The percepts processed last round.
	 * @throws MSTTranslationException
	 * @throws MSTQueryException
	 */
	private void processPercepts(PerceptUpdate percepts) throws GOALActionFailedException {
		this.eventGenerator.event(Channel.PERCEPTS, null, null, "processing percepts.");

		// Perform the operations (if any)
		if (!percepts.isEmpty()) {
			updatePercepts(percepts.getAddList(), percepts.getDeleteList());
		}

		this.eventGenerator.event(Channel.PERCEPTS, null, null, "percepts processed.");
	}

	public void updatePercepts(List<Percept> add, List<Percept> remove) throws GOALActionFailedException {
		SourceInfo event = (getEventModule() == null) ? null : getEventModule().getDefinition();
		mentalState.Result result = this.mentalState.createResult(BASETYPE.PERCEPTBASE, this.agentName.toString());
		for (Percept delete : remove) {
			try {
				result.merge(this.mentalState.removePercept(delete, this.eventGenerator).get(0));
			} catch (MSTDatabaseException | MSTQueryException e) {
				throw new GOALActionFailedException("deleting the percept '" + delete + "' failed.", e);
			}
		}
		for (Percept insert : add) {
			try {
				result.merge(this.mentalState.percept(insert, this.eventGenerator).get(0));
			} catch (MSTDatabaseException | MSTQueryException e) {
				throw new GOALActionFailedException("inserting the percept '" + insert + "' failed.", e);
			}
		}
		this.eventGenerator.event(Channel.PERCEPTS_CONDITIONAL_VIEW, result, event);
	}

	/**
	 * Processes all given messages. Processing involves updating the mental model
	 * of the sending agent in a way that depends on the messages mood, which is
	 * indicated by the ACL's performative.
	 *
	 * @throws MSTQueryException
	 * @throws MSTDatabaseException
	 */
	private void processMessages(Set<Message> newMessages, Set<Message> previousMessages)
			throws GOALActionFailedException {
		this.eventGenerator.event(Channel.MAILS, null, null, "processing messages.", this.agentName);

		// Compute which messages need to be deleted and which messages need to
		// be added to the messages base using the list of messages from the
		// previous round. The set of messages to be deleted/added are called
		// lists for historical reasons.
		Set<Message> deleteList = Sets.difference(previousMessages, newMessages);
		Set<Message> addList = Sets.difference(newMessages, previousMessages);

		// Perform the operations (if any)
		if (!addList.isEmpty() || !deleteList.isEmpty()) {
			updateMessages(addList, deleteList);
		}

		this.eventGenerator.event(Channel.MAILS, null, null, "messages processed.");
	}

	public void updateMessages(Set<Message> add, Set<Message> remove) throws GOALActionFailedException {
		SourceInfo event = (getEventModule() == null) ? null : getEventModule().getDefinition();
		mentalState.Result result = this.mentalState.createResult(BASETYPE.MESSAGEBASE, this.agentName.toString());
		for (Message delete : remove) {
			try {
				result.merge(this.mentalState.removeMessage(delete, this.eventGenerator));
			} catch (MSTDatabaseException | MSTQueryException e) {
				throw new GOALActionFailedException("deleting the nessage '" + delete + "' failed.", e);
			}
		}
		for (Message insert : add) {
			try {
				result.merge(this.mentalState.received(insert, this.eventGenerator));
			} catch (MSTDatabaseException | MSTQueryException e) {
				throw new GOALActionFailedException("inserting the message '" + insert + "' failed.", e);
			}
		}
		this.eventGenerator.event(Channel.MAILS_CONDITIONAL_VIEW, result, event);
	}

	/**
	 * @return the event generator {@link ExecutionEventGeneratorInterface}.
	 */
	public ExecutionEventGeneratorInterface getEventGenerator() {
		return this.eventGenerator;
	}

	/**
	 * @return boolean True iff the timeout was reached.
	 */
	public boolean timedOut() {
		return (this.timeout > 0) && (System.currentTimeMillis() > this.timeout);
	}

	private PerceptUpdate getPercepts() throws GOALActionFailedException {
		try {
			PerceptUpdate percepts = this.environment.getPercepts();
			if (this.timerqueue.size() > 0) {
				List<Percept> timerPercepts = new ArrayList<>(this.timerqueue.size());
				this.timerqueue.drainTo(timerPercepts);
				PerceptUpdate timerUpdate = new PerceptUpdate(timerPercepts, new ArrayList<>(0));
				percepts.merge(timerUpdate); // FIXME: never deleted now?
			}
			return percepts;
		} catch (EnvironmentInterfaceException e) {
			throw new GOALActionFailedException(Resources.get(WarningStrings.FAILED_GET_PERCEPT), e);
		}
	}

	/**
	 * Perform preparations for starting a new cycle:
	 * <ul>
	 * <li>Increment the round counter (if not asleep).</li>
	 * <li>Display round separator via debugger (if not asleep).</li>
	 * <li>Collect and process percepts.</li>
	 * <li>Collect and process messages.</li>
	 * </ul>
	 *
	 * This function may go to sleep until there are new percepts or messages.
	 *
	 * @param isRealActionPerformed is true if a 'real' action (i.e. not a module
	 *                              call) has been performed between this call and
	 *                              the previous call to
	 *                              {@link #startCycle(boolean)}. We only consider
	 *                              going to sleep if this is false, and we also use
	 *                              this to determine whether to start a new cycle
	 *                              or not.
	 * @throws GOALActionFailedException
	 */
	public boolean startCycle(boolean isRealActionPerformed) throws GOALActionFailedException {
		if (getRoundCounter() > 1) {
			this.lastAction = null;
		}
		if (timedOut()) {
			throw new DebuggerKilledException("timeout reached", null);
		}

		Agent<?> agent = this.registry.getAgent(this.agentName);
		PerceptUpdate perceptUpdate = getPercepts();
		Set<Message> newMessages = agent.getMessages();
		boolean event = isRealActionPerformed;
		if (!event) {
			event = !perceptUpdate.isEmpty();
		}
		if (!event) {
			event = !newMessages.equals(this.previousMessages);
		}

		/**
		 * if sleep condition held previously and now, we go to sleep mode. In sleep
		 * mode we wait till new messages or percepts come in.
		 */
		if (CorePreferences.getSleepRepeatingAgent() && this.environment instanceof DefaultEnvironmentCapabilities
				&& this.sleepConditionsHoldingPreviousCycle && !event && notStepping()) {
			this.eventGenerator.event(Channel.SLEEP, null, null, "going to sleep.");
			while (!event) {
				// TODO: would be nice to be event triggered here,
				// and wake up on new message or percept, e.g., by
				// using a blocking queue. But we are using a pull model for
				// percepts... Maybe we can hand over responsibility for
				// checking our percepts to the environment port that is
				// running in its own thread and have that port notify us
				// when something has changed!?
				this.parent.endTurn();
				do {
					try {
						Thread.sleep(1);
					} catch (InterruptedException dobreak) {
						break;
					} // in sequential mode, keep sleeping until we get the turn again
				} while (CorePreferences.getSequentialExecution() && this.parent.isRunning() && !this.parent.hasTurn());
				perceptUpdate = getPercepts();
				newMessages = agent.getMessages();
				event = !perceptUpdate.isEmpty();
				if (!event) {
					event = !newMessages.equals(this.previousMessages);
				}
				if (this.parent instanceof IDEGOALInterpreter) {
					IDEGOALInterpreter interpreter = (IDEGOALInterpreter) this.parent;
					event |= (interpreter.getDebugger().getRunMode() != RunMode.RUNNING);
				}
				event |= timedOut();
			}
			this.eventGenerator.event(Channel.SLEEP, null, null, "woke up.");
		}
		// Increment round counter and display round separator via debugger.
		incrementRoundCounter();
		String prefix = "";
		int actionCount = getAndResetActionCount();
		int messageCount = getAndResetMessageCount();
		int queryCount = this.mentalState.getAndResetQueryCount();
		if (LoggingPreferences.getPrintStats()) {
			prefix = String.format(
					"non-state actions: %d, send actions %d, state queries: %d, total[beliefs: %d, goals: %d, messages: %d, percepts: %d]",
					actionCount, messageCount, queryCount, this.mentalState.getBeliefCount(),
					this.mentalState.getGoalCount(), this.mentalState.getMessageCount(),
					this.mentalState.getPerceptCount()) + "\n";
		}
		this.eventGenerator.event(Channel.REASONING_CYCLE_SEPARATOR, this.roundCounter, null,
				prefix + "+++++++ Cycle " + this.roundCounter + " +++++++");

		// Get and process percepts.
		processPercepts(perceptUpdate);
		// Get and process messages.
		processMessages(newMessages, this.previousMessages);
		this.previousMessages = newMessages;

		// Store sleep condition state
		this.sleepConditionsHoldingPreviousCycle = !event;

		return event;
	}

	private boolean notStepping() {
		if (this.parent instanceof GOALInterpreter<?>) {
			GOALInterpreter<?> interpeter = (GOALInterpreter<?>) this.parent;
			if (interpeter.getDebugger() instanceof SteppingDebugger) {
				SteppingDebugger debugger = (SteppingDebugger) interpeter.getDebugger();
				return (debugger.getRunMode() == RunMode.RUNNING);
			}
		}
		return true;
	}

	/**
	 * Returns the main module from the {@link #agentDf}. If the program does not
	 * have a main module, a "dummy" instance of a main module is returned.
	 *
	 * @return The main module of the program, or a "dummy" instance if the program
	 *         does not have a main module.
	 */
	public Module getMainModule() {
		return this.mainModule;
	}

	/**
	 * Returns the init module from the {@link #agentDf}. If the program does not
	 * have an init module, null is returned.
	 *
	 * @return The init module of the program (possibly null).
	 */
	public Module getInitModule() {
		return this.initModule;
	}

	/**
	 * Returns the event module from the {@link #agentDf}. If the program does not
	 * have an event module, null is returned.
	 *
	 * @return The event module of the program (possibly null).
	 */
	public Module getEventModule() {
		return this.eventModule;
	}

	/**
	 * Returns the shutdown module from the {@link #agentDf}. If the program does
	 * not have an shutdown module, null is returned.
	 *
	 * @return The shutdown module of the program (possibly null).
	 */
	public Module getShutdownModule() {
		return this.shutdownModule;
	}

	/**
	 * Returns the module that was entered most recently.
	 *
	 * @return The (non-anonymous) module that was entered last (including main).
	 */
	public Module getActiveModule() {
		if (this.activeStackOfModules.isEmpty()) {
			return this.mainModule;
		} else {
			return this.activeStackOfModules.peek();
		}
	}

	/**
	 * Pushes (non-anonymous) module that was just entered onto stack and changes
	 * top level context if one of init, event, or main module has been entered.
	 *
	 * @param module A (non-anonymous) module.
	 */
	public void enterModule(Module module) {
		if (module.isAnonymous()) {
			return;
		}

		this.activeStackOfModules.push(module);

		boolean main = (this.mainModule == null) ? false : module.toString().equals(this.mainModule.toString());
		boolean event = (this.eventModule == null) ? false : module.toString().equals(this.eventModule.toString());
		boolean init = (this.initModule == null) ? false : module.toString().equals(this.initModule.toString());
		if (main) {
			this.topLevelRunContext = UseCase.MAIN;
		} else if (event) {
			this.topLevelRunContext = UseCase.EVENT;
		} else if (init) {
			this.topLevelRunContext = UseCase.INIT;
		} else if (module == this.shutdownModule) {
			this.topLevelRunContext = UseCase.SHUTDOWN;
		}
		// top level context does not change for other
		// kinds of modules.
	}

	/**
	 * Removes the last entered (non-anonymous) module from the stack of active
	 * modules. Should be called when exiting *any* module. The
	 * {@link RunState#topLevelRunContext} is updated here as well.
	 *
	 * @param module The module that is exited.
	 * @return {@code true} if another module is re-entered from a non- anonymous
	 *         module.
	 */
	public boolean exitModule(Module module) {
		if (module.isAnonymous()) {
			return false;
		}
		boolean event = (this.eventModule == null) ? false : module.toString().equals(this.eventModule.toString());
		boolean init = (this.initModule == null) ? false : module.toString().equals(this.initModule.toString());
		if (event || init) {
			// We're leaving the init or event module and returning
			// to main top level context.
			this.topLevelRunContext = UseCase.MAIN;
		}
		// top level context does not change for other
		// kinds of modules. If we're leaving the main module,
		// main module should be only element on stack; in that
		// case we're leaving the agent, no need to reset context.
		if (!this.activeStackOfModules.isEmpty()) {
			this.activeStackOfModules.pop();
		}
		// Report module re-entry on module's debug channel.
		return !this.activeStackOfModules.isEmpty();
	}

	/**
	 * Check if main module is context in which we run now.
	 *
	 * @return {@code true} if main module is context in which we run now.
	 */
	public boolean isMainModuleRunning() {
		return this.topLevelRunContext.equals(UseCase.MAIN);
	}

	/**
	 * Get the action selector to be used in ADAPTIVE mode.
	 *
	 * @return the Learner
	 */
	public Learner getLearner() {
		return this.learner;
	}

	/**
	 * Get the environment reward. May return null if environment does not provide a
	 * reward.
	 *
	 * @return the reward, or null if no reward available.
	 */
	public Double getReward() {
		try {
			return this.environment.getReward();
		} catch (EnvironmentInterfaceException e) {
			// new Warning(Resources.get(WarningStrings.FAILED_ENV_GET_REWARD) +
			// " because:" + e.getMessage());
			return null;
		}
	}

	public void send(SendAction send, Message message) {
		message.setSender(this.agentName);
		Warning warning = this.registry.postMessage(message);
		if (warning != null) {
			this.eventGenerator.event(Channel.WARNING, warning, send.getSourceInfo());
		}
		++this.messageCount;
		this.lastAction = send;
	}

	private int getAndResetMessageCount() {
		final int count = this.messageCount;
		this.messageCount = 0;
		return count;
	}

	public void doPerformAction(Action<?> action) throws GOALActionFailedException {
		if (action instanceof UserSpecAction && ((UserSpecAction) action).isExternal()) {
			try {
				Translator translator = TranslatorFactory.getTranslator(getKRI());
				eis.iilang.Action eis = translator.convert((UserSpecAction) action);
				this.environment.performAction(eis);
			} catch (EnvironmentInterfaceException e) {
				if (e instanceof ActException && (((ActException) e).getType() == ActException.FAILURE
						|| ((ActException) e).getType() == ActException.NOTSPECIFIC)) {
					// Failure-type act exception, which includes e.g.
					// trying to do an action whilst the environment is paused:
					// show a warning message only.
					Warning warning = new Warning(
							String.format(Resources.get(WarningStrings.FAILED_ACTION_EXECUTE), action.toString())
									+ " because: " + e.getMessage(),
							e);
					this.eventGenerator.event(Channel.WARNING, warning, action.getSourceInfo());
				} else {
					// Other act exception, like an unrecognized action,
					// an illegal parameter, or entity problems:
					// kill the agent directly (fatal error).
					throw new GOALActionFailedException(
							String.format(Resources.get(WarningStrings.FAILED_ACTION_EXECUTE), action.toString()), e);
				}
			} catch (MSTTranslationException | InstantiationFailedException e) {
				throw new GOALActionFailedException(
						String.format(Resources.get(WarningStrings.FAILED_ACTION_EXECUTE), action.toString()), e);
			}
		} else if (action instanceof MentalAction) { // interactive console only
			throw new GOALActionFailedException("cannot perform KR actions here!");
		} else if (action instanceof LogAction) {
			log((LogAction) action);
		} else if (action instanceof PrintAction) {
			print((PrintAction) action);
		} else if (action instanceof StartTimerAction) {
			startTimer((StartTimerAction) action);
		} else if (action instanceof CancelTimerAction) {
			cancelTimer((CancelTimerAction) action);
		} else if (action instanceof SleepAction) {
			sleep((SleepAction) action);
		} else if (action instanceof SubscribeAction) {
			subscribe((SubscribeAction) action);
		} else if (action instanceof UnsubscribeAction) {
			unsubscribe((UnsubscribeAction) action);
		}
		this.lastAction = action;
		++this.actionCount;
	}

	public Action<?> getLastAction() {
		return this.lastAction;
	}

	private int getAndResetActionCount() {
		final int count = this.actionCount;
		this.actionCount = 0;
		return count;
	}

	/**
	 * Starts a timer for the given action, containing a name, interval, and
	 * duration. If a timer with the given name already exists, it will be
	 * cancelled. In any case a new timer will be created that generates a percept
	 * 'timer(name,elapsedTime)' at every given interval for the given duration. The
	 * elapsedTime is rounded to multiples of the given interval. The first percept
	 * will be generated after the first interval has elapsed, and the last
	 * percept's elapsedTime will be equal to the given duration.
	 *
	 * @param startTimer The @link{StartTimerAction}.
	 */
	private void startTimer(StartTimerAction startTimer) {
		final String name = startTimer.getParameters().get(0).toString();
		final long interval = Long.parseLong(startTimer.getParameters().get(1).toString());
		final long duration = Long.parseLong(startTimer.getParameters().get(2).toString());
		final AgentTimer timer = new AgentTimer(name, interval, duration);
		final Future<?> existing = this.timers.get(timer);
		if (existing != null) {
			existing.cancel(true);
		}
		final Future<?> future = this.timerservice.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				Percept percept = new Percept("timer", new Identifier(timer.getName()),
						new Numeral(timer.getElapsed()));
				RunState.this.timerqueue.add(percept);
				if (timer.hasFinished()) {
					Future<?> cancel = RunState.this.timers.remove(timer);
					cancel.cancel(true);
				}
			}
		}, interval, interval, TimeUnit.MILLISECONDS);
		this.timers.put(timer, future);
	}

	/**
	 * Cancels a timer with the name as given in the action if it exists and is
	 * still running; generates a warning otherwise.
	 *
	 * @param cancelTimer The @link{CancelTimerAction}.
	 */
	private void cancelTimer(CancelTimerAction cancelTimer) {
		for (final Term timername : cancelTimer.getParameters()) {
			final String name = timername.toString();
			final AgentTimer timerstub = new AgentTimer(name, 0, 0);
			final Future<?> existing = this.timers.get(timerstub);
			boolean cancelled = false;
			if (existing != null) {
				cancelled = existing.cancel(true);
			}
			if (!cancelled) {
				new Warning("timer '" + name + "' was not running or already cancelled.").emit();
			}
		}
	}

	/**
	 * Handles actual logging of {@link LogAction}.
	 *
	 * @param log
	 */
	private void log(LogAction log) {
		for (final Term paramTerm : log.getParameters()) {
			final String param = paramTerm.toString();
			boolean bb = false, gb = false, kb = false, mb = false, pb = false;
			switch (LogOptions.fromString(param)) {
			case BB:
				bb = true;
				break;
			case GB:
				gb = true;
				break;
			case KB:
				kb = true;
				break;
			case MB:
				mb = true;
				break;
			case PB:
				pb = true;
				break;
			default:
			case TEXT:
				this.logActionsLogger.log(param);
				break;
			}
			if (kb || bb || pb || mb || gb) {
				try {
					String ms = this.mentalState.toString(kb, bb, pb, mb, gb, true);
					this.logActionsLogger.log(ms);
				} catch (MSTDatabaseException | MSTQueryException e) {
					this.logActionsLogger.log(e.getMessage());
				}
			}
		}
	}

	private void print(PrintAction print) {
		for (Object parameter : print.getParameters()) {
			String output = parameter.toString();
			boolean beginQuote = output.startsWith("\"") || output.startsWith("'");
			boolean endQuote = output.endsWith("\"") || output.endsWith("'");
			output = output.substring(beginQuote ? 1 : 0, endQuote ? output.length() - 1 : output.length());
			if (this.parent instanceof GOALInterpreter<?>
					&& ((GOALInterpreter<?>) this.parent).getDebugger() instanceof NOPDebugger) {
				System.out.println(output);
			} else {
				this.eventGenerator.event(Channel.PRINT, output, print.getSourceInfo(), output);
			}
		}
	}

	private void sleep(SleepAction sleep) throws GOALActionFailedException {
		long timeout = Long.parseLong(sleep.getParameters().get(0).toString());
		if (CorePreferences.getSequentialExecution()) {
			final long endtime = System.currentTimeMillis() + timeout;
			// if executing sequentially, keep ending our turn when it is given
			// to us during
			// the sleep period
			while (System.currentTimeMillis() < endtime) {
				this.parent.endTurn();
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					break;
				}
			}
			// wait until we are given the turn again after the sleep period
			while (this.parent.isRunning() && !this.parent.hasTurn()) {
				try {
					Thread.sleep(1);
				} catch (InterruptedException e) {
					break;
				}
			}
		} else {
			try { // execute the requested sleep
				Thread.sleep(timeout);
			} catch (InterruptedException e) {
				throw new GOALActionFailedException("failed to execute '" + sleep + "'.", e);
			}
		}
	}

	private void subscribe(SubscribeAction subscribe) {
		for (Object parameter : subscribe.getParameters()) {
			this.registry.subscribe(this.agentName, parameter.toString());
		} // TODO: check results
	}

	private void unsubscribe(UnsubscribeAction unsubscribe) {
		for (Object parameter : unsubscribe.getParameters()) {
			this.registry.unsubscribe(this.agentName, parameter.toString());
		} // TODO: check results
	}

	/**
	 * Sets a new focus based on the focus method of the given module.
	 *
	 * @throws GOALActionFailedException
	 */
	public void setFocus(Module module, MentalStateCondition focus) throws GOALActionFailedException {
		MentalStateWithEvents mentalState = getMentalState();
		ExecutionEventGeneratorInterface generator = getEventGenerator();
		try {
			mentalState.Result focused = mentalState.setFocus(module.toString(), focus, module.getFocusMethod());
			if (focused != null) {
				generator.event(Channel.GB_CHANGES, focused, module.getDefinition(), "focused to goalbase '%s'.",
						focused.getFocus());
				generator.event(Channel.GB_UPDATES, focused, module.getDefinition(), focused.toString());
			}
		} catch (MSTDatabaseException | MSTQueryException e) {
			throw new GOALActionFailedException("failed to set focus to '" + module.getName() + "'.", e);
		}
	}

	/**
	 * Defocus the given module (if it was focused).
	 *
	 * @throws GOALActionFailedException
	 */
	public void removeFocus(Module module) throws GOALActionFailedException {
		MentalStateWithEvents mentalState = getMentalState();
		if (mentalState.isFocussedOn(module.toString())) {
			ExecutionEventGeneratorInterface generator = getEventGenerator();
			try {
				mentalState.Result defocused = mentalState.defocus();
				generator.event(Channel.GB_UPDATES, defocused, module.getDefinition(), defocused.toString());
				generator.event(Channel.GB_CHANGES, defocused, module.getDefinition(), "dropped goalbase '%s'",
						defocused.getFocus());
			} catch (MSTQueryException | MSTDatabaseException e) {
				throw new GOALActionFailedException("failed to defocus from '" + module.getName() + "'.", e);
			}
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (this.agentName != null) {
			builder.append("agent=").append(this.agentName).append(", ");
		}
		if (this.environment != null) {
			builder.append("environment=").append(this.environment).append(", ");
		}
		if (this.mentalState != null) {
			builder.append("state=");
			try {
				builder.append(this.mentalState.toString(true, true, true, true, true, true));
			} catch (MSTDatabaseException | MSTQueryException e) {
				builder.append(e.getMessage());
			}
			builder.append(", ");
		}
		builder.append("round=").append(this.roundCounter).append(", ");
		if (this.activeStackOfModules != null) {
			builder.append("stack=").append(this.activeStackOfModules).append(", ");
		}
		if (this.topLevelRunContext != null) {
			builder.append("context=").append(this.topLevelRunContext);
		}
		return builder.toString();
	}
}
