package org.eclipse.gdt.debug.ui;

public class GoalVariablesViewMails extends GoalVariablesView {
	public final static String VIEW_ID = "org.eclipse.gdt.debug.ui.GoalVariablesViewMails";

	@Override
	protected GoalVariableType getVariableType() {
		return GoalVariableType.MAILS;
	}
}