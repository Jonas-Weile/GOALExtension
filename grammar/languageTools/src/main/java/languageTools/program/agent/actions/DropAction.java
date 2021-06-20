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
import krTools.language.Update;
import krTools.parser.SourceInfo;
import languageTools.analyzer.module.ModuleValidator;
import languageTools.parser.MOD2GParser;
import languageTools.program.agent.selector.Selector;

/**
 * Drops all goals entailed by the goal to be dropped from the goal base.
 * <p>
 * If the action is closed, the drop action can be performed.
 * </p>
 */
public class DropAction extends MentalAction {
	/**
	 * Creates a drop action that drops all goals from the goal base that follow
	 * from the goal to be dropped.
	 *
	 * @param selector
	 *            The {@link Selector} of this action.
	 * @param goal
	 *            The goal, i.e., {@link Update}, to be dropped.
	 * @param info
	 *            The source code location of this action, if available;
	 *            {@code null} otherwise.
	 */
	public DropAction(Selector selector, Update goal, SourceInfo info) {
		super(ModuleValidator.getTokenName(MOD2GParser.DROP), selector, info);
		addParameter(goal);
	}

	@Override
	public void addParameter(Update parameter) {
		this.parameters.add(parameter);
		// allow free drop parameters (i.e. potentially matching multiple goals)
	}

	/**
	 * Returns the goal, represented by an {@link Update}, that is to be dropped.
	 *
	 * @return The goal to be dropped.
	 */
	public Update getUpdate() {
		return getParameters().get(0);
	}

	@Override
	public DropAction applySubst(Substitution substitution) {
		return new DropAction((getSelector() == null) ? null : getSelector().applySubst(substitution),
				(getUpdate() == null) ? null : getUpdate().applySubst(substitution), getSourceInfo());
	}
}
