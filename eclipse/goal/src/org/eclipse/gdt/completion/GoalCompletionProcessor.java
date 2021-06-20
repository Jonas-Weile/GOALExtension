package org.eclipse.gdt.completion;

import org.eclipse.dltk.ui.text.completion.ScriptCompletionProcessor;
import org.eclipse.gdt.GoalNature;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.ui.IEditorPart;

public class GoalCompletionProcessor extends ScriptCompletionProcessor {
	public GoalCompletionProcessor(final IEditorPart editor, final ContentAssistant assistant, final String partition) {
		super(editor, assistant, partition);
	}

	@Override
	protected String getNatureId() {
		return GoalNature.GOAL_NATURE;
	}
}