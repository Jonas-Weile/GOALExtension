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
import krTools.language.Substitution;
import languageTools.program.agent.actions.ExitModuleAction;

/**
 * Executor for the exit module action.
 */
public class ExitModuleActionExecutor extends ActionExecutor {
	/**
	 * Executor for the exit module action.
	 *
	 * @param action
	 *            An exit module action.
	 * @param substitution
	 *            Substitution for instantiating parameters of the exit module
	 *            action.
	 */
	ExitModuleActionExecutor(ExitModuleAction action, Substitution substitution) {
		super(action, substitution);
	}

	@Override
	public Result execute(RunState runState) {
		return new Result(getAction());
	}
}
