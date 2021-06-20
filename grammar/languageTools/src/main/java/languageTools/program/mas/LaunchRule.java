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

import java.util.Collections;
import java.util.List;

/**
 * A launch rule, part of a MAS program, launches agents. Launch rules that are
 * conditional on the availability of entities in an environment only launch an
 * agent when an entity becomes available and connect the agent to that entity.
 */
public class LaunchRule {

	// Entity requirements
	private final Entity entity;
	// Launch instructions for launching agents
	private final List<LaunchInstruction> instructions;

	/**
	 * Creates a new launch rule.
	 *
	 * @param instructions
	 *            A list of launch instructions.
	 */
	public LaunchRule(Entity entity, List<LaunchInstruction> instructions) {
		this.entity = entity;
		this.instructions = instructions;
	}

	/**
	 * @return The entity requirements of this rule, or {@code null} if the rule
	 *         is unconditional.
	 */
	public Entity getEntity() {
		return this.entity;
	}

	/**
	 * @return The launch instructions of this rule.
	 */
	public List<LaunchInstruction> getInstructions() {
		return Collections.unmodifiableList(this.instructions);
	}

	/**
	 * 
	 * @return false iff all launch instructions in this rule have a maximum
	 *         number of agents specified. Returns true otherwise: then this
	 *         rule catches all entities of the given type.
	 */
	public boolean isCatchAll() {
		for (LaunchInstruction launchInstruction : getInstructions()) {
			if (launchInstruction.getMaxNumberOfAgentsToLaunch() == Integer.MAX_VALUE) {
				// this means the rule has no max
				return true;
			}
		}
		return false;
	}

	/**
	 * @return {@code true} if entity requirements have been specified for rule.
	 */
	public boolean isConditional() {
		return this.entity != null;
	}

	@Override
	public String toString() {
		StringBuilder string = new StringBuilder();
		string.append("launch ");
		if (this.instructions != null) {
			for (LaunchInstruction launch : this.instructions) {
				string.append(launch.toString());
			}
		}
		string.append(".");
		return string.toString();
	}
}
