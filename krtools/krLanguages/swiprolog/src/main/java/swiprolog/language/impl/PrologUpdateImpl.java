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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import krTools.language.DatabaseFormula;
import krTools.language.Expression;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Update;
import krTools.language.Var;
import krTools.parser.SourceInfo;
import swiprolog.language.PrologCompound;
import swiprolog.language.PrologUpdate;

/**
 * See {@link Update}.
 */
class PrologUpdateImpl implements PrologUpdate {
	/**
	 *
	 */
	private final PrologCompound compound;
	/**
	 * List of literals that occur positively in the term used to construct this
	 * update.
	 */
	private final List<DatabaseFormula> positiveLiterals = new LinkedList<>();
	/**
	 * List of literals that occur negated in the term used to construct this
	 * update.
	 */
	private final List<DatabaseFormula> negativeLiterals = new LinkedList<>();

	/**
	 * Creates a Prolog {@link Update}.
	 *
	 * <p>
	 * Analyzes the JPL term and separates the positive from the negative literals
	 * to create add and delete lists.
	 * </p>
	 *
	 * @param term A JPL term. Assumes that this term is a conjunction and can be
	 *             split into a list of conjuncts.
	 * @param info A source info object.
	 */
	PrologUpdateImpl(final PrologCompound compound) {
		this.compound = compound;
		// Sort positive and negative literals, assuming that each conjunct
		// is a database formula (which should have been checked by the parser).
		for (final Term conjunct : compound.getOperands(",")) {
			final PrologCompound term = (PrologCompound) conjunct;
			if ((term.getArity() == 1) && term.getArg(0) instanceof PrologCompound && term.getName().equals("not")) {
				final PrologCompound content = (PrologCompound) term.getArg(0);
				this.negativeLiterals.add(PrologImplFactory.getDBFormula(content));
			} else if (!((term.getArity() == 0) && "true".equals(term.getName()))) {
				this.positiveLiterals.add(PrologImplFactory.getDBFormula(term));
			}
		}
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
	public List<DatabaseFormula> getAddList() {
		return Collections.unmodifiableList(this.positiveLiterals);
	}

	@Override
	public List<DatabaseFormula> getDeleteList() {
		return Collections.unmodifiableList(this.negativeLiterals);
	}

	@Override
	public PrologUpdate applySubst(final Substitution s) {
		return PrologImplFactory.getUpdate((PrologCompound) this.compound.applySubst(s));
	}

	@Override
	public Query toQuery() {
		return PrologImplFactory.getQuery(this.compound);
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
		} else if (!(obj instanceof PrologUpdateImpl)) {
			return false;
		}
		final PrologUpdateImpl other = (PrologUpdateImpl) obj;
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