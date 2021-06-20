package goal.tools.history.events;

import java.util.ArrayList;
import java.util.List;

import goal.core.runtime.service.agent.RunState;
import goal.tools.errorhandling.exceptions.GOALActionFailedException;
import krTools.language.DatabaseFormula;
import krTools.parser.SourceInfo;
import languageTools.program.ProgramMap;

public class ModificationEvent extends AbstractEvent {
	private final ModificationAction update;

	public ModificationEvent(final ModificationAction update) {
		this.update = update;
	}

	public ModificationAction getUpdate() {
		return this.update;
	}

	@Override
	public SourceInfo getSource(final ProgramMap map) {
		return this.update.getSourceInfo(map);
	}

	@Override
	public List<String> getLookupData(final ProgramMap map) {
		final List<String> result = new ArrayList<>(this.update.getAdded().size() + this.update.getRemoved().size());
		for (final DatabaseFormula add : this.update.getAdded()) {
			result.add(add.getSignature());
		}
		for (final DatabaseFormula rem : this.update.getRemoved()) {
			result.add(rem.getSignature());
		}
		return result;
	}

	@Override
	public void execute(final RunState runState, final boolean reverse) throws GOALActionFailedException {
		this.update.execute(runState, reverse);
	}

	@Override
	public String getDescription(final RunState runState) {
		return this.update.toString();
	}

	@Override
	public int hashCode() {
		return (this.update == null) ? 0 : this.update.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || !(obj instanceof ModificationEvent)) {
			return false;
		}
		ModificationEvent other = (ModificationEvent) obj;
		if (this.update == null) {
			if (other.update != null) {
				return false;
			}
		} else if (!this.update.equals(other.update)) {
			return false;
		}
		return true;
	}
}
