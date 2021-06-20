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
package swiprolog.validator;

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
import swiprolog.errors.ParserErrorMessages;
import swiprolog.language.PrologCompound;
import swiprolog.language.PrologQuery;
import swiprolog.language.PrologTerm;
import swiprolog.language.impl.PrologImplFactory;
import swiprolog.visitor.Visitor4;

/**
 * Parse, visit and validate. All errors occurring during parse or validation
 * are thrown. Normally you use Validator4 which also stores errors as they
 * occur.
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
		Term conj = this.visitor.visitPossiblyEmptyConjunct();
		if (!(conj instanceof PrologCompound)) {
			this.errors.add(new ParserException(ParserErrorMessages.EXPECTED_COMPOUND.toReadableString(conj),
					(conj == null) ? null : conj.getSourceInfo()));
			return null;
		} else if (conj.toString().equals("true")) { // special case.
			return PrologImplFactory.getUpdate((PrologCompound) conj);
		} else {
			try {
				return SemanticTools.conj2Update((PrologCompound) conj);
			} catch (ParserException e) {
				this.errors.add(e);
				return null;
			}
		}
	}

	/**
	 * Validate a Prolog program. Assumes that the parser has been set up properly.
	 *
	 * @return List<DatabaseFormula>, or {@code null} if a parser error occurs.
	 */
	public List<DatabaseFormula> program() {
		List<DatabaseFormula> dbfs = new LinkedList<>();
		for (Term t : this.visitor.visitPrologtext()) {
			if (t instanceof PrologCompound) {
				try {
					dbfs.add(SemanticTools.DBFormula((PrologCompound) t));
				} catch (ParserException e) {
					this.errors.add(e);
				}
			} else {
				this.errors.add(new ParserException(ParserErrorMessages.EXPECTED_COMPOUND.toReadableString(t),
						(t == null) ? null : t.getSourceInfo()));
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
		for (Term t : this.visitor.visitPrologtext()) {
			if (t instanceof PrologCompound) {
				try {
					goals.add(PrologImplFactory.getQuery(SemanticTools.toGoal(t)));
				} catch (ParserException e) {
					this.errors.add(e);
				}
			} else {
				this.errors.add(new ParserException(ParserErrorMessages.EXPECTED_COMPOUND.toReadableString(t),
						(t == null) ? null : t.getSourceInfo()));
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
		Term term = this.visitor.visitPossiblyEmptyDisjunct();
		if (term instanceof PrologCompound) {
			try {
				return SemanticTools.toQuery((PrologCompound) term);
			} catch (ParserException e) {
				this.errors.add(e);
			}
		} else {
			this.errors.add(new ParserException(ParserErrorMessages.EXPECTED_COMPOUND.toReadableString(term),
					(term == null) ? null : term.getSourceInfo()));
		}
		return null;
	}

	/**
	 * Validate the variable. Returns a variable obtained by parsing the input.
	 *
	 * @return {@link Var} or null if error occurred.
	 */
	public Var var() {
		Term term = this.visitor.visitTerm0();
		if (term instanceof Var) {
			return (Var) term;
		} else {
			this.errors.add(new ParserException(ParserErrorMessages.EXPECTED_VAR.toReadableString(term),
					(term == null) ? null : term.getSourceInfo()));
			return null;
		}
	}

	/**
	 * Validate a term
	 *
	 * @return term, or null if error occurs
	 * @throws ParserException
	 */
	public Term term() {
		return this.visitor.visitTerm0();
	}

	/**
	 * Validate a set of parameters.
	 *
	 * @return A list of {@link Term}s.
	 */
	public List<Term> terms() {
		Term t = this.visitor.visitTerm1000();
		if (t instanceof PrologCompound) {
			return new ArrayList<>(((PrologCompound) t).getOperands(","));
		} else if (t instanceof PrologTerm) {
			List<Term> single = new ArrayList<>(1);
			single.add(t);
			return single;
		} else {
			return new ArrayList<>(0);
		}
	}

	/**
	 * Get all errors that occurred, both in validator and in visitor.
	 *
	 * @return all errors that occurred
	 */
	public SortedSet<ParserException> getErrors() {
		SortedSet<ParserException> allErrors = new TreeSet<>();
		allErrors.addAll(this.visitor.getErrors());
		allErrors.addAll(this.errors);
		return allErrors;
	}

	/**
	 * @return true iff parsing was successful which means {@link #getErrors()}
	 *         returns empty list.
	 */
	public boolean isSuccess() {
		return getErrors().isEmpty();
	}
}