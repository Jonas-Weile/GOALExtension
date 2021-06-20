package org.eclipse.gdt.debug.history;

import org.eclipse.debug.internal.ui.commands.actions.DebugCommandActionDelegate;

@SuppressWarnings("restriction")
public class ForwardCommandActionDelegate extends DebugCommandActionDelegate {
	public ForwardCommandActionDelegate() {
		setAction(new ForwardCommandAction());
	}
}
