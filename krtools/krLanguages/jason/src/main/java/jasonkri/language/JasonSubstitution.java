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

package jasonkri.language;

import jason.asSemantics.Unifier;
import jason.asSyntax.VarTerm;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;

public class JasonSubstitution implements Substitution {

	/**
	 * A Jason substitution (called unifier).
	 */
	private Unifier substitution;

	public JasonSubstitution(Unifier subtitution) {
		this.substitution = subtitution;
	}

	/**
	 * @return An original Jason substitution.
	 */
	public Unifier getUnifier() {
		return this.substitution;
	}

	@Override
	public Set<Var> getVariables() {
		Set<Var> vars = new LinkedHashSet<Var>();
		Iterator<VarTerm> varIt = this.substitution.iterator();
		while (varIt.hasNext()) {
			VarTerm var = varIt.next();
			vars.add(new JasonVar(var, null)); // HACK #3554
		}
		return vars;
	}

	@Override
	public JasonTerm get(Var vari) {
		JasonVar var = (JasonVar) vari;
		jason.asSyntax.Term term = this.substitution.get(var.getName());
		if (term == null) {
			return null;
		}
		return new JasonTerm(term, var.getSourceInfo());

	}

	@Override
	public void addBinding(Var var, Term term) {
		String varname = ((JasonVar) var).getName();
		if (isBound(varname)) {
			this.substitution.bind(new VarTerm(varname),
					((JasonTerm) term).getJasonTerm());
		}
	}

	/**
	 * Check if var is bound.
	 * 
	 * @param varname
	 *            the varname to check
	 * @return true iff var is bound in this substitution.
	 */
	private boolean isBound(String varname) {
		return this.substitution.get(varname) == null;
	}

	@Override
	public JasonSubstitution combine(Substitution substitution) {
		Unifier unifier = this.substitution.clone();
		unifier.compose(((JasonSubstitution) substitution).getUnifier());
		return new JasonSubstitution(unifier);
	}

	@Override
	public boolean remove(Var var) {
		if (!isBound(((JasonVar) var).getName())) {
			return false;
		}
		this.substitution.remove((VarTerm) ((JasonVar) var).getJasonTerm());
		return true;
	}

	@Override
	public boolean retainAll(Collection<Var> variables) {
		Iterator<VarTerm> varIt = this.substitution.iterator();
		Set<VarTerm> varsToBeRemoved = new LinkedHashSet<VarTerm>();

		while (varIt.hasNext()) {
			VarTerm var = varIt.next();
			if (!variables.contains(new JasonVar(var, null))) {
				varsToBeRemoved.add(var);
			}
		}

		for (VarTerm var : varsToBeRemoved) {
			this.substitution.remove(var);
		}

		return !varsToBeRemoved.isEmpty();
	}

	/**
	 * create a copy of this substitution. The copy contains the same
	 * substitutions as this. Modification of the copy should leave this
	 * unaffected. The copy can be shallow - the terms referenced in this can be
	 * referenced in the copy.
	 */
	@Override
	public JasonSubstitution clone() {
		return new JasonSubstitution(this.substitution.clone());
	}

	@Override
	public String toString() {
		return substitution.toString();
	}
}
