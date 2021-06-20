package org.eclipse.gdt.actions;

import org.eclipse.gdt.editor.GoalEditor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

public class Utils {

	public static GoalEditor getActiveEditor() {
		final IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (window != null) {
			final IWorkbenchPage page = window.getActivePage();
			if (page != null) {
				final IEditorPart editor = page.getActiveEditor();
				if (editor instanceof GoalEditor) {
					return (GoalEditor) editor;
				}
			}
		}
		return null;
	}
}