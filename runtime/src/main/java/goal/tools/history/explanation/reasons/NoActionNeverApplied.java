package goal.tools.history.explanation.reasons;

import java.util.Set;

import languageTools.program.agent.actions.Action;

/**
 * Possible instantiation of a {@link NoActionReason}: rule(condition)s
 * selecting the action always resulted in other instantiations.
 */
public class NoActionNeverApplied extends NoActionReason {
	private Set<Action<?>> otherInstances;

	public NoActionNeverApplied(final Action<?> action) {
		super(action);
	}

	public void setOtherInstances(final Set<Action<?>> instances) {
		this.otherInstances = instances;
	}

	@Override
	public String toString() {
		final StringBuilder string = new StringBuilder();
		string.append(this.action)
				.append(" was never executed because other instantiations of the action were applied instead: ")
				.append(this.otherInstances).append(".");
		return string.toString();
	}
}
