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

import java.util.Set;

/**
 * Variables may occur in {@link DatabaseFormula}, {@link Query}, and
 * {@link Update}. Any useful knowledge representation language has variables
 * (only in the most simple applications you can do without variables).
 *
 * <p>
 * Make sure to also implement {@link java.lang.Object#equals(Object)} and
 * {@link java.lang.Object#hashCode()}, which are needed for implementing
 * {@link Expression#mgu(Expression)}.
 * </p>
 */
public interface Var extends Term {
	/**
	 * Get a variant of this variable for resolving name conflicts. This is used
	 * eg when an actioncall uses the same variables as the actionspec. The
	 * variant of this variable should be different but similar to the existing
	 * var, and not use the given names already in use.
	 *
	 * @param usedNames
	 *            a set of Vars already in use.
	 */
	Var getVariant(Set<Var> usedNames);
}
