package org.eclipse.gdt.editor;

import org.eclipse.dltk.ui.text.ScriptSourceViewerConfiguration;
import org.eclipse.dltk.ui.text.ScriptTextTools;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.ui.texteditor.ITextEditor;

public class GoalTextTools extends ScriptTextTools {

	private final static String[] LEGAL_CONTENT_TYPES = new String[] {};
	private final IPartitionTokenScanner fPartitionScanner;

	public GoalTextTools(final boolean autoDisposeOnDisplayDispose) {
		super(IGoalPartitions.GOAL_PARTITIONING, LEGAL_CONTENT_TYPES, autoDisposeOnDisplayDispose);
		this.fPartitionScanner = new GoalPartitionScanner();
	}

	@Override
	public ScriptSourceViewerConfiguration createSourceViewerConfiguraton(final IPreferenceStore preferenceStore,
			final ITextEditor editor, final String partitioning) {
		return new GoalSourceViewerConfiguration(getColorManager(), preferenceStore, editor, partitioning);
	}

	@Override
	public IPartitionTokenScanner getPartitionScanner() {
		return this.fPartitionScanner;
	}
}