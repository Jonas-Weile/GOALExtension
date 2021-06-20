package languageTools.program.test;

import java.util.Iterator;

import languageTools.program.agent.actions.Action;
import languageTools.program.agent.actions.ActionCombo;
import languageTools.program.test.testcondition.Until;

public class TestAction {
	private final ActionCombo action;
	private final Until condition;

	public TestAction(ActionCombo action) {
		this.action = action;
		this.condition = null;
	}

	public TestAction(ActionCombo action, Until condition) {
		this.action = action;
		this.condition = condition;
	}

	public ActionCombo getAction() {
		return this.action;
	}

	public Until getCondition() {
		return this.condition;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		result.append("do");
		Iterator<Action<?>> actions = this.action.getActions().iterator();
		while (actions.hasNext()) {
			result.append(" ").append(actions.next());
			if (actions.hasNext()) {
				result.append(",");
			}
		}
		return result.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.action == null) ? 0 : this.action.hashCode());
		result = prime * result + ((this.condition == null) ? 0 : this.condition.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || !(obj instanceof TestAction)) {
			return false;
		}
		TestAction other = (TestAction) obj;
		if (this.action == null) {
			if (other.action != null) {
				return false;
			}
		} else if (!this.action.equals(other.action)) {
			return false;
		}
		if (this.condition == null) {
			if (other.condition != null) {
				return false;
			}
		} else if (!this.condition.equals(other.condition)) {
			return false;
		}
		return true;
	}
}
