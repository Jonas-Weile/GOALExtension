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

package languageTools.program.agent.msc;

import java.util.Set;

import krTools.language.Substitution;
import krTools.language.Var;
import krTools.parser.SourceInfo;
import languageTools.program.GoalParsedObject;

/**
 * Abstraction of a {@link MentalLiteral} and a {@link Macro}, to facilitate
 * storing {@link Macro}s in {@link MentalStateCondition}s.
 */
public abstract class MentalFormula extends GoalParsedObject {
	protected MentalFormula(SourceInfo info) {
		super(info);
	}

	/**
	 * Applies substitution to this {@link MentalFormula}.
	 *
	 * @param substitution
	 *            The substitution to transform this atom with.
	 * @return Mental formula in which variables bound by substitution have been
	 *         substituted.
	 */
	public abstract MentalFormula applySubst(Substitution substitution);

	/**
	 * @return All free variables in this atom. When this atom is used in a query,
	 *         these variables are bound.
	 */
	public abstract Set<Var> getFreeVar();

	/**
	 * @return All signatures of the (non-built-in) predicates used in the formula.
	 */
	public abstract Set<String> getUsedSignatures();
}
