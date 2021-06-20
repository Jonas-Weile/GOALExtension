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

import krTools.language.DatabaseFormula;
import swiprolog.database.PrologDatabase;

/**
 * <p>
 * A Prolog database formula is an expression that can be inserted into a
 * {@link PrologDatabase}.
 * </p>
 *
 * <p>
 * Performs no checks whether a Prolog term can be inserted into a Prolog
 * database for efficiency reasons (to avoid such checks at run time, e.g., when
 * creating a new instantiated formula when applying a substitution). The
 * responsibility to check this is delegated to the parser (which ensures that
 * the check is only performed at compile time).
 * </p>
 */
public interface PrologDBFormula extends PrologExpression, DatabaseFormula {
	/**
	 * @return The {@link PrologCompound} wrapped in this formula.
	 */
	public PrologCompound getCompound();
}