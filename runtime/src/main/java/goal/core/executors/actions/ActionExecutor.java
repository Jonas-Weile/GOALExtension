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

package goal.core.executors.actions;

import java.util.ArrayList;
import java.util.List;

import events.Channel;
import goal.core.executors.SelectorExecutor;
import goal.core.executors.stack.ActionStackExecutor;
import goal.core.runtime.service.agent.Result;
import goal.core.runtime.service.agent.RunState;
import goal.tools.errorhandling.exceptions.GOALActionFailedException;
import goal.tools.errorhandling.exceptions.GOALRuntimeErrorException;
import krTools.language.Substitution;
import languageTools.program.agent.AgentId;
import languageTools.program.agent.Module.FocusMethod;
import languageTools.program.agent.actions.Action;
import languageTools.program.agent.actions.AdoptAction;
import languageTools.program.agent.actions.CancelTimerAction;
import languageTools.program.agent.actions.DeleteAction;
import languageTools.program.agent.actions.DropAction;
import languageTools.program.agent.actions.ExitModuleAction;
import languageTools.program.agent.actions.InsertAction;
import languageTools.program.agent.actions.LogAction;
import languageTools.program.agent.actions.MentalAction;
import languageTools.program.agent.actions.ModuleCallAction;
import languageTools.program.agent.actions.PrintAction;
import languageTools.program.agent.actions.SendAction;
import languageTools.program.agent.actions.SleepAction;
import languageTools.program.agent.actions.StartTimerAction;
import languageTools.program.agent.actions.SubscribeAction;
import languageTools.program.agent.actions.UnsubscribeAction;
import languageTools.program.agent.actions.UserSpecCallAction;
import languageTools.program.agent.msc.MentalStateCondition;
import languageTools.program.agent.selector.Selector;
import mentalState.MSCResult;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;
import mentalState.executors.MentalStateConditionExecutor;

/**
 * Abstract class for action executors, and also factory for action executors.
 *
 * <p>
 * An action executor determines how to execute an {@link Action}: apply the
 * current substitution, evaluate its precondition, executing an environment
 * action, changing the mental state, etc.
 * <p>
 * Usually {@link ActionExecutor}s are not executed directly but inserted into
 * an {@link ActionStackExecutor}.
 */
public abstract class ActionExecutor {
	/**
	 * The action to be executed.
	 */
	private final Action<?> action;
	/**
	 * Substitution to be used for instantiating parameters of action.
	 */
	private final Substitution substitution;

	/**
	 * Executor for action.
	 *
	 * @param action
	 *            The {@link Action} to be executed.
	 *
	 * @param substitution
	 *            Substitution for instantiating parameters of the
	 *            user-specified action. These are the parameter values as on
	 *            the caller's side. e.g. <code>[X/1, Y/2]</code>. There may be
	 *            additional variables in this substi that are not used in the
	 *            actual action.
	 */
	ActionExecutor(Action<?> action, Substitution substitution) {
		this.action = action;
		this.substitution = substitution;
	}

	/**
	 * @return The action to be executed.
	 */
	public Action<?> getAction() {
		return this.action;
	}

	/**
	 * @return The substitution that has been passed on for instantiating action
	 *         parameters. These are the parameter values as on the caller's
	 *         side. e.g. <code>[X/1, Y/2]</code>. There may be additional
	 *         variables in this substi that are not used in the actual action.
	 */
	public Substitution getSourceSubstitution() {
		return this.substitution;
	}

	/**
	 * @return The substitution that ...
	 */
	public Substitution getTargetSubstitution() {
		return this.substitution;
	}

	/**
	 * Evaluates the precondition of the action. Assumes that the precondition
	 * will not yield multiple answers. <br>
	 * Progress reports must be done on the {@link RunState#getEventGenerator()}
	 *
	 * @param runState
	 *            The run state used for evaluating the precondition.
	 * @return The result of evaluating the precondition.
	 * @throws MSTQueryException
	 *             If evaluation of the precondition failed.
	 * @throws MSTDatabaseException
	 */
	public MSCResult evaluatePrecondition(RunState runState) throws GOALActionFailedException {
		MentalStateCondition msc = getAction().getPrecondition();
		MentalStateConditionExecutor msce = new MentalStateConditionExecutor(msc, getTargetSubstitution());
		try {
			return msce.evaluate(runState.getMentalState(), FocusMethod.NONE, runState.getEventGenerator());
		} catch (MSTQueryException | MSTDatabaseException e) {
			throw new GOALActionFailedException(
					"evaluation of pre-condition of '" + getAction().applySubst(getSourceSubstitution()) + "' failed.",
					e);
		}
	}

	/**
	 * Default implementation for test whether action can be executed.
	 *
	 * @return {@code true} if action is closed; {@false otherwise}.
	 */
	public boolean canBeExecuted() {
		return getAction().applySubst(getSourceSubstitution()).isClosed();
	}

	/**
	 * Implements the action specific execution method.
	 *
	 * @param runState
	 *            The current run state.
	 * @return The result of performing the action.
	 * @throws GOALActionFailedException
	 *             If executing the action failed.
	 */
	public abstract Result execute(RunState runState) throws GOALActionFailedException;

	/**
	 * Performs the action using {@link #execute(RunState)}. First evaluates the
	 * precondition, generates the resulting action executors, and then executes
	 * them.
	 *
	 * @param runState
	 *            The run state in which the action is executed.
	 * @return The result of performing the action.
	 * @throws GOALActionFailedException
	 *             If the action is not closed.
	 */
	public final Result perform(RunState runState) throws GOALActionFailedException {
		if (canBeExecuted()) {
			Result result = execute(runState);
			Channel report = Channel.ACTION_EXECUTED_BUILTIN;
			if (getAction() instanceof UserSpecCallAction) {
				report = Channel.ACTION_EXECUTED_BUILTIN;
			} else if (getAction() instanceof SendAction) {
				report = Channel.ACTION_EXECUTED_MESSAGING;
			}
			runState.getEventGenerator().event(report, getAction(), getAction().getSourceInfo(), "performed '%s'.",
					getAction().applySubst(getSourceSubstitution()));
			return result;
		} else {
			throw new GOALActionFailedException("attempt to execute '" + getAction() + "' with "
					+ getSourceSubstitution() + " left free variables: "
					+ getAction().applySubst(getSourceSubstitution()).getFreeVar() + ".");
		}
	}

	/**
	 * Returns the list of agent names that match the selector if the action is
	 * an instance of {@link RunState}; empty list otherwise.
	 *
	 * @param runstate
	 *            The {@link RunState} in which the action is executed.
	 * @return A list of agents that match the (mental) action's selector
	 */
	protected final List<AgentId> resolveSelector(RunState runState) {
		if (getAction() instanceof MentalAction) {
			MentalAction action = (MentalAction) getAction();
			// selector can be of the form (X,Y).
			Selector selector = action.getSelector().applySubst(getSourceSubstitution());
			return new SelectorExecutor(selector).evaluate(runState);
		} else {
			return new ArrayList<>(0);
		}
	}

	protected final void updateGoalState(RunState runState) throws MSTDatabaseException, MSTQueryException {
		List<mentalState.Result> allAchievedGoals = runState.getMentalState().updateGoalState();
		for (mentalState.Result achievedGoals : allAchievedGoals) {
			// TODO: message talks about dropping only now
			runState.getEventGenerator().event(Channel.GOAL_ACHIEVED, achievedGoals, this.action.getSourceInfo());
		}
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " for " + getAction().getName() + " with " + getSourceSubstitution();
	}

	/**
	 * ActionExecutor factory. Creates an action executor for the action.
	 *
	 * @param action
	 *            The action for which an executor is created.
	 * @param substitution
	 *            Substitution for instantiating the action's parameters.
	 * @return An action executor for the action.
	 */
	public static ActionExecutor getActionExecutor(Action<?> action, Substitution substitution) {
		if (action instanceof AdoptAction) {
			return new AdoptActionExecutor((AdoptAction) action, substitution);
		} else if (action instanceof DeleteAction) {
			return new DeleteActionExecutor((DeleteAction) action, substitution);
		} else if (action instanceof DropAction) {
			return new DropActionExecutor((DropAction) action, substitution);
		} else if (action instanceof ExitModuleAction) {
			return new ExitModuleActionExecutor((ExitModuleAction) action, substitution);
		} else if (action instanceof InsertAction) {
			return new InsertActionExecutor((InsertAction) action, substitution);
		} else if (action instanceof LogAction) {
			return new LogActionExecutor((LogAction) action, substitution);
		} else if (action instanceof ModuleCallAction) {
			return new ModuleCallActionExecutor((ModuleCallAction) action, substitution);
		} else if (action instanceof PrintAction) {
			return new PrintActionExecutor((PrintAction) action, substitution);
		} else if (action instanceof SendAction) {
			return new SendActionExecutor((SendAction) action, substitution);
		} else if (action instanceof SleepAction) {
			return new SleepActionExecutor((SleepAction) action, substitution);
		} else if (action instanceof SubscribeAction) {
			return new SubscribeActionExecutor((SubscribeAction) action, substitution);
		} else if (action instanceof UnsubscribeAction) {
			return new UnsubscribeActionExecutor((UnsubscribeAction) action, substitution);
		} else if (action instanceof UserSpecCallAction) {
			return new UserSpecActionExecutor((UserSpecCallAction) action, substitution);
		} else if (action instanceof StartTimerAction) {
			return new StartTimerActionExecutor((StartTimerAction) action, substitution);
		} else if (action instanceof CancelTimerAction) {
			return new CancelTimerActionExecutor((CancelTimerAction) action, substitution);
		} else {
			throw new GOALRuntimeErrorException("unknown action type '" + action + "'.");
		}
	}
}
