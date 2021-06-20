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

/**
 * Provides and abstract representation of the capabilities of the agent in the
 * environment.
 *
 * An agent needs to be able to retrieve percepts and rewards from an
 * environment and an agent needs to be able to execute actions in the
 * environment.
 *
 * Implementing classes can provide this functionality as they see fit.
 */
public interface EnvironmentCapabilities {

	/**
	 * Get the reward that the environment provides to this agent.
	 *
	 * @return Double containing number between 0 and 1 (usually), or {@code null}
	 *         if the environment did not provide a reward.
	 * @throws EnvironmentInterfaceException
	 */
	public abstract Double getReward() throws EnvironmentInterfaceException;

	/**
	 * Sends a user-specified action to the environment in which it should be
	 * executed.
	 *
	 * @param action
	 *            the action to be executed in the environment
	 * @throws EnvironmentInterfaceException
	 */
	public abstract void performAction(Action action) throws EnvironmentInterfaceException;

	/**
	 * Collects percepts from the environment. When no percepts could be collected
	 * this method should return an empty set.
	 *
	 * @return the collection of percepts received from the environment
	 * @throws EnvironmentInterfaceException
	 */
	public abstract PerceptUpdate getPercepts() throws EnvironmentInterfaceException;

	/**
	 * Releases any resources held.
	 *
	 * @throws EnvironmentInterfaceException
	 */
	public abstract void dispose() throws EnvironmentInterfaceException;

}