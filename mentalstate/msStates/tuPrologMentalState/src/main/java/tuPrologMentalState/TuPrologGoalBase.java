package tuPrologMentalState;

import krTools.language.Update;
import mentalState.GoalBase;
import mentalState.MentalState;
import mentalState.SingleGoal;
import mentalState.error.MSTDatabaseException;

public class TuPrologGoalBase extends GoalBase {
	protected TuPrologGoalBase(MentalState owner, String name) {
		super(owner, name);
	}

	@Override
	protected SingleGoal createGoal(Update goal) throws MSTDatabaseException {
		return new TuPrologGoal(this, goal);
	}
}
