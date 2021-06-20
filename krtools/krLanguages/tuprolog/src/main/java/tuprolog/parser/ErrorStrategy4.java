package tuprolog.parser;

import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.FailedPredicateException;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.IntervalSet;

import tuprolog.errors.ParserErrorMessages;

public class ErrorStrategy4 extends DefaultErrorStrategy {
	@Override
	public void reportNoViableAlternative(Parser parser, NoViableAltException e) throws RecognitionException {
		parser.notifyErrorListeners(e.getOffendingToken(), getExpectationTxt((Parser) e.getRecognizer()),
				getException("NoViableAlternative", parser));
	}

	@Override
	public void reportInputMismatch(Parser parser, InputMismatchException e) {
		parser.notifyErrorListeners(e.getOffendingToken(), getExpectationTxt((Parser) e.getRecognizer()),
				getException("InputMismatch", parser));
	}

	@Override
	public void reportFailedPredicate(Parser parser, FailedPredicateException e) {
		parser.notifyErrorListeners(e.getOffendingToken(), getExpectationTxt((Parser) e.getRecognizer()),
				getException("FailedPredicate", parser));
	}

	/**
	 * This method is called to report a syntax error which requires the removal
	 * of a token from the input stream. At the time this method is called, the
	 * erroneous symbol is current {@code LT(1)} symbol and has not yet been
	 * removed from the input stream. When this method returns, {@code parser}
	 * is in error recovery mode.
	 *
	 * <p>
	 * This method is called when {@link #singleTokenDeletion} identifies
	 * single-token deletion as a viable recovery strategy for a mismatched
	 * input error.
	 * </p>
	 *
	 * <p>
	 * Like the default implementation this method simply returns if the handler
	 * is already in error recovery mode. Otherwise, it calls
	 * {@link #beginErrorCondition} to enter error recovery mode, followed by
	 * calling {@link Parser#notifyErrorListeners}.
	 * </p>
	 *
	 * <p>
	 * The method has been modified to report more readable error messages.
	 * </p>
	 *
	 * @param parser
	 *            the parser instance
	 */
	@Override
	public void reportUnwantedToken(Parser parser) {
		if (!inErrorRecoveryMode(parser)) {
			beginErrorCondition(parser);
			Token t = parser.getCurrentToken();
			parser.notifyErrorListeners(t, getExpectationTxt(parser), getException("UnwantedToken", parser));
		}
	}

	/**
	 * This method is called to report a syntax error which requires the
	 * insertion of a missing token into the input stream. At the time this
	 * method is called, the missing token has not yet been inserted. When this
	 * method returns, {@code recognizer} is in error recovery mode.
	 *
	 * <p>
	 * This method is called when {@link #singleTokenInsertion} identifies
	 * single-token insertion as a viable recovery strategy for a mismatched
	 * input error.
	 * </p>
	 *
	 * <p>
	 * The default implementation simply returns if the handler is already in
	 * error recovery mode. Otherwise, it calls {@link #beginErrorCondition} to
	 * enter error recovery mode, followed by calling
	 * {@link Parser#notifyErrorListeners}.
	 * </p>
	 *
	 * @param recognizer
	 *            the parser instance
	 */
	@Override
	public void reportMissingToken(Parser parser) {
		if (!inErrorRecoveryMode(parser)) {
			beginErrorCondition(parser);
			Token t = parser.getCurrentToken();
			parser.notifyErrorListeners(t, getExpectationTxt(parser), getException("MissingToken", parser));
		}
	}

	/**
	 * Used to control display of token in an error message.
	 *
	 * During development it may be useful to use t.toString() (which, for
	 * CommonToken, dumps everything about the token).
	 */
	@Override
	public String getTokenErrorDisplay(Token t) {
		if (t == null) {
			return "????";
		}

		// Default is to use token name from list in parser
		String s = getSymbolText(t);
		if (s == null) {
			s = "<" + getSymbolType(t) + ">";
		} else {
			s = escapeWSAndQuote(s.toLowerCase());
		}

		// Handle specific cases to produce more readable output
		String prettyprint = prettyPrintToken(t);
		return (prettyprint != null) ? prettyprint : s;
	}

	/**
	 * Helper method for reporting multiple expected alternatives
	 *
	 * @param tokens
	 *            Set of expected tokens
	 * @return String representation of token set
	 */
	private String getExpectationTxt(Parser parser) {
		IntervalSet tokens = getExpectedTokens(parser);
		if (tokens.size() < 5) { // list all expected tokens if less than 5
			int size = tokens.toList().size();
			String str = (size > 1 ? "either " : "");
			for (int i = 0; i < size; i++) {
				int type = tokens.toList().get(i);
				str += prettyPrintToken(type);
				str += (i < size - 2 ? ", " : "");
				str += (i == size - 2 ? " or " : "");
			}
			return str;
		} else { // otherwise output parser rule context
			return prettyPrintRuleContext(parser.getRuleContext().getRuleIndex());
		}
	}

	/**
	 * We use a general RecognitionException with a particular text to signal to
	 * the error strategy what type of issue we found.
	 *
	 * @param text
	 *            Label to indicate error type
	 * @param parser
	 * @return The recognition exception
	 */
	private RecognitionException getException(String text, Parser parser) {
		return new RecognitionException(text, parser, parser.getInputStream(), parser.getRuleContext());
	}

	protected String prettyPrintToken(Token t) {
		String txt = prettyPrintToken(getSymbolType(t));
		switch (t.getType()) {
		case Prolog4Parser.VARIABLE:
			return txt + " '" + t.getText() + "'";
		case Prolog4Parser.NAME:
			return txt + " '" + t.getText() + "'";
		case Prolog4Parser.NUMBER:
			return txt + " '" + t.getText() + "'";
		case Prolog4Parser.STRING:
			return txt + " " + t.getText();
		default:
			return txt;
		}
	}

	protected String prettyPrintToken(int type) {
		switch (type) {
		case Token.EOF:
			return "end of file/section";
		case Prolog4Parser.ENDTOKEN:
			return "'.'";
		case Prolog4Parser.NAME:
			return "an atom";
		case Prolog4Parser.NUMBER:
			return "a number";
		case Prolog4Parser.VARIABLE:
			return "a variable";
		case Prolog4Parser.STRING:
			return "a string";
		default:
			// Do not improve, simply return token symbol as is
			return Prolog4Parser.VOCABULARY.getDisplayName(type);
		}
	}

	/**
	 * pretty print a name of a parser rule. We convert often to general
	 * "an operator" because ANTLR produces too specific errors in many cases,
	 * which misleads the user. #3511
	 *
	 * @param ruleIndex
	 * @return
	 */
	public String prettyPrintRuleContext(int ruleIndex) {
		switch (ruleIndex) {
		case Prolog4Parser.RULE_prologfile:
			return ParserErrorMessages.CLAUSES.toReadableString();
		case Prolog4Parser.RULE_prologtext:
			return ParserErrorMessages.CLAUSES.toReadableString();
		case Prolog4Parser.RULE_directiveorclause:
			return ParserErrorMessages.CLAUSE.toReadableString();
		case Prolog4Parser.RULE_directive:
			return ParserErrorMessages.DIRECTIVE.toReadableString();
		case Prolog4Parser.RULE_clause:
			return ParserErrorMessages.CLAUSE.toReadableString();
		case Prolog4Parser.RULE_arglist:
			return ParserErrorMessages.EXPRESSIONS.toReadableString();
		case Prolog4Parser.RULE_possiblyEmptyConjunct:
			return ParserErrorMessages.TERMS.toReadableString();
		case Prolog4Parser.RULE_possiblyEmptyDisjunct:
			return ParserErrorMessages.DISJUNCT_OF_TERMS.toReadableString();
		case Prolog4Parser.RULE_expression:
			return ParserErrorMessages.EXPRESSION.toReadableString();
		case Prolog4Parser.RULE_listterm:
			return ParserErrorMessages.LIST.toReadableString();
		case Prolog4Parser.RULE_items:
			return ParserErrorMessages.EXPRESSIONS.toReadableString();
		case Prolog4Parser.RULE_prefixoperator:
			return ParserErrorMessages.PREFIXOP.toReadableString();
		case Prolog4Parser.RULE_prefixop:
			return ParserErrorMessages.PREFIX_OPERATOR.toReadableString();
		case Prolog4Parser.RULE_term0:
			return ParserErrorMessages.TERM0.toReadableString();
		case Prolog4Parser.RULE_term50:
			return ParserErrorMessages.TERM50.toReadableString();
		case Prolog4Parser.RULE_term100:
			return ParserErrorMessages.TERM100.toReadableString();
		case Prolog4Parser.RULE_term200:
			return ParserErrorMessages.TERM200.toReadableString();
		case Prolog4Parser.RULE_term400:
			return ParserErrorMessages.TERM400.toReadableString();
		case Prolog4Parser.RULE_term400b:
			return ParserErrorMessages.TERM400B.toReadableString();
		case Prolog4Parser.RULE_term500:
			return ParserErrorMessages.TERM500.toReadableString();
		case Prolog4Parser.RULE_term500b:
			return ParserErrorMessages.TERM500B.toReadableString();
		case Prolog4Parser.RULE_term700:
			return ParserErrorMessages.TERM700.toReadableString();
		case Prolog4Parser.RULE_term900:
			return ParserErrorMessages.TERM900.toReadableString();
		case Prolog4Parser.RULE_term1000:
			return ParserErrorMessages.TERM1000.toReadableString();
		case Prolog4Parser.RULE_term1050:
			return ParserErrorMessages.TERM1050.toReadableString();
		case Prolog4Parser.RULE_term1100:
			return ParserErrorMessages.TERM1100.toReadableString();
		case Prolog4Parser.RULE_term1105:
			return ParserErrorMessages.TERM1105.toReadableString();
		case Prolog4Parser.RULE_term1200:
			return ParserErrorMessages.TERM1200.toReadableString();
		default:
			// getting here would be a bug
			throw new IllegalArgumentException("unknown parser rule index '" + ruleIndex + "'.");
		}
	}
}
