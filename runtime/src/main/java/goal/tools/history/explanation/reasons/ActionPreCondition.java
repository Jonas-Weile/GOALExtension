package goal.tools.history.explanation.reasons;

import krTools.language.Substitution;
import languageTools.program.agent.msc.MentalStateCondition;

/**
 * Part of an {@link ActionReason}: the precondition of that action held with a
 * certain substitution.
 */
public class ActionPreCondition extends Reason {
	private final MentalStateCondition msc;
	private Substitution subst;

	public ActionPreCondition(final MentalStateCondition msc, final int state) {
		super(msc.getSourceInfo(), state);
		this.msc = msc;
	}

	public void setSubstitution(final Substitution subst) {
		this.subst = subst;
	}

	@Override
	public String toString() {
		final StringBuilder string = new StringBuilder();
		string.append("the pre-condition '").append(this.msc).append("' held with ").append(this.subst).append(" at <")
				.append(this.location).append("> in state ").append(this.state);
		return string.toString();
	}
}
