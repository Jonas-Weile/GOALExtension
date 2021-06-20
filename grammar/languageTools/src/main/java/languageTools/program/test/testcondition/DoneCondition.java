package languageTools.program.test.testcondition;

import languageTools.program.agent.actions.ActionCombo;

public class DoneCondition {
	private final ActionCombo action;
	private final boolean positive;

	public DoneCondition(ActionCombo action, boolean positive) {
		this.action = action;
		this.positive = positive;
	}

	public ActionCombo getAction() {
		return this.action;
	}

	public boolean isPositive() {
		return this.positive;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.positive ? 0 : 1);
		result = prime * result + ((this.action == null) ? 0 : this.action.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || !(obj instanceof DoneCondition)) {
			return false;
		}
		DoneCondition other = (DoneCondition) obj;
		if (this.positive != other.isPositive()) {
			return false;
		}
		if (this.action == null) {
			if (other.getAction() != null) {
				return false;
			}
		} else if (!this.action.equals(other.getAction())) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		if (!this.positive) {
			builder.append("not(");
		}
		builder.append("done(").append(this.action).append(")");
		if (!this.positive) {
			builder.append(")");
		}
		return builder.toString();
	}
}
