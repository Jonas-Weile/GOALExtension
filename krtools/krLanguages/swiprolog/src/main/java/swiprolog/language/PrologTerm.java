/**
 * Knowledge Representation Tools. Copyright (C) 2014 Koen Hindriks.
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

package swiprolog.language;

import krTools.language.Expression;
import krTools.language.Substitution;
import krTools.language.Term;

/**
 * Represents a Prolog term.
 */
public interface PrologTerm extends PrologExpression, Term {
	/**
	 * @return Iff the term can be (and is allowed to be) used as a query.
	 */
	public boolean isQuery();

	/**
	 * @return Iff the term is a number (integer or double).
	 */
	public boolean isNumeric();

	@Override
	public default Substitution mgu(Expression expression) {
		if (expression instanceof Term) {
			return unify((Term) expression, new PrologSubstitution());
		} else {
			return null;
		}
	}

	public Substitution unify(Term term, Substitution substitution);
}
