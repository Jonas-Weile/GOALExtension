
package goal.tools.history.events;

import java.util.ArrayList;
import java.util.List;

import goal.core.runtime.service.agent.RunState;
import goal.tools.errorhandling.exceptions.GOALActionFailedException;
import krTools.language.Substitution;
import krTools.parser.ParsedObject;
import krTools.parser.SourceInfo;
import languageTools.program.ProgramMap;
import languageTools.program.agent.Module;
import languageTools.program.agent.Module.FocusMethod;

public class LeaveEvent extends AbstractEvent {
	private final int source;
	private final Substitution parameters;

	public LeaveEvent(final RunState runState, final Module module, final Substitution subst) {
		this.source = runState.getMap().getIndex(module.getDefinition());
		this.parameters = subst;
	}

	@Override
	public SourceInfo getSource(final ProgramMap map) {
		return ((Module) map.getObject(this.source)).getDefinition();
	}

	@Override
	public List<String> getLookupData(final ProgramMap map) {
		final List<String> result = new ArrayList<>(1);
		final ParsedObject get = map.getObject(this.source);
		if (get instanceof Module) {
			result.add(((Module) get).getSignature());
		}
		return result;
	}

	@Override
	public void execute(final RunState runState, final boolean reverse) throws GOALActionFailedException {
		final Module enter = (Module) runState.getMap().getObject(this.source);
		final boolean focus = (enter.getFocusMethod() != FocusMethod.NONE);
		if (reverse) {
			runState.enterModule(enter);
			if (focus) {
				runState.setFocus(enter, null);
			}
		} else {
			if (focus) {
				runState.removeFocus(enter);
			}
			runState.exitModule(enter);
		}
		// TODO: use parameter context
	}

	@Override
	public String getDescription(final RunState runState) {
		return "Left module";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = this.source;
		result = prime * result + ((this.parameters == null) ? 0 : this.parameters.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || !(obj instanceof LeaveEvent)) {
			return false;
		}
		LeaveEvent other = (LeaveEvent) obj;
		if (this.parameters == null) {
			if (other.parameters != null) {
				return false;
			}
		} else if (!this.parameters.equals(other.parameters)) {
			return false;
		}
		return (this.source == other.source);
	}
}
