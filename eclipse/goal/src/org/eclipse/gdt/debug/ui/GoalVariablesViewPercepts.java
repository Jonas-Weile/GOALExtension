package org.eclipse.gdt.debug.ui;

public class GoalVariablesViewPercepts extends GoalVariablesView {
	public final static String VIEW_ID = "org.eclipse.gdt.debug.ui.GoalVariablesViewPercepts";

	@Override
	protected GoalVariableType getVariableType() {
		return GoalVariableType.PERCEPTS;
	}
}
