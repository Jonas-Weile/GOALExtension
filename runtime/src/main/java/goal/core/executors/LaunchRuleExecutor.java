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

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import goal.core.agent.Agent;
import goal.core.agent.AgentFactory;
import goal.core.agent.GOALInterpreter;
import goal.core.runtime.service.environmentport.EnvironmentPort;
import goal.tools.debugger.Debugger;
import goal.tools.errorhandling.exceptions.GOALLaunchFailureException;
import goal.tools.profiler.Profiles;
import languageTools.program.mas.AgentDefinition;
import languageTools.program.mas.Entity;
import languageTools.program.mas.LaunchInstruction;
import languageTools.program.mas.LaunchRule;

/**
 * Executor for a launch rule.
 */
public class LaunchRuleExecutor<DEBUGGER extends Debugger, CONTROLLER extends GOALInterpreter<DEBUGGER>> {
	/**
	 * The launch rule to be executed.
	 */
	private final LaunchRule rule;
	/**
	 * Executor for the launch instructions of the rule.
	 */
	private final List<LaunchInstructionExecutor> executors = new LinkedList<>();
	/**
	 * Factory for creating agents.
	 */
	private final AgentFactory<DEBUGGER, CONTROLLER> agentFactory;
	/**
	 * Indicates whether rule is applicable or not (any more).
	 */
	private boolean applicable = true;

	/**
	 * Executor for a launch rule.
	 *
	 * @param rule A launch rule of the form [when entity] launch instruction (,
	 *             instruction)*.
	 */
	public LaunchRuleExecutor(LaunchRule rule, AgentFactory<DEBUGGER, CONTROLLER> agentFactory) {
		this.rule = rule;
		this.agentFactory = agentFactory;

		// Create executors for launch instructions.
		for (LaunchInstruction instruction : rule.getInstructions()) {
			this.executors.add(new LaunchInstructionExecutor(instruction));
		}
	}

	/**
	 * Applies a launch rule if it is applicable.
	 *
	 * @param entityName Name of an entity available in environment (can be
	 *                   {@code null}).
	 * @param entityType Type of an entity available in environment (can be
	 *                   {@code null}).
	 * @return {@code true} if the rule was successfully applied, {@code false}
	 *         otherwise.
	 * @throws GOALLaunchFailureException If agent could not be built.
	 */
	public List<Agent<CONTROLLER>> apply(String entityName, String entityType, EnvironmentPort environment,
			Profiles profiles) throws GOALLaunchFailureException {
		List<Agent<CONTROLLER>> agents = new LinkedList<>();

		// Check whether rule is applicable.
		boolean applicable = this.applicable && match(entityName, entityType);
		if (applicable) {
			// Execute the rule's launch instruction(s).
			AgentDefinition agentDf;
			for (LaunchInstructionExecutor executor : this.executors) {
				agentDf = executor.execute(entityName, entityType);
				if (agentDf != null) {
					// Create required number of agents.
					agents.addAll(buildAgents(executor.getNr(), executor.getLaunchInstruction(), agentDf,
							executor.getGivenName(entityName), environment, profiles));
				}
			}

			// If the requirements of the rule were satisfied, i.e. the rule was
			// applicable
			// but no instruction returned an agent definition, the rule
			// applications must
			// have been exhausted (maximum number of applications must have
			// been reached).
			this.applicable = !agents.isEmpty();
		}

		return agents;
	}

	/**
	 * Checks whether entity requirements of the rule have been satisfied.
	 * Conditional launch rules require a non-null entity name.
	 *
	 * @param entityName Name of an entity available in environment (can be
	 *                   {@code null}).
	 * @param entityType Type of an entity available in environment (can be
	 *                   {@code null}).
	 * @return {@code true} if entity requirements are satisfied, {@code false}
	 *         otherwise. Returns {@code false} for conditional launch rule if
	 *         entity name is {@code null}.
	 */
	private boolean match(String entityName, String entityType) {
		// Get entity requirements of the form * | type = id | name = id, or
		// {@code null}.
		Entity entity = this.rule.getEntity();

		// Check whether rule is unconditional.
		boolean entityAvailable = (entityName != null);
		if (entity == null) {
			// Unconditional rule
			return !entityAvailable;
		} else {
			// Evaluate requirements of conditional rule.
			boolean nameOK = (entity.getName() == null) || (entity.getName().equals(entityName));
			boolean typeOK = (entity.getType() == null) || (entity.getType().equals(entityType));
			return entityAvailable && nameOK && typeOK;
		}
	}

	/**
	 * Creates a requested number of agents using the given agent definition.
	 *
	 * @param nr            The number of agents to create.
	 * @param launch        The respective launch instruction.
	 * @param agentDf       Agent definition used for creating the agent.
	 * @param agentBaseName A base name for naming agents.
	 * @param environment   An environment to connect the agent to.
	 *
	 * @return The list of agents created.
	 * @throws GOALLaunchFailureException If message box could not be created for
	 *                                    agent.
	 */
	private List<Agent<CONTROLLER>> buildAgents(int nr, LaunchInstruction launch, AgentDefinition agentDf,
			String agentBaseName, EnvironmentPort environment, Profiles profiles) throws GOALLaunchFailureException {
		List<Agent<CONTROLLER>> agents = new ArrayList<>(nr);
		for (int i = 0; i < nr; i++) {
			agents.add(this.agentFactory.build(launch, agentDf, agentBaseName, environment, profiles));
		}
		return agents;
	}

}