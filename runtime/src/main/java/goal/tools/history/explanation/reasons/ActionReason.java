package goal.tools.history.explanation.reasons;

import languageTools.program.agent.actions.Action;

/**
 * Explains why an action was performed. Exists of both an
 * {@link ActionPreCondition} and an {@link ActionRuleCondition}.
 */
public class ActionReason extends Reason {
	private final Action<?> action;
	private ActionPreCondition sub1;
	private ActionRuleCondition sub2;

	public ActionReason(final Action<?> action, final int state) {
		super(action.getSourceInfo(), state);
		this.action = action;
	}

	public void setPreCondition(final ActionPreCondition sub1) {
		this.sub1 = sub1;
	}

	public void setRuleCondition(final ActionRuleCondition sub2) {
		if (this.sub1 == null) {
			throw new IllegalStateException();
		}
		this.sub2 = sub2;
		this.sub1.setSubstitution(sub2.getSubstitution());
	}

	public boolean hasPreConditionReason() {
		return (this.sub1 != null);
	}

	public boolean hasRuleConditionReason() {
		return (this.sub2 != null);
	}

	@Override
	public String toString() {
		final StringBuilder string = new StringBuilder();
		string.append(this.action).append(" was executed in state ").append(this.state).append(" because ")
				.append(this.sub1).append(" and ").append(this.sub2).append(".");
		return string.toString();
	}
}
