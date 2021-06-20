package goal.tools.history.events;

import java.util.ArrayList;
import java.util.List;

import goal.core.runtime.service.agent.RunState;
import goal.tools.errorhandling.exceptions.GOALActionFailedException;
import krTools.parser.ParsedObject;
import krTools.parser.SourceInfo;
import languageTools.program.ProgramMap;
import languageTools.program.agent.Module;
import languageTools.program.agent.Module.FocusMethod;

public class EnterEvent extends AbstractEvent {
	private final int source;

	public EnterEvent(final RunState runState, final Module module) {
		this.source = runState.getMap().getIndex(module.getDefinition());
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
		final Module exit = (Module) runState.getMap().getObject(this.source);
		final boolean focus = (exit.getFocusMethod() != FocusMethod.NONE);
		if (reverse) {
			if (focus) {
				runState.removeFocus(exit);
			}
			runState.exitModule(exit);
		} else {
			runState.enterModule(exit);
			if (focus) {
				runState.setFocus(exit, null);
			}
		}
	}

	@Override
	public String getDescription(final RunState runState) {
		return "Entered module";
	}

	@Override
	public int hashCode() {
		return this.source;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || !(obj instanceof EnterEvent)) {
			return false;
		}
		EnterEvent other = (EnterEvent) obj;
		return (this.source == other.source);
	}
}
