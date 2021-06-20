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
import languageTools.program.GoalParsedObject;
import languageTools.program.agent.Module.FocusMethod;
import languageTools.program.agent.actions.Action;
import languageTools.program.agent.actions.ActionCombo;
import languageTools.program.agent.actions.ModuleCallAction;
import languageTools.program.agent.msc.MentalStateCondition;

/**
 * A rule consists of a condition (body) and an action (head). The condition of
 * a rule is a {@link MentalStateCondition}. The action of a rule is an
 * {@link ActionCombo}. A rule is applicable if the condition of the rule AND
 * the precondition of the action hold. In that case, the action can be selected
 * for execution.
 */
public abstract class Rule extends GoalParsedObject {
	/**
	 * The condition of the rule.
	 */
	private final MentalStateCondition condition;
	/**
	 * The action of the rule.
	 */
	private ActionCombo action;

	/**
	 * Creates a new {@link Rule}
	 *
	 * @param condition
	 *            Determines when the rule is applicable.
	 * @param action
	 *            The action to perform if the rule is applicable.
	 */
	protected Rule(MentalStateCondition condition, ActionCombo action, SourceInfo info) {
		super(info);
		this.condition = condition;
		this.action = action;
	}

	/**
	 * Gets the condition (head) of this {@link Rule}.
	 *
	 * @return The condition of this {@link Rule} used for evaluating whether
	 *         the rule is applicable.
	 */
	public MentalStateCondition getCondition() {
		return this.condition;
	}

	/**
	 * Returns the action of this rule.
	 *
	 * @return The {@link ActionCombo} that is performed if this {@link Rule} is
	 *         applied.
	 */
	public ActionCombo getAction() {
		return this.action;
	}

	/**
	 * Sets the {@link ActionCombo} for this {@link Rule}.
	 *
	 * @param action
	 *            The action to be associated with this rule.
	 */
	public void setAction(ActionCombo action) {
		this.action = action;
	}

	/**
	 * Applies a substitution to this rule.
	 *
	 * @param substitution
	 *            A substitution.
	 * @return A rule where variables that are bound by the substitution have
	 *         been instantiated (or renamed).
	 */
	public abstract Rule applySubst(Substitution substitution);

	/**
	 * @return The focus method of the first module in the combo. TODO: what if
	 *         multiple modules use different focus methods?
	 */
	public FocusMethod getFocusMethod() {
		for (Action<?> action : this.action) {
			if (action instanceof ModuleCallAction) {
				return ((ModuleCallAction) action).getTarget().getFocusMethod();
			}
		}

		// By default, return NONE.
		return FocusMethod.NONE;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = getClass().hashCode();
		result = prime * result + ((this.action == null) ? 0 : this.action.hashCode());
		result = prime * result + ((this.condition == null) ? 0 : this.condition.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || !getClass().equals(obj.getClass())) {
			return false;
		}
		Rule other = (Rule) obj;
		if (this.action == null) {
			if (other.action != null) {
				return false;
			}
		} else if (!this.action.equals(other.action)) {
			return false;
		}
		if (this.condition == null) {
			if (other.condition != null) {
				return false;
			}
		} else if (!this.condition.equals(other.condition)) {
			return false;
		}
		return true;
	}
}
