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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;

/**
 * A substitution is a mapping of distinct variables to terms. A substitution is
 * said to bind the term to the variable if it maps the variable to the term. A
 * substitution may be empty.
 */
public class PrologSubstitution extends LinkedHashMap<Var, Term> implements Substitution {
	private static final long serialVersionUID = 5134215246806477693L;

	/**
	 * Creates an empty {@link Substitution}.
	 */
	public PrologSubstitution() {
		super();
	}

	/**
	 * Creates a substitution from a single variable and term.
	 *
	 * @param var
	 *            Variable that is bound.
	 * @param term
	 *            Term that is bound to variable.
	 */
	public PrologSubstitution(Var var, Term term) {
		super();
		put(var, term);
	}

	/**
	 * Create a {@link PrologSubstitution} from a JPL substitution.
	 *
	 * @param solutions
	 *            JPL substitution.
	 */
	private PrologSubstitution(Map<Var, Term> solution) {
		super(solution);
	}

	/**
	 * Returns the list of {@link Var}iables bound by this
	 * {@link PrologSubstitution}.
	 *
	 * <p>
	 * Source information, if available, is lost.
	 * </p>
	 *
	 * @return The variables in the domain of this substitution.
	 */
	@Override
	public List<Var> getVariables() {
		return new ArrayList<>(keySet());
	}

	@Override
	public Term get(Var var) {
		return super.get(var);
	}

	@Override
	public Term put(Var var, Term term) {
		if (var instanceof PrologVar && !((PrologVar) var).isAnonymous()) {
			return super.put(var, term);
		} else {
			return null;
		}
	}

	@Override
	public void addBinding(Var var, Term term) {
		if (containsKey(var)) {
			throw new RuntimeException(
					"attempt to add '" + var + "' to substitution " + this + " that already binds the variable.");
		} else {
			put(var, term);
		}
	}

	@Override
	public boolean remove(Var var) {
		return (super.remove(var) != null);
	}

	@Override
	public Substitution combine(Substitution substitution) {
		// Combining with {@code null}, i.e., failure, yields a failure {@code
		// null}.
		if (substitution == null) {
			return null;
		}
		Substitution combination = new PrologSubstitution();

		// Apply the parameter substitution to this substitution.
		for (Var var : keySet()) {
			// Add binding for variable to term obtained by applying the
			// parameter substitution to the original term.
			Term term = var.applySubst(substitution);
			// Add binding if resulting term is not equal to variable.
			if (var.equals(term)) {
				combination.addBinding(var, get(var));
			} else {
				combination.addBinding(var, term);
			}
		}
		// Add the bindings of the parameter substitution for variables that are
		// not in the domain of this substitution; otherwise check for
		// inconsistencies.
		for (Var var : substitution.getVariables()) {
			if (get(var) == null) {
				Term term = substitution.get(var).applySubst(substitution);
				if (!var.equals(term)) {
					combination.addBinding(var, term);
				}
			} else { // two bindings for one and the same variable.
				// Check whether terms can be unified
				Substitution mgu = substitution.get(var).mgu(get(var));
				if (mgu != null) {
					combination = combination.combine(mgu);
				} else { // fail: two different bindings for one and the same
					// variable.
					combination = null;
					break;
				}
			}
		}

		return combination;
	}

	@Override
	public boolean retainAll(Collection<Var> varsToRetain) {
		boolean removed = false;
		Iterator<Var> vars = keySet().iterator();
		while (vars.hasNext()) {
			Var var = vars.next();
			if (!varsToRetain.contains(var)) {
				vars.remove();
				removed = true;
			}
		}
		return removed;
	}

	@Override
	public Substitution clone() {
		return new PrologSubstitution(this);
	}

	/**
	 * Returns a string representation of this {@link PrologSubstitution}.
	 *
	 * @return The string representation of this substitution.
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();

		builder.append("[");
		boolean addComma = false;

		for (Var var : keySet()) {
			if (addComma) {
				builder.append(", ");
			}
			builder.append(var).append("/");
			builder.append(get(var).toString());
			addComma = true;
		}
		builder.append("]");

		return builder.toString();
	}
}
