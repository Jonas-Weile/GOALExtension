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

package swiprolog.language.impl;

import java.util.LinkedHashSet;
import java.util.Set;

import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;
import krTools.parser.SourceInfo;
import swiprolog.language.PrologVar;

/**
 * A Prolog variable.
 */
class PrologVarImpl extends org.jpl7.Variable implements PrologVar {
	/**
	 * Information about the source used to construct this variable.
	 */
	private final SourceInfo info;
	/**
	 * A set containing only the variable itself (cached for performance).
	 */
	private final Set<Var> freeVar = new LinkedHashSet<>(1);
	/**
	 * Cache the variable's hash for performance.
	 */
	private final int hashcode;

	/**
	 * Creates a variable.
	 *
	 * @param var  The variable name
	 * @param info A source info object.
	 */
	PrologVarImpl(String name, SourceInfo info) {
		super(name);
		this.info = info;
		this.hashcode = name.hashCode();
		this.freeVar.add(this);
	}

	@Override
	public SourceInfo getSourceInfo() {
		return this.info;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public boolean isClosed() {
		return false;
	}

	@Override
	public boolean isQuery() {
		return false;
	}

	@Override
	public Set<Var> getFreeVar() {
		return this.freeVar;
	}

	@Override
	public Var getVariant(Set<Var> usedNames) {
		int n = 1;
		Var newVar;
		do {
			newVar = PrologImplFactory.getVar(this.name + "_" + n, this.info);
			n++;
		} while (usedNames.contains(newVar));

		return newVar;
	}

	@Override
	public String getSignature() {
		return this.name + "/0";
	}

	@Override
	public Term applySubst(Substitution s) {
		Term value = (s == null) ? null : s.get(this);
		return (value == null) ? this : value;
	}

	@Override
	public int hashCode() {
		return this.hashcode;
	}

	@Override
	public String toString() {
		return getName();
	}
}