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

import goal.core.agent.Agent;
import goal.tools.errorhandling.exceptions.GOALRuntimeErrorException;

/**
 * This exception is thrown when an {@link Agent} died. Or more specifically,
 * when the Debugger of that agent detects that {@link Debugger#kill()} has been
 * called. Dee {@link Debugger}
 */
public class DebuggerKilledException extends GOALRuntimeErrorException {
	/** Generated serialVersionUID */
	private static final long serialVersionUID = 5945016622339822624L;

	public DebuggerKilledException() {
		super("Debugger terminated the agent");
	}

	public DebuggerKilledException(String string, Exception e) {
		super(string, e);
	}
}