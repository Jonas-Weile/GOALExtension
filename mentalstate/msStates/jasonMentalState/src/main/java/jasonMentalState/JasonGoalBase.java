package jasonMentalState;

import krTools.language.Update;
import mentalState.GoalBase;
import mentalState.MentalState;
import mentalState.SingleGoal;
import mentalState.error.MSTDatabaseException;

public class JasonGoalBase extends GoalBase {
	protected JasonGoalBase(MentalState owner, String name) {
		super(owner, name);
	}

	@Override
	protected SingleGoal createGoal(Update goal) throws MSTDatabaseException {
		return new JasonGoal(this, goal);
	}
}
