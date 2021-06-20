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

package languageTools.program.agent.rules;

import krTools.language.Substitution;
import krTools.parser.SourceInfo;
import languageTools.analyzer.module.ModuleValidator;
import languageTools.parser.MOD2GParser;
import languageTools.program.agent.actions.ActionCombo;
import languageTools.program.agent.msc.MentalStateCondition;

/**
 * <p>
 * A rule of the form:<br>
 * <code>&nbsp;&nbsp;&nbsp;&nbsp;if CONDITION then RESULT</code><br>
 * <br>
 * Where <code>CONDITION</code> is a {@link MentalStateCondition}, and
 * <code>RESULT</code> an {@link ActionCombo}.
 * </p>
 * <p>
 * When evaluated in a rule set, of all possible {@link Substitution}s that make
 * the rule valid, only one may be executed.<br>
 * This type of rule can be located in any <code>program{}</code>-section of any
 * module in a GOAL agent, as well as in the <code>perceptrules{}</code>
 * section.
 * </p>
 */
public class IfThenRule extends Rule {
	/**
	 * Creates a new {@link IfThenRule} from a condition and a result.
	 *
	 * @param condition
	 *            The condition of the new rule. The result will never happen if
	 *            this condition is not satisfied.
	 * @param action
	 *            The result of the new rule. If there is a {@link Substitution}
	 *            that makes the condition {@code true} and this a valid
	 *            {@link ActionCombo}, it may be considered for execution.
	 */
	public IfThenRule(MentalStateCondition condition, ActionCombo action, SourceInfo info) {
		super(condition, action, info);
	}

	@Override
	public IfThenRule applySubst(Substitution substitution) {
		return new IfThenRule((getCondition() == null) ? null : getCondition().applySubst(substitution),
				(getAction() == null) ? null : getAction().applySubst(substitution), getSourceInfo());
	}

	@Override
	public String toString() {
		String condition = "<missing condition>";
		if (getCondition() != null) {
			condition = getCondition().toString();
		}

		String actions = "<missing actions>";
		if (getAction() != null) {
			actions = getAction().toString();
		}

		return ModuleValidator.getTokenName(MOD2GParser.IF) + " " + condition + " "
				+ ModuleValidator.getTokenName(MOD2GParser.THEN) + " " + actions;
	}
}
