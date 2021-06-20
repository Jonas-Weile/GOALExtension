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

package krTools.language;

/**
 * A query is an expression that can be queried on a database for an answer.
 *
 * <p>
 * The expression should be a formula that may evaluate to true or false,
 * possibly after instantiating any free variables. The corresponding
 * InferenceEngine should support the evaluation of a QueryExpression on a
 * database. Typically, as a result of the evaluation, a substitution - binding
 * free variables in the QueryExpression with terms - may be returned by the
 * InferenceEngine.
 * </p>
 *
 * <p>
 * Make sure to also implement {@link java.lang.Object#equals(Object)} and
 * {@link java.lang.Object#hashCode()}, which are needed for implementing
 * {@link Expression#mgu(Expression)}.
 * </p>
 */

public interface Query extends Expression {
	@Override
	Query applySubst(Substitution substitution);

	/**
	 * Converts this query to an {@link Update}.
	 *
	 * @return An {@link Update} if this query can be converted to an update; should
	 *         return {@code null} otherwise.
	 */
	Update toUpdate();

	/**
	 * Converts this query to a single {@link DatabaseFormula}.
	 *
	 * @return An {@link DatabaseFormula} if this query can be converted to a single
	 *         database formula; should return {@code null} otherwise.
	 */
	DatabaseFormula toDBF();
}
