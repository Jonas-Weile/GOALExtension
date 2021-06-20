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

package languageTools.program.mas;

import java.util.Map;

/**
 * A launch (instruction) is an instruction, part of a launch rule, in a MAS
 * file to launch an agent.
 */
public class LaunchInstruction {
	/**
	 * The agent name whose definition is used for launching an agent.
	 */
	private final String agentName;
	/**
	 * The name given to the agent when it is launched; three options:
	 * <ul>
	 * <li>The name of the entity is used</li>
	 * <li>The name specified in the launch instruction is used</li>
	 * <li>The agent name of the agent definition is used</li>
	 * </ul>
	 */
	private String givenName = null;
	/**
	 * The agent definition used for launching an agent.
	 */
	private AgentDefinition agentDf = null;
	/**
	 * The requested number of agents that should be launched when the instruction
	 * is executed. Default is 1.
	 */
	private int numberOfAgentsToLaunch = 1;
	/**
	 * The maximum number of agents that should be launched by repeated number of
	 * times this instruction is executed. By default, the maximum is set to
	 * Integer.MAX_VALUE.
	 */
	private int maxNumberOfAgentsToLaunch = Integer.MAX_VALUE;
	// LEARNING
	private double alpha = 0.9;
	private double gamma = 0.9;
	private double epsilon = 0.1;
	private double decay = 0.05;

	/**
	 * Creates a launch instruction.
	 *
	 * @param agentName The agent name whose definition will be used by this launch
	 *                  instruction.
	 */
	public LaunchInstruction(String agentName) {
		this.agentName = agentName;
	}

	/**
	 * @return The agent name of the definition that is used to launch an agent.
	 */
	public String getAgentName() {
		return this.agentName;
	}

	/**
	 * Adds an agent definition that will be used for launching an actual agent.
	 *
	 * @param agentDf An agent definition.
	 */
	public void addAgentDf(AgentDefinition agentDf) {
		this.agentDf = agentDf;
	}

	/**
	 * @return The agent definition associated with this launch instruction, if any,
	 *         {@code null} otherwise.
	 */
	public AgentDefinition getAgentDf() {
		return this.agentDf;
	}

	public void addConstraint(Map.Entry<String, Object> constraint) {
		switch (constraint.getKey()) {
		case "name":
			this.givenName = (String) constraint.getValue();
			break;
		case "nr":
			this.numberOfAgentsToLaunch = (Integer) constraint.getValue();
			break;
		case "max":
			this.maxNumberOfAgentsToLaunch = (Integer) constraint.getValue();
			break;
		case "alpha":
			this.alpha = (Double) constraint.getValue();
			break;
		case "gamma":
			this.gamma = (Double) constraint.getValue();
			break;
		case "epsilon":
			this.epsilon = (Double) constraint.getValue();
			break;
		case "decay":
			this.decay = (Double) constraint.getValue();
			break;
		}
	}

	/**
	 * Creates a given name for agent, derived from either the name of the agent
	 * definition, the entity the agent is connected to or a given name, and the
	 * number of applications of the launch instruction.
	 *
	 * @param entityName   The name of the entity that this agent is connected to,
	 *                     if any. Assumes that entity names are unique.
	 * @param applications The number of times this instruction has been applied.
	 *                     Used to differentiate agents when this instruction is
	 *                     applied more than once.
	 * @return The name that should be given to the agent that is launched.
	 *
	 *         TODO: How to handle case where environments re-use same name for
	 *         different entities?
	 */
	public String getGivenName(String entityName, int applications) {
		String name;

		if (this.givenName == null) {
			name = this.agentName;
		} else if (this.givenName.equals("*")) {
			name = entityName;
		} else {
			name = this.givenName + (applications == 0 ? "" : applications);
		}

		return name;
	}

	/**
	 * @return The requested number of agents that this instruction should launch
	 *         each time that it is applied.
	 */
	public int getNumberOfAgentsToLaunch() {
		return this.numberOfAgentsToLaunch;
	}

	/**
	 * @return The maximum number of agents that this instruction should launch.
	 *         Default is Integer.MAX_VALUE.
	 */
	public int getMaxNumberOfAgentsToLaunch() {
		return this.maxNumberOfAgentsToLaunch;
	}

	public double getAlpha() {
		return this.alpha;
	}

	public double getGamma() {
		return this.gamma;
	}

	public double getEpsilon() {
		return this.epsilon;
	}

	public double getDecay() {
		return this.decay;
	}

	/**
	 * @return String representation of this Launch instruction.
	 */
	@Override
	public String toString() {
		return this.agentName; // HACK 3626
		// StringBuilder string = new StringBuilder();
		// if (!this.givenName.isEmpty()) {
		// string.append(this.givenName + ":");
		// }
		// string.append(this.agentName);
		// if (this.numberOfAgentsToLaunch > 1) {
		// string.append("[" + this.numberOfAgentsToLaunch + "]");
		// }
		// return string.toString();
	}
}
