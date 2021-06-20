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

import java.io.File;

import goal.core.agent.AbstractAgentFactory;
import goal.core.agent.AgentFactory;
import goal.preferences.CorePreferences;
import goal.preferences.LoggingPreferences;
import goal.tools.adapt.Learner;
import goal.tools.debugger.IDEDebugger;
import goal.tools.debugger.LoggingObserver;
import goal.tools.errorhandling.exceptions.GOALLaunchFailureException;
import goal.tools.errorhandling.exceptions.GOALRunFailedException;
import goal.tools.profiler.Profiles;

public class DebugRun extends AbstractRun<IDEDebugger, IDEGOALInterpreter> {
	/**
	 * Creates a debug run for a MAS file.
	 *
	 * @param masFile A MAS file.
	 * @throws GOALRunFailedException
	 */
	public DebugRun(File masFile) throws GOALRunFailedException {
		super(masFile);
	}

	private class DebugRunAgentFactory extends AbstractAgentFactory<IDEDebugger, IDEGOALInterpreter> {
		public DebugRunAgentFactory() throws GOALLaunchFailureException {
			super(DebugRun.this.timeout);
		}

		@Override
		protected IDEDebugger provideDebugger() {
			IDEDebugger debugger = new IDEDebugger(getAgentId(), getManager(), getEnvironmentPort());
			if (DebugRun.this.debuggerOutput) {
				new LoggingObserver(debugger).subscribe();
			}
			return debugger;
		}

		@Override
		protected IDEGOALInterpreter provideController(IDEDebugger debugger, Learner learner, Profiles profiles) {
			IDEGOALInterpreter controller = new IDEGOALInterpreter(getAgentDf(), getRegistry(), debugger, learner,
					profiles);
			if (!CorePreferences.getRemoveKilledAgent() || LoggingPreferences.getEnableHistory()) {
				// FIXME: this should not be allowed anymore,
				// as each agent has to be cleaned up in its own thread!
				controller.keepDataOnTermination();
			}
			return controller;
		}
	}

	@Override
	protected AgentFactory<IDEDebugger, IDEGOALInterpreter> buildAgentFactory() throws GOALLaunchFailureException {
		return new DebugRunAgentFactory();
	}

	@Override
	public void cleanup() {
		// Do nothing; runtime is manually cleaned
	}
}