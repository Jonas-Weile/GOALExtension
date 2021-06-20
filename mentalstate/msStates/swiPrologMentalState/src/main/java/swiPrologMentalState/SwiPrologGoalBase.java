package swiPrologMentalState;

import krTools.language.Update;
import mentalState.GoalBase;
import mentalState.MentalState;
import mentalState.SingleGoal;
import mentalState.error.MSTDatabaseException;

public class SwiPrologGoalBase extends GoalBase {
	private volatile int singleGoalCounter;
	
	protected SwiPrologGoalBase(MentalState owner, String name) {
		super(owner, name);
	}

	@Override
	protected SingleGoal createGoal(Update goal) throws MSTDatabaseException {
		return new SwiPrologGoal(this, this.singleGoalCounter++, goal);
	}
}
