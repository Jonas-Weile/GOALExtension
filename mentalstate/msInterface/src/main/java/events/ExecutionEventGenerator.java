package events;

import java.util.LinkedList;
import java.util.List;

import krTools.parser.SourceInfo;

/**
 * A source of agent execution ("breakpoint") events.
 */
public class ExecutionEventGenerator implements ExecutionEventGeneratorInterface {
	/**
	 * Ensures the order is fixed.
	 */
	private List<ExecutionEventListener> listeners = new LinkedList<>();

	@Override
	public void event(Channel channel, Object associateObject, SourceInfo associateSource, String message,
			Object... args) {
		for (ExecutionEventListener l : this.listeners.toArray(new ExecutionEventListener[this.listeners.size()])) {
			l.goalEvent(channel, associateObject, associateSource, message, args);
		}
	}

	@Override
	public void event(Channel channel, Object associateObject, SourceInfo associateSource) {
		for (ExecutionEventListener l : this.listeners.toArray(new ExecutionEventListener[this.listeners.size()])) {
			l.goalEvent(channel, associateObject, associateSource);
		}
	}

	@Override
	public void addListener(ExecutionEventListener l) {
		this.listeners.add(l);
	}

	@Override
	public void removeListener(ExecutionEventListener l) {
		this.listeners.remove(l);
	}

	@Override
	public void clearListeners() {
		this.listeners.clear();
	}
}
