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
package goal.tools.debugger;

import goal.core.agent.GOALInterpreter;
import goal.core.runtime.RuntimeManager;
import goal.core.runtime.service.environmentport.EnvironmentPort;
import languageTools.program.agent.AgentId;

public class IDEDebugger extends ObservableDebugger {
	private final DebugSettingSynchronizer observer;

	/**
	 * @param id
	 *            the {@link AgentId} that this debugger controls.
	 * @param env
	 *            The current environment (if any), used when the 'new agents copy
	 *            environment run state' option is enabled.
	 */
	public IDEDebugger(AgentId id, RuntimeManager<?, ? extends GOALInterpreter<? extends SteppingDebugger>> manager,
			EnvironmentPort env) {
		super(id, manager, env);
		this.observer = new DebugSettingSynchronizer(this);
	}

	@Override
	protected RunMode getInitialRunMode() {
		return RunMode.STEPPING;
	}

	@Override
	public void reset() {
		setRunMode(RunMode.RUNNING);
	}

	@Override
	public void dispose() {
		this.observer.stop();
	}
}