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
import krTools.language.Term;
import krTools.language.Var;
import krTools.parser.SourceInfo;
import languageTools.analyzer.module.ModuleValidator;
import languageTools.parser.MOD2GParser;
import languageTools.program.agent.actions.ActionCombo;
import languageTools.program.agent.msc.MentalStateCondition;

/**
 * <p>
 * A rule of the form:<br>
 * <code>&nbsp;&nbsp;&nbsp;&nbsp;listall CONDITION -&gt; VAR do RESULT.</code>
 * <br>
 * or:<br>
 * <code>&nbsp;&nbsp;&nbsp;&nbsp;listall VAR &lt;- CONDITION do RESULT.</code>
 * </p>
 * <p>
 * When evaluated, all possible substitutions that make the condition true are
 * aggregated into a single {@link Term}, with which the given variable is
 * instantiated. Only that variable will be bound in the {@link ActionCombo}
 * that is the result of the rule (aside from possibly parameters of the parent
 * module).<br>
 * The {@link Term} is created as a list of lists, where each 'sub-list'
 * represents a single Substitution. Each value in the 'sub-list' is the value a
 * variable in the condition would have if the corresponding
 * {@link Substitution} was applied. The order of the variables is constant, and
 * is the order of occurrence in the condition (in <i>positive</i> literals).
 * </p>
 */
public class ListallDoRule extends Rule {
	/**
	 * The variable that, when this rule is applied, is instantiated with the
	 * set of substitutions of this rule's condition.
	 */
	private final Var variable;

	/**
	 * Creates a new {@link ListallDoRule}.
	 *
	 * @param condition
	 *            The condition of the rule.
	 * @param variable
	 *            The variable to which the instances of the condition will be
	 *            bound.
	 * @param action
	 *            The result of executing the new rule.
	 */
	public ListallDoRule(MentalStateCondition condition, Var variable, ActionCombo action, SourceInfo info) {
		super(condition, action, info);
		this.variable = variable;
	}

	/**
	 * @return The variable in this rule to which all substitutions of the
	 *         condition are mapped.
	 */
	public Var getVariable() {
		return this.variable;
	}

	@Override
	public ListallDoRule applySubst(Substitution substitution) {
		// Make sure to not instantiate the variable assigned to by this rule.
		// TODO: can we delegate this to check during compile time?
		Substitution safesubstitution = substitution.clone();
		safesubstitution.remove(getVariable());
		return new ListallDoRule((getCondition() == null) ? null : getCondition().applySubst(substitution),
				getVariable(), (getAction() == null) ? null : getAction().applySubst(safesubstitution),
				getSourceInfo());
	}

	@Override
	public String toString() {
		String variable = "<missing variable>";
		if (this.variable != null) {
			variable = this.variable.toString();
		}

		String condition = "<missing condition>";
		if (getCondition() != null) {
			condition = getCondition().toString();
		}

		String actions = "<missing actions>";
		if (getAction() != null) {
			actions = getAction().toString();
		}

		return ModuleValidator.getTokenName(MOD2GParser.LISTALL) + " " + variable
				+ ModuleValidator.getTokenName(MOD2GParser.RTLARROW) + condition + " "
				+ ModuleValidator.getTokenName(MOD2GParser.DO) + " " + actions;
	}
}
