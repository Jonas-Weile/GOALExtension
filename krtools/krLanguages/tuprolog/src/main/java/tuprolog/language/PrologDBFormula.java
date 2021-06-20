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

import java.util.Map;

import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.parser.SourceInfo;
import tuprolog.database.PrologDatabase;

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
public class PrologDBFormula extends PrologExpression implements DatabaseFormula {
	/**
	 * Creates a Prolog database formula that can be part of a Prolog database.
	 *
	 * @param term
	 *            A JPL term.
	 * @param info
	 *            A source info object.
	 */
	public PrologDBFormula(alice.tuprolog.Term term, SourceInfo info) {
		super(term, info);
	}

	@Override
	public PrologDBFormula applySubst(Substitution substitution) {
		Map<String, alice.tuprolog.Term> jplSubstitution = (substitution == null) ? null
				: ((PrologSubstitution) substitution).getJPLSolution();
		return new PrologDBFormula(JPLUtils.applySubst(jplSubstitution, getTerm()), getSourceInfo());
	}

	@Override
	public boolean isQuery() {
		return JPLUtils.isQuery(getTerm());
	}

	/**
	 * Converts this database formula into a query, simply using the JPL term of
	 * this {@link DatabaseFormula}. Does not perform any check whether the JPL
	 * term can also be used as a query. Use {@link #toQuery()} to perform this
	 * check.
	 *
	 * @return A {@link Query}.
	 */
	@Override
	public Query toQuery() {
		return new PrologQuery(getTerm(), getSourceInfo());
	}
}