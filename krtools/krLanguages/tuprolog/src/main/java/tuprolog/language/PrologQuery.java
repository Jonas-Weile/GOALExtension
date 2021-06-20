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

import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Update;
import krTools.parser.SourceInfo;

/**
 * A Prolog query.
 */
public class PrologQuery extends PrologExpression implements Query {
	/**
	 * Creates a Prolog query.
	 *
	 * <p>
	 * Performs no checks whether the JPL term can be queried on a Prolog
	 * database for efficiency reasons (to avoid checks at run time, e.g., as a
	 * result from applying a substitution). These checks have been delegated to
	 * the parser (to perform checks at compile time only).
	 * </p>
	 *
	 * @param term
	 *            A JPL term that can be used as a query.
	 * @param info
	 *            A source info object.
	 */
	public PrologQuery(alice.tuprolog.Term term, SourceInfo info) {
		super(term, info);
	}

	@Override
	public Query applySubst(Substitution s) {
		Map<String, alice.tuprolog.Term> jplSubstitution = (s == null) ? null
				: ((PrologSubstitution) s).getJPLSolution();
		return new PrologQuery(JPLUtils.applySubst(jplSubstitution, getTerm()), getSourceInfo());
	}

	@Override
	public boolean isUpdate() {
		// TODO
		return true;
	}

	/**
	 * ASSUMES the inner prolog term of the query can also be parsed as an
	 * update. If called on (a-)goal literals in the context of a module, this
	 * has already been checked by the parser.
	 */
	@Override
	public Update toUpdate() {
		return new PrologUpdate(getTerm(), getSourceInfo());
	}
}