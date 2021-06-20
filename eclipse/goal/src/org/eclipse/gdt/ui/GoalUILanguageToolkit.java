package org.eclipse.gdt.ui;

import org.eclipse.dltk.core.IDLTKLanguageToolkit;
import org.eclipse.dltk.ui.AbstractDLTKUILanguageToolkit;
import org.eclipse.gdt.Activator;
import org.eclipse.gdt.GoalLanguageToolkit;
import org.eclipse.jface.preference.IPreferenceStore;

public class GoalUILanguageToolkit extends AbstractDLTKUILanguageToolkit {

	@Override
	public IPreferenceStore getPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	@Override
	public IDLTKLanguageToolkit getCoreToolkit() {
		return GoalLanguageToolkit.getDefault();
	}
}