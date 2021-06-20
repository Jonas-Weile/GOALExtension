/**
 * The GOAL Runtime Environment. Copyright (C) 2015 Koen Hindriks.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package goal.tools.debugger;

import events.Channel;
import goal.tools.debugger.SteppingDebugger.RunMode;
import krTools.parser.SourceInfo;

/**
 * Stores information about a (debug) event.
 */
public class DebugEvent {
	private final RunMode mode;
	private final String source; // name of the source that generated the event
	/**
	 * The channel for which this event was created.
	 */
	private final Channel channel;
	/**
	 * The object (or an instance of the object) referred to in the breakpoint.
	 */
	private final Object associatedObject;
	private final SourceInfo associatedSource;
	private final String rawMsg;
	private final Object[] rawArgs;

	/**
	 * Creates a debug event for some channel, with some associated object.
	 *
	 * @param mode
	 *            The run mode of the debugger that generated the event.
	 * @param source
	 *            The source (typically a debugger) that created the event
	 * @param message
	 *            Some description of the event. Usually a breakpoint message.
	 * @param channel
	 *            The channel on which the event is published.
	 * @param association
	 *            The object to associate with this {@link DebugEvent}. May be null
	 *            if the event is not associated with any object.
	 */
	public DebugEvent(RunMode mode, String source, Channel channel, Object associatedObject,
			SourceInfo associatedSource, String message, Object... args) {
		this.mode = mode;
		this.source = source;
		this.channel = channel;
		this.associatedObject = associatedObject;
		this.associatedSource = associatedSource;
		this.rawMsg = message;
		this.rawArgs = args;
	}

	public DebugEvent(RunMode mode, String source, Channel channel, Object associatedObject,
			SourceInfo associatedSource) {
		this(mode, source, channel, associatedObject, associatedSource, null);
	}

	/**
	 * @return The run mode as reported by the event.
	 */
	public RunMode getRunMode() {
		return this.mode;
	}

	public String getSource() {
		return this.source;
	}

	public String getRawMessage() {
		return this.rawMsg;
	}

	public Object[] getRawArguments() {
		return this.rawArgs;
	}

	/**
	 * @return The channel on which this event was published
	 */
	public Channel getChannel() {
		return this.channel;
	}

	/**
	 * @return The object associated with this {@link DebugEvent}. May be null.
	 */
	public Object getAssociatedObject() {
		return this.associatedObject;
	}

	public SourceInfo getAssociatedSource() {
		return this.associatedSource;
	}

	@Override
	public String toString() {
		String returned = "[" + getSource() + "] ";
		if (this.rawMsg == null || this.rawMsg.isEmpty()) {
			returned += getAssociatedObject().toString();
		} else if (this.rawArgs == null || this.rawArgs.length == 0) {
			returned += this.rawMsg;
		} else {
			returned += String.format(this.rawMsg, this.rawArgs);
		}
		return returned;
	}
}