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
package goal.core.executors;

import languageTools.program.mas.AgentDefinition;
import languageTools.program.mas.LaunchInstruction;

public class LaunchInstructionExecutor {

	/**
	 * Launch instruction of the form name = id | name = * | min = num | max = num.
	 */
	private final LaunchInstruction instruction;
	/**
	 * Counters for keeping track of the number of times the instruction has been
	 * applied, the number of the times it should each time be minimally applied (if
	 * possible), and the maximum number of times it should be applied (overall).
	 */
	private int count = 0;
	private final int nr;
	private final int max;
	/**
	 * Indicates how many agents should be created.
	 */
	private int applications = 0;

	/**
	 * Creates an executor for the launch instruction of a launch rule.
	 *
	 * @param instruction A launch instruction.
	 */
	public LaunchInstructionExecutor(LaunchInstruction instruction) {
		this.instruction = instruction;
		this.nr = instruction.getNumberOfAgentsToLaunch();
		this.max = instruction.getMaxNumberOfAgentsToLaunch();
	}

	/**
	 * Executes the launch instruction. Sets a local parameter that indicates how
	 * many agents should be created; use
	 *
	 * @param entityName Name of an entity available in environment (can be
	 *                   {@code null}).
	 * @param entityType Type of an entity available in environment (can be
	 *                   {@code null}).
	 * @return The agent definition associated with the launch instruction, or
	 *         {@code null} if the maximum number of applications of the launch
	 *         instruction has been reached.
	 */
	public AgentDefinition execute(String entityName, String entityType) {
		// Check if maximum number of applications has already been reached.
		if (this.count >= this.max) {
			return null;
		}

		// Compute how many times the rule should be applied this time.
		if (this.count + this.nr <= this.max) {
			this.applications = this.nr;
		} else {
			this.applications = this.max - this.count;
		}

		// Return agent definition associated with the instruction.
		return this.instruction.getAgentDf();
	}

	/**
	 * Provides the number of agents that should be created, if any, after a call to
	 * {@link #execute(String, String)}. Resets this number to zero after calling
	 * this method. If no agents should be created, the method returns 0. Also
	 * increases the count with the returned application number.
	 *
	 * @return The number of agents that should be created.
	 */
	public int getNr() {
		int applications = this.applications;
		this.applications = 0;
		this.count += applications;
		return applications;
	}

	/**
	 * @param entityName The name of an entity, if any
	 * @return
	 */
	public String getGivenName(String entityName) {
		return this.instruction.getGivenName(entityName, 0);
	}

	public LaunchInstruction getLaunchInstruction() {
		return this.instruction;
	}
}
