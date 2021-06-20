package org.eclipse.gdt.editor;

import org.eclipse.dltk.ui.text.ScriptTextTools;
import org.eclipse.dltk.ui.text.completion.ContentAssistPreference;
import org.eclipse.gdt.Activator;

public class GoalContentAssistPreference extends ContentAssistPreference {
	private static GoalContentAssistPreference instance;

	public static ContentAssistPreference getDefault() {
		if (instance == null) {
			instance = new GoalContentAssistPreference();
		}
		return instance;
	}

	@Override
	protected ScriptTextTools getTextTools() {
		return Activator.getDefault().getTextTools();
	}
}