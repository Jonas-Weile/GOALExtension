package goal.tools.planner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import goal.tools.adapt.ModuleID;
import goal.tools.planner.planners.SHOPPlanner;
import krTools.exceptions.KRDatabaseException;
import krTools.language.Substitution;
import languageTools.program.agent.Module;
import languageTools.program.agent.rules.Rule;
import languageTools.program.planner.PlanningModule;
import mentalState.MentalStateWithEvents;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;

public class PlannerFactory {

	/**
	 * Utility class; constructor is hidden.
	 */
	private PlannerFactory() { }
	private static Map<ModuleID, Planner> planners = new HashMap<>();
	
	public static Map<ModuleID, Planner> getPlanners() {
		return planners;
	}
	
	// TODO : at the moment, simply return the test planner
	public synchronized static Planner getPlanner(PlanningModule module, MentalStateWithEvents mentalState, Substitution substitution) throws MSTDatabaseException, MSTQueryException, KRDatabaseException {
		ModuleID id = new ModuleID(module.getSignature());
		Planner planner = getPlanners().get(id);
		
		if (planner == null) {
			planner = new SHOPPlanner(module, mentalState, substitution.clone());
			planners.put(id, planner);
		} else {
			planner.reset(mentalState, substitution.clone());
		}
		
		return planner;
	}



}
