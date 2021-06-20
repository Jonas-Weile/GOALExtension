package org.eclipse.gdt.debug.history;

import org.eclipse.debug.internal.ui.commands.actions.DebugCommandActionDelegate;

@SuppressWarnings("restriction")
public class LookupCommandActionDelegate extends DebugCommandActionDelegate {
	public LookupCommandActionDelegate() {
		setAction(new LookupCommandAction());
	}
}
