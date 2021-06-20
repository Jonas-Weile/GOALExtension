package goal.tools.history.explanation.reasons;

import java.util.Set;

import krTools.parser.SourceInfo;
import languageTools.program.agent.actions.Action;

/**
 * Possible instantiation of a {@link NoActionReason}: rules selecting the
 * action were never evaluated.
 */
public class NoActionNeverEvaluated extends NoActionReason {
	private Set<SourceInfo> relatedRules;

	public NoActionNeverEvaluated(final Action<?> action) {
		super(action);
	}

	public void setRelatedRules(final Set<SourceInfo> relatedRules) {
		this.relatedRules = relatedRules;
	}

	@Override
	public String toString() {
		final StringBuilder string = new StringBuilder();
		string.append(this.action).append(" was never executed because the rule(s) possibly selecting the action, ")
				.append(this.relatedRules).append(", were never reached.");
		return string.toString();
	}
}
