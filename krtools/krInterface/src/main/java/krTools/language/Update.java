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

import java.util.List;

/**
 * An update is an expression that can be used to update a database.
 *
 * <p>
 * It is assumed that an update is a conjunction of basic updates, where basic
 * updates are, for example, either positive or negative literals, or a
 * conditional effect in ADL.
 * </p>
 *
 * <p>
 * Updates are related to {@link DatabaseFormula} because we view updates here
 * as an instruction to <i>insert</i> and to <i>delete</i> content from a
 * database. As database formulas can be inserted into (and removed again from)
 * a database, an update can be viewed as an instruction to insert or <i>add a
 * list</i> to and remove or <i>delete a list</i> from a database.
 * </p>
 *
 * <p>
 * An update may have free variables as any other logical expression. Before
 * applying an update to a database, however, these variables may need to have
 * been instantiated (by applying a substitution).
 * </p>
 *
 * <p>
 * Make sure to also implement {@link java.lang.Object#equals(Object)} and
 * {@link java.lang.Object#hashCode()}, which are needed for implementing
 * {@link Expression#mgu(Expression)}.
 * </p>
 */
public interface Update extends Expression {
	/**
	 * Returns the add list of this {@link Update} in the form of a list of
	 * {@link DatabaseFormula}s. The formulas returned are positive literals.
	 *
	 * @return The add list of this update, i.e. the positive literals that occur in
	 *         this update.
	 */
	List<DatabaseFormula> getAddList();

	/**
	 * Returns the delete list of this {@link Update} in the form of a list of
	 * {@link DatabaseFormula}s. The formulas returned are positive literals.
	 *
	 * @return The delete list of this update, i.e. the negative literals that occur
	 *         in this update (but the negations are removed in the list that is
	 *         returned).
	 */
	List<DatabaseFormula> getDeleteList();

	@Override
	Update applySubst(Substitution substitution);

	/**
	 * Converts this update to a query.
	 *
	 * @return A {@link Query} if this update can be converted to a query; should
	 *         return {@code null} otherwise.
	 */
	Query toQuery();

	/**
	 * Converts this update to a single database formula.
	 *
	 * @return A {@link DatabaseFormula} if this update can be converted to a single
	 *         database formula; should return {@code null} otherwise.
	 */
	DatabaseFormula toDBF();
}
