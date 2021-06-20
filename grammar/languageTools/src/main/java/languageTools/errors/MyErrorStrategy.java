package languageTools.errors;

import org.antlr.v4.runtime.DefaultErrorStrategy;
import org.antlr.v4.runtime.FailedPredicateException;
import org.antlr.v4.runtime.InputMismatchException;
import org.antlr.v4.runtime.NoViableAltException;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.IntervalSet;

import languageTools.errors.ParserError.SyntaxError;

public abstract class MyErrorStrategy extends DefaultErrorStrategy {

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
		if (inErrorRecoveryMode(parser)) {
			return;
		}

		beginErrorCondition(parser);

		Token t = parser.getCurrentToken();
		parser.notifyErrorListeners(t, getExpectationTxt(parser), getException("UnwantedToken", parser));
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
	 * @param parser
	 *            The parser instance.
	 */
	@Override
	public void reportMissingToken(Parser parser) {
		if (inErrorRecoveryMode(parser)) {
			return;
		}

		beginErrorCondition(parser);

		Token t = parser.getCurrentToken();
		parser.notifyErrorListeners(t, getExpectationTxt(parser), getException("MissingToken", parser));
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
		s = (prettyprint != null ? prettyprint : s);

		return s;
	}

	/**
	 * This is the default implementation which simply prints the token symbol
	 * as it occurs in the (lexer) grammar.
	 *
	 * <p>
	 * Overwrite this method to improve standard output.
	 * </p>
	 *
	 * @param t
	 *            Token to print
	 * @return Token symbol text as it occurs in (lexer) grammar
	 */
	public String prettyPrintToken(Token t) {
		return prettyPrintToken(getSymbolType(t));
	}

	/**
	 * This is the default implementation which simply returns the token symbol
	 * type.
	 *
	 * <p>
	 * Overwrite this method to improve standard output.
	 * </p>
	 *
	 * @param type
	 *            Type of token to print
	 * @return Token symbol text as it occurs in (lexer) grammar
	 */
	public String prettyPrintToken(int type) {
		return Integer.toString(type);
	}

	/**
	 * This is the default implementation which simply prints the rule name as
	 * it occurs in the (parser) grammar.
	 *
	 * <p>
	 * Overwrite this method to improve standard output.
	 * </p>
	 *
	 * @param ruleIndex
	 *            Index of parser rule
	 * @return Rule name text as it occurs in (parser) grammar
	 */
	public String prettyPrintRuleContext(int ruleIndex) {
		return Integer.toString(ruleIndex);
	}

	/**
	 * Need to delegate this to classes that extend this class because
	 * establishing error type for lexer is token-based...
	 *
	 * @param token
	 * @return Syntax error.
	 */
	public abstract SyntaxError getLexerErrorType(Token token);

	/**
	 * Helper method for reporting multiple expected alternatives
	 *
	 * @param tokens
	 *            Set of expected tokens
	 * @return String representation of token set
	 */
	private String getExpectationTxt(Parser parser) {
		String str;

		IntervalSet tokens = getExpectedTokens(parser);
		if (tokens.size() < 7) { // list all expected tokens if less than 7
			int size = tokens.toList().size();
			str = (size > 1 ? "either " : "");

			for (int i = 0; i < size; i++) {
				int type = tokens.toList().get(i);
				str += prettyPrintToken(type);
				str += (i < size - 2 ? ", " : "");
				str += (i == size - 2 ? " or " : "");
			}
		} else { // otherwise output parser rule context
			str = prettyPrintRuleContext(parser.getRuleContext().getRuleIndex());
		}

		return str;
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

}
