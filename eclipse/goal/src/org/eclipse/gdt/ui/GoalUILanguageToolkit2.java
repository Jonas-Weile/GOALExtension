package org.eclipse.gdt.ui;

import org.eclipse.dltk.ui.text.ScriptSourceViewerConfiguration;
import org.eclipse.gdt.editor.GoalSimpleSourceViewerConfiguration;

public class GoalUILanguageToolkit2 extends GoalUILanguageToolkit {

	@Override
	public ScriptSourceViewerConfiguration createSourceViewerConfiguration() {
		return new GoalSimpleSourceViewerConfiguration(getTextTools().getColorManager(), getPreferenceStore(), null,
				getPartitioningId(), false);
	}
}