package goal.tools.history.explanation.reasons;

import krTools.language.Substitution;
import languageTools.program.agent.msc.MentalStateCondition;

/**
 * Part of an {@link ActionReason}: a rule(condition) selecting that action held
 * with a certain substitution.
 */
public class ActionRuleCondition extends Reason {
	private final MentalStateCondition msc;
	private final Substitution subst;

	public ActionRuleCondition(MentalStateCondition msc, Substitution subst, int state) {
		super(msc.getSourceInfo(), state);
		this.msc = msc;
		this.subst = subst;
	}

	public Substitution getSubstitution() {
		return this.subst;
	}

	@Override
	public String toString() {
		final StringBuilder string = new StringBuilder();
		string.append("the rule condition '").append(this.msc).append("' held with ").append(this.subst).append(" at <")
				.append(this.location).append("> in state ").append(this.state);
		return string.toString();
	}
}
