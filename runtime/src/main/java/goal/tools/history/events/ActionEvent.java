package goal.tools.history.events;

import java.util.ArrayList;
import java.util.List;

import goal.core.runtime.service.agent.RunState;
import goal.tools.errorhandling.exceptions.GOALActionFailedException;
import krTools.language.Term;
import krTools.parser.ParsedObject;
import krTools.parser.SourceInfo;
import languageTools.program.ProgramMap;
import languageTools.program.agent.Module;
import languageTools.program.agent.actions.Action;
import languageTools.program.agent.actions.ModuleCallAction;

public class ActionEvent extends AbstractEvent {
	private final int action;

	public ActionEvent(final RunState runState, final Action<?> action) {
		this.action = runState.getMap().getIndex(action.getSourceInfo());
	}

	@Override
	public SourceInfo getSource(final ProgramMap map) {
		return map.getObject(this.action).getSourceInfo();
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
		final ParsedObject get = map.getObject(this.action);
		if (get instanceof Action<?>) {
			return (Action<?>) get;
		} else if (get instanceof Module) {
			return new ModuleCallAction((Module) get, new ArrayList<Term>(0), get.getSourceInfo());
		} else {
			return null;
		}
	}

	@Override
	public void execute(final RunState runState, final boolean reverse) throws GOALActionFailedException {
		// nothing to do here...
	}

	@Override
	public String getDescription(final RunState runState) {
		return "Action executed";
	}

	@Override
	public int hashCode() {
		return this.action;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || !(obj instanceof ActionEvent)) {
			return false;
		}
		ActionEvent other = (ActionEvent) obj;
		return (this.action == other.action);
	}
}
