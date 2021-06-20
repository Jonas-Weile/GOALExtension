package tuprolog.parser;

import org.antlr.v4.runtime.ANTLRErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.antlr.v4.runtime.LexerNoViableAltException;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.atn.ATN;
import org.antlr.v4.runtime.atn.LexerATNSimulator;
import org.antlr.v4.runtime.misc.Interval;

public class Lexer4 extends Lexer {
	private final Prolog4Lexer lexer;

	public Lexer4(CharStream input, ANTLRErrorListener errorlistener) {
		super(input);
		this.lexer = getNewLexer(input);
		this.lexer.removeErrorListeners();
		this.lexer.addErrorListener(errorlistener);
	}

	@Override
	public Token nextToken() {
		return this.lexer.nextToken();
	}

	@Override
	public String[] getRuleNames() {
		return this.lexer.getRuleNames();
	}

	@Override
	public String getGrammarFileName() {
		return this.lexer.getGrammarFileName();
	}

	@Override
	public ATN getATN() {
		return this.lexer.getATN();
	}

	@Override
	public LexerATNSimulator getInterpreter() {
		return this.lexer.getInterpreter();
	}

	protected Prolog4Lexer getNewLexer(CharStream input) {
		return new Prolog4Lexer(input) {
			@Override
			public void notifyListeners(LexerNoViableAltException e) {
				String text = this._input.getText(Interval.of(this._tokenStartCharIndex, this._input.index()));
				String msg = this.getErrorDisplay(text);
				ANTLRErrorListener listener = getErrorListenerDispatch();
				listener.syntaxError(this, null, this._tokenStartLine, this._tokenStartCharPositionInLine, msg, e);
			}
		};
	}
}
