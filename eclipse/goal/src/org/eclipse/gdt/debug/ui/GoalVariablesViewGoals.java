package org.eclipse.gdt.debug.ui;

public class GoalVariablesViewGoals extends GoalVariablesView {
	public final static String VIEW_ID = "org.eclipse.gdt.debug.ui.GoalVariablesViewGoals";

	@Override
	protected GoalVariableType getVariableType() {
		return GoalVariableType.GOALS;
	}
}