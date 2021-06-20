package goal.tools.planner;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import krTools.language.Substitution;
import languageTools.program.agent.actions.Action;

public class Plan {

	private Deque<Step> steps = new ArrayDeque<>();
	
	public Plan(List<Step> steps) {
		this.steps.addAll(steps);
	}
	
	public Plan() {	}

	public Step popNextStep() {
		if (this.steps.size() > 0) {
			return this.steps.pop();
		}
		
		return null;
	}
	
	public boolean isEmpty() {
		return steps.isEmpty();
	}
	
	public Plan addStep(Action<?> action, Substitution substitution) {
		this.steps.add(new Step(action, substitution));
		return this;
	}
	
	public void addStep(Step step) {
		this.steps.add(step);
	}
	
	public void removeStep(Step step) {
		this.steps.remove(step);
	}
	
	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		int i = 1;
		for (Step step: steps) {
			s.append("Step ");
			s.append(i++);
			s.append(": ");
			s.append(step.toString());
			s.append("\n");
		}
		return s.toString();
	}
	
}
