package org.eclipse.gdt.debug.history;

import org.eclipse.debug.internal.ui.commands.actions.DebugCommandActionDelegate;

@SuppressWarnings("restriction")
public class BackCommandActionDelegate extends DebugCommandActionDelegate {
	public BackCommandActionDelegate() {
		setAction(new BackCommandAction());
	}
}
