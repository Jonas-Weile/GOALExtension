package org.eclipse.gdt;

import org.eclipse.dltk.core.AbstractLanguageToolkit;
import org.eclipse.dltk.core.IDLTKLanguageToolkit;

public class GoalLanguageToolkit extends AbstractLanguageToolkit {
	private static GoalLanguageToolkit toolkit;

	public static IDLTKLanguageToolkit getDefault() {
		if (toolkit == null) {
			toolkit = new GoalLanguageToolkit();
		}
		return toolkit;
	}

	@Override
	public String getLanguageName() {
		return "GOAL";
	}

	@Override
	public String getNatureId() {
		return GoalNature.GOAL_NATURE;
	}

	@Override
	public String getLanguageContentType() {
		return "org.eclipse.gdt.contentType";
	}
}