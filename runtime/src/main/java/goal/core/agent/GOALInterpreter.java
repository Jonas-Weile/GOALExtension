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
package goal.core.agent;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import events.Channel;
import events.ExecutionEventListener;
import goal.core.executors.modules.ModuleExecutor;
import goal.core.executors.stack.CallStack;
import goal.core.executors.stack.StackExecutor;
import goal.core.runtime.service.agent.NettoRunTime;
import goal.core.runtime.service.agent.RunState;
import goal.preferences.CorePreferences;
import goal.preferences.ProfilerPreferences;
import goal.tools.adapt.Learner;
import goal.tools.debugger.Debugger;
import goal.tools.debugger.DebuggerKilledException;
import goal.tools.debugger.ObservableDebugger;
import goal.tools.debugger.SteppingDebugger;
import goal.tools.errorhandling.Warning;
import goal.tools.errorhandling.exceptions.GOALActionFailedException;
import goal.tools.errorhandling.exceptions.GOALLaunchFailureException;
import goal.tools.errorhandling.exceptions.GOALRuntimeErrorException;
import goal.tools.logging.InfoLog;
import goal.tools.profiler.Profiler;
import goal.tools.profiler.Profiles;
import krTools.parser.SourceInfo;
import languageTools.program.agent.Module;
import languageTools.program.agent.Module.RuleEvaluationOrder;
import languageTools.program.agent.actions.Action;
import languageTools.program.agent.actions.ActionCombo;
import languageTools.program.agent.actions.MentalAction;
import languageTools.program.mas.AgentDefinition;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;

/**
 * Agent controller (interpreter) for an agent ({@link AgentDefinition}).
 * <p>
 * The controller can be provided with a {@link Debugger} that will be called at
 * specific points during a run.
 * </p>
 *
 * A {@link Learner} will be consulted during the Adaptive sections of a
 * AgentDefinition.
 *
 * @param <DEBUGGER> class of the Debugger used by the interpreter.
 */
public class GOALInterpreter<DEBUGGER extends Debugger> extends Controller {
	/**
	 * The {@link RunState} of this agent. Records the current state of the
	 * interpreter in the GOAL Program.
	 */
	private RunState runState;
	/**
	 * Program ran by the interpreter.
	 */
	private final AgentDefinition agentDf;
	/**
	 * The agent registry.
	 */
	private final AgentRegistry<?> registry;
	/**
	 * Debugger used while running the interpreter.
	 */
	private final DEBUGGER debugger;
	/**
	 * Learner consulted during adaptive sections of the program.
	 */
	private final Learner learner;
	/**
	 * The executor call stack
	 */
	private final CallStack stack;
	/**
	 * The profiler. null if disabled
	 */
	private Profiler profiler = null;
	/**
	 * The timeout. 0 if disabled
	 */
	private long timeout = 0;
	private final Profiles profiles;

	/**
	 * Constructs a new interpreter.
	 *
	 * @param agentDf  to run
	 * @param debugger used to debug the program
	 * @param learner  used to evaluate adaptive modules
	 * @param profiles the profiles database of all agents
	 */
	public GOALInterpreter(AgentDefinition agentDf, AgentRegistry<?> registry, DEBUGGER debugger, Learner learner,
			Profiles profiles) {
		this.agentDf = agentDf;
		this.registry = registry;
		this.debugger = debugger;
		this.learner = learner;
		this.profiles = profiles;
		this.stack = new CallStack();
	}

	/**
	 * @return the current run state of the interpreter
	 */
	public RunState getRunState() {
		return this.runState;
	}

	/*
	 * @return the current debugger used by the interpeter
	 */
	public DEBUGGER getDebugger() {
		return this.debugger;
	}

	@Override
	protected void initalizeController(Agent<? extends Controller> agent, ExecutorService executor, long timeout)
			throws GOALLaunchFailureException {
		super.initalizeController(agent, executor, timeout);
		this.timeout = timeout;
	}

	protected void createRunstate() throws GOALLaunchFailureException {
		this.runState = new RunState(this, this.agent.getId(), this.agent.getEnvironment(), this.agent.getLogging(),
				this.agentDf, this.registry, this.learner, this.timeout);

		ExecutionEventListener listener = new ExecutionEventListener() {
			@Override
			public void goalEvent(Channel channel, Object associateObject, SourceInfo associateSource, String message,
					Object... args) {
				GOALInterpreter.this.debugger.breakpoint(channel, associateObject, associateSource, message, args);
			}
		};
		this.runState.getEventGenerator().addListener(listener);

		if (ProfilerPreferences.getProfiling()) {
			this.profiler = new Profiler(this.runState.getTimer(), agentDf.getName());
			this.runState.getEventGenerator().addListener(this.profiler);
		}
	}

	@Override
	public void onReset() {
		try {
			this.debugger.reset();
			this.runState.reset();
		} catch (GOALLaunchFailureException e) {
			throw new GOALRuntimeErrorException(e); // FIXME
		}
	}

	@Override
	public void onTerminate() {
		Module shutdown = (this.runState == null) ? null : this.runState.getShutdownModule();
		if (shutdown != null) {
			try {
				CallStack temp = new CallStack();
				ModuleExecutor shutdownExec = ModuleExecutor.getModuleExecutor(temp, this.runState, shutdown,
						shutdown.getKRInterface().getSubstitution(null), RuleEvaluationOrder.LINEARALL);
				temp.push(shutdownExec);
				this.debugger.reset();
				while (temp.canExecute()) {
					temp.pop();
					temp.getPopped().getResult();
				}
			} catch (GOALActionFailedException e) {
				new Warning("failed to execute shutdown module", e).emit();
			}
		}
		this.debugger.kill();

		// show the profile results if enabled.
		if (this.profiler != null) {
			profiler.stop();
			profiles.add(this.profiler.getProfile());
			this.profiler.getProfile().log(this.agent.getId());
		}
	}

	@Override
	protected Runnable getRunnable(final ExecutorService executor, final Callable<Callable<?>> in) {
		return new Runnable() {
			@SuppressWarnings("unchecked")
			@Override
			public void run() {
				try {
					Callable<Callable<?>> call = in;
					if (call == null) {
						// Inform the timer that thread is started, and create
						// the runstate
						NettoRunTime localtimer = new NettoRunTime();
						createRunstate();
						if (GOALInterpreter.this.debugger instanceof ObservableDebugger) {
							GOALInterpreter.this.runState.getEventGenerator().event(Channel.REASONING_CYCLE_SEPARATOR,
									0, null, "starting agent.");
						} else {
							new InfoLog("starting agent '" + GOALInterpreter.this.agent.getId() + "'.").emit();
						}
						// Start the first cycle.
						boolean eventOccurred = GOALInterpreter.this.runState.startCycle(false);
						// Add the main module to the execution stack.
						Module main = GOALInterpreter.this.runState.getMainModule();
						ModuleExecutor mainExec = ModuleExecutor.getModuleExecutor(GOALInterpreter.this.stack,
								GOALInterpreter.this.runState, main, main.getKRInterface().getSubstitution(null),
								RuleEvaluationOrder.LINEAR);
						// mainExec.setSource(UseCase.MAIN);
						GOALInterpreter.this.stack.push(mainExec);
						// If we have an event module, AND there are some events
						// to proces, add it to the stack as well,
						// i.e. execute it before the main module.
						Module event = GOALInterpreter.this.runState.getEventModule();
						if (event != null && eventOccurred) {
							ModuleExecutor eventExec = ModuleExecutor.getModuleExecutor(GOALInterpreter.this.stack,
									GOALInterpreter.this.runState, event, event.getKRInterface().getSubstitution(null),
									RuleEvaluationOrder.LINEARALL);
							// eventExec.setSource(UseCase.EVENT);
							GOALInterpreter.this.stack.push(eventExec);
						}
						// If we have an init module, add it to the stack as
						// well, i.e. execute it once before the other modules.
						Module init = GOALInterpreter.this.runState.getInitModule();
						if (init != null) {
							ModuleExecutor initExec = ModuleExecutor.getModuleExecutor(GOALInterpreter.this.stack,
									GOALInterpreter.this.runState, init, init.getKRInterface().getSubstitution(null),
									RuleEvaluationOrder.LINEARALL);
							// initExec.setSource(UseCase.INIT);
							GOALInterpreter.this.stack.push(initExec);
						}
						// Make executing the first cycle the initial call.
						call = new Callable<Callable<?>>() {
							@Override
							public Callable<?> call() throws Exception {
								return executeCycle(GOALInterpreter.this.runState.getRoundCounter());
							}
						};
						// Report that we are ready now
						long took = (localtimer.get() / 1000000);
						if (GOALInterpreter.this.debugger instanceof ObservableDebugger) {
							GOALInterpreter.this.runState.getEventGenerator().event(Channel.REASONING_CYCLE_SEPARATOR,
									0, null, "started agent (took " + took + "ms).");
						} else {
							new InfoLog(
									"started agent '" + GOALInterpreter.this.agent.getId() + "' (took " + took + "ms).")
											.emit();
						}
					}
					Callable<Callable<?>> out = null;
					if (call != null) {
						// Run the current task
						out = (Callable<Callable<?>>) call.call();
					}
					if (out != null && isRunning()) {
						// Submit the next task (when any)
						executor.execute(getRunnable(executor, out));
					} else {
						// Clean-up (terminate/dispose)
						GOALInterpreter.this.learner.terminate(GOALInterpreter.this.runState.getMentalState(),
								GOALInterpreter.this.runState.getReward());
						setTerminated();
						new InfoLog("agent '" + GOALInterpreter.this.agent.getId() + "' terminated successfully.")
								.emit();
					}
				} catch (final Exception e) { // Thread failure handling
					GOALInterpreter.this.throwable = e;
					if (e instanceof DebuggerKilledException) {
						// "normal" forced termination by the debugger.
						new InfoLog("agent '" + GOALInterpreter.this.agent.getId() + "' was killed externally.", e)
								.emit();
					} else {
						// something went wrong
						new Warning("agent '" + GOALInterpreter.this.agent.getId() + "' was forcefully terminated.", e)
								.emit();
					}
					try {
						setTerminated();
					} catch (final InterruptedException ie) {
						new Warning("unable to properly terminate agent '" + GOALInterpreter.this.agent.getId() + "'.",
								ie).emit();
					}
				}
			}
		};
	}

	/**
	 * Uses the current {@link CallStack} for execution, i.e. popping (and thus
	 * executing) {@link StackExecutor}s from it, but only as long as we are in the
	 * indicated cycle. If we're not, and the stack is not empty (yet), a
	 * {@link Callable} will be returned that can be used to execute the next cycle
	 * (i.e. by feeding it into the executorservice).
	 *
	 * @param cycle the cycle to execute
	 * @return
	 * @throws GOALActionFailedException
	 */
	private Callable<Callable<?>> executeCycle(final int cycle) throws GOALActionFailedException {
		while (CorePreferences.getSequentialExecution() && isRunning() && !hasTurn()) {
			// in sequential mode, wait until we are given the turn
			try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
				break;
			}
		}
		try {
			while (isRunning() && this.stack.canExecute() && this.runState.getRoundCounter() == cycle) {
				this.stack.pop();
				this.stack.getPopped().getResult();
			}
		} finally {
			endTurn();
			this.runState.getTimer().leaveThread();
		}

		if (isRunning() && this.stack.canExecute()) {
			return new Callable<Callable<?>>() {
				@Override
				public Callable<?> call() throws Exception {
					return executeCycle(cycle + 1);
				}
			};
		} else {
			return null;
		}
	}

	/**
	 * @see {@link CallStack#getIndex()}
	 */
	public int getStackIndex() {
		return this.stack.getIndex();
	}

	/**
	 * @return the program ran by the interpreter
	 */
	public AgentDefinition getProgram() {
		return this.agentDf;
	}

	/**
	 * Execute actions manually.
	 *
	 * @param actions The actions to be executed.
	 * @throws GOALActionFailedException
	 */
	public void doPerformAction(ActionCombo actions) throws GOALActionFailedException {
		for (Action<?> action : actions.getActions()) {
			boolean keeprunning = (action instanceof MentalAction) && (this.debugger instanceof SteppingDebugger);
			if (keeprunning) {
				((SteppingDebugger) this.debugger).setKeepRunning(true);
			}
			this.runState.doPerformAction(action);
			if (keeprunning) {
				((SteppingDebugger) this.debugger).setKeepRunning(false);
			}
		}
	}

	@Override
	public void dispose() {
		this.debugger.dispose();

		try {
			if (this.runState != null) {
				this.runState.dispose();
			}
		} catch (MSTDatabaseException | MSTQueryException e) {
			throw new GOALRuntimeErrorException(e);
		}

		this.stack.clear();

		// agent is disposed by the AgentService, which is in turn disposed by
		// the RuntimeManager, so we don't need to do that here
	}
}
