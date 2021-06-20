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
package goal.tools.debugger;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import eis.iilang.EnvironmentState;
import events.Channel;
import events.NoEventGenerator;
import goal.core.agent.Agent;
import goal.core.agent.GOALInterpreter;
import goal.core.runtime.RuntimeManager;
import goal.core.runtime.service.environmentport.EnvironmentPort;
import goal.preferences.CorePreferences;
import goal.tools.eclipse.QueryTool;
import krTools.language.Substitution;
import krTools.parser.SourceInfo;
import languageTools.program.agent.AgentId;
import languageTools.program.agent.Module.FocusMethod;
import languageTools.program.agent.msc.MentalStateCondition;
import mentalState.MentalStateWithEvents;
import mentalState.executors.MentalStateConditionExecutor;

/**
 * <p>
 * Provides two functions:
 * <ul>
 * <li>Provides control over the run mode of an agent.
 * <li>Provides listener functionality so that others can hear about debug
 * events happening here.
 * </ul>
 * </p>
 *
 * <p>
 * In the core, various breakpoints have been defined for agents. At each of
 * these breakpoints, an agent will call
 * {@link SteppingDebugger#breakpoint(String, Channel)}. Upon such calls, the
 * debugger checks whether any {@link DebugObserver} that subscribed to the
 * debugger wants to view messages associated with the breakpoint.
 * </p>
 *
 * <p>
 * If someone (not necessarily a {@link DebugObserver}) wants to pause on a
 * breakpoint, the debugger enforces this and makes the agent pause on the next
 * breakpoint.
 * </p>
 *
 * <p>
 * The debugger is used to view debug events as follows:<br>
 * <ol>
 * <li>Create a {@link DebugObserver} and implement its interface.
 * <li>{@link #subscribe(DebugObserver)} the {@link DebugObserver} to interact
 * with the debugger of the agent.
 * <li>Call {@link #addPause(DebugObserver, Channel)} and
 * {@link #subscribe(DebugObserver, Channel)} as necessary.
 * <li>Run the agent.
 * </ol>
 * </p>
 * See also the notes with {@link #breakpoint(String, Channel)}
 * <p>
 * We currently do not use a standard Observer/Observable pattern because agents
 * may not be interested in all events, depending on the debug settings.
 * </p>
 * <p>
 * To control the debugger and the agent running, you can directly call
 * {@link #finestep()}, {@link #step()} and {@link #run()}. To kill the agent,
 * call {@link #kill()}.
 */
public class SteppingDebugger implements Debugger {
	/**
	 * Possible run modes of the debugger.
	 */
	public static enum RunMode {
		/**
		 * Agent runs and does not pause on any of the breakpoints.
		 * <p>
		 * Note that although breakpoints are passed over, associated messages of a
		 * breakpoint are independently reported from the observer's run mode, based on
		 * viewing subscriptions to a channel.
		 * </p>
		 */
		RUNNING(2),
		/**
		 * Agent is in stepping mode. In this mode, agent will be PAUSED at the next
		 * breakpoint that is set with {@link Debugger#addPause(Channel)}. Note that it
		 * will effectively be running if no breakpoints were set.
		 */
		STEPPING(4),
		/**
		 * Like stepping, but in this mode the agent will be paused at ANY next
		 * breakpoint (and not just on the ones set with
		 * {@link Debugger#addPause(Channel)}). When the agent actually reached the
		 * breakpoint the state changes to {@link #PAUSED}.
		 */
		FINESTEPPING(5),
		/**
		 * Agent has stepped onto a breakpoint and is not running anymore. Call
		 * {@link Debugger#step()} or {@link Debugger#finestep()} to step to next
		 * breakpoint.
		 */
		PAUSED(6),
		/**
		 * An agent is paused but the {@link QueryTool} needs to do a query.
		 */
		QUERYING(7),
		/**
		 * Corresponding agent process has been killed. Agent has been terminated.
		 * Debugger will immediately throw a {@link DebuggerKilledException} when it
		 * discovers this run mode, causing the agent to terminate on its next step.
		 */
		KILLED(10),
		/**
		 * Process state is unknown, may be any of above. This is actually a hack, only
		 * used by the environment to indicate that we don't know the environment's
		 * runstate.
		 */
		UNKNOWN(0),
		/**
		 * Process is remote, unknown. prio is 0, indicating everything more informative
		 * than this should be preferred.
		 */
		REMOTEPROCESS(0);

		private int priority;

		RunMode(int p) {
			this.priority = p;
		}

		/**
		 * @param othermode another mode
		 * @return the mode with the highest prio, given this mode and another mode.
		 */
		public RunMode merge(RunMode othermode) {
			if (this.priority > othermode.getPriority()) {
				return this;
			}
			return othermode;
		}

		/**
		 * @return the prio of this run mode.
		 */
		public int getPriority() {
			return this.priority;
		}
	}

	/**
	 * Every debugger is typically associated with a unique agent. Other names are
	 * also used, e.g. for temporary debuggers.
	 */
	protected final String name;
	/**
	 * Parent runtime
	 */
	private final RuntimeManager<?, ? extends GOALInterpreter<? extends SteppingDebugger>> manager;
	/**
	 * Runtime-global (user-defined) breakpoint flag
	 */
	protected volatile boolean global;
	/**
	 * set of channels, indicating that the debugger will pause on when hit.
	 */
	private final Set<Channel> pausingChannels = new LinkedHashSet<>();
	/**
	 * The default run mode of the debugger: run without stopping anywhere.
	 */
	protected RunMode runMode;
	/**
	 * If set to true, any encountered breakpoint will be ignored
	 */
	protected volatile boolean keepRunning = false;
	/**
	 * If a source position is in this set, we break on it.
	 */
	private final Set<SourceInfo> breakpoints = new LinkedHashSet<>();
	/**
	 * If a source position is in this set, we do not break on it, as we have
	 * already done so in the current cycle.
	 */
	private final Set<SourceInfo> had = new LinkedHashSet<>();
	/**
	 * Used in the query runmode only.
	 */
	protected volatile MentalStateCondition query;
	protected volatile Set<Substitution> queryResult;

	/**
	 * Creates debugger for given label. Names other than agent names are used for
	 * temporary debuggers. By default a new debugger has {@link RunMode#RUNNING}
	 * and {@link Channel#REASONING_CYCLE_SEPARATOR} enabled.
	 *
	 * @param name The name of the debugger.
	 * @param env  The current environment (if any), used when the 'new agents copy
	 *             environment run state' option is enabled.
	 */
	public SteppingDebugger(String name,
			RuntimeManager<?, ? extends GOALInterpreter<? extends SteppingDebugger>> manager, EnvironmentPort env) {
		this.name = name;
		this.manager = manager;
		this.runMode = getInitialRunMode();
		if (CorePreferences.getAgentCopyEnvRunState()) {
			final EnvironmentState state = (env == null) ? null : env.getEnvironmentState();
			if (state == null || state.equals(EnvironmentState.RUNNING)) {
				this.runMode = RunMode.RUNNING;
			} else if (state.equals(EnvironmentState.PAUSED)) {
				this.runMode = RunMode.STEPPING;
			} // killed or initializing? leave the default...
		}
	}

	/**
	 * Creates debugger for given agent. By default a new debugger has
	 * {@link RunMode#RUNNING} and {@link Channel#REASONING_CYCLE_SEPARATOR}
	 * enabled.
	 *
	 * @param id  The relevant agent (its name will be used for the debugger).
	 * @param env The current environment (if any), used when the 'new agents copy
	 *            environment run state' option is enabled.
	 */
	public SteppingDebugger(AgentId id,
			RuntimeManager<?, ? extends GOALInterpreter<? extends SteppingDebugger>> manager, EnvironmentPort env) {
		this(id.toString(), manager, env);
	}

	/**
	 * @return The initial run mode for the debugger. This is RUNNING by default,
	 *         but subclasses can override this.
	 */
	protected RunMode getInitialRunMode() {
		return RunMode.RUNNING;
	}

	protected void setGlobalBreakpoint() {
		this.global = true;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see goal.tools.debugger.IDebugger#getName()
	 */
	@Override
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the overall run mode.
	 *
	 * @return overall run mode of debugger.
	 */
	public RunMode getRunMode() {
		return this.runMode;
	}

	/**
	 * Changes the run mode. Only notifies observers of a change in run mode.
	 * <p>
	 * Also wakes up the debugged thread of the agent, to check if it can proceed
	 * running.
	 * </p>
	 *
	 * @param mode The run mode for the debugger.
	 */
	protected void setRunMode(RunMode mode) {
		if (this.runMode != mode) {
			synchronized (this) {
				this.runMode = mode;
				// wake up any processes that have been paused.
				notifyAll();
			}
		}
	}

	public boolean keepRunning() {
		return this.keepRunning;
	}

	public void setKeepRunning(boolean keepRunning) {
		this.keepRunning = keepRunning;
	}

	/***************************************************************/
	/************** FUNCTIONS TO ENFORCE DEBUGGING. ***************/

	/********** TO BE CALLED BY THREAD TO BE DEBUGGED. ************/
	/**************************************************************/

	/*
	 * (non-Javadoc)
	 *
	 * @see goal.tools.debugger.IDebugger#breakpoint(goal.tools.debugger.Channel,
	 * java.lang.Object, java.lang.String, java.lang.Object)
	 */
	@SuppressWarnings("fallthrough")
	@Override
	public void breakpoint(Channel channel, Object associateObject, SourceInfo associateSource, String message,
			Object... args) {
		if (channel == Channel.REASONING_CYCLE_SEPARATOR) {
			this.had.clear();
		} else if (!keepRunning()) {
			if (this.global) {
				this.global = false;
				setRunMode(RunMode.STEPPING);
			} else if (channel == Channel.TESTFAILURE || checkUserBreakpointHit(associateSource, channel)) {
				setRunMode(RunMode.PAUSED);
			}
		}
		// Process special hidden run mode channel.
		switch (getRunMode()) {
		case KILLED:
			throw new DebuggerKilledException();
		case RUNNING:
			break; // just continue.
		case STEPPING:
			if (!this.pausingChannels.contains(channel)) {
				break;
			}
			// we need to pause on this channel, fall through;
		case FINESTEPPING:
			// pause on any channel that we encounter when in fine stepping
			// mode, and fall through (await unpause)
			if (!keepRunning()) {
				setRunMode(RunMode.PAUSED);
			}
		case PAUSED:
			// Wait for agent to wake up.
			if (!keepRunning()) {
				awaitUnPause();
			}
			break;
		default:
		case UNKNOWN:
		case REMOTEPROCESS:
			// nothing we can do.
			break;
		}
		return;
	}

	/**
	 * Waits as long as we are in {@link RunMode#PAUSED} mode.
	 */
	protected void awaitUnPause() {
		synchronized (this) {
			while (getRunMode() == RunMode.PAUSED) {
				try {
					wait();
				} catch (InterruptedException e) {
					throw new DebuggerKilledException();
				}
			}
		}

		// We can either be interrupted while waiting
		// or the run mode can be changed.
		// When killed this thread has to break out ASAP.
		final RunMode runMode = getRunMode();
		if (runMode == RunMode.KILLED) {
			throw new DebuggerKilledException();
		} else if (runMode == RunMode.QUERYING) {
			runQuery();
			this.runMode = RunMode.PAUSED;
			awaitUnPause();
		}
	}

	private void runQuery() {
		try {
			final GOALInterpreter<?> controller = this.manager.getAgent(new AgentId(this.name)).getController();
			final MentalStateWithEvents mentalState = controller.getRunState().getMentalState();
			final Substitution empty = mentalState.getOwner().getKRInterface().getSubstitution(null);
			this.queryResult = new MentalStateConditionExecutor(this.query, empty)
					.evaluate(mentalState, FocusMethod.NONE, new NoEventGenerator()).getAnswers();
		} catch (final Exception e) {
			e.printStackTrace(); // FIXME
			this.queryResult = new HashSet<>(0);
		}
		this.query = null;
	}

	public Substitution[] query(final MentalStateCondition msc) {
		if (getRunMode() != RunMode.PAUSED) {
			return null;
		}

		this.query = msc;
		setRunMode(RunMode.QUERYING);
		while (this.queryResult == null) {
			try {
				Thread.sleep(1);
			} catch (final InterruptedException ie) {
				return null;
			}
		}

		final Substitution[] result = this.queryResult.toArray(new Substitution[this.queryResult.size()]);
		this.queryResult = null;
		return result;
	}

	// ******************** notification methods ***************************/

	/***************************************************************/
	/************** FUNCTIONS TO CONTROL DEBUGGING *****************/
	/***************************************************************/

	/******************** control methods ***************************/

	/**
	 * Puts the {@link SteppingDebugger} in {@link RunMode#STEPPING} mode. The
	 * debugger will then halt on the first breakpoint that someone is listening to.
	 */
	public void step() {
		setRunMode(RunMode.STEPPING);
	}

	/**
	 * Puts the {@link SteppingDebugger} in {@link RunMode#FINESTEPPING} mode.
	 */
	public void finestep() {
		setRunMode(RunMode.FINESTEPPING);
	}

	/**
	 * Puts the {@link SteppingDebugger} into {@link RunMode#RUNNING} mode.
	 */
	public void run() {
		setRunMode(RunMode.RUNNING);
	}

	/**
	 * Set the agent's run mode to {@link RunMode#KILLED}.
	 * <p>
	 * This does not immediately kill the agent; the idea instead is to throw
	 * {@link DebuggerKilledException} when the agent hits the next breakpoint (or
	 * is already at a breakpoint), which should the agent.
	 * </p>
	 */
	@Override
	public void kill() {
		setRunMode(RunMode.KILLED);
	}

	/**
	 * add channel to pause channels. The agent will be paused when it hits a
	 * breakpoint on this channel. See also {@link DebugSettingSynchronizer}.
	 *
	 * @param channel is channel that causes agent to pause.
	 */
	public void addPause(Channel channel) {
		this.pausingChannels.add(channel);
	}

	/**
	 * remove channel from pause channels. Agent will not be paused anymore when
	 * agent hits breakpoint on this channel.
	 *
	 * @param channel is channel that causes agent to pause.
	 */
	public void removePause(Channel channel) {
		this.pausingChannels.remove(channel);
	}

	/**
	 * Returns a brief description of the {@link SteppingDebugger}, including: its
	 * name, observers per channel, and channels on which to pause when in stepping
	 * mode. Details of the exact representation or format are not specified here.
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder("Debugger name = ");
		builder.append(this.name);
		builder.append("\nPausing channels:\n");
		builder.append(this.pausingChannels.toString());
		builder.append("\nRunmode = ");
		builder.append(this.runMode);
		return builder.toString();
	}

	/********************************************************************/
	/********************* Breakpoint Handling **************************/

	/********************************************************************/
	/**
	 * Check if we hit a user set breakpoint.
	 */
	protected boolean checkUserBreakpointHit(SourceInfo source, Channel channel) {
		if (source != null && this.breakpoints.contains(source) && channel.getLevel() > 0
				&& channel.getLevel() < Integer.MAX_VALUE) {
			if (this.had.contains(source)) {
				return false;
			} else {
				this.had.add(source);
				if (CorePreferences.getGlobalBreakpoints() && this.manager != null) {
					for (Agent<? extends GOALInterpreter<? extends SteppingDebugger>> agent : this.manager
							.getAgents()) {
						SteppingDebugger other = agent.getController().getDebugger();
						other.setGlobalBreakpoint();
					}
				}
				return true;
			}
		} else {
			return false;
		}
	}

	/**
	 * set breakpoints for this observer to a given set of {@link IParsedObject}s.
	 *
	 * @param breakpoints The set of breakpoints.
	 */
	public void setBreakpoints(Set<SourceInfo> breakpoints) {
		this.breakpoints.clear();
		if (breakpoints != null) {
			this.breakpoints.addAll(breakpoints);
		}
	}

	@Override
	public void reset() {
		setRunMode(getInitialRunMode());
	}

	@Override
	public void dispose() {
		// Does nothing.
	}
}