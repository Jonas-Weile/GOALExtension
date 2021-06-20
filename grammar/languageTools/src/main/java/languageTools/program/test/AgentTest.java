/**
 * The GOAL Grammar Tools. Copyright (C) 2014 Koen Hindriks.
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

package languageTools.program.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import krTools.parser.SourceInfo;
import languageTools.program.GoalParsedObject;

public class AgentTest extends GoalParsedObject {
	/**
	 * Base name of the agent under test.
	 */
	private final String agentName;
	/**
	 * The actions to perform in the test.
	 */
	private final List<TestAction> actions;

	/**
	 * @param agentName
	 *            base name of the agent
	 * @param actions
	 *            to run
	 */
	public AgentTest(String agentName, List<TestAction> actions, SourceInfo info) {
		super(info);
		this.agentName = agentName;
		this.actions = actions;
	}

	/**
	 * @param agentName
	 *            base name of the agent
	 */
	public AgentTest(String agentName, SourceInfo info) {
		this(agentName, new ArrayList<TestAction>(0), info);
	}

	/**
	 * @return base name of the agent under test
	 */
	public String getAgentName() {
		return this.agentName;
	}

	/**
	 * @return the test actions
	 */
	public List<TestAction> getActions() {
		return Collections.unmodifiableList(this.actions);
	}

	@Override
	public String toString() {
		return "AgentTest [agentName=" + this.agentName + ", actions=" + this.actions + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.agentName == null) ? 0 : this.agentName.hashCode());
		result = prime * result + ((this.actions == null) ? 0 : this.actions.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || !(obj instanceof AgentTest)) {
			return false;
		}
		AgentTest other = (AgentTest) obj;
		if (this.agentName == null) {
			if (other.agentName != null) {
				return false;
			}
		} else if (!this.agentName.equals(other.agentName)) {
			return false;
		}
		if (this.actions == null) {
			if (other.actions != null) {
				return false;
			}
		} else if (!this.actions.equals(other.actions)) {
			return false;
		}
		return true;
	}
}