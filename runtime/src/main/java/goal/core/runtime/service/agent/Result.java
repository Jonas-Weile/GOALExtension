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

import languageTools.program.agent.Module;
import languageTools.program.agent.actions.Action;
import languageTools.program.agent.actions.ExitModuleAction;
import languageTools.program.agent.actions.ModuleCallAction;
import languageTools.program.agent.rules.Rule;

/**
 * Container for the results obtained by executing actions that is passed on to
 * {@link Module}s, {@link RuleSet}s, and {@link Rule}s. immutable.
 *
 * See, e.g.: {@link Module#run(goal.core.agent.Agent)} and
 * {@link Rule#run(goal.core.agent.AgentInt)}.
 */
public class Result {
	/**
	 * Flag that keeps track of whether last call to {@link #Result(Action)}
	 * reported that action was performed, i.e., Action was not {@code null}.
	 */
	private final boolean justPerformedAction;
	/**
	 * Flag that keeps track of whether, when justPerformedAction is true, the
	 * action that was performed is a 'real' action (i.e. not a module call).
	 */
	private final boolean actionWasReal;

	public final static Result START = new Result(false, false, RunStatus.RUNNING);
	public static final Result SOFTSTOP = new Result(false, false, RunStatus.SOFT_TERMINATED);

	public enum RunStatus {
		/** module running normally */
		RUNNING,
		/** someone called module.exit */
		HARD_TERMINATED,
		/** Module reached normal exit, eg last rule has been done */
		SOFT_TERMINATED;

		/**
		 * @param status
		 *            another status.
		 * @return merge of two {@link RunStatus}es. Basically, HARD_TERMINATED
		 *         > SOFT_TERMINATED > RUNNING.
		 */
		public RunStatus merge(RunStatus otherStatus) {
			if (this == RunStatus.HARD_TERMINATED || otherStatus == HARD_TERMINATED) {
				return HARD_TERMINATED;
			}
			if (this == RunStatus.SOFT_TERMINATED || otherStatus == RunStatus.SOFT_TERMINATED) {
				return RunStatus.SOFT_TERMINATED;
			}
			return RunStatus.RUNNING;
		}
	};

	private final RunStatus status;

	/**
	 * 
	 * @param performedAction
	 *            true iff module already performed some action.
	 * @param actionWasReal
	 *            true iff a real action (eg not a module call) was done
	 * @param status
	 *            the {@link RunStatus}
	 */
	public Result(boolean performedAction, boolean actionWasReal, RunStatus status) {
		this.justPerformedAction = performedAction;
		this.actionWasReal = actionWasReal;
		this.status = status;
	}

	/**
	 * Add action to the result so far. An action counts as a 'real' action and
	 * is added to the list of executed actions if it is an external
	 * UserSpecAction.
	 *
	 * @param action
	 *            The result of executing an action that is to be added to this
	 *            result, or, {@code null} if no action was performed.
	 */
	public Result(Action<?> action) {
		this(action != null, !(action instanceof ExitModuleAction) && !(action instanceof ModuleCallAction),
				action instanceof ExitModuleAction ? RunStatus.HARD_TERMINATED : RunStatus.RUNNING);
	}

	/**
	 * Checks if any action has just been performed, i.e., last call to
	 * {@link #add(Action)} reported that action was performed and Action was
	 * not {@code null}.
	 *
	 * @return {@code true} if any action has been performed; {@code false}
	 *         otherwise.
	 */
	public boolean justPerformedAction() {
		return this.justPerformedAction;
	}

	/**
	 * Checks if any 'real' action has just been performed, i.e., last call to
	 * {@link #add(Action)} reported that action was performed and Action was
	 * not {@code null}, and Action was not a {@link ModuleCallAction}.
	 *
	 * @return {@code true} if any action has been performed; {@code false}
	 *         otherwise.
	 */
	public boolean justPerformedRealAction() {
		return this.justPerformedAction && this.actionWasReal;
	}

	// /**
	// * set if if {@link ExitModuleAction} has been performed
	// *
	// * @return {@code true} if module was terminated.
	// */
	// public boolean isModuleTerminated() {
	// return status != RunStatus.RUNNING;
	// }

	/**
	 * @return the current run status.
	 */
	public RunStatus getStatus() {
		return status;
	}

	/**
	 * Merges a new result with this {@link Result}.
	 *
	 * @param result
	 *            The result to be merged with this one.
	 */
	public Result merge(Result result) {
		return new Result(justPerformedAction | result.justPerformedAction, actionWasReal | result.actionWasReal,
				status.merge(result.status));
	}

	@Override
	public String toString() {
		return "Result[" + justPerformedAction + "," + actionWasReal + "," + status + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (actionWasReal ? 1231 : 1237);
		result = prime * result + (justPerformedAction ? 1231 : 1237);
		result = prime * result + ((status == null) ? 0 : status.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Result other = (Result) obj;
		if (actionWasReal != other.actionWasReal)
			return false;
		if (justPerformedAction != other.justPerformedAction)
			return false;
		if (status != other.status)
			return false;
		return true;
	}
}