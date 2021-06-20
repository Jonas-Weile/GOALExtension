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

import java.util.Collection;
import java.util.List;

import krTools.KRInterface;

/**
 * A substitution is a mapping of distinct variables to terms. A substitution is
 * said to bind the term to the variable if it maps the variable to the term. A
 * substitution may be empty.
 *
 * <p>
 * Also see: {@link KRInterface#getSubstitution(java.util.Map)}.
 * </p>
 */
public interface Substitution {
	/**
	 * Returns the list of {@link Var}s bound by this {@link Substitution}.
	 *
	 * @return The variables in the domain of this substitution.
	 */
	List<Var> getVariables();

	/**
	 * Returns the term to which the variable is bound, if any.
	 *
	 * @param var
	 *            variable for which a binding is searched for.
	 * @return term that is bound to variable in substitution, if such a binding
	 *         exists, otherwise null.
	 */
	Term get(Var var);

	/**
	 * Extends the substitution with a new binding for a variable. It first
	 * checks if variable has already been bound. If so, nothing happens, else
	 * new binding is added.
	 */
	void addBinding(Var var, Term term);

	/**
	 * Combines two substitutions.
	 *
	 * @param substitution
	 *            A substitution to be combined with this {@link Substitution},
	 *            or {@code null} (representing a failure to unify two terms).
	 *
	 * @return The substitution resulting from combining this
	 *         {@link Substitution} with the parameter substitution. Returns
	 *         {@code null} if both substitutions cannot be combined because
	 *         they both have different bindings for one and the same variable;
	 *         also returns {@code null} if the parameter substitution is equal
	 *         to {@code null}.
	 */
	Substitution combine(Substitution substitution);

	/**
	 * Removes the binding to a variable from this Substitution. The result of
	 * this method is that this {@link Substitution} does not bind the given
	 * {@link Var}.
	 *
	 * @param var
	 *            The variable to remove the Binding from.
	 * @return {@code true} iff the {@link Var} used to be bound by this
	 *         {@link Substitution}.
	 */
	boolean remove(Var var);

	/**
	 * Removes all bindings in this {@link Substitution} that do not bind any of
	 * the given {@link Var}s.
	 *
	 * @param variables
	 *            A super-set of the variables that should be bound by this
	 *            substitution after this call.
	 * @return {@code true} if any binding was removed from this
	 *         {@link Substitution}.
	 */
	boolean retainAll(Collection<Var> variables);

	/**
	 * create a copy of this substitution. The copy contains the same
	 * substitutions as this. Modification of the copy should leave this
	 * unaffected. The copy can be shallow - the terms referenced in this can be
	 * referenced in the copy.
	 */
	Substitution clone();
}
