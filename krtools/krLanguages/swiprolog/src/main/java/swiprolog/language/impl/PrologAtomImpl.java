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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.jpl7.JPL;

import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;
import krTools.parser.SourceInfo;
import swiprolog.language.PrologCompound;
import swiprolog.parser.PrologOperators;

/**
 * A Prolog variable.
 */
class PrologAtomImpl extends org.jpl7.Atom implements PrologCompound {
	/**
	 * Information about the source used to construct this atom.
	 */
	private final SourceInfo info;
	/**
	 * Cache the atom's hash for performance
	 */
	private final int hashcode;

	/**
	 * Creates an atom (i.e. a compound without arguments).
	 *
	 * @param name The atom's name
	 * @param info A source info object.
	 */
	PrologAtomImpl(String name, SourceInfo info) {
		super(name, name.equals(JPL.LIST_NIL.name()) ? "reserved_symbol" : "text");
		this.info = info;
		this.hashcode = name.hashCode();
	}

	@Override
	public SourceInfo getSourceInfo() {
		return this.info;
	}

	@Override
	public String getName() {
		return name();
	}

	@Override
	public int getArity() {
		return 0;
	}

	@Override
	public Term getArg(int i) {
		return null;
	}

	@Override
	public boolean isClosed() {
		return true;
	}

	@Override
	public boolean isPredication() {
		return PrologOperators.is_L_atom(this.name);
	}

	@Override
	public boolean isPredicateIndicator() {
		return false;
	}

	@Override
	public boolean isDirective() {
		return false;
	}

	@Override
	public boolean isQuery() {
		return true; // FIXME
	}

	@Override
	public Set<Var> getFreeVar() {
		return new HashSet<>(0);
	}

	@Override
	public String getSignature() {
		return this.name + "/0";
	}

	@Override
	public List<Term> getOperands(String operator) {
		List<Term> list = new ArrayList<>(1);
		list.add(this);
		return list;
	}

	@Override
	public PrologAtomImpl applySubst(Substitution s) {
		return this;
	}

	@Override
	public int hashCode() {
		return this.hashcode;
	}

	@Override
	public Iterator<Term> iterator() {
		return Collections.emptyIterator();
	}

	@Override
	public String toString() {
		return getName();
	}
}