package org.eclipse.gdt.debug.ui;

import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.internal.console.ConsoleView;

@SuppressWarnings("restriction")
public class GoalAgentConsoleView extends ConsoleView {
	public final static String VIEW_ID = "org.eclipse.gdt.AgentConsoleView";

	@Override
	public void consolesAdded(final IConsole[] consoles) {
		if (consoles.length == 1 && consoles[0].getName().equals(getViewSite().getSecondaryId())) {
			super.consolesAdded(consoles);
			setPartName(consoles[0].getName());
			setPinned(true);
		}
	}

	@Override
	public void setFocus() {
	}
}