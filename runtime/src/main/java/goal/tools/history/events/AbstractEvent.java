package goal.tools.history.events;

import java.util.List;

import org.nustaq.serialization.FSTConfiguration;
import org.nustaq.serialization.FSTObjectInput;
import org.nustaq.serialization.FSTObjectOutput;

import goal.core.runtime.service.agent.RunState;
import goal.tools.errorhandling.exceptions.GOALActionFailedException;
import krTools.parser.SourceInfo;
import languageTools.program.ProgramMap;

public abstract class AbstractEvent implements Comparable<AbstractEvent> {
	@SuppressWarnings("unchecked")
	public static FSTConfiguration getSerialization() {
		final FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();
		conf.setForceSerializable(true);
		conf.getStreamCoderFactory().getInput().set(new FSTObjectInput(conf));
		conf.getStreamCoderFactory().getOutput().set(new FSTObjectOutput(conf));
		conf.registerClass(
				// ActionEvent/CallEvent/EntryEvent/ExitEvent/InspectionEvent
				ActionEvent.class, CallEvent.class, EnterEvent.class, LeaveEvent.class, InspectionEvent.class,
				// ModificationEvent > ModificationAction
				ModificationEvent.class, ModificationAction.class);
		return conf;
	}

	abstract public SourceInfo getSource(final ProgramMap map);

	abstract public List<String> getLookupData(final ProgramMap map);

	abstract public void execute(final RunState runState, final boolean reverse) throws GOALActionFailedException;

	abstract public String getDescription(final RunState runState);

	@Override
	abstract public boolean equals(final Object other);

	@Override
	abstract public int hashCode();

	@Override
	public int compareTo(final AbstractEvent o) {
		return 0; // FIXME?!
	}
}
