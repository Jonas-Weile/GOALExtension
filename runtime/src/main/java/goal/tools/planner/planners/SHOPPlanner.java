package goal.tools.planner.planners;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.Set;

import events.Channel;
import goal.tools.planner.Plan;
import goal.tools.planner.Planner;
import goal.tools.planner.Step;
import krTools.exceptions.KRDatabaseException;
import krTools.exceptions.KRQueryFailedException;
import krTools.language.Substitution;
import languageTools.program.planner.Decomposition;
import languageTools.program.planner.PlanningMethod;
import languageTools.program.planner.PlanningModule;
import languageTools.program.planner.PlanningOperator;
import languageTools.program.planner.PlanningTask;
import mentalState.MentalStateWithEvents;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;

public class SHOPPlanner extends Planner {

	public SHOPPlanner(PlanningModule module, MentalStateWithEvents mentalState, Substitution substitution) throws KRDatabaseException, MSTDatabaseException, MSTQueryException {
		super(module, mentalState, substitution);
	}

	
	@Override
	public Plan doPlanning() throws MSTQueryException, KRQueryFailedException {			
		Deque<PlanningTask> tasks = setupTaskQueue();
		Plan plan = new Plan();	
		
		plan = findPlan(0, tasks, plan);
		
		if (plan == null) {
			// FAILURE!!
			getGenerator().event(Channel.PRINT, getModule(), getModule().getSourceInfo(), "Failed to find plan :( ");
			return null;
		}
		
		return plan;
	}

	
	private Deque<PlanningTask> setupTaskQueue()  {
		PlanningTask mainTask = getModule().getSubstitutedTask(getSubstitution());		
		Deque<PlanningTask> tasks = new ArrayDeque<>();
		tasks.add(mainTask);
		return tasks;
	}
	
	
	private Plan findPlan(int depth, Deque<PlanningTask> tasks, Plan plan) throws MSTQueryException, KRQueryFailedException {
		
		if (tasks.isEmpty()) {
			getGenerator().event(Channel.PRINT, getModule(), getModule().getSourceInfo(), "Planning - No more tasks! Plan is: %s", plan);
			return plan;
		}
		
		getGenerator().event(Channel.PRINT, getModule(), getModule().getSourceInfo(), "Planning - at depth: %s - tasks: %s", depth, tasks);
		
		// Create a copy of the tasks stack - we do not want to change the original one!
		Deque<PlanningTask> remainingTasks = new ArrayDeque<>(tasks.size());
		remainingTasks.addAll(tasks);
		PlanningTask nextTask = remainingTasks.pop();
		
		if (nextTask.isPrimitive()) {
			PlanningOperator operator = getOperatorForTask(nextTask);
			for (Substitution substitution : getPossibleSubstitutionsForOperator(operator)) {
				// execute operation
				Step step = createStepAndPerformOperator(operator, substitution);
				plan.addStep(step);
				
				// print
				getGenerator().event(Channel.PRINT, getModule(), getModule().getSourceInfo(), "Planning - primitive task: %s. Adding step: %s", nextTask, step);
				
				// seek plan
				Plan solution = findPlan(depth + 1, remainingTasks, plan);
				if (solution != null) {
					return solution;
				}
				
				// undo operation
				undoOperator(operator, substitution);
				plan.removeStep(step);
			}
		} else {
			PlanningMethod method = getMethodForTask(nextTask);
			for (Decomposition decomposition : method.getDecompositions()) {				
				for(Substitution substitution : getPossibleSubstitutionsForDecomposition(decomposition)) {
					// get subtask from decomposition and add to new list
					Deque<PlanningTask> subtasks = applyDecomposition(decomposition, substitution);
					Deque<PlanningTask> newTaskStack = new ArrayDeque<>(remainingTasks.size() + subtasks.size());
					newTaskStack.addAll(subtasks);
					newTaskStack.addAll(remainingTasks);
					
					// print
					getGenerator().event(Channel.PRINT, getModule(), getModule().getSourceInfo(), "Planning - compound task: %s. Adding tasks: %s", nextTask, subtasks);
					
					// seek plan
					Plan solution = findPlan(depth + 1, newTaskStack, plan);
					if (solution != null) {
						return solution;
					}
				}
			}
		}
		
		getGenerator().event(Channel.PRINT, getModule(), getModule().getSourceInfo(), "Planning - FAILURE! task: %s, isPrimitive: %s could not be solved..", nextTask, nextTask.isPrimitive());
		
		return null;
	}

	
	
	private PlanningOperator getOperatorForTask(PlanningTask task) {
		return getModule().getSubstitutedOperator(task.getSignature(), task.getParameters(), getSubstitution());
	}
	
	
	private Step createStepAndPerformOperator(PlanningOperator operator, Substitution substitution) throws MSTQueryException, KRQueryFailedException {
		executeOperator(operator, substitution);
		Step step = new Step(operator.getAction(), operator.createSubstitution(substitution));
		return step;
	}
	
	
	private void undoOperator(PlanningOperator operator, Substitution substitution) throws MSTQueryException {
		executeOperatorReversed(operator, substitution);
		
	}
	
	
	private PlanningMethod getMethodForTask(PlanningTask task) {
		return getModule().getSubstitutedMethod(task.getSignature(), task.getParameters(), getSubstitution());
	}
	
}
