/**
 * The GOAL Grammar Tools. Copyright (C) 2014 Koen Hindriks.
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

package languageTools.analyzer;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.file.InvalidPathException;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.atn.ATNConfigSet;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.dfa.DFA;
import org.antlr.v4.runtime.misc.ParseCancellationException;
import org.antlr.v4.runtime.tree.ErrorNode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.lang3.StringUtils;

import cognitiveKr.CognitiveKR;
import cognitiveKrFactory.CognitiveKRFactory;
import cognitiveKrFactory.InstantiationFailedException;
import krFactory.KRFactory;
import krTools.exceptions.ParserException;
import krTools.language.Term;
import krTools.language.Var;
import krTools.parser.SourceInfo;
import languageTools.errors.MyErrorStrategy;
import languageTools.errors.ParserError;
import languageTools.errors.ParserError.SyntaxError;
import languageTools.errors.ValidatorError;
import languageTools.errors.ValidatorError.ValidatorErrorType;
import languageTools.errors.ValidatorWarning;
import languageTools.errors.ValidatorWarning.ValidatorWarningType;
import languageTools.errors.mas.MASErrorStrategy;
import languageTools.parser.InputStreamPosition;
import languageTools.program.Program;

/**
 * A validator parses and validates a program file, i.e., checks for both
 * syntactic and semantic issues. During the validation process a
 * {@link Program} is created that can be obtained by {@link #getProgram()}
 * after calling {@link #validate()}.
 *
 * {@link #validate()} may generate {@link ParserError}s if the parser detected
 * problems and {@link ValidatorError}s if the program specific validator
 * detected problems. It may also generate {@link ValidatorWarning}s. Upon
 * completion, {@link #validate()} sets a flag in the program which indicates
 * whether the program is valid or not.
 */
public abstract class Validator<L extends Lexer, P extends Parser, E extends MyErrorStrategy, Q extends Program>
		implements ANTLRErrorListener {
	/**
	 * Name of the file that is validated.
	 */
	private final String filename;
	/**
	 * Used in i.e. Eclipse: the content of this string are fed into the
	 * lexer/parser instead of the actual file's contents
	 */
	private String override;
	protected final File source;
	protected final FileRegistry registry;
	private Program program;
	protected ValidatorSecondPass secondPass;
	/**
	 * Lexer generated tokens.
	 */
	private CommonTokenStream tokens;

	/**
	 * Creates a validator.
	 *
	 * @param filename The name of the file to be validated.
	 */
	public Validator(String filename, FileRegistry registry) {
		this.filename = filename;
		this.source = new File(filename);
		this.registry = registry;
	}

	public void override(String content) {
		this.override = content;
	}

	protected abstract L getNewLexer(CharStream stream);

	protected abstract P getNewParser(TokenStream stream);

	/**
	 * Starts parser at a specific grammar rule.
	 *
	 * @return parse tree
	 */
	protected abstract ParseTree startParser();

	/**
	 * Gets the error strategy.
	 *
	 * A validator should need only one instance of this.
	 *
	 * @return The error strategy used by this {@link #Validator(String)}.
	 */
	protected abstract MyErrorStrategy getTheErrorStrategy();

	protected abstract Q getNewProgram(File file) throws IOException;

	/**
	 * @return Name of the file that is validated.
	 */
	public String getFilename() {
		return this.filename;
	}

	/**
	 * @return The program that was constructed during validation.
	 */
	public Program getProgram() {
		return this.program;
	}

	public void overrideProgram(Program program) {
		this.program = program;
	}

	public FileRegistry getRegistry() {
		return this.registry;
	}

	public CognitiveKR getCognitiveKR() throws ParserException {
		if (this.program == null || this.program.getKRInterface() == null) {
			throw new ParserException("cannot get cognitive KR for a null program or KRI.", new File(this.filename));
		} else {
			try {
				return CognitiveKRFactory.getCognitiveKR(this.program.getKRInterface());
			} catch (InstantiationFailedException e) {
				throw new ParserException("could not instantiate cognitive KR.", this.program.getSourceInfo(), e);
			}
		}
	}

	/**
	 * @return The source file validated by this validator.
	 */
	public File getSource() {
		return this.source;
	}

	/**
	 * Parses the file to be validated.
	 */
	/**
	 * Parses the file.
	 *
	 * @return The ANTLR parser for the file.
	 * @throws IOException If the file does not exist.
	 */
	private ParseTree parseFile() throws IOException {
		CharStream stream;
		if (this.override == null) {
			try {
				stream = CharStreams.fromFileName(getFilename());
			} catch (InvalidPathException e) {
				throw new IOException(e);
			}
		} else {
			stream = CharStreams.fromString(this.override, getFilename());
		}

		// Create a lexer that feeds off of input CharStream (also redirects
		// error listener).
		L lexer = getNewLexer(stream);
		// Redirect error output
		lexer.removeErrorListeners();
		lexer.addErrorListener(this);

		// Create a buffer of tokens pulled from the lexer.
		this.tokens = new CommonTokenStream(lexer);

		// Create a parser that feeds off the tokens buffer.
		P parser = getNewParser(this.tokens);
		try {
			// First try with simpler/faster SLL(*)
			parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
			// We don't want error messages or recovery during first try
			parser.removeErrorListeners();
			parser.setErrorHandler(new BailErrorStrategy());
			return startParser();
			// If we get here, there was no syntax error and SLL(*) was enough;
			// there is no need to try full LL(*)
		} catch (ParseCancellationException e) {
			// First rewind the input stream
			this.tokens.seek(0);
			// Use full (custom) error reporting now
			parser.setErrorHandler(getTheErrorStrategy());
			parser.addErrorListener(this);
			// Now try full LL(*)
			parser.getInterpreter().setPredictionMode(PredictionMode.LL);
			return startParser();
		}
	}

	public void validate() {
		validate(true);
	}

	/**
	 * Builds a symbol table and validates file.
	 *
	 * Each time you call this a new lexer, parser and program are created.
	 */
	public void validate(boolean secondPass) {
		// Prepare by parsing the file.
		ParseTree tree = null;
		try {
			tree = parseFile();
		} catch (IOException e) {
			reportError(SyntaxError.FILE_COULDNOT_OPEN, null, getFilename());
			return;
		}
		try {
			File file = new File(getFilename());
			this.program = getNewProgram(file);
			if (this.registry.needsProcessing(file)) {
				// Build and validate program.
				visit(tree);
				// Before second pass, register result so far to avoid duplicate
				// processing. This is also sufficient for taking care of
				// self-referencing module files.
				this.registry.register(this.source, this.program);
			} else {
				this.program = this.registry.getProgram(file);
			}

			// Only do a second pass if asked so, and if first pass did not give
			// syntax errors so we at least parsed ok
			if (secondPass && !this.registry.hasSyntaxError()) {
				ValidatorSecondPass secondpass = getSecondPass();
				if (secondpass != null) {
					secondpass.validate();
				}
			}
		} catch (Exception e) {
			// Convert stack trace to string
			StringWriter sw = new StringWriter();
			e.printStackTrace(new PrintWriter(sw));
			reportError(SyntaxError.FATAL, this.program.getSourceInfo(), e.getMessage() + "\n" + sw.toString());
		}
	}

	public ValidatorSecondPass getSecondPass() {
		if (this.secondPass == null) {
			this.secondPass = createSecondPass();
		}
		return this.secondPass;
	}

	/**
	 * Second pass validator (can be null).
	 */
	protected abstract ValidatorSecondPass createSecondPass();

	/**
	 * Dumps all tokens to console.
	 */
	public void printLexerTokens() {
		for (Token token : this.tokens.getTokens()) {
			System.out.print("'" + token.getText() + "<" + token.getType() + ">' ");
		}
	}

	/**
	 * Report syntax error.
	 *
	 * Collects details about the exact position in the input stream from an ANTLR
	 * ParserRuleContext object.
	 *
	 * @param type The type of syntax error that is added.
	 * @param info Source info object.
	 * @param args Additional info to be inserted into warning message.
	 */
	public boolean reportError(SyntaxError type, SourceInfo info, String... args) {
		// FIXME: Make SourceInfo in KRTools abstract class and move code from
		// InputStreamPosition to SourceInfo.
		// Map onto input stream position for pretty printing of code location.
		SourceInfo isp = null;
		if (info != null) {
			isp = new InputStreamPosition(info.getLineNumber(), info.getCharacterPosition(), info.getStartIndex(),
					info.getStopIndex(), info.getSource());
		}

		return this.registry.addSyntaxError(new ParserError(type, isp, args));
	}

	/**
	 * Report (validation) error.
	 *
	 * @param type The semantic (validation) error that is added.
	 * @param info A source info object.
	 * @param args Additional info to be inserted into warning message.
	 */
	public boolean reportError(ValidatorErrorType type, SourceInfo info, String... args) {
		return this.registry.addError(new ValidatorError(type, info, args));
	}

	/**
	 * Report (validation) error.
	 *
	 * Collects details about the exact position in the input stream from an ANTLR
	 * ParserRuleContext object.
	 *
	 * @param type    The semantic (validation) error that is added.
	 * @param context The ANTLR ParserRuleContext object.
	 * @param args    Additional info to be inserted into warning message.
	 */
	public boolean reportError(ValidatorErrorType type, ParserRuleContext context, String... args) {
		return this.registry.addError(new ValidatorError(type, getSourceInfo(context), args));
	}

	/**
	 * Report (validation) error.
	 *
	 * Collects details about the exact position in the input stream from an ANTLR
	 * TerminalNode object.
	 *
	 * @param type The semantic (validation) error that is added.
	 * @param node An ANTLR TerminalNode object.
	 * @param args Additional info to be inserted into warning message.
	 */
	protected boolean reportError(ValidatorErrorType type, TerminalNode node, String... args) {
		return this.registry.addError(new ValidatorError(type, getSourceInfo(node), args));
	}

	/**
	 * Reports a parsing exception that occurred while parsing embedded language
	 * fragments.
	 *
	 * @param e   The exception generated by the embedded language parser. TODO: the
	 *            ctx is no longer passed, how is this handled now?
	 * @param ctx The context of the parser where the embedded language fragment is
	 *            located.
	 */
	public void reportParsingException(ParserException e) {
		String msg = e.getMessage();
		if (e.getCause() != null && e.getCause().getMessage() != null) {
			msg += " because " + e.getCause().getMessage();
		}

		reportError(SyntaxError.EMBEDDED_LANGUAGE_ERROR, e, KRFactory.getName(this.program.getKRInterface()), msg);
	}

	/**
	 * Reports parsing errors that occurred while parsing embedded language
	 * fragments.
	 *
	 * @param parser          The parser that generated the errors.
	 * @param relativeLineNr  Relative source code line position (start of the
	 *                        embedded fragment in source).
	 * @param relativeCharPos Relative source code character position (start of the
	 *                        embedded fragment in source).
	 */
	protected void reportEmbeddedLanguageErrors(CognitiveKR ckr) {
		for (SourceInfo error : ckr.getErrors()) {
			reportError(SyntaxError.EMBEDDED_LANGUAGE_ERROR, error, KRFactory.getName(this.program.getKRInterface()),
					error.getMessage());
		}
	}

	/**
	 * @param ctx An ANTLR {@link ParserRuleContext}.
	 * @return A source info object ({@link InputStreamPosition}) with information
	 *         extracted from the rule context.
	 */
	public SourceInfo getSourceInfo(ParserRuleContext ctx) {
		try {
			return (ctx == null) ? null
					: new InputStreamPosition(ctx.getStart(), ctx.getStop() == null ? ctx.getStart() : ctx.getStop(),
							this.source.getCanonicalPath());
		} catch (IOException e) {
			return null; // TODO
		}
	}

	/**
	 * @param node An ANTLR {@link TerminalNode}.
	 * @return A source info object ({@link InputStreamPosition}) with information
	 *         extracted from the terminal node.
	 */
	public SourceInfo getSourceInfo(TerminalNode node) {
		try {
			return (node == null) ? null
					: new InputStreamPosition(node.getSymbol(), node.getSymbol(), this.source.getCanonicalPath());
		} catch (IOException e) {
			return null; // TODO
		}
	}

	/**
	 * Report warning.
	 *
	 * Collects details about the exact position in the input stream from an ANTLR
	 * ParserRuleContext object.
	 *
	 * @param type The warning that is added.
	 * @param info A source info object.
	 * @param args Additional info to be inserted into warning message.
	 */
	public boolean reportWarning(ValidatorWarningType type, SourceInfo info, String... args) {
		return this.registry.addWarning(new ValidatorWarning(type, info, args));
	}

	/**
	 * Report warning.
	 *
	 * Collects details about the exact position in the input stream from an ANTLR
	 * ParserRuleContext object.
	 *
	 * @param type    The warning that is added.
	 * @param context The ANTLR ParserRuleContext object.
	 * @param args    Additional info to be inserted into warning message.
	 */
	public boolean reportWarning(ValidatorWarningType type, ParserRuleContext context, String... args) {
		return this.registry.addWarning(new ValidatorWarning(type, getSourceInfo(context), args));
	}

	/**
	 * report a new warning.
	 *
	 * @param warning the ValidatorWarning to report
	 */
	public void reportWarning(ValidatorWarning warning) {
		this.registry.addWarning(warning);
	}

	/**
	 * Report warning.
	 *
	 * Collects details about the exact position in the input stream from an ANTLR
	 * TerminalNode object.
	 *
	 * @param type The warning that is added.
	 * @param node The ANTLR TerminalNode object.
	 * @param args Additional info to be inserted into warning message.
	 */
	protected boolean reportWarning(ValidatorWarningType type, TerminalNode node, String... args) {
		return this.registry.addWarning(new ValidatorWarning(type, getSourceInfo(node), args));
	}

	// -------------------------------------------------------------
	// Syntax error handling (implements ANTLRErrorListener)
	// -------------------------------------------------------------

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
		InputStreamPosition pos;
		try {
			pos = (this.source == null) ? null
					: new InputStreamPosition(line, charPositionInLine, start, stop, this.source.getCanonicalPath());
		} catch (IOException exc) {
			pos = null; // TODO
		}

		if (recognizer instanceof Lexer) { // lexer error
			handleLexerError(recognizer, offendingSymbol, pos, msg, e);
		} else {
			handleParserError(recognizer, offendingSymbol, pos, msg, e);
		}
	}

	/**
	 * Adds new error for token recognition problem (lexer).
	 *
	 * @param pos  input stream position
	 * @param text character(s) that could not be recognized
	 */
	public void handleLexerError(Recognizer<?, ?> recognizer, Object offendingSymbol, InputStreamPosition pos,
			String text, RecognitionException e) {
		// Determine type of syntax error
		SyntaxError type = null;
		if (offendingSymbol instanceof Token) {
			type = getTheErrorStrategy().getLexerErrorType((Token) offendingSymbol);
		} else { // if nothing else, by default, assume token recognition
			// problem
			type = SyntaxError.TOKENRECOGNITIONERROR;
		}

		// switch (type) {
		// case TOKENRECOGNITIONERROR:
		// Check if this and last error were both token recognition errors;
		// if so, merge them
		// if (!this.syntaxErrors.isEmpty() &&
		// this.syntaxErrors.last().getType().equals(type)) {
		// Message error = this.syntaxErrors.last();
		// Use old input stream position, but first get new stop index
		// int stop = pos.getStopIndex();
		// pos = (InputStreamPosition) error.getSource();
		// pos.setStopIndex(stop);
		// Concatenate symbols that were not recognized
		// text = error.getArguments()[0] + text;
		// Remove previous error
		// this.syntaxErrors.remove(error);
		// }
		// break;
		// case UNTERMINATEDSTRINGLITERAL:
		// case UNTERMINATEDSINGLEQUOTEDSTRINGLITERAL:
		// nothing to do
		// break;
		// default:
		// throw new UnsupportedOperationException("unexpected lexer error
		// whilst
		// handling '" + text + "'.", e);
		// }

		text = removeTabsAndNewLines(text);
		this.registry.addSyntaxError(new ParserError(type, pos, text));
	}

	/**
	 * Adds error for parsing problem.
	 *
	 * <p>
	 * Simply pushes parser error msg forward. See {@link MASErrorStrategy} for
	 * handling of parsing errors.
	 * </p>
	 *
	 * @param pos input stream position
	 */
	public void handleParserError(Recognizer<?, ?> recognizer, Object offendingSymbol, InputStreamPosition pos,
			String expectedtokens, RecognitionException e) {
		// We need the strategy to get access to our customized token displays
		MyErrorStrategy strategy = (MyErrorStrategy) ((Parser) recognizer).getErrorHandler();

		// Report the various types of syntax errors
		SyntaxError type = null;
		String offendingTokenText = strategy.getTokenErrorDisplay((Token) offendingSymbol);
		if (e.getMessage().equals("NoViableAlternative")) {
			type = SyntaxError.NOVIABLEALTERNATIVE;
			reportError(type, pos, offendingTokenText, expectedtokens);
		} else if (e.getMessage().equals("InputMismatch")) {
			type = SyntaxError.INPUTMISMATCH;
			reportError(type, pos, offendingTokenText, expectedtokens);
		} else if (e.getMessage().equals("FailedPredicate")) {
			type = SyntaxError.FAILEDPREDICATE;
			reportError(type, pos, offendingTokenText, expectedtokens);
		} else if (e.getMessage().equals("UnwantedToken")) {
			type = SyntaxError.UNWANTEDTOKEN;
			reportError(type, pos, offendingTokenText);
		} else if (e.getMessage().equals("MissingToken")) {
			type = SyntaxError.MISSINGTOKEN;
			reportError(type, pos, expectedtokens);
		} else {
			type = SyntaxError.UNEXPECTEDINPUT;
			reportError(type, pos, offendingTokenText, expectedtokens);
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

	/**
	 * {@inheritDoc}
	 */
	public abstract Void visit(ParseTree tree);

	/**
	 * {@inheritDoc}
	 */
	public Void visitChildren(RuleNode node) {
		return null;
	};

	/**
	 * {@inheritDoc}
	 */
	public Void visitTerminal(TerminalNode node) {
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	public Void visitErrorNode(ErrorNode node) {
		return null;
	}

	// ----------------------------------------------------------------------------
	// Helper methods - Resolving KRI and processing embedded KR language
	// fragments
	// ----------------------------------------------------------------------------

	/**
	 * Delegate parsing of PARLIST terminal node to KR parser and checks whether
	 * terms are variables. Reports an error if this is not the case.
	 *
	 * @param pars String text from PARLIST terminal.
	 * @param ctx  Parser context where PARLIST was found.
	 * @return List of terms.
	 */
	public List<Var> visitVARLIST(TerminalNode parlist, ParserRuleContext ctx) {
		List<Term> parameters = visitPARLIST(parlist, getSourceInfo(ctx));
		List<Var> vars = new LinkedList<>();
		for (Term term : parameters) {
			if (!term.isVar()) {
				reportError(SyntaxError.PARAMETER_NOT_A_VARIABLE, getSourceInfo(ctx),
						getTheErrorStrategy().prettyPrintRuleContext(ctx.getRuleIndex()), term.toString());
			} else {
				vars.add((Var) term);
			}
		}
		return vars;
	}

	/**
	 * Delegates parsing of terminal node of parameter list to KR parser.
	 *
	 * @param parlist Terminal node representing a parameter list.
	 * @param info    Source information.
	 * @return {@code null} if there are no parameters, a list of terms otherwise.
	 */
	public List<Term> visitPARLIST(TerminalNode parlist, SourceInfo info) {
		List<Term> parameters = null;
		if (parlist != null) {
			try {
				CognitiveKR ckr = getCognitiveKR();
				info = (info == null) ? null
						: new InputStreamPosition(info.getLineNumber(), info.getCharacterPosition() + 1,
								info.getStartIndex() + 1, info.getStopIndex() - 1, info.getSource());
				parameters = ckr.visitArguments(removeLeadTrailCharacters(parlist.getText()), info);
				reportEmbeddedLanguageErrors(ckr);
			} catch (ParserException e) {
				reportParsingException(e);
			}
		}
		if (parameters == null) {
			return new ArrayList<>(0);
		} else {
			return parameters;
		}
	}

	// -------------------------------------------------------------
	// Helper methods
	// -------------------------------------------------------------

	/**
	 * Appends path of source file to filename.
	 *
	 * @param filename The name of a file.
	 * @return The filename with path to source file appended.
	 */
	protected String getPathRelativeToSourceFile(String filename) {
		final File f = new File(getFilename());
		return new File(f.getParent(), filename).getPath();
	}

	/**
	 * @param set Of items to pretty print as list
	 * @return string with comma separated list of set items, or plain single item,
	 *         or empty string
	 */
	public String prettyPrintSet(Set<?> set) {
		StringBuilder str = new StringBuilder();

		Iterator<?> setIterator = set.iterator();
		if (setIterator.hasNext()) {
			str.append(setIterator.next());
		}
		while (setIterator.hasNext()) {
			String next = setIterator.next().toString();
			str.append(setIterator.hasNext() ? ", " : " and ");
			str.append(next);
		}

		return str.toString();
	}

	/**
	 * Removes leading and trailing characters from a string.
	 *
	 * @param quoted A string with e.g. quotes.
	 * @return The string without leading/trailing characters.
	 */
	public String removeLeadTrailCharacters(String quoted) {
		return quoted.substring(1, quoted.length() - 1);
	}

	/**
	 * Removes tabs, newlines, etc. from string.
	 *
	 * @param text Input string
	 * @return Output string without tabs, etc.
	 */
	private String removeTabsAndNewLines(String text) {
		return StringUtils.deleteWhitespace(text);
	}

	/**
	 * Turns a list of terminal nodes into a single string.
	 *
	 * @param nodes List of terminal nodes.
	 * @return A string representing the string content of the nodes.
	 */
	protected String implode(List<TerminalNode> nodes) {
		StringBuilder builder = new StringBuilder();
		for (TerminalNode character : nodes) {
			if (character != null) {
				builder.append(character.getText());
			}
		}
		return builder.toString();
	}
}
