package events;

import krTools.parser.SourceInfo;
import languageTools.program.mas.AgentDefinition;

/**
 * Mechanism to collect events from the runtime and dispatches them to
 * listeners. This may be used to create debuggers, profilers, event tracers
 * etc. This replaces the old breakpoints from the debugger. This is the
 * interface only, {@link ExecutionEventGenerator} is the actual impl.
 *
 */
public interface ExecutionEventGeneratorInterface {
	/**
	 * <p>
	 * Informs the event generator that our thread has reached a strategic
	 * event/breakpoint. This method should be called <em>only</em> by the
	 * agent, never by an external thread. The call can block if one of our
	 * listeners blocks while handling this event (so to create a paused agent).
	 * <p>
	 * IMPORTANT: do NOT call this from inside a synchronized block if you want
	 * to support multi-threading.
	 * </p>
	 * <p>
	 * It is recommended to use past tense for breakpoints reporting an event
	 * that has happened, and to use future tense for events that are going to
	 * happen after the breakpoint.
	 * </p>
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
	 * @throws DebuggerKilledException
	 *             exception if someone called {@link #kill()} and the runMode
	 *             was set to {@link RunMode#KILLED}, or if the breakpoint halts
	 *             and is subsequently interrupted. This is because such an
	 *             interrupt is caused by an interrupted() call to the thread
	 *             which happens only when this agent has to die.
	 */
	public void event(Channel channel, Object associateObject,
			SourceInfo associateSource, String message, Object... args);

	public void event(Channel channel, Object associateObject, SourceInfo associateSource);

	/**
	 * Call this to receive events. You can subscribe only once.
	 *
	 * @param l
	 *            listener to add
	 */
	public void addListener(ExecutionEventListener l);

	/**
	 * Call this to stop receiving events.
	 *
	 * @param l
	 *            listener to remove
	 */
	public void removeListener(ExecutionEventListener l);

	/**
	 * Make all listeners stop receiving events.
	 */
	public void clearListeners();
}
