package tuprolog.errors;

import java.util.MissingFormatArgumentException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public enum ParserErrorMessages {
	/** ?? */
	VAR_EXPECTED,
	/**
	 * number is NAN
	 */
	NUMBER_NAN,
	/**
	 * Number maps to infinity
	 */
	NUMBER_INFINITY,
	/**
	 * Converting number to double
	 */
	NUMBER_TOO_LARGE_CONVERTING,
	/**
	 * Number failed to parse
	 */
	NUMBER_NOT_PARSED,
	/**
	 * Found X but need Y
	 */
	FOUND_BUT_NEED,
	/**
	 * Failed predicate
	 */
	FAILED_PREDICATE,
	/**
	 * misplaced/bad token
	 */
	TOKEN_BAD,
	/**
	 * Missing token
	 */
	TOKEN_MISSING,
	/**
	 * Found X but expected Y.
	 */
	EXPECTED_TEXT,
	/**
	 * a prefix op
	 */
	PREFIX_OPERATOR,
	/**
	 * An (other) operator
	 */
	AN_OTHER_OPERATOR,
	/**
	 * .. cannot be used here
	 */
	CANNOT_BE_USED,
	/**
	 * descriptions for all the parser terms
	 */
	TERM0, TERM50, TERM100, TERM200, TERM400, TERM400B, TERM500, TERM500B, TERM700, TERM900, TERM1000, TERM1050, TERM1100, TERM1105, TERM1200, EXPRESSION, EXPRESSIONS, DISJUNCT_OF_EXPRESSIONS, LIST, CLAUSE, CLAUSES, DIRECTIVE, PREFIXOP, TERMS, DISJUNCT_OF_TERMS,

	/**
	 * Expected var but found something else
	 */
	EXPECTED_VAR,
	/**
	 * Head can not be a var
	 */
	HEAD_CANT_BE_VAR,
	/**
	 * Head must be a clause
	 */
	HEAD_MUST_BE_CLAUSE,
	/**
	 * Can not redefine built-in predicate
	 */
	CANNOT_REDEFINE_BUILT_IN,
	/**
	 * Predicate is protected
	 */
	PROTECTED_PREDICATE,
	/**
	 * Variables cant be used as goal
	 */
	VARIABLES_NOT_AS_GOAL,
	/**
	 * numbers cant be used as goal
	 */
	NUMBER_NOT_AS_GOAL,
	/**
	 * use of predicate not supoorted
	 */
	PREDICATE_NOT_SUPPORTED,
	/**
	 * clauses cant be used as goal
	 */
	CLAUSE_NOT_AS_GOAL

	;

	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("tuprolog.errors.ParserErrorMessages");

	public String toReadableString(String... args) {
		try {
			return String.format(BUNDLE.getString(name()), (Object[]) args);
		} catch (MissingResourceException e1) {
			if (args.length > 0) {
				return args[0];
			} else {
				return name();
			}
		} catch (MissingFormatArgumentException e2) {
			return BUNDLE.getString(name());
		}
	}

}