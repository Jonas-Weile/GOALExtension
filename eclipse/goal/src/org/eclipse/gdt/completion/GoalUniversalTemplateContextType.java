package org.eclipse.gdt.completion;

import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.ui.templates.ScriptTemplateContext;
import org.eclipse.dltk.ui.templates.ScriptTemplateContextType;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.templates.TemplateContextType;

public class GoalUniversalTemplateContextType extends ScriptTemplateContextType {
	public static final String CONTEXT_TYPE_ID = "org.eclipse.gdt.templates";

	public GoalUniversalTemplateContextType() {
	}

	public GoalUniversalTemplateContextType(final String id, final String name) {
		super(id, name);
	}

	public GoalUniversalTemplateContextType(final String id) {
		super(id);
	}

	@Override
	public ScriptTemplateContext createContext(final IDocument document, final int completionPosition, final int length,
			final ISourceModule sourceModule) {
		return new GoalTemplateContext(this, document, completionPosition, length, sourceModule);
	}

	public static class GoalTemplateContext extends ScriptTemplateContext {
		protected GoalTemplateContext(final TemplateContextType type, final IDocument document,
				final int completionOffset, final int completionLength, final ISourceModule sourceModule) {
			super(type, document, completionOffset, completionLength, sourceModule);
		}
	}
}