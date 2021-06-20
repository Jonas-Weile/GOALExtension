package goal.tools.planner.planners;

import java.util.Set;

import events.Channel;
import events.ExecutionEventGeneratorInterface;
import goal.tools.planner.Plan;
import goal.tools.planner.Planner;
import krTools.exceptions.KRDatabaseException;
import krTools.exceptions.KRQueryFailedException;
import krTools.language.Substitution;
import languageTools.program.planner.PlanningModule;
import languageTools.program.planner.PlanningOperator;
import languageTools.program.planner.PlanningTask;
import mentalState.MentalStateWithEvents;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;

public class SimplePlanner extends Planner {

	public SimplePlanner(PlanningModule module, MentalStateWithEvents mentalState, Substitution substitution) throws KRDatabaseException, MSTDatabaseException, MSTQueryException {
		super(module, mentalState, substitution);
	}

	@Override
	public Plan doPlanning() throws MSTQueryException, KRQueryFailedException {
		Plan plan = new Plan();
		PlanningTask mainTask = getModule().getSubstitutedTask(getSubstitution());		
		
		if (mainTask.isPrimitive()) {	
			PlanningOperator operator = getModule().getSubstitutedOperator(mainTask.getSignature(), mainTask.getParameters(), getSubstitution());
			Set<Substitution> results = getPossibleSubstitutionsForOperator(operator);	
			
			if (results != null) {
				plan.addStep(operator.getAction(), results.iterator().next());
			}
		}
		
		return plan;
	}
}
