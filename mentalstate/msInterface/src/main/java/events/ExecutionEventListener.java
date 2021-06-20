package events;

import krTools.parser.SourceInfo;
import languageTools.program.mas.AgentDefinition;

/**
 * Listener for GOAL execution events (previously debug breakpoints but also
 * usable for tracing an agent, profiling etc).
 *
 */
public abstract class ExecutionEventListener {
	/**
	 * called when an event occured.
	 *
	 * @param channel
	 *            channel on which to the breakpoint message is reported. Also
	 *            see class {@link Channel}.
	 * @param associate
	 *            The object being associated with the breakpoint. May be null
	 *            if the breakpoint is not associated with any object. See
	 *            {@link AgentDefinition#getBreakpointObjects()} for which
	 *            {@link IParsedObject}s the user can put a breakpoint on.
	 * @param message
	 *            is a user-readable message that describes the breakpoint
	 *            event.
	 * @param args
	 *            Any additional arguments to embed in the message
	 */
	public abstract void goalEvent(Channel channel, Object associateObject, SourceInfo associateSource, String message,
			Object... args);

	public void goalEvent(Channel channel, Object associateObject, SourceInfo associateSource) {
		goalEvent(channel, associateObject, associateSource, null);
	}
}
