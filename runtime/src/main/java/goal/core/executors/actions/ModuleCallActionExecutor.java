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

import goal.core.runtime.service.agent.Result;
import goal.core.runtime.service.agent.RunState;
import goal.tools.errorhandling.exceptions.GOALActionFailedException;
import krTools.language.Substitution;
import languageTools.program.agent.Module.FocusMethod;
import languageTools.program.agent.actions.ModuleCallAction;
import languageTools.program.agent.msc.MentalStateCondition;
import mentalState.MSCResult;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;
import mentalState.executors.MentalStateConditionExecutor;

/**
 * Executor for the module call action.
 */
public class ModuleCallActionExecutor extends ActionExecutor {
	/**
	 * Executor for the module call action.
	 *
	 * @param action
	 *            A module call action.
	 * @param substitution
	 *            Substitution for instantiating parameters of the module call
	 *            action.
	 */
	ModuleCallActionExecutor(ModuleCallAction action, Substitution substitution) {
		super(action, substitution);
	}

	/**
	 * A module can always be executed and has no precondition (or a precondition
	 * {@code true}, if you like). <br>
	 * Execute reports will be done with {@link RunState#getEventGenerator()}
	 *
	 * @throws MSTQueryException
	 *             If evaluation of precondition failed.
	 * @throws MSTDatabaseException
	 */
	@Override
	public MSCResult evaluatePrecondition(RunState runState) throws GOALActionFailedException {
		ModuleCallAction action = ((ModuleCallAction) getAction());
		MentalStateCondition msc = getAction().getPrecondition();
		MentalStateConditionExecutor msce = new MentalStateConditionExecutor(msc, getSourceSubstitution());
		try {
			return msce.evaluate(runState.getMentalState(), FocusMethod.NONE, runState.getEventGenerator());
		} catch (MSTQueryException | MSTDatabaseException e) {
			throw new GOALActionFailedException(
					"evaluation of pre-condition of '" + action.applySubst(getSourceSubstitution()) + "' failed.", e);
		}
	}

	/**
	 * A module call action which does not have a named module as target, i.e. is
	 * associated with a set of nested rules, can always be executed. Otherwise, the
	 * parameters of the action should be closed.
	 *
	 * @return {@code true} if targeted module is either anonymous, or the
	 *         parameters of the module call are closed.
	 */
	@Override
	public boolean canBeExecuted() {
		if (((ModuleCallAction) getAction()).getTarget().isAnonymous()) {
			return true;
		} else {
			return super.canBeExecuted();
		}
	}

	@Override
	public Result execute(RunState runState) throws GOALActionFailedException {
		throw new RuntimeException("a ModuleCallAction cannot be executed independently.");
	}

}