/**
 * The GOAL Grammar Tools. Copyright (C) 2014 Koen Hindriks.
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

package languageTools.program.agent.actions;

import krTools.language.Substitution;
import krTools.language.Term;
import krTools.parser.SourceInfo;
import languageTools.analyzer.module.ModuleValidator;
import languageTools.parser.MOD2GParser;

/**
 * Sleeps the agent for the indicated amount of milliseconds.
 */
public class SleepAction extends NonMentalAction {
	/**
	 * Creates a {@link SleepAction}.
	 *
	 * @param parameter
	 *            The parameter that determines how long we need to sleep
	 *            (milliseconds).
	 */
	public SleepAction(Term parameter, SourceInfo info) {
		super(ModuleValidator.getTokenName(MOD2GParser.SLEEP), info);
		addParameter(parameter);
	}

	@Override
	public SleepAction applySubst(Substitution substitution) {
		return new SleepAction((getParameters() == null || getParameters().isEmpty()) ? null
				: getParameters().get(0).applySubst(substitution), getSourceInfo());
	}
}
