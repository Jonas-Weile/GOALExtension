package goal.tools.planner;

import java.util.List;

import krTools.exceptions.KRQueryFailedException;
import languageTools.program.agent.rules.Rule;
import mentalState.MentalStateWithEvents;
import mentalState.error.MSTQueryException;

public interface PlanningAlgorithm {
	public Plan doPlanning() throws MSTQueryException, KRQueryFailedException;
}
