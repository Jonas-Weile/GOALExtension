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
import languageTools.program.agent.actions.LogAction;

/**
 * Executor for the log action.
 */
public class LogActionExecutor extends ActionExecutor {
	/**
	 * Executor for the log action.
	 *
	 * @param action
	 *            A log action.
	 * @param substitution
	 *            Substitution for instantiating parameters of the log action.
	 */
	LogActionExecutor(LogAction action, Substitution substitution) {
		super(action, substitution);
	}

	@Override
	public Result execute(RunState runState) throws GOALActionFailedException {
		Action<?> instantiated = getAction().applySubst(getSourceSubstitution());
		runState.doPerformAction(instantiated);
		return new Result(instantiated);
	}

	/**
	 * Available options for logging.
	 */
	public enum LogOptions {
		/** export belief base */
		BB,
		/** export goal base */
		GB,
		/** export percept base */
		PB,
		/** export mailbox */
		MB,
		/** export knowledge base */
		KB,
		/** export plain text */
		TEXT;

		/**
		 * Maps a string to an {@link LogOptions}.
		 *
		 * @param type
		 *            A string representing the type of logging to be done.
		 * @return The action type that corresponds with the parameter.
		 */
		public static LogOptions fromString(String type) {
			try {
				return valueOf(type.toUpperCase());
			} catch (IllegalArgumentException e) {
				return TEXT;
			}
		}
	}
}
