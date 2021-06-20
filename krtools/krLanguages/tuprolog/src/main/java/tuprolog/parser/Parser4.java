package tuprolog.parser;

import java.io.IOException;
import java.io.Reader;
import java.util.BitSet;
import java.util.SortedSet;
import java.util.TreeSet;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.atn.ParserATNSimulator;
import org.antlr.v4.runtime.dfa.DFA;

import krTools.exceptions.ParserException;
import krTools.parser.SourceInfo;
import tuprolog.errors.ParserErrorMessages;
import tuprolog.parser.Prolog4Parser.ListtermContext;
import tuprolog.parser.Prolog4Parser.PossiblyEmptyConjunctContext;
import tuprolog.parser.Prolog4Parser.PossiblyEmptyDisjunctContext;
import tuprolog.parser.Prolog4Parser.PrologtextContext;
import tuprolog.parser.Prolog4Parser.Term0Context;
import tuprolog.parser.Prolog4Parser.Term1000Context;

/**
 * {@link Prolog4Parser} but stores all errors coming from {@link ANTLR} so that
 * we can later report them. All (unchecked) {@link RecognitionException}s are
 * shielded (actually, never thrown directly from the {@link Prolog4Parser}) and
 * changed to {@link ParserException}s.Also it re-throws if there was an error
 * after parsing. This is needed because {@link Prolog4Parser} does not throw
 * when errors occur, instead it "recovers" and never reports us so. <br>
 * This parser therefore checks the results and re-throws the first exception so
 * that we can handle problems with the normal throw/catch mechanisms higher up.
 */
public class Parser4 implements ANTLRErrorListener {
	private final Prolog4Parser parser;
	private final SortedSet<ParserException> errors = new TreeSet<ParserException>();
	private final SourceInfo sourceInfo;
	private final ANTLRInputStream stream;
	private final Lexer lexer;

	/**
	 * Constructor. Adjusts the tokeniser input stream position matching the
	 * given start position
	 *
	 * @param reader
	 *            the input text stream to use for parsing.
	 * @param info
	 *            The start position (line number, column etc) for this parse.
	 *            Used if the text received really is part of a bigger file. If
	 *            set to null, we use a default info object starting at line 1
	 *            with a file reference set to null.
	 * @throws IOException
	 */
	public Parser4(Reader reader, SourceInfo info) throws IOException {
		if (info == null) {
			this.sourceInfo = new SourceInfoObject(null, 1, 1, 0, 0);
		} else {
			this.sourceInfo = info;
		}
		this.stream = new ANTLRInputStream(reader);
		this.stream.name = (this.sourceInfo.getSource() == null) ? "" : this.sourceInfo.getSource().getPath();

		this.lexer = new Lexer4(this.stream, this);
		this.lexer.setLine(this.sourceInfo.getLineNumber() + 1);
		this.lexer.setCharPositionInLine(this.sourceInfo.getCharacterPosition() + 1);

		CommonTokenStream tokens = new CommonTokenStream(this.lexer);
		this.parser = new Prolog4Parser(tokens);
		this.parser.setErrorHandler(new ErrorStrategy4());
		this.parser.removeErrorListeners();
		this.parser.addErrorListener(this);
		this.parser.setBuildParseTree(true);
	}

	/**
	 * @return the initial source info field of this parser.
	 *
	 */
	public SourceInfo getSourceInfo() {
		return this.sourceInfo;
	}

	/**
	 * Check if we processed the whole stream we were given
	 */
	private void checkEndOfInputReached() {
		if (this.stream.index() < this.stream.size()) {
			final SourceInfoObject info = new SourceInfoObject(this.sourceInfo.getSource(), this.lexer.getLine(),
					this.lexer.getCharPositionInLine(), this.sourceInfo.getStartIndex() + this.stream.index() + 1,
					this.sourceInfo.getStartIndex() + this.stream.size() + 1);
			this.errors.add(new ParserException("Unrecognized spurious input", info));
		}
	}

	/**
	 * Get the errors that occurred during parsing.
	 *
	 * @return error set.
	 */
	public SortedSet<ParserException> getErrors() {
		return this.errors;
	}

	/**
	 * Check if parse was a success. To be called after parsing.
	 *
	 *
	 * @return true if parsing was a success = no errors.
	 */
	public boolean isSuccess() {
		return this.errors.isEmpty();
	}

	public ParserATNSimulator getInterpreter() {
		return this.parser.getInterpreter();
	}

	/**
	 * Renders string from a parse tree result.
	 *
	 * @param tree
	 *            {@link ParserRuleContext} to render
	 * @return String representation of tree.
	 */
	public String toStringTree(ParserRuleContext tree) {
		return tree.toStringTree(this.parser);
	}

	/*************** Implements {@link ANTLRErrorListener} *******************/
	@Override
	public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
			String msg, RecognitionException e) {
		int start = recognizer.getInputStream().index();
		int stop = start;
		if (offendingSymbol != null) {
			CommonToken token = (CommonToken) offendingSymbol;
			start = token.getStartIndex();
			stop = token.getStopIndex();
		}
		SourceInfoObject pos = new SourceInfoObject(this.sourceInfo.getSource(), line, charPositionInLine,
				this.sourceInfo.getStartIndex() + start + 1, this.sourceInfo.getStartIndex() + stop + 1);
		if (recognizer instanceof Lexer) {
			handleLexerError(recognizer, offendingSymbol, pos, msg, e);
		} else {
			handleParserError(recognizer, offendingSymbol, pos, msg, e);
		}
	}

	/**
	 * Adds new error for token recognition problem (lexer).
	 *
	 * @param pos
	 *            input stream position
	 * @param stop
	 *            stopIndex of last recognition error
	 * @param text
	 *            character(s) that could not be recognized
	 */
	public void handleLexerError(Recognizer<?, ?> recognizer, Object offendingSymbol, SourceInfoObject pos, String text,
			RecognitionException e) {
		text = text.replace("\\r", "").replace("\\n", " ").replace("\\t", " ").replace("\\f", "");
		this.errors.add(new ParserException(ParserErrorMessages.CANNOT_BE_USED.toReadableString(text), pos));
	}

	/**
	 * Adds error for parsing problem.
	 *
	 * <p>
	 * Simply pushes parser error msg forward. See {@link #MASErrorStrateg} for
	 * handling of parsing errors.
	 * </p>
	 *
	 * @param pos
	 *            input stream position
	 * @param msg
	 *            reported parser error msg
	 */
	public void handleParserError(Recognizer<?, ?> recognizer, Object offendingSymbol, SourceInfoObject pos,
			String expectedtokens, RecognitionException e) {
		// We need the strategy to get access to our customized token displays
		ErrorStrategy4 strategy = (ErrorStrategy4) ((Parser) recognizer).getErrorHandler();
		// Report the various types of syntax errors
		String offendingTokenText = strategy.getTokenErrorDisplay((Token) offendingSymbol);
		// TODO: copies and hardcodes derived from LanguageTools->Validator
		if (e.getMessage().equals("NoViableAlternative")) {
			this.errors.add(new ParserException(
					ParserErrorMessages.FOUND_BUT_NEED.toReadableString(offendingTokenText, expectedtokens), pos));
		} else if (e.getMessage().equals("InputMismatch")) {
			this.errors.add(new ParserException(
					ParserErrorMessages.FOUND_BUT_NEED.toReadableString(offendingTokenText, expectedtokens), pos));
		} else if (e.getMessage().equals("FailedPredicate")) {
			this.errors.add(new ParserException(ParserErrorMessages.FAILED_PREDICATE.toReadableString(), pos));
		} else if (e.getMessage().equals("UnwantedToken")) {
			this.errors
					.add(new ParserException(ParserErrorMessages.TOKEN_BAD.toReadableString(offendingTokenText), pos));
		} else if (e.getMessage().equals("MissingToken")) {
			this.errors
					.add(new ParserException(ParserErrorMessages.TOKEN_MISSING.toReadableString(expectedtokens), pos));
		} else {
			this.errors.add(new ParserException(
					ParserErrorMessages.EXPECTED_TEXT.toReadableString(offendingTokenText, expectedtokens), pos));
		}
	}

	@Override
	public void reportAmbiguity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, boolean exact,
			BitSet ambigAlts, ATNConfigSet configs) {
	}

	@Override
	public void reportAttemptingFullContext(Parser recognizer, DFA dfa, int startIndex, int stopIndex,
			BitSet conflictingAlts, ATNConfigSet configs) {
	}

	@Override
	public void reportContextSensitivity(Parser recognizer, DFA dfa, int startIndex, int stopIndex, int prediction,
			ATNConfigSet configs) {
	}

	/************** Actual public Parser functionality *******************/
	public Term0Context term0() {
		Term0Context t = this.parser.term0();
		checkEndOfInputReached();
		return t;
	}

	public PossiblyEmptyConjunctContext possiblyEmptyConjunct() {
		PossiblyEmptyConjunctContext t = this.parser.possiblyEmptyConjunct();
		checkEndOfInputReached();
		return t;
	}

	public PrologtextContext prologtext() {
		PrologtextContext t = this.parser.prologtext();
		checkEndOfInputReached();
		return t;
	}

	public PossiblyEmptyDisjunctContext possiblyEmptyDisjunct() {
		PossiblyEmptyDisjunctContext t = this.parser.possiblyEmptyDisjunct();
		checkEndOfInputReached();
		return t;
	}

	public Term1000Context term1000() {
		Term1000Context t = this.parser.term1000();
		checkEndOfInputReached();
		return t;
	}

	public ListtermContext listterm() {
		ListtermContext t = this.parser.listterm();
		checkEndOfInputReached();
		return t;
	}
}