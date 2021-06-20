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

package languageTools.program.actionspec;

import java.util.ArrayList;
import java.util.List;

import krTools.KRInterface;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Update;
import krTools.parser.SourceInfo;
import languageTools.program.agent.actions.Action;
import languageTools.program.agent.msc.BelLiteral;
import languageTools.program.agent.msc.MentalFormula;
import languageTools.program.agent.msc.MentalStateCondition;
import languageTools.program.agent.selector.Selector;
import languageTools.program.agent.selector.Selector.SelectorType;

/**
 * A user-specified action of the form 'name(parameters)' with one or more
 * associated action specifications (i.e., precondition, postcondition pairs).
 * Parameters are optional.
 * <p>
 * A user-specified action should at least have one associated action
 * specification. In case an action has multiple action specifications the order
 * of the specifications in the program is taken into account: a specification
 * that occurs before another one is used whenever it is applicable.
 * </p>
 */
public class UserSpecAction extends Action<Term> {
	/**
	 * Representing whether the action should be sent to an external environment.
	 * Default value is {@code true} meaning that an attempt should be made to sent
	 * the action to an external environment. In case value is {@code false} no such
	 * attempt should be made.
	 */
	private final boolean external;
	/**
	 * The pre-condition of the action, i.e., a query representing a condition for
	 * successful action execution.
	 */
	private final ActionPreCondition precondition;
	/**
	 * The post-condition of the action, i.e., an update representing the effects of
	 * the action, possibly null. We want to add the addList (and delete the
	 * deleteList).
	 */
	private final ActionPostCondition positivePostcondition;
	/**
	 * The post-condition of the action, i.e., an update representing the effects of
	 * the action, possibly null. We want to delete the addList (and add the
	 * deleteList).
	 */
	private final ActionPostCondition negativePostcondition;

	/**
	 * Creates a {@link UserSpecAction} with name, parameter list, and sets flag
	 * whether action should be sent to external environment or not.
	 *
	 * @param name
	 *            The name of the action.
	 * @param parameters
	 *            The action parameters.
	 * @param external
	 *            Parameter indicating whether action should be sent to external
	 *            environment or not. {@code true} indicates that action should be
	 *            sent to environment; {@code false} indicates that action should
	 *            not be sent to environment.
	 * @param precondition
	 * @param positivePostCondition
	 * @param negativePostCondition
	 * @param info
	 * @param kr
	 *            the {@link KRInterface}
	 */
	public UserSpecAction(String name, List<Term> parameters, boolean external, ActionPreCondition precondition,
			ActionPostCondition positivePostcondition, ActionPostCondition negativePostcondition, SourceInfo info) {
		super(name, info);

		for (Term parameter : parameters) {
			addParameter(parameter);
		}
		this.external = external;
		this.precondition = precondition;
		this.positivePostcondition = positivePostcondition;
		this.negativePostcondition = negativePostcondition;
	}

	/**
	 * @return {@code true} if this is an external action, i.e., one that should be
	 *         sent to environment.
	 */
	public boolean isExternal() {
		return this.external;
	}

	/**
	 * @return A {@link MentalStateCondition} of the form "bel(precondition)" that
	 *         represents the precondition of this action.
	 */
	@Override
	public MentalStateCondition getPrecondition() {
		// Create mental state condition of the form "self.bel(precondition)".
		if (this.precondition == null) {
			return new MentalStateCondition(null, null);
		} else {
			List<MentalFormula> formulalist = new ArrayList<>(1);
			formulalist.add(new BelLiteral(true, new Selector(SelectorType.SELF, this.precondition.getSourceInfo()),
					this.precondition.getPreCondition(), this.precondition.getSignatures(),
					this.precondition.getSourceInfo()));
			return new MentalStateCondition(formulalist, this.precondition.getSourceInfo());
		}
	}

	public ActionPreCondition getFullPreCondition() {
		return this.precondition;
	}

	/**
	 * @return An {@link Update} that represents the effect of this
	 *         {@link UserSpecAction}, possibly null. Add the addList (and delete
	 *         the deleteList).
	 */
	public ActionPostCondition getPositivePostcondition() {
		return this.positivePostcondition;
	}

	/**
	 * @return An {@link Update} that represents the effect of this
	 *         {@link UserSpecAction}, possibly null. Delete the addList (and add
	 *         the deleteList).
	 */
	public ActionPostCondition getNegativePostcondition() {
		return this.negativePostcondition;
	}

	@Override
	public UserSpecAction applySubst(Substitution substitution) {
		// Apply substitution to action parameters, pre- and post-condition.
		List<Term> parameters = new ArrayList<>(getParameters().size());
		for (Term parameter : getParameters()) {
			parameters.add(parameter.applySubst(substitution));
		}
		ActionPreCondition precondition = (getFullPreCondition() == null) ? null
				: getFullPreCondition().applySubst(substitution);
		ActionPostCondition positivePostcondition = (getPositivePostcondition() == null) ? null
				: getPositivePostcondition().applySubst(substitution);
		ActionPostCondition negativePostcondition = (getNegativePostcondition() == null) ? null
				: getNegativePostcondition().applySubst(substitution);
		return new UserSpecAction(getName(), parameters, isExternal(), precondition, positivePostcondition,
				negativePostcondition, getSourceInfo());
	}

	/**
	 * Builds a string representation of this {@link ActionSpecification}.
	 *
	 * @param linePrefix
	 *            A prefix used to indent parts of a program, e.g., a single space
	 *            or tab.
	 * @param indent
	 *            A unit to increase indentation with, e.g., a single space or tab.
	 * @return A string-representation of this action specification.
	 */
	public String toString(String linePrefix, String indent) {
		StringBuilder str = new StringBuilder();
		str.append(linePrefix + "<action: " + super.toString() + ",\n");
		str.append(linePrefix + indent + "<precondition: " + this.precondition + ">,\n");
		str.append(linePrefix + indent + "<postcondition+: " + this.positivePostcondition + ">\n");
		str.append(linePrefix + indent + "<postcondition-: " + this.negativePostcondition + ">\n");
		str.append(linePrefix + ">");
		return str.toString();
	}
}
