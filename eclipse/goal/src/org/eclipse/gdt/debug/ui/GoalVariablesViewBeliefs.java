package org.eclipse.gdt.debug.ui;

public class GoalVariablesViewBeliefs extends GoalVariablesView {
	public final static String VIEW_ID = "org.eclipse.gdt.debug.ui.GoalVariablesViewBeliefs";

	@Override
	protected GoalVariableType getVariableType() {
		return GoalVariableType.BELIEFS;
	}
}