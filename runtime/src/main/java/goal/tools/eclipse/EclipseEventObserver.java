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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import eis.iilang.EnvironmentState;
import events.Channel;
import events.ExecutionEventListener;
import goal.core.agent.Agent;
import goal.core.runtime.RuntimeEvent;
import goal.core.runtime.RuntimeEventObserver;
import goal.core.runtime.RuntimeManager;
import goal.core.runtime.service.environmentport.EnvironmentPort;
import goal.tools.BreakpointManager;
import goal.tools.IDEGOALInterpreter;
import goal.tools.debugger.DebugEvent;
import goal.tools.debugger.IDEDebugger;
import goal.tools.eclipse.DebugCommand.Command;
import krTools.parser.SourceInfo;
import languageTools.program.agent.AgentId;

public class EclipseEventObserver implements RuntimeEventObserver {
	private final Map<AgentId, EclipseDebugObserver> observers;
	private final BreakpointManager mngr;
	private InputReaderWriter writer;
	private EnvironmentPort environment = null;

	public EclipseEventObserver(BreakpointManager mngr) {
		this.observers = new ConcurrentHashMap<>();
		this.mngr = mngr;
	}

	public void setWriter(final InputReaderWriter writer) {
		this.writer = writer;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void eventOccured(RuntimeManager<?, ?> observable, RuntimeEvent event) {
		switch (event.getType()) {
		case ENVIRONMENT_LAUNCHED:
			this.environment = (EnvironmentPort) event.getSource();
			this.writer.write(new DebugCommand(Command.ENV_CREATED, this.environment,
					this.environment.getEnvironmentState().name()));
			break;
		case ENVIRONMENT_RUNMODE_CHANGED:
			EnvironmentState state = this.environment.getEnvironmentState();
			if (state != EnvironmentState.INITIALIZING) {
				this.writer.write(new DebugCommand(Command.ENV_STATE, this.environment, state.name()));
			}
			break;
		case AGENT_IS_LOCAL_AND_READY:
			final Agent<IDEGOALInterpreter> agent = (Agent<IDEGOALInterpreter>) event.getSource();
			final EclipseDebugObserver debugobserver = new EclipseDebugObserver(agent, this.writer);
			debugobserver.subscribe();
			this.observers.put(agent.getId(), debugobserver);
			final IDEDebugger debugger = agent.getController().getDebugger();
			debugger.setBreakpoints(this.mngr.getBreakpoints());
			debugobserver.notifyBreakpointHit(
					new DebugEvent(debugger.getRunMode(), getClass().getSimpleName(), Channel.RUNMODE, null, null));
			break;
		default:
			break;
		}
	}

	public EclipseDebugObserver getObserver(final Agent<IDEGOALInterpreter> agent) {
		return this.observers.get(agent.getId());
	}

	public static class EclipseEventListener extends ExecutionEventListener {
		private final EclipseDebugObserver observer;

		public EclipseEventListener(final EclipseDebugObserver observer) {
			this.observer = observer;
		}

		@Override
		public void goalEvent(Channel channel, Object associateObject, SourceInfo associateSource, String message,
				Object... args) {
			this.observer.notifyBreakpointHit(new DebugEvent(null, getClass().getSimpleName(), channel, associateObject,
					associateSource, message, args));
		}
	}
}
