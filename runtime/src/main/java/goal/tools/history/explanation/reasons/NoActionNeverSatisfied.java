package goal.tools.history.explanation.reasons;

import languageTools.program.agent.actions.Action;

/**
 * Possible instantiation of a {@link NoActionReason}: either the rule
 * condition(s) selecting the action or its precondition were never satisfied.
 */
public class NoActionNeverSatisfied extends NoActionReason {
	private boolean ruleSatisfied;

	public NoActionNeverSatisfied(final Action<?> action) {
		super(action);
	}

	public void setRuleSatisfied() {
		this.ruleSatisfied = true;
	}

	@Override
	public String toString() {
		final StringBuilder string = new StringBuilder();
		string.append(this.action).append(" was never executed because ");
		if (this.ruleSatisfied) {
			string.append("even though the action was selected, its precondition was never satisfied");
		} else {
			string.append("none of the (condition(s) of) the rule(s) selecting the action were ever satisfied");
		}
		string.append(".");
		return string.toString();
	}
}
