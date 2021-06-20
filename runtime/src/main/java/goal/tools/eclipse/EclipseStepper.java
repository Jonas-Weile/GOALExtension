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
package goal.tools.eclipse;

import goal.core.agent.Agent;
import goal.preferences.CorePreferences;
import goal.tools.IDEGOALInterpreter;
import goal.tools.debugger.DebugEvent;

public class EclipseStepper {
	public enum StepMode {
		INTO, OVER, OUT;
	}

	private final Agent<IDEGOALInterpreter> agent;
	private StepMode stepMode;
	private int stepLevel;

	public EclipseStepper(final Agent<IDEGOALInterpreter> agent) {
		this.agent = agent;
		reset();
	}

	protected void reset() {
		this.stepMode = null;
		this.stepLevel = 0;
	}

	public boolean processEvent(final DebugEvent event) {
		int current = this.agent.getController().getStackIndex();
		if (this.stepMode == StepMode.INTO || event.getChannel().getLevel() == 0) {
			// An into-step doesn't care about the stack level;
			// cycle-breakpoints should always be paused on by default,
			// though a user can disable this for specific breakpoints.
			switch (event.getChannel()) {
			case GOAL_ACHIEVED:
				return CorePreferences.getBreakOnGoalAchieved();
			default:
				return true;
			}
		} else if (this.stepMode == StepMode.OVER) {
			// An over-step should result in the same
			// or a lower lever in the stack.
			return (current <= this.stepLevel);
		} else if (this.stepMode == StepMode.OUT) {
			// An out-step should result in a lower level in the stack
			return (current < this.stepLevel);
		} else {
			// We are not stepping; fall through...
			return true;
		}
	}

	public void processCommand(final DebugCommand command) {
		switch (command.getCommand()) {
		case STEP:
			this.stepMode = StepMode.valueOf(command.getData());
			this.stepLevel = this.agent.getController().getStackIndex();
			break;
		case RUN:
			reset();
			break;
		default:
			break;
		}
	}
}
