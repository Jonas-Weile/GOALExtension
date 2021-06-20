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
import languageTools.analyzer.module.ModuleValidator;
import languageTools.parser.MOD2GParser;

/**
 * Prints its parameter to System.out.
 */
public class PrintAction extends NonMentalAction {
	/**
	 * Creates a {@link PrintAction}.
	 *
	 * @param parameter
	 *            The parameter that determines what needs to be printed.
	 */
	public PrintAction(List<Term> parameters, SourceInfo info) {
		super(ModuleValidator.getTokenName(MOD2GParser.PRINT), info);
		for (Term parameter : parameters) {
			addParameter(parameter);
		}
	}

	@Override
	public PrintAction applySubst(Substitution substitution) {
		// Apply substitution to parameters.
		List<Term> parameters = new ArrayList<>(getParameters().size());
		for (Term term : getParameters()) {
			parameters.add(term.applySubst(substitution));
		}
		// Create new action with instantiated parameters.
		return new PrintAction(parameters, getSourceInfo());
	}
}
