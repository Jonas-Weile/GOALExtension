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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import krTools.language.Expression;
import krTools.language.Substitution;
import krTools.language.Var;
import krTools.parser.SourceInfo;

public abstract class PrologExpression implements Expression {
	/**
	 * A JPL term representing a Prolog expression.
	 */
	private final alice.tuprolog.Term term;
	/**
	 * Information about the source used to construct this expression.
	 */
	private final SourceInfo info;

	/**
	 * Creates a Prolog expression.
	 *
	 * @term A JPL term.
	 */
	public PrologExpression(alice.tuprolog.Term term, SourceInfo info) {
		this.term = term;
		this.info = info;
	}

	/**
	 * Returns the JPL term.
	 *
	 * @return A {@link alice.tuprolog.Term}.
	 */
	public alice.tuprolog.Term getTerm() {
		return this.term;
	}

	/**
	 * @return A {@link SourceInfo} object with information about the source
	 *         used to construct this expression.
	 */
	@Override
	public SourceInfo getSourceInfo() {
		return this.info;
	}

	/**
	 * Checks whether this expression is a variables.
	 *
	 * @return {@code true} if this expression is a variable; {@code false}
	 *         otherwise.
	 */
	@Override
	public boolean isVar() {
		return getTerm() instanceof alice.tuprolog.Var;
	}

	/**
	 * Returns the (free) variables that occur in this expression.
	 *
	 * @return The (free) variables that occur in this expression.
	 */
	@Override
	public Set<Var> getFreeVar() {
		List<String> jplvars = new ArrayList<>(JPLUtils.getFreeVar(getTerm()));
		Set<Var> variables = new LinkedHashSet<>(jplvars.size());
		for (String var : jplvars) {
			variables.add(new PrologVar(new alice.tuprolog.Var(var), getSourceInfo()));
		}
		return variables;
	}

	/**
	 * Checks whether this expression is closed, i.e., has no occurrences of
	 * (free) variables.
	 *
	 * @return {@code true} if this expression is closed.
	 */
	@Override
	public boolean isClosed() {
		return JPLUtils.getFreeVar(getTerm()).isEmpty();
	}

	/**
	 * Returns a most general unifier, if it exists, that unifies this and the
	 * given expression.
	 *
	 * @return A unifier for this and the given expression, if it exists;
	 *         {@code null} otherwise.
	 */
	@Override
	public Substitution mgu(Expression expression) {
		alice.tuprolog.Term otherterm = ((PrologExpression) expression).getTerm();
		return PrologSubstitution.getSubstitutionOrNull(JPLUtils.mgu(getTerm(), otherterm));
	}

	/**
	 * Returns the signature of this expression.
	 * <p>
	 * Signature is funcname+"/"+#arguments, eg "member/2". default is
	 * mainoperator+"/"+arity so you do not have ot override this. Note that
	 * signature of a variable is set to X/0.
	 * </p>
	 *
	 * @return The signature of this Prolog expression.
	 */
	@Override
	public String getSignature() {
		return JPLUtils.getSignature(getTerm());
	}

	/**
	 *
	 */
	public boolean isEmpty() {
		return getSignature().equals("true/0");
	}

	@Override
	public String toString() {
		return JPLUtils.toString(getTerm());
	}

	@Override
	public int hashCode() {
		return JPLUtils.hashCode(getTerm());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		PrologExpression other = (PrologExpression) obj;
		if (getTerm() == null) {
			if (other.getTerm() != null) {
				return false;
			}
		} // JPL does not implement equals...
		else if (!JPLUtils.equals(getTerm(), other.getTerm())) {
			return false;
		}
		return true;
	}
}