package org.eclipse.gdt.lexer;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.eclipse.core.runtime.IPath;
import org.eclipse.dltk.ui.text.AbstractScriptScanner;
import org.eclipse.dltk.ui.text.IColorManager;
import org.eclipse.gdt.Messages;
import org.eclipse.gdt.editor.IGoalColorConstants;

import languageTools.parser.MAS2GLexer;

public class MASSourceLexer implements ISourceLexer {
	private MAS2GLexer lexer;

	@Override
	public Set<String> getApplicableExtensions() {
		final Set<String> returned = new HashSet<>(1);
		returned.add(Messages.MASFileExtension);
		return Collections.unmodifiableSet(returned);
	}

	@Override
	public void createLexer(final CharStream input) {
		this.lexer = new MAS2GLexer(input);
		this.lexer.removeErrorListeners();
	}

	@Override
	public Lexer getLexer() {
		return this.lexer;
	}

	@Override
	public int getWhitespaceToken() {
		return MAS2GLexer.WS;
	}

	@Override
	public String getColor(final int token) {
		switch (token) {
		case MAS2GLexer.LINE_COMMENT:
		case MAS2GLexer.BLOCK_COMMENT:
			return IGoalColorConstants.GOAL_COMMENT;
		case MAS2GLexer.AGENT:
		case MAS2GLexer.ENVIRONMENT:
		case MAS2GLexer.EVENT:
		case MAS2GLexer.INIT:
		case MAS2GLexer.MAIN:
		case MAS2GLexer.SHUTDOWN:
		case MAS2GLexer.MODULE:
			return IGoalColorConstants.GOAL_KEYWORD;
		case MAS2GLexer.AS:
		case MAS2GLexer.DEFINE:
		case MAS2GLexer.LAUNCH:
		case MAS2GLexer.LAUNCHPOLICY:
		case MAS2GLexer.MAX:
		case MAS2GLexer.NAME:
		case MAS2GLexer.NUMBER:
		case MAS2GLexer.TYPE:
		case MAS2GLexer.USE:
		case MAS2GLexer.WHEN:
		case MAS2GLexer.WITH:
			return IGoalColorConstants.GOAL_DECLARATION;
		case MAS2GLexer.STAR:
		case MAS2GLexer.COMMA:
		case MAS2GLexer.CRBR:
		case MAS2GLexer.DOT:
		case MAS2GLexer.EQUALS:
		case MAS2GLexer.MINUS:
		case MAS2GLexer.PLUS:
		case MAS2GLexer.SLBR:
		case MAS2GLexer.SRBR:
			return IGoalColorConstants.GOAL_OPERATOR;
		case MAS2GLexer.SingleQuotedStringLiteral:
		case MAS2GLexer.StringLiteral:
			return IGoalColorConstants.GOAL_STRING;
		default:
			return IGoalColorConstants.GOAL_DEFAULT;
		}
	}

	@Override
	public Set<Integer> getSubTokens() {
		return new HashSet<>(0);
	}

	@Override
	public AbstractScriptScanner getSubScanner(final IPath path, final IColorManager manager) {
		return null;
	}
}