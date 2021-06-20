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
import languageTools.program.agent.actions.Action;
import languageTools.program.agent.actions.StartTimerAction;

/**
 * Executor for the start timer action.
 */
public class StartTimerActionExecutor extends ActionExecutor {
	/**
	 * Executor for the start timer action.
	 *
	 * @param action
	 *            A start timer action.
	 * @param substitution
	 *            Substitution for instantiating parameters of the action.
	 */
	StartTimerActionExecutor(StartTimerAction action, Substitution substitution) {
		super(action, substitution);
	}

	@Override
	public Result execute(RunState runState) throws GOALActionFailedException {
		Action<?> instantiated = getAction().applySubst(getSourceSubstitution());
		runState.doPerformAction(instantiated);
		return new Result(instantiated);
	}
}
