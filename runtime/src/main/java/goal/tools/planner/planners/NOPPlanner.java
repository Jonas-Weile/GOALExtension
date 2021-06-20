package goal.tools.planner.planners;

import goal.tools.planner.Plan;
import goal.tools.planner.Planner;
import krTools.exceptions.KRDatabaseException;
import krTools.language.Substitution;
import languageTools.program.planner.PlanningModule;
import mentalState.MentalStateWithEvents;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;

public class NOPPlanner extends Planner {

	public NOPPlanner(PlanningModule module, MentalStateWithEvents mentalState, Substitution substitution) throws KRDatabaseException, MSTDatabaseException, MSTQueryException {
		super(module, mentalState, substitution);
	}

	@Override
	public Plan doPlanning() {
		return null;
	}
}
