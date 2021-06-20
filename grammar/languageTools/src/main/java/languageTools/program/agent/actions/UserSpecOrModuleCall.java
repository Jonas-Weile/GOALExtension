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

import krTools.KRInterface;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.parser.SourceInfo;
import languageTools.program.actionspec.UserSpecAction;

/**
 * Action used for parsing purposes only. Used because at the parsing time it
 * cannot yet be determined whether the parsed object should be resolved to
 * either a user-specified action or to a focus action (module invocation).<br>
 * This 'action' is replaced during validation of the agent program by a proper
 * action.<br>
 * A UserOrFocusAction cannot be executed.
 */
public class UserSpecOrModuleCall extends NonMentalAction {
	/**
	 * Creates an action that can either be a {@link ModuleCallAction} or a
	 * {@link UserSpecAction}.
	 *
	 * @param name
	 *            The name of the action.
	 * @param parameters
	 *            The parameters of the action.
	 * @param info
	 *            The associated source code info.
	 * @param kr
	 *            the {@link KRInterface}
	 */
	public UserSpecOrModuleCall(String name, List<Term> parameters, SourceInfo info) {
		super(name, info);
		for (Term parameter : parameters) {
			addParameter(parameter);
		}
	}

	@Override
	public Action<?> applySubst(Substitution substitution) {
		// Apply substitution to action parameters.
		List<Term> parameters = new ArrayList<>(getParameters().size());
		for (Term parameter : getParameters()) {
			parameters.add(parameter.applySubst(substitution));
		}
		return new UserSpecOrModuleCall(getName(), parameters, getSourceInfo());
	}
}
