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
package goal.core.runtime.service.environmentport.environmentport.events;

import eis.iilang.Percept;

/**
 * event to indicate new percept. Coupled to the
 * {@link eis.AgentListener#handlePercept(String, eis.iilang.Percept)}
 */
public class NewPerceptEvent extends EnvironmentEvent {
	/** Auto-generated serial version UID */
	private static final long serialVersionUID = 6387717146943403919L;
	private final Percept percept;
	private final String agent;

	public NewPerceptEvent(String agent, Percept percept) {
		this.agent = agent;
		this.percept = percept;
	}

	/**
	 * @return The agent
	 */
	public String getAgent() {
		return this.agent;
	}

	/**
	 * @return The percept
	 */
	public Percept getPercept() {
		return this.percept;
	}
}