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
import goal.core.agent.GOALInterpreter;
import goal.tools.adapt.Learner;
import goal.tools.debugger.Debugger;
import goal.tools.debugger.LoggingObserver;
import goal.tools.debugger.NOPDebugger;
import goal.tools.debugger.ObservableDebugger;
import goal.tools.errorhandling.exceptions.GOALLaunchFailureException;
import goal.tools.errorhandling.exceptions.GOALRunFailedException;
import goal.tools.profiler.Profiles;
import languageTools.program.mas.MASProgram;

/**
 * A single run of a {@link MASProgram}. This class can be used to launch, run
 * and terminate a MAS program once. The result of the run can be inspected by
 * setting a {@link ResultInspector}.
 * <p>
 * During the run the MAS will use agents running the {@link GOALInterpreter}
 * which will use the {@link NOPDebugger}.
 * </p>
 */
public class SingleRun extends AbstractRun<Debugger, GOALInterpreter<Debugger>> {
	/**
	 * Creates a single run for a MAS file.
	 *
	 * @param masFile A MAS file.
	 * @throws GOALRunFailedException
	 */
	public SingleRun(File masFile) throws GOALRunFailedException {
		super(masFile);
	}

	public SingleRun(MASProgram mas) {
		super(mas);
	}

	private class SingleRunAgentFactory extends AbstractAgentFactory<Debugger, GOALInterpreter<Debugger>> {
		public SingleRunAgentFactory() throws GOALLaunchFailureException {
			super(SingleRun.this.timeout);
		}

		@Override
		protected Debugger provideDebugger() {
			if (SingleRun.this.debuggerOutput) {
				ObservableDebugger observabledebugger = new ObservableDebugger(getAgentId(), null,
						getEnvironmentPort());
				observabledebugger.setKeepRunning(true);
				new LoggingObserver(observabledebugger).subscribe();
				return observabledebugger;
			} else {
				return new NOPDebugger(getAgentId());
			}
		}

		@Override
		protected GOALInterpreter<Debugger> provideController(Debugger debugger, Learner learner, Profiles profiles) {
			return new GOALInterpreter<>(getAgentDf(), getRegistry(), debugger, learner, profiles);
		}
	}

	@Override
	protected AgentFactory<Debugger, GOALInterpreter<Debugger>> buildAgentFactory() throws GOALLaunchFailureException {
		return new SingleRunAgentFactory();
	}
}