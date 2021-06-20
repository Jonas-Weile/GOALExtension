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
import eis.iilang.Action;
import goal.tools.errorhandling.Resources;
import goal.tools.errorhandling.WarningStrings;

/**
 * NOP implementation of {@link EnvironmentCapabilities}.
 */
public class NoEnvironmentCapabilities implements EnvironmentCapabilities {
	@Override
	public Double getReward() {
		return 0.0;
	}

	@Override
	public PerceptUpdate getPercepts() {
		return new PerceptUpdate();
	}

	@Override
	public void performAction(Action action) {
		// No environment is attached.
		throw new IllegalStateException(
				String.format(Resources.get(WarningStrings.FAILED_ACTION_AGENT_NOT_ATTACHED), action.toProlog()));
	}

	@Override
	public void dispose() {
		// Does nothing.
	}
}