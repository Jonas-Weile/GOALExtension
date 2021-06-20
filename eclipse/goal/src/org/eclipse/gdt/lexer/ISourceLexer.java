package org.eclipse.gdt.lexer;

import java.util.Set;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Lexer;
import org.eclipse.core.runtime.IPath;
import org.eclipse.dltk.ui.text.AbstractScriptScanner;
import org.eclipse.dltk.ui.text.IColorManager;

public interface ISourceLexer {
	public Set<String> getApplicableExtensions();

	public void createLexer(CharStream input);

	public Lexer getLexer();

	public int getWhitespaceToken();

	public String getColor(int token);

	public Set<Integer> getSubTokens();

	public AbstractScriptScanner getSubScanner(IPath path, IColorManager manager);
}
