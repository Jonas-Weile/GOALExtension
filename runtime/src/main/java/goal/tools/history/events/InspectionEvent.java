package goal.tools.history.events;

import java.util.LinkedList;
import java.util.List;

import events.NoEventGenerator;
import goal.core.runtime.service.agent.RunState;
import goal.tools.errorhandling.exceptions.GOALActionFailedException;
import krTools.parser.ParsedObject;
import krTools.parser.SourceInfo;
import languageTools.program.ProgramMap;
import languageTools.program.agent.msc.MentalLiteral;
import languageTools.program.agent.msc.MentalStateCondition;
import mentalState.MSCResult;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;

public class InspectionEvent extends AbstractEvent {
	private final int msc;

	public InspectionEvent(final RunState runState, final MentalStateCondition msc) {
		this.msc = runState.getMap().getIndex(msc.getSourceInfo());
	}

	@Override
	public SourceInfo getSource(final ProgramMap map) {
		return map.getObject(this.msc).getSourceInfo();
	}

	@Override
	public List<String> getLookupData(final ProgramMap map) {
		final List<String> result = new LinkedList<>();
		final MentalStateCondition msc = getMentalStateCondition(map);
		if (msc != null) {
			for (final MentalLiteral literal : msc.getAllLiterals()) {
				result.addAll(literal.getUsedSignatures());
			}
		}
		return result;
	}

	public MentalStateCondition getMentalStateCondition(final ProgramMap map) {
		final ParsedObject get = map.getObject(this.msc);
		if (get instanceof MentalStateCondition) {
			return (MentalStateCondition) get;
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
		final MentalStateCondition msc = (MentalStateCondition) runState.getMap().getObject(this.msc);
		try {
			final MSCResult result = runState.getMentalState().evaluate(msc, runState.getKRI().getSubstitution(null),
					new NoEventGenerator());
			return result.holds() ? ("Query held with " + result.getAnswers()) : "Query did not hold";
		} catch (MSTDatabaseException | MSTQueryException e) {
			return e.getMessage();
		}
	}

	@Override
	public int hashCode() {
		return this.msc;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || !(obj instanceof InspectionEvent)) {
			return false;
		}
		InspectionEvent other = (InspectionEvent) obj;
		return (this.msc == other.msc);
	}
}
