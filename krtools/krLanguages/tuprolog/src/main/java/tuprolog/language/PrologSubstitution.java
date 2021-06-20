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

package tuprolog.language;

import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;

/**
 * A substitution is a mapping of distinct variables to terms. A substitution is
 * said to bind the term to the variable if it maps the variable to the term. A
 * substitution may be empty.
 */
public class PrologSubstitution implements Substitution {
	/**
	 * TODO: Check!!!
	 *
	 * Substitution stored as a {@link Map} with tuples {@link String},
	 * {@link alice.tuprolog.Term} indicating a substitution of variable for
	 * term. We do not use {@link alice.tuprolog.Var} as keys because
	 * {@link alice.tuprolog.Var} has no implementation for hashCode and
	 * therefore putting these in a map will fail #2211. Using String brings us
	 * closest to what JPL is doing internally.
	 */

	/**
	 * Create empty JPL substitution.
	 */
	private Map<String, alice.tuprolog.Term> jplSubstitution = new Hashtable<>();

	/**
	 * Creates an empty {@link Substitution}.
	 */
	public PrologSubstitution() {
	}

	/**
	 * Creates a substitution from a single variable and term.
	 *
	 * @param var
	 *            Variable that is bound.
	 * @param term
	 *            Term that is bound to variable.
	 */
	public PrologSubstitution(alice.tuprolog.Var var, alice.tuprolog.Term term) {
		this.jplSubstitution.put(var.getName(), term);
	}

	/**
	 * Create {@link PrologSubstitution} from JPL substitution.
	 *
	 * @param solutions
	 *            JPL substitution.
	 */
	private PrologSubstitution(Map<String, alice.tuprolog.Term> solution) {
		this.jplSubstitution = solution;
	}

	public static PrologSubstitution getSubstitutionOrNull(Map<String, alice.tuprolog.Term> solution) {
		if (solution == null) {
			return null;
		} else {
			return new PrologSubstitution(solution);
		}
	}

	/**
	 * @return A JPL substitution.
	 */
	public Map<String, alice.tuprolog.Term> getJPLSolution() {
		return this.jplSubstitution;
	}

	/**
	 * Returns the set of {@link Var}iables bound by this
	 * {@link PrologSubstitution}.
	 *
	 * <p>
	 * Source information, if available, is lost.
	 * </p>
	 *
	 * @return The variables in the domain of this substitution.
	 */
	@Override
	public Set<Var> getVariables() {
		Set<Var> variables = new LinkedHashSet<>(this.jplSubstitution.size());
		// Build VariableTerm from alice.tuprolog.Var.
		for (String varname : this.jplSubstitution.keySet()) {
			alice.tuprolog.Var var = new alice.tuprolog.Var(varname);
			variables.add(new PrologVar(var, null));
		}
		return variables;
	}

	@Override
	public Term get(Var variable) {
		alice.tuprolog.Var jplvar = (alice.tuprolog.Var) ((PrologVar) variable).getTerm();
		if (this.jplSubstitution.containsKey(jplvar.getName())) {
			return new PrologTerm(this.jplSubstitution.get(jplvar.getName()), null);
		} else {
			return null;
		}
	}

	@Override
	public void addBinding(Var v, Term term) {
		alice.tuprolog.Var var = (alice.tuprolog.Var) ((PrologVar) v).getTerm();
		if (this.jplSubstitution.containsKey(var.getName())) {
			throw new RuntimeException(
					"attempt to add '" + v + "' to substitution " + this + " that already binds the variable.");
		}
		this.jplSubstitution.put(var.getName(), ((PrologTerm) term).getTerm());
	}

	@Override
	public Substitution combine(Substitution substitution) {
		Map<String, alice.tuprolog.Term> combined = null;
		if (substitution != null) {
			combined = JPLUtils.combineSubstitutions(this.jplSubstitution,
					((PrologSubstitution) substitution).getJPLSolution());
		}
		return getSubstitutionOrNull(combined);
	}

	@Override
	public boolean remove(Var variable) {
		alice.tuprolog.Var var = (alice.tuprolog.Var) ((PrologVar) variable).getTerm();
		if (this.jplSubstitution.containsKey(var.getName())) {
			return this.jplSubstitution.remove(var.getName()) != null;
		} else {
			return false;
		}
	}

	@Override
	public boolean retainAll(Collection<Var> varsToRetain) {
		Set<String> varnamesToRetain = new HashSet<>(varsToRetain.size());
		for (Var v : varsToRetain) {
			varnamesToRetain.add(((PrologVar) v).getVariable().getName());
		}
		Set<String> currentVars = new HashSet<>(this.jplSubstitution.keySet());

		boolean removed = false;
		for (String varname : currentVars) {
			if (!varnamesToRetain.contains(varname)) {
				this.jplSubstitution.remove(varname);
				removed = true;
			}
		}
		return removed;
	}

	@Override
	public PrologSubstitution clone() {
		return new PrologSubstitution(new Hashtable<>(this.jplSubstitution));
	}

	/**
	 * Returns a string representation of this {@link PrologSubstitution}.
	 *
	 * @return The string representation of this substitution.
	 */
	@Override
	public String toString() {
		Set<String> variables = this.jplSubstitution.keySet();

		StringBuilder builder = new StringBuilder();

		builder.append("[");
		boolean addComma = false;

		for (String var : variables) {
			if (addComma) {
				builder.append(", ");
			}
			builder.append(var).append("/");
			PrologTerm term = new PrologTerm(this.jplSubstitution.get(var), null);
			builder.append(term.toString());
			addComma = true;
		}
		builder.append("]");

		return builder.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.jplSubstitution == null) ? 0 : this.jplSubstitution.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		PrologSubstitution other = (PrologSubstitution) obj;
		if (this.jplSubstitution == null) {
			if (other.jplSubstitution != null) {
				return false;
			}
		} else if (!this.jplSubstitution.equals(other.jplSubstitution)) {
			return false;
		}
		return true;
	}
}
