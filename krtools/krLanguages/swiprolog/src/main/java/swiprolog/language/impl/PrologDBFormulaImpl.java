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

import java.util.Set;

import krTools.language.DatabaseFormula;
import krTools.language.Expression;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Update;
import krTools.language.Var;
import krTools.parser.SourceInfo;
import swiprolog.database.PrologDatabase;
import swiprolog.language.PrologCompound;
import swiprolog.language.PrologDBFormula;

/**
 * <p>
 * A Prolog database formula is an expression that can be inserted into a
 * {@link PrologDatabase}.
 * </p>
 *
 * <p>
 * Performs no checks whether a Prolog term can be inserted into a Prolog
 * database for efficiency reasons (to avoid such checks at run time, e.g., when
 * creating a new instantiated formula when applying a substitution). The
 * responsibility to check this is delegated to the parser (which ensures that
 * the check is only performed at compile time).
 * </p>
 */
class PrologDBFormulaImpl implements PrologDBFormula {
	private final PrologCompound compound;

	/**
	 * Creates a Prolog database formula that can be part of a Prolog database.
	 *
	 * @param compound A Prolog compound.
	 */
	PrologDBFormulaImpl(PrologCompound compound) {
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
	public PrologDBFormula applySubst(Substitution substitution) {
		return PrologImplFactory.getDBFormula((PrologCompound) this.compound.applySubst(substitution));
	}

	/**
	 * Converts this database formula into a query, simply using the JPL term of
	 * this {@link DatabaseFormula}. Does not perform any check whether the JPL term
	 * can also be used as a query. Use {@link #toQuery()} to perform this check.
	 *
	 * @return A {@link Query}.
	 */
	@Override
	public Query toQuery() {
		return PrologImplFactory.getQuery(this.compound);
	}

	@Override
	public Update toUpdate() {
		return PrologImplFactory.getUpdate(this.compound);
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
	public Substitution mgu(Expression expression) {
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
	public boolean equals(Object obj) {
		if (this == obj || obj == null) {
			return true;
		} else if (!(obj instanceof PrologDBFormulaImpl)) {
			return false;
		}
		PrologDBFormulaImpl other = (PrologDBFormulaImpl) obj;
		if (this.compound == null) {
			if (other.compound != null) {
				return false;
			}
		} else if (!this.compound.equals(other.compound)) {
			return false;
		}
		return true;
	}
}