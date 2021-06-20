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
import krTools.parser.SourceInfo;
import languageTools.program.agent.AgentId;

public class NOPDebugger implements Debugger {

	private final String id;
	private boolean killed = false;

	public NOPDebugger(AgentId id) {
		this(id.toString());
	}

	public NOPDebugger(String id) {
		this.id = id;
	}

	@Override
	public void breakpoint(Channel channel, Object associateObject, SourceInfo associateSource, String message,
			Object... args) {
		if (this.killed) {
			throw new DebuggerKilledException();
		}
	}

	@Override
	public String getName() {
		return this.id;
	}

	@Override
	public void kill() {
		this.killed = true;
	}

	@Override
	public void reset() {
		this.killed = false;
	}

	@Override
	public void dispose() {
	}

}