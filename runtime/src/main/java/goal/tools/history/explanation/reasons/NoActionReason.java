package goal.tools.history.explanation.reasons;

import languageTools.program.agent.actions.Action;

/**
 * Explains why an action was NOT performed (if it actually was not; see
 * {@link ActionReason}. There are 3 basic reasons that can instantiate this
 * class: {@link NoActionNeverEvaluated} rules selecting the action were never
 * evaluated, {@link NoActionNeverApplied} rule(condition)s selecting the action
 * always resulted in other instantiations, and {@link NoActionNeverSatisfied}
 * either the rule condition(s) selecting the action or its precondition were
 * never satisfied.
 */
public abstract class NoActionReason extends Reason {
	protected final Action<?> action;

	protected NoActionReason(final Action<?> action) {
		super(action.getSourceInfo(), -1);
		this.action = action;
	}
}
