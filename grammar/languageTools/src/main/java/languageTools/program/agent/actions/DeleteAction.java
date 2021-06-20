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
 * Deletes an {@link Update} from the belief base base and/or mail box. As these
 * are two different databases that the agent maintains, two updates are
 * associated with the delete action. One update for the agent's belief base and
 * one for the agent's mail box.
 * <p>
 * If the action is closed, the delete action can be performed, but only the
 * update to the belief base is required to be closed. The update to the agent's
 * mail box may contain variables in order to be able to remove interrogatives
 * (questions) that have been sent or received from the mail box again.
 * </p>
 * <p>
 * Percepts of the form {@code percept(...)} cannot be removed from the percept
 * base by a delete action. Percepts are automatically removed from the agent's
 * percept base every start of a reasoning cycle.
 * </p>
 */
public class DeleteAction extends MentalAction {
	/**
	 * Creates a delete action that removes a belief from the belief base of an
	 * agent; also may remove content from the mail box.
	 *
	 * @param selector
	 *            The {@link Selector} of this action.
	 * @param update
	 *            The {@link Update} to be removed from to the belief base.
	 */
	public DeleteAction(Selector selector, Update update, SourceInfo info) {
		super(ModuleValidator.getTokenName(MOD2GParser.DELETE), selector, info);
		addParameter(update);
	}

	@Override
	public void addParameter(Update parameter) {
		super.addParameter(parameter);
		// this.parameters.add(parameter);
		// FIXME: would like to have anonymous variables here,
		// but that doesn't link well with the Prolog Theory atm.
	}

	/**
	 * @return The update that is to be inserted.
	 */
	public Update getUpdate() {
		return getParameters().get(0);
	}

	@Override
	public DeleteAction applySubst(Substitution substitution) {
		return new DeleteAction((getSelector() == null) ? null : getSelector().applySubst(substitution),
				(getParameters() == null || getParameters().isEmpty()) ? null
						: getParameters().get(0).applySubst(substitution),
				getSourceInfo());
	}
}
