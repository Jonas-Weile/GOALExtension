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
 * <code>&nbsp;&nbsp;&nbsp;&nbsp;forall CONDITION do RESULT.</code><br>
 * <br>
 * Where <code>CONDITION</code> is a {@link MentalStateCondition}, and
 * <code>RESULT</code> an {@link ActionCombo}.
 * </p>
 * <p>
 * When evaluated in a rule set, all possible substitutions that make the rule
 * valid may be executed. If the rule is selected for execution, <i>all</i>
 * valid substitutions must be executed.<br>
 * This type of rule can be located in any <code>program{}</code>-section of any
 * module in a GOAL agent.
 * </p>
 */
public class ForallDoRule extends Rule {
	/**
	 * Creates a new {@link ForallDoRule}.
	 *
	 * @param condition
	 *            What should hold before the rule should be executed.
	 * @param action
	 *            The result of executing the rule.
	 */
	public ForallDoRule(MentalStateCondition condition, ActionCombo action, SourceInfo info) {
		super(condition, action, info);
	}

	@Override
	public ForallDoRule applySubst(Substitution substitution) {
		return new ForallDoRule((getCondition() == null) ? null : getCondition().applySubst(substitution),
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

		return ModuleValidator.getTokenName(MOD2GParser.FORALL) + " " + condition + " "
				+ ModuleValidator.getTokenName(MOD2GParser.DO) + " " + actions;
	}

}
