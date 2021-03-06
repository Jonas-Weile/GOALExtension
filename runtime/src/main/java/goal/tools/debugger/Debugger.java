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
import goal.core.agent.Controller;
import goal.tools.debugger.SteppingDebugger.RunMode;
import krTools.parser.SourceInfo;
import languageTools.program.mas.AgentDefinition;

public interface Debugger {
	/**
	 * <p>
	 * Informs the debugger that the thread under debug has reached a
	 * breakpoint. This method should be called <em>only</em> by the agent,
	 * never by an external thread. The call blocks if the breakpoint is in the
	 * {@link #pausingChannels}. It unblocks only after someone calls the debug
	 * control functions such as {@link #run()}. <br>
	 * If we notice the runmode is {@link RunMode#KILLED} (this is checked in
	 * {@link #breakpoint(Channel, Object, SourceInfo, String, Object...)},
	 * {@link DebuggerKilledException} is thrown attempting to kill the agent.
	 * Therefore, <em>call breakpoint at places where it is safe to get killed
	 * (eg when temporarily opened files have already properly been closed).
	 * </em>
	 * </p>
	 * *
	 * <p>
	 * Usage of breakpoint():<br>
	 * A call to breakpoint() may be inserted BEFORE the agent action takes
	 * place, so the debugger can indicate what the system is going to do, as
	 * well as AFTER the agent action has taken place. Doing so enables a
	 * programmer to see what is scheduled to happen BEFORE the action occurs,
	 * and to check what has happened after the action has been performed in
	 * order to identify any problems due to the action and in order to
	 * facilitate an effective analysis (this allows a programmer to check its
	 * expectations).
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
	public abstract void breakpoint(Channel channel, Object associateObject, SourceInfo associateSource, String message,
			Object... args);

	/**
	 * Obtains the name of this {@link Debugger}. If this {@link Debugger} is
	 * debugging an agent, it should be set the same name as the agent so that
	 * we can offer the user some consistent feedback.
	 *
	 * @return The name of this {@link Debugger}.
	 */
	public abstract String getName();

	/**
	 * Once called the next call to
	 * {@link Debugger#breakpoint(Channel, Object, String, Object...)} should
	 * result in a {@link DebuggerKilledException}. Such controlled exit
	 * mechanism is the recommended way to exit a thread.
	 *
	 */
	public abstract void kill();

	/**
	 * Resets the internal state of the Debugger. Used in conjuction with
	 * {@link Controller#reset()}.
	 */
	public abstract void reset();

	/**
	 * Called when the agent using the debugger is disposed of. A debugger
	 * should release all resources and listeners.
	 */
	public abstract void dispose();

}