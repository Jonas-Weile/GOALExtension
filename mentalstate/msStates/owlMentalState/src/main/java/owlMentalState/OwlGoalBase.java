package owlMentalState;

import krTools.language.Update;
import mentalState.GoalBase;
import mentalState.MentalState;
import mentalState.SingleGoal;
import mentalState.error.MSTDatabaseException;

public class OwlGoalBase extends GoalBase {

	protected OwlGoalBase(MentalState owner, String name) {
		super(owner, name);
	}

	@Override
	protected SingleGoal createGoal(Update goal) throws MSTDatabaseException {
		return new OwlSingleGoal(this, goal);
	}

}
