package goal.tools.history.events;

import java.util.ArrayList;
import java.util.List;

import goal.core.runtime.service.agent.RunState;
import goal.tools.errorhandling.exceptions.GOALActionFailedException;
import krTools.language.Substitution;
import krTools.parser.ParsedObject;
import krTools.parser.SourceInfo;
import languageTools.program.ProgramMap;
import languageTools.program.agent.actions.Action;

public class CallEvent extends AbstractEvent {
	private final int call;
	private final Substitution subst;

	public CallEvent(final RunState runState, final Action<?> call, final Substitution subst) {
		this.call = runState.getMap().getIndex(call.getSourceInfo());
		this.subst = subst;
	}

	@Override
	public SourceInfo getSource(final ProgramMap map) {
		return map.getObject(this.call).getSourceInfo();
	}

	@Override
	public List<String> getLookupData(final ProgramMap map) {
		final Action<?> action = getAction(map);
		if (action == null) {
			return new ArrayList<>(0);
		} else {
			final List<String> result = new ArrayList<>(1);
			result.add(action.getSignature());
			return result;
		}
	}

	public Action<?> getAction(final ProgramMap map) {
		final ParsedObject get = map.getObject(this.call);
		return (get instanceof Action<?>) ? (Action<?>) get : null;
	}

	public Substitution getSubstitution() {
		return this.subst;
	}

	@Override
	public void execute(final RunState runState, final boolean reverse) throws GOALActionFailedException {
		// nothing to do here...
	}

	@Override
	public String getDescription(final RunState runState) {
		return "Called action with " + this.subst;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = this.call;
		result = prime * result + ((this.subst == null) ? 0 : this.subst.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || !(obj instanceof CallEvent)) {
			return false;
		}
		CallEvent other = (CallEvent) obj;
		if (this.call != other.call) {
			return false;
		}
		if (this.subst == null) {
			if (other.subst != null) {
				return false;
			}
		} else if (!this.subst.equals(other.subst)) {
			return false;
		}
		return true;
	}
}
