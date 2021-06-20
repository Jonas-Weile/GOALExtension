package goal.core.executors.modules;

import events.Channel;
import events.ExecutionEventGeneratorInterface;
import goal.core.executors.stack.ActionComboStackExecutor;
import goal.core.executors.stack.CallStack;
import goal.core.runtime.service.agent.Result;
import goal.core.runtime.service.agent.RunState;
import goal.tools.errorhandling.exceptions.GOALActionFailedException;
import goal.tools.planner.Plan;
import goal.tools.planner.Planner;
import goal.tools.planner.PlannerFactory;
import goal.tools.planner.Step;
import goal.tools.planner.planners.SHOPPlanner;
import krTools.exceptions.KRDatabaseException;
import krTools.exceptions.KRQueryFailedException;
import krTools.language.Substitution;
import languageTools.program.agent.Module.RuleEvaluationOrder;
import languageTools.program.agent.actions.Action;
import languageTools.program.agent.actions.ActionCombo;
import languageTools.program.planner.PlanningModule;
import mentalState.MentalStateWithEvents;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;

public class PlanningModuleExecutor extends ModuleExecutor {
	/**
	 * The plan belonging to the module.
	 * An empty plan specifies no possible moves.
	 */
	private Plan plan = null;
	ExecutionEventGeneratorInterface generator;
	
	
	/**
	 * Create an executor for a {@link PlanningModule}.
	 *
	 * @param parent           The {@link CallStack} that we are working in.
	 * @param runstate         The {@link RunState} (i.e. agent) that we are working
	 *                         for.
	 * @param module           The {@link PlanningModule} that is to be executed.
	 * @param substitution     The {@link Substitution} to be used for instantiating
	 *                         parameters of the module.
	 * @param defaultRuleOrder the order in which the rules in this module are to be
	 *                         evaluated if the module didn't specify an order.
	 */
	PlanningModuleExecutor(CallStack parent, RunState runstate, PlanningModule module, Substitution substitution,
			RuleEvaluationOrder defaultRuleOrder) {
		super(parent, runstate, module, substitution, defaultRuleOrder);
		this.generator = this.runstate.getEventGenerator();
		
	}
	
	@Override
	public void popped() {
		try {
			if (this.failure == null) {
				execute();
			}
		} catch (GOALActionFailedException e) {
			this.failure = e;
		} catch (MSTDatabaseException | MSTQueryException | KRDatabaseException | KRQueryFailedException e) {
			this.failure = new GOALActionFailedException("failed to execute module", e);
		}
	}
	
	
	private void execute() throws GOALActionFailedException, MSTDatabaseException, MSTQueryException, KRDatabaseException, KRQueryFailedException {
		// Check if this is the first call to this module.
		if (!hasPreparedMentalState()) {
			prepareMentalState();
			resetStatus();
		}
		
		// Check if we have just finished executing an action.
		ActionComboStackExecutor previous = previousActionComboStackExecutorkOrNull();
				
		// We need to exit the module if there is no possible plan or the last action failed.
		boolean exit = (previous == null) ? false : !previous.getResult().justPerformedAction();
				
		if (previous != null) {
			this.result = this.result.merge(previous.getResult());
			exit = isModuleTerminated(exit);
		}
		
		
		
		generator.event(Channel.PRINT, getModule(), getModule().getSourceInfo(), "PLANNING MODULE - current values:  exit = '%s', previous = '%s'.", exit, previous);

		// Clean up if we should exit the module;
		// and execute it otherwise.
		if (exit) {
			cleanUp();
			terminateModule();
			generator.event(Channel.MODULE_EXIT, getModule(), getModule().getSourceInfo(), "leaving planning module: '%s'.",
					getModule());
		} else {
			executeModule();
		}
	}
	
	private void resetStatus() {
		this.result = Result.START;
	}
				
	private ActionComboStackExecutor previousActionComboStackExecutorkOrNull() {
		return (getPrevious() instanceof ActionComboStackExecutor)
				? (ActionComboStackExecutor) getPrevious()
				: null;
	}
	
	private void cleanUp() {
		this.plan = null;
	}

	private void executeModule() throws GOALActionFailedException, MSTDatabaseException, MSTQueryException, KRDatabaseException, KRQueryFailedException {
		if (performedActionFromPlan()) {
			resetStatus();
			if (doEvent(true, this.result))
				return;
		}
		
		if (this.plan == null) {
			createNewPlan();
		}
		
		ActionComboStackExecutor nextAction = nextStepInPlan();
		if (nextAction != null) {
			select(this);
			select(null); // stub for rule-stack
			select(nextAction);
		}
	}
	
	private boolean performedActionFromPlan() {
		return plan != null && this.result.justPerformedAction();
	}
	
	private void createNewPlan() throws MSTDatabaseException, MSTQueryException, KRDatabaseException, KRQueryFailedException {
		MentalStateWithEvents mentalState = this.runstate.getMentalState();		
		Planner planner = PlannerFactory.getPlanner((PlanningModule) getModule(), mentalState, getSubstitution());
		
		// TODO : REMOVE ME!!!! VERY ILLEGAL!
		planner.setGenerator(this.generator);
		
		this.plan = planner.createNewPlan();	
	}

	private ActionComboStackExecutor nextStepInPlan() throws GOALActionFailedException {		
		// Check validity of plan
		Step nextStep = null;
		
		if (plan != null) {
			nextStep = this.plan.popNextStep();
		}
			
		if (nextStep != null) {
			Action<?> nextAction = nextStep.getAction();
			Substitution nextSubstitution = nextStep.getSubstitution();
			
			ActionCombo combo = new ActionCombo(nextAction.getSourceInfo());
			combo.addAction(nextAction);
			ActionComboStackExecutor stub = (ActionComboStackExecutor) getExecutor(combo, nextSubstitution);
			stub.setFocus(getFocus());
			
			return stub;
		}
		
		// No more steps in plan
		cleanUp();
		return null;
	}
	
	

	@Override
	public Result getResult() throws GOALActionFailedException {
		if (this.failure == null) {
			return this.result;
		} else {
			throw this.failure;
		}
	}

}
