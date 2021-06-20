package org.eclipse.gdt;

import org.eclipse.dltk.ui.IDLTKUILanguageToolkit;
import org.eclipse.dltk.ui.actions.OpenTypeAction;
import org.eclipse.gdt.ui.GoalUILanguageToolkit;

public class GoalOpenTypeAction extends OpenTypeAction {
	@Override
	protected IDLTKUILanguageToolkit getUILanguageToolkit() {
		return new GoalUILanguageToolkit();
	}
}