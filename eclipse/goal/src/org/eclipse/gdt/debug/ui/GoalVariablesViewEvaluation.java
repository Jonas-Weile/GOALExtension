package org.eclipse.gdt.debug.ui;

public class GoalVariablesViewEvaluation extends GoalVariablesView {
	public final static String VIEW_ID = "org.eclipse.gdt.debug.ui.GoalVariablesViewEvaluation";

	@Override
	protected GoalVariableType getVariableType() {
		return GoalVariableType.EVALUATION;
	}
}