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
 * Action that "inserts" an {@link Update} into a belief base of the agent.
 */
public class InsertAction extends MentalAction {
	/**
	 * Creates an {@link InsertAction}.
	 *
	 * @param selector
	 *            The {@link Selector} of this action.
	 * @param update
	 *            The {@link Update} to be inserted.
	 */
	public InsertAction(Selector selector, Update update, SourceInfo info) {
		super(ModuleValidator.getTokenName(MOD2GParser.INSERT), selector, info);
		addParameter(update);
	}

	/**
	 * @return The update that is to be inserted.
	 */
	public Update getUpdate() {
		return getParameters().get(0);
	}

	@Override
	public InsertAction applySubst(Substitution substitution) {
		return new InsertAction((getSelector() == null) ? null : getSelector().applySubst(substitution),
				(getUpdate() == null) ? null : getUpdate().applySubst(substitution), getSourceInfo());
	}
}
