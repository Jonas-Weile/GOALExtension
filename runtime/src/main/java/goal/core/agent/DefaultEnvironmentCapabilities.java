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

import eis.PerceptUpdate;
import eis.exceptions.EnvironmentInterfaceException;
import eis.iilang.Action;
import goal.core.runtime.service.environmentport.EnvironmentPort;
import languageTools.program.agent.AgentId;

/**
 * Provides the Agents {@link EnvironmentCapabilities} through an
 * {@link EnvironmentPort}. The environment port is shared between multiple
 * agents and provides more functionality then needed by the agent. This hides
 * these details from the agent.
 */
public class DefaultEnvironmentCapabilities implements EnvironmentCapabilities {
	private final EnvironmentPort environment;
	private final AgentId id;

	/**
	 * Constructs the default environment capabilities.
	 *
	 * @param agentId         of the agent
	 * @param environmentPort used to talk to the environment
	 */
	public DefaultEnvironmentCapabilities(AgentId agentId, EnvironmentPort environmentPort) {
		this.id = agentId;
		this.environment = environmentPort;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see goal.core.agent.Capabilities#getReward()
	 */
	@Override
	public Double getReward() throws EnvironmentInterfaceException {
		return this.environment.getReward(this.id.toString());

	}

	/*
	 * (non-Javadoc)
	 *
	 * @see goal.core.agent.Capabilities#getPercepts()
	 */
	@Override
	public PerceptUpdate getPercepts() throws EnvironmentInterfaceException {
		return this.environment.getPercepts(this.id.toString());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see goal.core.agent.Capabilities#performAction(languageTools.program.agent
	 * .actions. UserSpecAction)
	 */
	@Override
	public void performAction(Action action) throws EnvironmentInterfaceException {
		this.environment.performAction(this.id.toString(), action);
	}

	@Override
	public void dispose() throws EnvironmentInterfaceException {
		this.environment.freeAgent(this.id.toString());
	}
}
