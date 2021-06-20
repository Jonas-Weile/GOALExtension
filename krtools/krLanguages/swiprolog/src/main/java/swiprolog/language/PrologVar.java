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

import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;

/**
 * A Prolog variable.
 */
public interface PrologVar extends PrologTerm, Var {
	/**
	 * @return The full name of the variable.
	 */
	public String getName();

	/**
	 * An underscore is an anonymous Prolog variable. Note that variables that
	 * *start* with _ are not anonymous.
	 *
	 * @return {@code true} if variable is anonymous, {@code false} otherwise.
	 */
	public default boolean isAnonymous() {
		return getName().equals("_");
	}

	@Override
	public default boolean isNumeric() {
		return false;
	}

	@Override
	public default Substitution unify(Term x, Substitution s) {
		if (s == null) {
			return null;
		} else if (equals(x)) {
			return s;
		}
		PrologTerm st = (PrologTerm) s.get(this);
		if (st != null) {
			return st.unify(x, s);
		}
		PrologTerm sx = (x instanceof Var) ? (PrologTerm) s.get((Var) x) : null;
		if (sx != null) {
			return unify(sx, s);
		} else if (x.getFreeVar().contains(this)) {
			return null;
		} else {
			s.addBinding(this, x);
			return s;
		}
	}
}