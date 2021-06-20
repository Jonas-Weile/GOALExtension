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
package goal.tools;

import java.util.Collection;

import goal.core.agent.Agent;
import goal.core.agent.Controller;

/**
 * This is an interface for an inspector callback function for a result of a
 * SingleRun. To be used in {@link SingleRun#run(ResultInspector)}
 *
 * @param <CONTROLLER>
 *            The type of agent controller (interpreter).
 */
public interface ResultInspector<CONTROLLER extends Controller> {

	/**
	 * FIXME what is handleResult expected to do?
	 *
	 * This function is called after the agents terminated.
	 *
	 * @param agents
	 *            the final states of the agents in the run.
	 */
	public void handleResult(Collection<Agent<CONTROLLER>> agents);

}