package languageTools.program.actionspec;

import krTools.language.Substitution;
import krTools.language.Update;
import krTools.parser.SourceInfo;
import languageTools.program.GoalParsedObject;

public class ActionPostCondition extends GoalParsedObject {
	/**
	 * A query representing a condition for successful action execution.
	 */
	private final Update postcondition;
	/**
	 * If true, the add and delete list of the Update should be reversed.
	 */
	private final boolean negative;

	public ActionPostCondition(Update postcondition, boolean negative, SourceInfo info) {
		super(info);
		this.postcondition = postcondition;
		this.negative = negative;
	}

	public Update getPostCondition() {
		return this.postcondition;
	}

	public boolean isNegative() {
		return this.negative;
	}

	public ActionPostCondition applySubst(Substitution subst) {
		return new ActionPostCondition((getPostCondition() == null) ? null : getPostCondition().applySubst(subst),
				isNegative(), getSourceInfo());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.negative ? 0 : 1);
		result = prime * result + ((this.postcondition == null) ? 0 : this.postcondition.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || !(obj instanceof ActionPostCondition)) {
			return false;
		}
		ActionPostCondition other = (ActionPostCondition) obj;
		if (this.negative != other.negative) {
			return false;
		}
		if (this.postcondition == null) {
			if (other.postcondition != null) {
				return false;
			}
		} else if (!this.postcondition.equals(other.postcondition)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return this.postcondition.toString();
	}
}
