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

import java.util.List;

import krTools.exceptions.ParserException;
import krTools.language.DatabaseFormula;
import krTools.language.Update;
import krTools.parser.SourceInfo;
import tuprolog.errors.ParserErrorMessages;
import tuprolog.language.JPLUtils;
import tuprolog.language.PrologDBFormula;
import tuprolog.language.PrologQuery;
import tuprolog.language.PrologTerm;
import tuprolog.language.PrologUpdate;
import tuprolog.parser.PrologOperators;

/**
 * Tools for semantic checking of {@link PrologTerm}s and conversion to
 * {@link Update} etc.
 */
public class SemanticTools {

	/**
	 * <p>
	 * Performs additional checks on top of {@link #basicUpdateCheck}, to check
	 * that conjunct is good update.
	 * </p>
	 *
	 * @param {@link
	 * 			PrologTerm} that is supposedly an update (ie conjunct of [not]
	 *            dbFormula)
	 * @returns the original term (no conversion is performed)
	 * @throws ParserException
	 *             if term is no good Update.
	 * @see #checkDBFormula
	 */
	public static PrologUpdate conj2Update(PrologTerm conjunct) throws ParserException {
		return new PrologUpdate(basicUpdateCheck(conjunct), conjunct.getSourceInfo());
	}

	/**
	 * <p>
	 * Checks if {@link PrologTerm} may be used as update, i.e. a conjunct of
	 * either a {@link DatabaseFormula} or not(DatabaseFormula).
	 *
	 * @param conjunct
	 *            is the PrologTerm to be checked which is a zipped conjunct.
	 *            (see {@link PrologTerm#getConjuncts})
	 * @throws ParserException
	 *             if term not acceptable as DatabaseFormula.
	 *             </p>
	 * @returns original conjunct (if term is good update)
	 */
	private static alice.tuprolog.Term basicUpdateCheck(PrologTerm conjunct) throws ParserException {
		List<alice.tuprolog.Term> terms = JPLUtils.getOperands(",", conjunct.getTerm());
		for (alice.tuprolog.Term term : terms) {
			if (JPLUtils.getSignature(term).equals("not/1")) {
				DBFormula(new PrologTerm(((alice.tuprolog.Struct) term).getArg(0), conjunct.getSourceInfo()));
			} else {
				DBFormula(new PrologTerm(term, conjunct.getSourceInfo()));
			}
		}
		return conjunct.getTerm();
	}

	/**
	 * <p>
	 * Converts given {@Link PrologTerm} into a {@link DatabaseFormula} object
	 * made of it. Basic idea is that it checks that assert() will work (not
	 * throw exceptions). This function checks only SINGLE formulas, not
	 * conjunctions. Use toDBFormulaList for that.
	 * </p>
	 * <p>
	 * Check ISO section 8.9.1.3. If term not of form "head:-body" then head is
	 * to be taken the term itself and body to be "true" We fail if<br>
	 * 1. Head is a variable<br>
	 * 2. head can not be converted to a predication (@see D-is-a-precication in
	 * ISO p.132- )<br>
	 * 3. body can not be converted to a goal<br>
	 * CHECK 4.
	 * "The predicate indicator Pred of Head is not that of a dynamic procedure"
	 * . What does that mean and should we do something to prevent this?
	 * </p>
	 * <p>
	 * ISO section 6.2 also deals with this. Basically it defines directive
	 * terms and clause terms, and combined they allow every term that can be
	 * part of the database.
	 * </p>
	 * <p>
	 * ISO section 7.4 defines the way they are used when loading a database.
	 * Particularly the operators that are Directives are treated specially. In
	 * GOAL we do not want to support these and need exclusion. this is done via
	 * PrologOperators.goalProtected()
	 * </p>
	 *
	 * @param conjunction
	 *            is a PrologTerm containing a conjunction
	 * @see PrologTerm#getConjuncts
	 * @see PrologTerm#useNotAllowed useNotAllowed
	 * @see toDBFormulaList
	 * @returns DatabaseFormula object made from conjunction
	 * @throws ParserException
	 *             If Prolog term is not a valid clause.
	 */
	public static DatabaseFormula DBFormula(PrologTerm term) throws ParserException {
		alice.tuprolog.Term head, body;

		if (term.getSignature().equals(":-/2")) {
			head = ((alice.tuprolog.Struct) term.getTerm()).getArg(0);
			body = ((alice.tuprolog.Struct) term.getTerm()).getArg(1);
		} else {
			head = term.getTerm();
			body = new alice.tuprolog.Struct("true");
		}

		if (head instanceof alice.tuprolog.Var) {
			throw new ParserException(ParserErrorMessages.HEAD_CANT_BE_VAR.toReadableString(term.toString()),
					term.getSourceInfo());
		} else if (!JPLUtils.isPredication(head)) {
			throw new ParserException(ParserErrorMessages.HEAD_MUST_BE_CLAUSE.toReadableString(term.toString()),
					term.getSourceInfo());
		}

		String signature = JPLUtils.getSignature(head);
		if (signature.equals(":-/1")) {
			alice.tuprolog.Struct directive = (alice.tuprolog.Struct) ((alice.tuprolog.Struct) term.getTerm())
					.getArg(0);
			signature = JPLUtils.getSignature(directive);
			if (JPLUtils.getSignature(directive).equals("dynamic/1")) {
				alice.tuprolog.Struct headTerm = (alice.tuprolog.Struct) directive.getArg(0);
				signature = headTerm.getName() + "/" + headTerm.getArity();
				if (signature.equals("//2")) {
					// the term is already a signature itself
					signature = headTerm.getArg(0) + "/" + headTerm.getArg(1);
				}
				if (PrologOperators.prologBuiltin(signature)) {
					throw new ParserException(ParserErrorMessages.CANNOT_REDEFINE_BUILT_IN.toReadableString(signature),
							term.getSourceInfo());
				}
			}
		} else if (PrologOperators.prologBuiltin(signature)) {
			throw new ParserException(ParserErrorMessages.CANNOT_REDEFINE_BUILT_IN.toReadableString(signature),
					term.getSourceInfo());
		}

		// check for special directives, and refuse those.
		String name = signature.substring(0, signature.indexOf('/'));
		if (PrologOperators.goalProtected(name)) {
			throw new ParserException(
					ParserErrorMessages.PROTECTED_PREDICATE.toReadableString(head.toString(), term.toString()),
					term.getSourceInfo());
		}

		// try to convert, it will throw if it fails.
		toGoal(body, term.getSourceInfo());
		return new PrologDBFormula(term.getTerm(), term.getSourceInfo());
	}

	/**
	 * Checks that term is a well formed Prolog goal.
	 * <p>
	 * ISO requires rebuild of the term but in our case we do not allow
	 * variables and hence a real rebuild is not necessary. Instead, we simply
	 * return the original term after checking.
	 * </p>
	 *
	 * @return the term "rewritten" as a Prolog goal according to ISO.
	 * @throws ParserException
	 *             If t is not a well formed Prolog goal.
	 */
	public static alice.tuprolog.Term toGoal(alice.tuprolog.Term t, SourceInfo source) throws ParserException {
		// 7.6.2.a use article 7.8.3
		if (t instanceof alice.tuprolog.Var) {
			throw new ParserException(ParserErrorMessages.VARIABLES_NOT_AS_GOAL.toReadableString(JPLUtils.toString(t)),
					source);
		}
		// footnote of 7.6.2. If T is a number then there is no goal which
		// corresponds to T.
		if (t instanceof alice.tuprolog.Number) {
			throw new ParserException(ParserErrorMessages.NUMBER_NOT_AS_GOAL.toReadableString(JPLUtils.toString(t)),
					source);
		}
		// 7.6.2.b
		if (t instanceof alice.tuprolog.Struct) {
			String sig = JPLUtils.getSignature(t);
			if (PrologOperators.goalProtected(((alice.tuprolog.Struct) t).getName())) {
				throw new ParserException(
						ParserErrorMessages.PREDICATE_NOT_SUPPORTED.toReadableString(JPLUtils.toString(t)), source);
			} else if (sig.equals(":-/2")) {
				throw new ParserException(ParserErrorMessages.CLAUSE_NOT_AS_GOAL.toReadableString(JPLUtils.toString(t)),
						source);
			} else if (sig.equals(",/2") || sig.equals(";/2") || sig.equals("->/2")) {
				toGoal(((alice.tuprolog.Struct) t).getArg(0), source);
				toGoal(((alice.tuprolog.Struct) t).getArg(1), source);
			}
		}
		// 7.6.2.c
		// no action required.
		return t;
	}

	/**
	 * <p>
	 * Checks that conjunction is a Prolog query and returns {@link PrologQuery}
	 * object made of it. This means that, if successful, the result can be
	 * queried to the (TU) prolog engine.
	 * </p>
	 * <p>
	 * ISO section 7.6.2 on p.27 specifies how to convert a term to a goal.
	 * </p>
	 *
	 * @param conjunction
	 *            is a PrologTerm containing a conjunction
	 * @see PrologTerm#getConjuncts
	 * @see PrologTerm#useNotAllowed useNotAllowed
	 * @returns Query object made from conjunction
	 * @throws ParserException
	 *             if prologTerm is not a good Query.
	 */
	public static PrologQuery toQuery(PrologTerm conjunction) throws ParserException {
		return new PrologQuery(toGoal(conjunction.getTerm(), conjunction.getSourceInfo()), conjunction.getSourceInfo());
	}
}