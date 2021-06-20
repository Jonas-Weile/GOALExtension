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
package goal.tools.eclipse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import eis.iilang.Parameter;
import goal.core.runtime.service.environmentport.EnvironmentPort;
import languageTools.program.agent.AgentId;

public class DebugCommand {
	private final static String PREFIX = "DC";
	private final static String DELIMITER = "|";

	public enum Command {
		// received
		PAUSE, RUN, STEP, BREAKS, STOP,
		// received and/or sent
		EVAL,
		// sent (1/2)
		RUNMODE, LOG, LAUNCHED, SUSPEND, INSERTED_BEL, DELETED_BEL, INSERTED_PERCEPT, DELETED_PERCEPT, INSERTED_MAIL, DELETED_MAIL,
		// sent (2/2)
		ADOPTED, DROPPED, ACHIEVED, FOCUS, DEFOCUS, RULE_EVALUATION, MODULE_ENTRY, MODULE_EXIT, EXECUTED, CLEAR,
		// environment
		ENV_CREATED, ENV_STATE, ENV_PAUSE, ENV_RUN,
		// history
		HISTORY_STATE, HISTORY_STEP, HISTORY_FORWARD, HISTORY_BACK,
		// explanation
		WHY_ACTION, WHY_NOT_ACTION;
	}

	private final Command command;
	private final AgentId agent;
	private final EnvironmentPort environment;
	private final List<String> data;

	public DebugCommand(final Command command, final List<String> data) {
		this.command = command;
		this.agent = null;
		this.environment = null;
		this.data = data;
	}

	public DebugCommand(final Command command, final String data) {
		this(command, new ArrayList<String>(1));
		if (!data.isEmpty()) {
			this.data.add(data);
		}
	}

	public DebugCommand(final Command command, final AgentId agent, final List<String> data) {
		this.command = command;
		this.agent = agent;
		this.environment = null;
		this.data = data;
	}

	public DebugCommand(final Command command, final AgentId agent, final String data) {
		this(command, agent, new ArrayList<String>(1));
		if (!data.isEmpty()) {
			this.data.add(data);
		}
	}

	public DebugCommand(final Command command, final AgentId agent) {
		this(command, agent, new ArrayList<String>(0));
	}

	public DebugCommand(final Command command, final EnvironmentPort environment, final List<String> data) {
		this.command = command;
		this.agent = null;
		this.environment = environment;
		this.data = data;
	}

	public DebugCommand(final Command command, final EnvironmentPort environment, final String data) {
		this(command, environment, new ArrayList<String>(1));
		if (!data.isEmpty()) {
			this.data.add(data);
		}
	}

	public DebugCommand(final Command command, final EnvironmentPort environment) {
		this(command, environment, new ArrayList<String>(0));
	}

	public Command getCommand() {
		return this.command;
	}

	public AgentId getAgent() {
		return this.agent;
	}

	public EnvironmentPort getEnvironment() {
		return this.environment;
	}

	public List<String> getAllData() {
		return Collections.unmodifiableList(this.data);
	}

	public String getData(int index) {
		return this.data.get(index);
	}

	public String getData() {
		if (this.data.isEmpty()) {
			return "";
		} else {
			return getData(0);
		}
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || !(obj instanceof DebugCommand)) {
			return false;
		} else {
			final DebugCommand other = (DebugCommand) obj;
			if (this.agent == null) {
				if (other.agent != null) {
					return false;
				}
			} else if (!this.agent.equals(other.agent)) {
				return false;
			}
			if (this.environment == null) {
				if (other.environment != null) {
					return false;
				}
			} else if (!this.environment.equals(this.environment)) {
				return false;
			} else if (this.command != other.command) {
				return false;
			} else if (this.data == null) {
				if (other.data != null) {
					return false;
				}
			} else if (!this.data.equals(other.data)) {
				return false;
			}
			return true;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.agent == null) ? 0 : this.agent.hashCode());
		result = prime * result + ((this.environment == null) ? 0 : this.environment.hashCode());
		result = prime * result + ((this.command == null) ? 0 : this.command.hashCode());
		result = prime * result + ((this.data == null) ? 0 : this.data.hashCode());
		return result;
	}

	@Override
	public String toString() {
		final StringBuffer buffer = new StringBuffer(PREFIX);
		final String agentName = (this.agent == null) ? "" : this.agent.toString();
		final String agent = agentName.replace('\n', ' ').replace(DELIMITER, "\\" + DELIMITER);
		final String envName = (this.environment == null) ? "" : this.environment.toString();
		final String environment = envName.replace('\n', ' ').replace(DELIMITER, "\\" + DELIMITER);
		buffer.append(DELIMITER).append(this.command.name()).append(DELIMITER).append(agent).append(DELIMITER)
				.append(environment).append(DELIMITER).append(this.data.size());
		for (final String data : this.data) {
			final String d = data.replace("\n", " --lb-- ").replace(DELIMITER, "\\" + DELIMITER);
			buffer.append(DELIMITER).append(d);
		}
		return buffer.toString();
	}

	public static DebugCommand fromString(final String string) {
		if (string.startsWith(PREFIX)) {
			final String[] s = string.split("(?<!\\\\)\\" + DELIMITER);
			if (s.length >= 5) {
				final Command command = Command.valueOf(s[1]);
				final AgentId agent = s[2].isEmpty() ? null : new AgentId(s[2].replace("\\" + DELIMITER, DELIMITER));
				final EnvironmentPort environment = s[3].isEmpty() ? null
						: new StubEnvironment(s[3].replace("\\" + DELIMITER, DELIMITER));
				final int size = Integer.parseInt(s[4]);
				final List<String> data = new ArrayList<>(size);
				for (int i = 5; i < (size + 5); i++) {
					data.add(s[i].replace("\\" + DELIMITER, DELIMITER).replace("--lb--", "\n"));
				}
				if (agent != null) {
					return new DebugCommand(command, agent, data);
				} else if (environment != null) {
					return new DebugCommand(command, environment, data);
				} else {
					return new DebugCommand(command, data);
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	public static class StubEnvironment extends EnvironmentPort {
		public StubEnvironment(String name) {
			super(null, name, new HashMap<String, Parameter>(0));
		}
	}
}
