package goal.tools.planner;

import krTools.language.Substitution;
import languageTools.program.agent.actions.Action;

public class Step {
	private final Action<?> action;
	private final Substitution substitution;
	
	public Step(Action<?> action, Substitution substitution) {
		this.action = action;
		this.substitution = substitution;
	}
	
	public Action<?> getAction() {
		return this.action;
	}
	
	public Substitution getSubstitution() {
		return this.substitution;
	}
	
	@Override
	public String toString() {
		return this.action.applySubst(this.substitution).toString();
	}
}