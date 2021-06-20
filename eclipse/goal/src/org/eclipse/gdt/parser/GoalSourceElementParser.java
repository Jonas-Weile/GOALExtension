package org.eclipse.gdt.parser;

import org.eclipse.dltk.compiler.SourceElementRequestVisitor;
import org.eclipse.dltk.core.AbstractSourceElementParser;
import org.eclipse.gdt.GoalNature;

public class GoalSourceElementParser extends AbstractSourceElementParser {
	@Override
	protected SourceElementRequestVisitor createVisitor() {
		return new GoalSourceElementRequestor(getRequestor());
	}

	@Override
	protected String getNatureId() {
		return GoalNature.GOAL_NATURE;
	}
}