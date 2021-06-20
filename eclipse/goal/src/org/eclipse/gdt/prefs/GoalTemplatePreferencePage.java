package org.eclipse.gdt.prefs;

import org.eclipse.dltk.ui.templates.ScriptTemplateAccess;
import org.eclipse.dltk.ui.templates.ScriptTemplatePreferencePage;
import org.eclipse.dltk.ui.text.ScriptSourceViewerConfiguration;
import org.eclipse.gdt.Activator;
import org.eclipse.gdt.completion.GoalTemplateAccess;
import org.eclipse.gdt.editor.GoalSourceViewerConfiguration;
import org.eclipse.gdt.editor.GoalTextTools;
import org.eclipse.gdt.editor.IGoalPartitions;
import org.eclipse.jface.text.IDocument;

public class GoalTemplatePreferencePage extends ScriptTemplatePreferencePage {
	@Override
	protected ScriptSourceViewerConfiguration createSourceViewerConfiguration() {
		return new GoalSourceViewerConfiguration(getTextTools().getColorManager(), getPreferenceStore(), null,
				IGoalPartitions.GOAL_PARTITIONING);
	}

	@Override
	protected void setDocumentParticioner(final IDocument document) {
		getTextTools().setupDocumentPartitioner(document, IGoalPartitions.GOAL_PARTITIONING);
	}

	@Override
	protected void setPreferenceStore() {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	protected ScriptTemplateAccess getTemplateAccess() {
		return GoalTemplateAccess.getInstance();
	}

	private GoalTextTools getTextTools() {
		return Activator.getDefault().getTextTools();
	}
}