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

import java.util.ArrayList;
import java.util.List;

import krTools.language.Substitution;
import krTools.language.Term;
import krTools.parser.SourceInfo;
import languageTools.analyzer.ValidatorSecondPass;
import languageTools.program.actionspec.ActionPreCondition;
import languageTools.program.actionspec.UserSpecAction;
import languageTools.program.agent.msc.MentalStateCondition;

/**
 * Represents a call from the program to a {@link UserSpecAction}. Constructed
 * in {@link ValidatorSecondPass} after resolving the
 * {@link UserSpecOrModuleCall} and figuring out which actual
 * {@link UserSpecAction} is being called.
 */
public class UserSpecCallAction extends NonMentalAction {
	/**
	 * The resolved {@link UserSpecAction} that the call points to.
	 */
	private final UserSpecAction actionSpec;

	/**
	 * Creates a new {@link UserSpecCallAction}, which will focus the agent's
	 * attention to a certain module when executed.
	 *
	 * @param spec
	 *            the user spec action that should be called.
	 * @param parameters
	 *            the parameters, exactly as used at the caller's side. These
	 *            still have to be substituted at runtime against the actual
	 *            variable values at that time and then renamed into the space
	 *            of the {@link #actionSpec}.
	 */
	public UserSpecCallAction(UserSpecAction spec, List<Term> parameters, SourceInfo info) {
		super(spec.getName(), info);
		for (Term term : parameters) {
			addParameter(term);
		}
		this.actionSpec = spec;
	}

	/**
	 * Applies the substitution to the parameters of this action and to the
	 * target module associated with the action. Assumes a target module has
	 * been associated with this action.
	 *
	 * @return Instantiated module call action, where free variables in
	 *         parameters have been substituted with terms from the
	 *         substitution.
	 */
	@Override
	public Action<Term> applySubst(Substitution substitution) {
		// Apply substitution to parameters.
		List<Term> parameters = new ArrayList<>(getParameters().size());
		for (Term term : getParameters()) {
			parameters.add(term.applySubst(substitution));
		}
		// Create new focus action with instantiated parameters.
		return new UserSpecCallAction((getSpecification() == null) ? null : getSpecification().applySubst(substitution),
				parameters, getSourceInfo());
	}

	/**
	 * @return the {@link UserSpecAction} defining this call
	 */
	public UserSpecAction getSpecification() {
		return this.actionSpec;
	}

	@Override
	public MentalStateCondition getPrecondition() {
		return this.actionSpec.getPrecondition();
	}

	public ActionPreCondition getFullPreCondition() {
		return this.actionSpec.getFullPreCondition();
	}
}