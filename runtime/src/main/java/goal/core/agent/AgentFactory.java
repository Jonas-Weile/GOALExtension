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
package goal.core.agent;

import goal.core.runtime.service.agent.AgentService;
import goal.core.runtime.service.environmentport.EnvironmentPort;
import goal.tools.debugger.Debugger;
import goal.tools.errorhandling.exceptions.GOALLaunchFailureException;
import goal.tools.profiler.Profiles;
import languageTools.program.agent.AgentId;
import languageTools.program.mas.AgentDefinition;
import languageTools.program.mas.LaunchInstruction;

/**
 * Constructs agents with a specific {@link GOALInterpreter} and
 * {@link Debugger}. For ease of use consider using the
 * {@link AbstractAgentFactory}.
 *
 * @see AgentService
 * @see AbstractAgentFactory
 *
 * @param <DEBUGGER> a subclass {@link Debugger}
 * @param <CONTROLLER> a subclass of {@link GOALInterpreter}
 */
public interface AgentFactory<DEBUGGER extends Debugger, CONTROLLER extends GOALInterpreter<DEBUGGER>> {

	/**
	 * Builds an Agent. Before throwing an exception upwards all clean up should
	 * have been done.
	 *
	 * @param launch        the respective launch instruction.
	 * @param program       the agent executes
	 * @param agentBaseName base name for the agent. This name dos not need to be
	 *                      unique.
	 * @param environment   in which the agent should be launched. May be null when
	 *                      no environment is available.
	 * @param profiles      the Profiles to use to store agent profiles
	 * @return a new agent.
	 * @throws GOALLaunchFailureException
	 */
	public Agent<CONTROLLER> build(LaunchInstruction launch, AgentDefinition program, String agentBaseName,
			EnvironmentPort environment, Profiles profiles) throws GOALLaunchFailureException;

	public void remove(AgentId agent);
}