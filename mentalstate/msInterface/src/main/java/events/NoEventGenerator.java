package events;

import krTools.parser.SourceInfo;

/**
 * Stub for ExecutionEventGeneratorInterface that completely ignores the calls.
 * Used when queries are done to the agent's mental state for introspection,
 * which should not be charged on the agent.
 */
public class NoEventGenerator implements ExecutionEventGeneratorInterface {
	@Override
	public void event(Channel channel, Object associateObject, SourceInfo associateSource, String message,
			Object... args) {
	}

	@Override
	public void event(Channel channel, Object associateObject, SourceInfo associateSource) {
	}

	@Override
	public void addListener(ExecutionEventListener l) {
	}

	@Override
	public void removeListener(ExecutionEventListener l) {
	}

	@Override
	public void clearListeners() {
	}
}
