package org.eclipse.gdt.prefs;

import org.eclipse.gdt.Activator;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public abstract class GoalPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
	public GoalPreferencePage() {
		super(FLAT);
	}

	@Override
	public void init(final IWorkbench workbench) {
		setPreferenceStore(Activator.getDefault().getPreferenceStore());
	}

	@Override
	public boolean performOk() {
		super.performOk();
		GoalPreferenceInitializer.saveGoalPrefs(getPreferenceStore());
		return true;
	}

	@Override
	public void performDefaults() {
		GoalPreferenceInitializer.resetGoalPrefs(getPreferenceStore());
		super.performDefaults();
	}
}