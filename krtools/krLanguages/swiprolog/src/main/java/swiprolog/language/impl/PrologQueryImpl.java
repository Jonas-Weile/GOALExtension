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

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import krTools.language.DatabaseFormula;
import krTools.language.Expression;
import krTools.language.Substitution;
import krTools.language.Update;
import krTools.language.Var;
import krTools.parser.SourceInfo;
import swiprolog.language.PrologCompound;
import swiprolog.language.PrologQuery;
import swiprolog.language.PrologTerm;
import swiprolog.language.PrologVar;

/**
 * A Prolog query.
 */
class PrologQueryImpl extends org.jpl7.Query implements PrologQuery {
	/**
	*
	*/
	private final PrologCompound compound;

	/**
	 * Creates a Prolog query.
	 *
	 * <p>
	 * Performs no checks whether the term can be queried on a Prolog database for
	 * efficiency reasons (to avoid checks at run time, e.g., as a result from
	 * applying a substitution). These checks have been delegated to the parser (to
	 * perform checks at compile time only).
	 * </p>
	 *
	 * @param compound A compound that can be used as a query.
	 */
	PrologQueryImpl(final PrologCompound compound) {
		super((org.jpl7.Term) compound);
		this.compound = compound;
	}

	@Override
	public PrologCompound getCompound() {
		return this.compound;
	}

	@Override
	public SourceInfo getSourceInfo() {
		return this.compound.getSourceInfo();
	}

	@Override
	public PrologQuery applySubst(final Substitution substitution) {
		return new PrologQueryImpl((PrologCompound) this.compound.applySubst(substitution));
	}

	/**
	 * ASSUMES the inner prolog term of the query can also be parsed as an update.
	 * If called on (a-)goal literals in the context of a module, this has already
	 * been checked by the parser.
	 */
	@Override
	public Update toUpdate() {
		return PrologImplFactory.getUpdate(this.compound);
	}
	
	@Override
	public DatabaseFormula toDBF() {
		return PrologImplFactory.getDBFormula(this.compound);
	}

	@Override
	public String getSignature() {
		return this.compound.getSignature();
	}

	@Override
	public boolean isClosed() {
		return this.compound.isClosed();
	}

	@Override
	public Set<Var> getFreeVar() {
		return this.compound.getFreeVar();
	}

	@Override
	public Substitution mgu(final Expression expression) {
		return this.compound.mgu(expression);
	}

	@Override
	public String toString() {
		return this.compound.toString();
	}

	@Override
	public int hashCode() {
		return this.compound.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj || obj == null) {
			return true;
		} else if (!(obj instanceof PrologQueryImpl)) {
			return false;
		}
		final PrologQueryImpl other = (PrologQueryImpl) obj;
		if (this.compound == null) {
			if (other.compound != null) {
				return false;
			}
		} else if (!this.compound.equals(other.compound)) {
			return false;
		}
		return true;
	}

	//
	// JPL overrides to directly produce Prolog###Impl objects for queries:
	//

	@Override
	protected Map<String, org.jpl7.Term> getCurrentSolutionBindings() {
		if (!this.open) {
			throw new org.jpl7.JPLException("Query is not open, cannot retreive solution bindings.");
		} else {
			final Map<String, org.jpl7.Term> varnames_to_Terms = new HashMap<>();
			final Map<org.jpl7.fli.term_t, PrologVar> vars_to_Vars = new HashMap<>();
			getSubst(varnames_to_Terms, vars_to_Vars, (org.jpl7.Compound) this.compound);
			return varnames_to_Terms;
		}
	}

	private static void getSubst(final Map<String, org.jpl7.Term> varnames_to_Terms,
			final Map<org.jpl7.fli.term_t, PrologVar> vars_to_Vars, final org.jpl7.Term arg) {
		if (arg instanceof org.jpl7.Compound) {
			for (final org.jpl7.Term subarg : ((org.jpl7.Compound) arg).args()) {
				getSubst(varnames_to_Terms, vars_to_Vars, subarg);
			}
		} else if (arg instanceof org.jpl7.Variable) {
			final String name = ((org.jpl7.Variable) arg).name();
			if (!name.equals("_") && varnames_to_Terms.get(name) == null) {
				varnames_to_Terms.put(name, (org.jpl7.Term) getTerm(vars_to_Vars, ((org.jpl7.Variable) arg).term_));
			}
		}
	}

	private static PrologTerm getTerm(final Map<org.jpl7.fli.term_t, PrologVar> vars_to_Vars,
			final org.jpl7.fli.term_t term) {
		switch (org.jpl7.fli.Prolog.term_type(term)) {
		case org.jpl7.fli.Prolog.VARIABLE:
			for (final org.jpl7.fli.term_t varX : vars_to_Vars.keySet()) {
				if (org.jpl7.fli.Prolog.compare(varX, term) == 0) {
					return vars_to_Vars.get(varX);
				}
			}
			final PrologVar Var = PrologImplFactory.getVar(new org.jpl7.Variable().name(), null);
			((org.jpl7.Variable)Var).term_ = term;
			vars_to_Vars.put(term, Var);
			return Var;
		case org.jpl7.fli.Prolog.ATOM:
			final org.jpl7.fli.StringHolder hString1 = new org.jpl7.fli.StringHolder();
			org.jpl7.fli.Prolog.get_atom_chars(term, hString1);
			return PrologImplFactory.getAtom(hString1.value, null);
		case org.jpl7.fli.Prolog.STRING:
			final org.jpl7.fli.StringHolder hString2 = new org.jpl7.fli.StringHolder();
			org.jpl7.fli.Prolog.get_string_chars(term, hString2);
			return PrologImplFactory.getAtom(hString2.value, null);
		case org.jpl7.fli.Prolog.INTEGER:
			final org.jpl7.fli.Int64Holder hInt64 = new org.jpl7.fli.Int64Holder();
			if (org.jpl7.fli.Prolog.get_integer(term, hInt64)) {
				return PrologImplFactory.getNumber(hInt64.value, null);
			} else {
				final org.jpl7.fli.StringHolder hString3 = new org.jpl7.fli.StringHolder();
				if (org.jpl7.fli.Prolog.get_integer_big(term, hString3)) {
					return PrologImplFactory.getAtom(hString3.value, null);
				} else {
					throw new org.jpl7.JPLException("unsupported integer passed from Prolog");
				}
			}
		case org.jpl7.fli.Prolog.RATIONAL:
			final org.jpl7.fli.StringHolder hString4 = new org.jpl7.fli.StringHolder();
			if (org.jpl7.fli.Prolog.get_rational(term, hString4)) {
				return PrologImplFactory.getAtom(hString4.value, null);
			} else {
				throw new org.jpl7.JPLException("unsupported rational passed from Prolog");
			}
		case org.jpl7.fli.Prolog.FLOAT:
			final org.jpl7.fli.DoubleHolder hFloatValue = new org.jpl7.fli.DoubleHolder();
			org.jpl7.fli.Prolog.get_float(term, hFloatValue);
			return PrologImplFactory.getNumber(hFloatValue.value, null);
		case org.jpl7.fli.Prolog.COMPOUND:
		case org.jpl7.fli.Prolog.LIST_PAIR:
			final org.jpl7.fli.StringHolder hString5 = new org.jpl7.fli.StringHolder();
			final org.jpl7.fli.IntHolder hInt1 = new org.jpl7.fli.IntHolder();
			org.jpl7.fli.Prolog.get_name_arity(term, hString5, hInt1);
			final PrologTerm[] args1 = new PrologTerm[hInt1.value];
			for (int i = 1; i <= hInt1.value; i++) {
				final org.jpl7.fli.term_t termi = org.jpl7.fli.Prolog.new_term_ref();
				org.jpl7.fli.Prolog.get_arg(i, term, termi);
				args1[i - 1] = getTerm(vars_to_Vars, termi);
			}
			return PrologImplFactory.getCompound(hString5.value, args1, null);
		case org.jpl7.fli.Prolog.LIST_NIL:
			return PrologImplFactory.getAtom(org.jpl7.JPL.LIST_NIL.name(), null);
		default: // should never happen
			throw new org.jpl7.JPLException("Unsupported term type: " + org.jpl7.fli.Prolog.term_type(term) + ".");
		}
	}
}