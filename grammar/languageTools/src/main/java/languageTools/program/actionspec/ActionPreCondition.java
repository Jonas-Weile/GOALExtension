package languageTools.program.actionspec;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import krTools.language.Query;
import krTools.language.Substitution;
import krTools.parser.SourceInfo;
import languageTools.program.GoalParsedObject;

public class ActionPreCondition extends GoalParsedObject {
	/**
	 * A query representing a condition for successful action execution.
	 */
	private final Query precondition;
	private final Set<String> signatures;

	public ActionPreCondition(Query precondition, Set<String> signatures, SourceInfo info) {
		super(info);
		this.precondition = precondition;
		this.signatures = (signatures == null) ? new LinkedHashSet<>(0) : signatures;
	}

	public Query getPreCondition() {
		return this.precondition;
	}

	public Set<String> getSignatures() {
		return Collections.unmodifiableSet(this.signatures);
	}

	public ActionPreCondition applySubst(Substitution subst) {
		return new ActionPreCondition((getPreCondition() == null) ? null : getPreCondition().applySubst(subst),
				getSignatures(), getSourceInfo());
	}

	@Override
	public int hashCode() {
		return (this.precondition == null) ? 0 : this.precondition.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || !(obj instanceof ActionPreCondition)) {
			return false;
		}
		ActionPreCondition other = (ActionPreCondition) obj;
		if (this.precondition == null) {
			if (other.precondition != null) {
				return false;
			}
		} else if (!this.precondition.equals(other.precondition)) {
			return false;
		}
		return true;
	}

	@Override
	public String toString() {
		return this.precondition.toString();
	}
}
