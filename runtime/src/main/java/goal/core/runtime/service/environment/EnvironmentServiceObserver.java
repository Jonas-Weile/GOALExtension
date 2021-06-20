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
package goal.core.runtime.service.environment;

import goal.core.runtime.service.environment.events.EnvironmentServiceEvent;

/**
 * observer for EnvironmentService
 */
public interface EnvironmentServiceObserver {
	/**
	 * Called when an event occurs
	 *
	 * @param environmentService
	 *            the {@link EnvironmentService}
	 * @param evt
	 *            the {@link EnvironmentServiceEvent}
	 */
	public void environmentServiceEventOccured(EnvironmentService environmentService, EnvironmentServiceEvent evt);

}