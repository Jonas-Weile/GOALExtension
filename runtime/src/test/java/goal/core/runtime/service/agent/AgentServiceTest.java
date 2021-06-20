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
package goal.core.runtime.service.agent;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import goal.core.agent.AbstractAgentFactory;
import goal.core.agent.AgentFactory;
import goal.core.agent.GOALInterpreter;
import goal.core.runtime.service.agent.events.AgentServiceEvent;
import goal.preferences.DebugPreferences;
import goal.tools.Run;
import goal.tools.adapt.Learner;
import goal.tools.debugger.Debugger;
import goal.tools.debugger.NOPDebugger;
import goal.tools.profiler.Profiles;
import languageTools.analyzer.FileRegistry;
import languageTools.analyzer.mas.MASValidator;
import languageTools.program.mas.MASProgram;

public class AgentServiceTest {
	private AgentService<Debugger, GOALInterpreter<Debugger>> runtimeService;

	@Before
	public void setUp() throws Exception {
		DebugPreferences.setDefault(Run.getDefaultPrefs());
		String filename = "src/test/resources/goal/agents/fibonaci4x.mas2g";
		FileRegistry registry = new FileRegistry();
		MASValidator mas2g = new MASValidator(filename, registry);
		mas2g.validate();
		final MASProgram program = mas2g.getProgram();
		mas2g.process();
		if (registry.hasAnyError()) {
			throw new Exception(registry.getAllErrors().toString());
		}

		AgentFactory<Debugger, GOALInterpreter<Debugger>> factory = new AbstractAgentFactory<Debugger, GOALInterpreter<Debugger>>(
				0) {
			@Override
			protected Debugger provideDebugger() {
				return new NOPDebugger(getAgentId());
			}

			@Override
			protected GOALInterpreter<Debugger> provideController(Debugger debugger, Learner learner,
					Profiles profiles) {
				return new GOALInterpreter<>(getAgentDf(), getRegistry(), debugger, learner, profiles);
			}
		};

		this.runtimeService = new AgentService<>(program, factory);
	}

	@After
	public void tearDown() throws Exception {
		this.runtimeService.awaitTermination();
		this.runtimeService.dispose();
	}

	private int agentsStarted = 0;

	@Test
	public void testStartStop() throws Exception {
		this.runtimeService.addObserver(new AgentServiceEventObserver() {
			@Override
			public void agentServiceEvent(AgentService<?, ?> rs, AgentServiceEvent evt) {
				AgentServiceTest.this.agentsStarted++;
			}
		});

		this.runtimeService.start();
		this.runtimeService.shutDown();
		this.runtimeService.awaitTermination();

		assertEquals(4, this.agentsStarted);
		assertEquals(4, this.runtimeService.getAgents().size());
		assertTrue(this.runtimeService.getAliveAgents().isEmpty());
	}
}