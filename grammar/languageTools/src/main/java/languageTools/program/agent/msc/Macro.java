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

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import krTools.KRInterface;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;
import krTools.parser.SourceInfo;

/**
 * A macro definition derived from:
 * <code>#define macroname(arglist) definition</code>
 */
public class Macro extends MentalFormula {
	/**
	 * The name/label of the macro
	 */
	private final String name;
	/**
	 * The arguments of this macro (can be empty)
	 */
	private final List<Term> parameters;
	/**
	 * The MSC this {@link Macro} is a shorthand for
	 */
	private MentalStateCondition definition;
	/**
	 * A cache of free variables in the parameters of the macro.
	 */
	private final Set<Var> free = new LinkedHashSet<>();

	/**
	 * Creates a new macro definition.
	 *
	 * @param name       The name/label of the macro
	 * @param parameters The parameters of the macro (can be the empty list but not
	 *                   null).
	 * @param definition The {@link MentalStateCondition} the macro is a shorthand
	 *                   for. If null, this macro reference still needs to be
	 *                   resolved. Should only be null for macro instances.
	 * @param info       Source info about this object.
	 */
	public Macro(String name, List<Term> parameters, MentalStateCondition definition, SourceInfo info) {
		super(info);
		this.name = name;
		this.parameters = parameters;
		this.definition = definition;
		for (Term t : this.parameters) {
			this.free.addAll(t.getFreeVar());
			if (t.isVar() && !t.isClosed()) {
				this.free.add((Var) t);
			}
		}
	}

	/**
	 * The name of this {@link Macro}.
	 *
	 * @return The name of this macro.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @return The list of formal parameters of this macro.
	 */
	public List<Term> getParameters() {
		return Collections.unmodifiableList(this.parameters);
	}

	@Override
	public Set<String> getUsedSignatures() {
		Set<String> signature = new LinkedHashSet<>(1);
		signature.add(getSignature());
		return signature;
	}

	/**
	 * Returns the definition of this {@link Macro}.
	 *
	 * @return The definition of this macro.
	 */
	public MentalStateCondition getDefinition() {
		return this.definition;
	}

	/**
	 * Set the definition of this {@link Macro}.
	 *
	 * @param definition The definition to be used for this macro.
	 */
	public void setDefinition(MentalStateCondition definition) {
		this.definition = definition;
	}

	@Override
	public Set<Var> getFreeVar() {
		return Collections.unmodifiableSet(this.free);
	}

	/**
	 * Applies a substitution to this macro.
	 * <p>
	 * If the substitution applied instantiates instead of only renames formal
	 * parameters of this macro, then the resulting macro may have a different
	 * number of formal parameters. For example, applying {X/f(Y,Z)} to a macro with
	 * one formal parameter X will return a new macro with two formal parameters Y
	 * and Z.
	 * </p>
	 *
	 * @return A version of this macro where variables have been renamed.
	 */
	@Override
	public Macro applySubst(Substitution substitution) {
		List<Term> parameters = new LinkedList<>();
		for (Term term : getParameters()) {
			Term instantiatedTerm = term.applySubst(substitution);
			parameters.addAll(instantiatedTerm.getFreeVar());
		}
		return new Macro(getName(), parameters,
				(getDefinition() == null) ? null : getDefinition().applySubst(substitution), getSourceInfo());
	}

	// TODO: move this functionality to KR interface...
	public Substitution mgu(Macro other, KRInterface kri) {
		if (other == null || !getSignature().equals(other.getSignature())
				|| getParameters().size() != other.getParameters().size()) {
			return null;
		} else if (getParameters().isEmpty()) {
			return kri.getSubstitution(null);
		} else {
			// Get mgu for first parameter
			Substitution substitution = getParameters().get(0).mgu(other.getParameters().get(0));
			// Get mgu's for remaining parameters
			for (int i = 1; i < getParameters().size() && substitution != null; i++) {
				Substitution mgu = getParameters().get(i).mgu(other.getParameters().get(i));
				substitution = substitution.combine(mgu);
			}
			return substitution;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
		result = prime * result + ((this.parameters == null) ? 0 : this.parameters.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || !(obj instanceof Macro)) {
			return false;
		}
		Macro other = (Macro) obj;
		if (this.name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!this.name.equals(other.name)) {
			return false;
		}
		if (this.parameters == null) {
			if (other.parameters != null) {
				return false;
			}
		} else if (!this.parameters.equals(other.parameters)) {
			return false;
		}
		return true;
	}

	/**
	 * @return A string with macro name and parameters.
	 */
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(this.name);
		if (!this.parameters.isEmpty()) {
			str.append("(");
			Iterator<Term> pars = this.parameters.iterator();
			while (pars.hasNext()) {
				str.append(pars.next());
				str.append(pars.hasNext() ? ", " : "");
			}
			str.append(")");
		}
		return str.toString();
	}

	/**
	 * Builds a string representation of this {@link Macro}.
	 *
	 * @param linePrefix A prefix used to indent parts of a program, e.g., a single
	 *                   space or tab.
	 * @param indent     A unit to increase indentation with, e.g., a single space
	 *                   or tab.
	 * @return A string-representation of this macro.
	 */
	public String toString(String linePrefix, String indent) {
		StringBuilder str = new StringBuilder();
		str.append(linePrefix + "<macro: " + this);
		str.append(", " + this.definition + ">");
		return str.toString();
	}

	/**
	 * Gets a string representing the signature of this {@link Macro}.
	 *
	 * @return A string of the format {macro name}/{number of parameters}
	 */
	public String getSignature() {
		return this.name + "/" + String.valueOf(getParameters().size());
	}
}
