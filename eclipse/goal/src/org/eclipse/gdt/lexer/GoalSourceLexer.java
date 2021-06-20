package org.eclipse.gdt.lexer;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.eclipse.core.runtime.IPath;
import org.eclipse.dltk.ui.text.AbstractScriptScanner;
import org.eclipse.dltk.ui.text.IColorManager;
import org.eclipse.gdt.Messages;
import org.eclipse.gdt.editor.IGoalColorConstants;

import ar.com.tadp.prolog.core.PrologCorePlugin;
import ar.com.tadp.prolog.core.ui.editor.text.PrologCodeScanner;
import krFactory.KRFactory;
import languageTools.parser.GOALLexer;

public class GoalSourceLexer implements ISourceLexer {
	private GOALLexer lexer;
	private final static Map<String, Class<? extends AbstractScriptScanner>> sub = new HashMap<>(1);

	static {
		sub.put(KRFactory.SWI_PROLOG, PrologCodeScanner.class);
	}

	@Override
	public Set<String> getApplicableExtensions() {
		final Set<String> returned = new HashSet<>(3);
		returned.add(Messages.ActionFileExtension);
		returned.add(Messages.ModuleFileExtension);
		returned.add(Messages.PlannerFileExtension);
		returned.add(Messages.TestFileExtension);
		return Collections.unmodifiableSet(returned);
	}

	@Override
	public void createLexer(final CharStream input) {
		this.lexer = new GOALLexer(input);
		this.lexer.removeErrorListeners();
	}

	@Override
	public Lexer getLexer() {
		return this.lexer;
	}

	@Override
	public int getWhitespaceToken() {
		return GOALLexer.WS;
	}

	@Override
	public String getColor(final int token) {
		switch (token) {
		case GOALLexer.LINE_COMMENT:
		case GOALLexer.BLOCK_COMMENT:
			return IGoalColorConstants.GOAL_COMMENT;
		case GOALLexer.ADOPT:
		case GOALLexer.AGOAL_OP:
		case GOALLexer.AS:
		case GOALLexer.BELIEF_OP:
		case GOALLexer.CANCELTIMER:
		case GOALLexer.DEFINE:
		case GOALLexer.DELETE:
		case GOALLexer.DO:
		case GOALLexer.DONE:
		case GOALLexer.DROP:
		case GOALLexer.EXIT:
		case GOALLexer.EXITMODULE:
		case GOALLexer.FOCUS:
		case GOALLexer.FORALL:
		case GOALLexer.GOAL_OP:
		case GOALLexer.GOALA_OP:
		case GOALLexer.IF:
		case GOALLexer.IN:
		case GOALLexer.INSERT:
		case GOALLexer.LISTALL:
		case GOALLexer.LOG:
		case GOALLexer.MODULE:
		case GOALLexer.ORDER:
		case GOALLexer.PERCEPT_OP:
		case GOALLexer.POST:
		case GOALLexer.PRE:
		case GOALLexer.PRINT:
		case GOALLexer.LEADSTO:
		case GOALLexer.SEND:
		case GOALLexer.SEND_IMP:
		case GOALLexer.SEND_IND:
		case GOALLexer.SEND_INT:
		case GOALLexer.SENT_OP:
		case GOALLexer.SENT_IMP_OP:
		case GOALLexer.SENT_IND_OP:
		case GOALLexer.SENT_INT_OP:
		case GOALLexer.SLEEP:
		case GOALLexer.STARTTIMER:
		case GOALLexer.SUBSCRIBE:
		case GOALLexer.THEN:
		case GOALLexer.TEST:
		case GOALLexer.TIMEOUT:
		case GOALLexer.UNSUBSCRIBE:
		case GOALLexer.USE:
		case GOALLexer.WITH:
		case GOALLexer.METHOD:
		case GOALLexer.OPERATOR:
		case GOALLexer.TASK:
		case GOALLexer.SUBTASKS:
			return IGoalColorConstants.GOAL_DECLARATION;
		case GOALLexer.NOT:
		case GOALLexer.TRUE:
		case GOALLexer.ALWAYS:
		case GOALLexer.EVENTUALLY:
		case GOALLexer.NEVER:
		case GOALLexer.UNTIL:
		case GOALLexer.BELIEFS:
		case GOALLexer.GOALS:
		case GOALLexer.KNOWLEDGE:
		case GOALLexer.ACTIONSPEC:
		case GOALLexer.PLANNER:
		case GOALLexer.MAS:
		case GOALLexer.ADAPTIVE:
		case GOALLexer.RANDOM:
		case GOALLexer.RANDOMALL:
		case GOALLexer.LINEAR:
		case GOALLexer.LINEARRANDOM:
		case GOALLexer.LINEARALL:
		case GOALLexer.LINEARALLRANDOM:
		case GOALLexer.NOACTION:
		case GOALLexer.NOGOALS:
		case GOALLexer.FILTER:
		case GOALLexer.NEW:
		case GOALLexer.NONE:
		case GOALLexer.SELECT:
		case GOALLexer.ALL:
		case GOALLexer.ALLOTHER:
		case GOALLexer.SELF:
		case GOALLexer.SOME:
		case GOALLexer.SOMEOTHER:
		case GOALLexer.THIS:
		case GOALLexer.EXTERNAL:
		case GOALLexer.INTERNAL:
			return IGoalColorConstants.GOAL_KEYWORD;
		case GOALLexer.CLBR:
		case GOALLexer.COMMA:
		case GOALLexer.CRBR:
		case GOALLexer.DOT:
		case GOALLexer.EQUALS:
		case GOALLexer.LTRARROW:
		case GOALLexer.MINUS:
		case GOALLexer.PLUS:
		case GOALLexer.RTLARROW:
		case GOALLexer.SLBR:
		case GOALLexer.SRBR:
			return IGoalColorConstants.GOAL_OPERATOR;
		case GOALLexer.SingleQuotedStringLiteral:
		case GOALLexer.StringLiteral:
			return IGoalColorConstants.GOAL_STRING;
		default:
			return IGoalColorConstants.GOAL_DEFAULT;
		}
	}

	@Override
	public Set<Integer> getSubTokens() {
		final Set<Integer> returned = new HashSet<>(3);
		returned.add(GOALLexer.KR_BLOCK);
		returned.add(GOALLexer.KRBLOCK);
		returned.add(GOALLexer.PARLIST);
		return Collections.unmodifiableSet(returned);
	}

	@Override
	public AbstractScriptScanner getSubScanner(final IPath path, final IColorManager manager) {
		try { // FIXME
				// final String name = GoalSourceParser.getKR(path).getName();
				// final Class<? extends AbstractScriptScanner> kr =
				// sub.get(name);
				// if (kr.equals(PrologCodeScanner.class)) {
			return new PrologCodeScanner(manager, PrologCorePlugin.getDefault().getPreferenceStore());
			// } else {
			// return null;
			// }
		} catch (final Exception e) {
			return null;
		}
	}
}