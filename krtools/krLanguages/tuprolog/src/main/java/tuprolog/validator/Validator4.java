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
package tuprolog.validator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import krTools.exceptions.ParserException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Term;
import krTools.language.Update;
import krTools.language.Var;
import tuprolog.errors.ParserErrorMessages;
import tuprolog.language.JPLUtils;
import tuprolog.language.PrologQuery;
import tuprolog.language.PrologTerm;
import tuprolog.language.PrologUpdate;
import tuprolog.language.PrologVar;
import tuprolog.visitor.Visitor4;

/**
 * Parse, visit and validate. All errors occurring during parse or validation
 * are thrown. Normally you use Validator4 which also stores errors as they
 * occur.
 *
 * Usage example to parse string as prolog term:
 * <code>validator = new Validator4(new Prolog4VisitorPlus(
				new ErrorStoringProlog4Parser(new StringReader(in), null)));
				PrologTerm term = validator.term();
				</code>
 */
public class Validator4 {
	private final Visitor4 visitor;
	private final List<ParserException> errors = new LinkedList<>();

	/**
	 * @param visitor
	 *            the {@link Visitor4} (that contains the parser)
	 */
	public Validator4(Visitor4 vis) {
		this.visitor = vis;
	}

	/**
	 * Validate an update or empty term.
	 *
	 * @return {@link Update} or null if there is error.
	 */
	public Update updateOrEmpty() {
		PrologTerm conj = this.visitor.visitPossiblyEmptyConjunct();
		if (conj == null) {
			return null;
		} else if (conj.toString().equals("true")) { // special case.
			return new PrologUpdate(conj.getTerm(), conj.getSourceInfo());
		} else {
			try {
				return SemanticTools.conj2Update(conj);
			} catch (ParserException e) {
				this.errors.add(e);
				return null;
			}
		}
	}

	/**
	 * Validate a Prolog program. Assumes that the parser has been set up
	 * properly.
	 *
	 * @return List<DatabaseFormula>, or {@code null} if a parser error occurs.
	 */
	public List<DatabaseFormula> program() {
		List<PrologTerm> prologTerms = this.visitor.visitPrologtext();
		List<DatabaseFormula> dbfs = new LinkedList<>();
		for (PrologTerm t : prologTerms) {
			try {
				dbfs.add(SemanticTools.DBFormula(t));
			} catch (ParserException e) {
				this.errors.add(e);
			}
		}
		return dbfs;
	}

	/**
	 * Validate a section that should contain Prolog goals, i.e., queries.
	 *
	 * @return List<Query>, or {@code null} if a parser error occurs.
	 */
	public List<Query> goalSection() {
		List<Query> goals = new LinkedList<>();
		for (PrologTerm t : this.visitor.visitPrologtext()) {
			// check that each term is a valid Prolog goal / query
			try {
				goals.add(new PrologQuery(SemanticTools.toGoal(t.getTerm(), t.getSourceInfo()), t.getSourceInfo()));
			} catch (ParserException e) {
				this.errors.add(e);
			}
		}
		return goals;
	}

	/**
	 * Validate a (possibly empty) query.
	 *
	 * @return A {@link PrologQuery}, or {@code null} if an error occurred.
	 */
	public PrologQuery queryOrEmpty() {
		PrologTerm term = this.visitor.visitPossiblyEmptyDisjunct();
		if (term != null) {
			try {
				return SemanticTools.toQuery(term);
			} catch (ParserException e) {
				this.errors.add(e);
			}
		}
		return null;
	}

	/**
	 * Validate the variable. Returns a variable obtained by parsing the input.
	 *
	 * @return {@link Var} or null if error occurred.
	 */
	public Var var() {
		PrologTerm term = this.visitor.visitTerm0();
		if (term != null && term.isVar()) {
			return (PrologVar) term;
		} else {
			this.errors.add(new ParserException(ParserErrorMessages.EXPECTED_VAR.toReadableString(term.toString()),
					term.getSourceInfo()));
			return null;
		}
	}

	/**
	 * Validate a term
	 *
	 * @return term, or null if error occurs
	 * @throws ParserException
	 */
	public PrologTerm term() {
		return this.visitor.visitTerm0();
	}

	/**
	 * Validate a set of parameters.
	 *
	 * @return A list of {@link Term}s.
	 */
	public List<Term> terms() {
		PrologTerm t = this.visitor.visitTerm1000();
		if (t == null) {
			return new ArrayList<>(0);
		} else {
			List<alice.tuprolog.Term> original = JPLUtils.getOperands(",", t.getTerm());
			List<Term> terms = new ArrayList<>(original.size());
			for (alice.tuprolog.Term term : original) {
				if (term instanceof alice.tuprolog.Var) {
					terms.add(new PrologVar((alice.tuprolog.Var) term, t.getSourceInfo()));
				} else {
					terms.add(new PrologTerm(term, t.getSourceInfo()));
				}
			}
			return terms;
		}

	}

	/**
	 * Get all errors that occurred, both in validator and in visitor.
	 *
	 * @return all errors that occurred
	 */
	public SortedSet<ParserException> getErrors() {
		SortedSet<ParserException> allErrors = new TreeSet<ParserException>();
		allErrors.addAll(this.visitor.getErrors());
		allErrors.addAll(this.errors);
		return allErrors;
	}

	/**
	 * @return true iff parsing was successfull which means {@link #getErrors()}
	 *         returns empty list.
	 */
	public boolean isSuccess() {
		return getErrors().isEmpty();
	}
}