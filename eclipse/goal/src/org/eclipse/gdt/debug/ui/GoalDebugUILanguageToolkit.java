package org.eclipse.gdt.debug.ui;

import org.eclipse.dltk.debug.ui.AbstractDebugUILanguageToolkit;
import org.eclipse.gdt.Activator;
import org.eclipse.gdt.debug.GoalDebugConstants;
import org.eclipse.jface.preference.IPreferenceStore;

public class GoalDebugUILanguageToolkit extends AbstractDebugUILanguageToolkit {

	@Override
	public String getDebugModelId() {
		return GoalDebugConstants.DEBUG_MODEL_ID;
	}

	@Override
	public IPreferenceStore getPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}
}
