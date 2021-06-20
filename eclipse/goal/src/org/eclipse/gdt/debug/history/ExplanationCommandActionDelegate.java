package org.eclipse.gdt.debug.history;

import org.eclipse.debug.internal.ui.commands.actions.DebugCommandActionDelegate;

@SuppressWarnings("restriction")
public class ExplanationCommandActionDelegate extends DebugCommandActionDelegate {
	public ExplanationCommandActionDelegate() {
		setAction(new ExplanationCommandAction());
	}
}
