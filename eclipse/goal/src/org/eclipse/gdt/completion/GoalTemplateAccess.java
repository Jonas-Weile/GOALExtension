package org.eclipse.gdt.completion;

import org.eclipse.dltk.ui.templates.ScriptTemplateAccess;
import org.eclipse.gdt.Activator;
import org.eclipse.jface.preference.IPreferenceStore;

public class GoalTemplateAccess extends ScriptTemplateAccess {
	private static GoalTemplateAccess instance = null;

	public static GoalTemplateAccess getInstance() {
		if (instance == null) {
			instance = new GoalTemplateAccess();
		}
		return instance;
	}

	@Override
	protected String[] getContextTypeIds() {
		return new String[] { GoalUniversalTemplateContextType.CONTEXT_TYPE_ID };
	}

	@Override
	protected String getCustomTemplatesKey() {
		return GoalUniversalTemplateContextType.CONTEXT_TYPE_ID;
	}

	@Override
	protected IPreferenceStore getPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}
}