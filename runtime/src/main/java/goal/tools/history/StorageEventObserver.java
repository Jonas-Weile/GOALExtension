package goal.tools.history;

import java.util.LinkedHashMap;
import java.util.Map;

import goal.core.agent.Agent;
import goal.core.agent.GOALInterpreter;
import goal.core.runtime.RuntimeEvent;
import goal.core.runtime.RuntimeEventObserver;
import goal.core.runtime.RuntimeManager;
import goal.tools.errorhandling.Warning;
import languageTools.program.agent.AgentId;

/**
 * Observer that creates a {@link EventStorage} and associated
 * {@link EventStorageObserver} when neeeded.
 */
public class StorageEventObserver implements RuntimeEventObserver {
	private static final Map<AgentId, EventStorage> storages = new LinkedHashMap<>();

	public StorageEventObserver() {
	}

	@SuppressWarnings("unchecked")
	@Override
	public void eventOccured(final RuntimeManager<?, ?> observable, final RuntimeEvent event) {
		switch (event.getType()) {
		case MAS_BORN:
			storages.clear();
			break;
		case AGENT_IS_LOCAL_AND_READY:
			final Agent<GOALInterpreter<?>> agent = (Agent<GOALInterpreter<?>>) event.getSource();
			final EventStorage storage = new EventStorage(agent.getId());
			storages.put(agent.getId(), storage);
			final EventStorageObserver observer = new EventStorageObserver(storage, agent);
			observer.subscribe();
			break;
		case MAS_DIED:
			for (final EventStorage history : storages.values()) {
				try {
					history.finish(true);
				} catch (final InterruptedException e) {
					new Warning("unclean shutdown of agent history", e).emit();
				}
			}
		default:
			break;
		}
	}

	public static EventStorage getHistory(AgentId agent) {
		return storages.get(agent);
	}
}
