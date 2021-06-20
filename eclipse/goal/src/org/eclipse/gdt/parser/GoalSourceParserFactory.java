package org.eclipse.gdt.parser;

import org.eclipse.dltk.ast.parser.ISourceParser;
import org.eclipse.dltk.ast.parser.ISourceParserFactory;

public class GoalSourceParserFactory implements ISourceParserFactory {

	@Override
	public ISourceParser createSourceParser() {
		return new GoalSourceParser();
	}
}