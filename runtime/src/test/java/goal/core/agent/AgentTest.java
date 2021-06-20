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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Before;
import org.junit.Test;

import goal.preferences.DebugPreferences;
import goal.tools.Run;
import goal.tools.adapt.FileLearner;
import goal.tools.debugger.NOPDebugger;
import goal.tools.profiler.Profiles;
import languageTools.analyzer.FileRegistry;
import languageTools.analyzer.mas.MASValidator;
import languageTools.program.agent.AgentId;
import languageTools.program.mas.AgentDefinition;
import languageTools.program.mas.LaunchInstruction;
import languageTools.program.mas.MASProgram;

public class AgentTest {
	private GOALInterpreter<NOPDebugger> controller;
	private Profiles profiles = mock(Profiles.class);

	@Before
	public void setUp() throws Exception {
		DebugPreferences.setDefault(Run.getDefaultPrefs());
		String filename = "src/test/resources/goal/agents/fibonaci.mas2g";
		FileRegistry registry = new FileRegistry();
		MASValidator mas2g = new MASValidator(filename, registry);
		mas2g.validate();
		MASProgram program = mas2g.getProgram();
		mas2g.process();

		if (registry.hasAnyError()) {
			throw new Exception(registry.getAllErrors().toString());
		}

		// Assumes a single agent has been defined.
		AgentId agentId = new AgentId(program.getAgentNames().iterator().next());
		AgentDefinition agentDf = program.getAgentDefinition(agentId.toString());

		ExecutorService pool = Executors.newSingleThreadExecutor();
		AgentRegistry<GOALInterpreter<NOPDebugger>> agents = new AgentRegistry<>(null);
		this.controller = new GOALInterpreter<>(agentDf, agents, new NOPDebugger(agentId),
				FileLearner.createFileLearner(new LaunchInstruction(agentId.toString()), agentDf), this.profiles);
		Agent<GOALInterpreter<NOPDebugger>> agent = new Agent<>(agentId, new NoEnvironmentCapabilities(),
				new NoLoggingCapabilities(), this.controller, pool, 0);
		agents.register(agent);
	}

	@Test
	public void testStart() throws Exception {
		assertFalse(this.controller.isRunning());
		assertTrue(this.controller.isTerminated());
		this.controller.run();
		assertTrue(this.controller.isRunning());
		assertFalse(this.controller.isTerminated());
		this.controller.awaitTermination();
		assertTrue(this.controller.isTerminated());
		assertFalse(this.controller.isRunning());
	}

	@Test
	public void testStartStop() throws Exception {
		assertFalse(this.controller.isRunning());
		assertTrue(this.controller.isTerminated());
		this.controller.run();
		assertTrue(this.controller.isRunning());
		assertFalse(this.controller.isTerminated());
		this.controller.terminate();
		this.controller.awaitTermination();
		assertFalse(this.controller.isRunning());
		assertTrue(this.controller.isTerminated());
	}

	@Test
	public void testIsRunningAfterStop() throws Exception {
		assertFalse(this.controller.isRunning());
		assertTrue(this.controller.isTerminated());
		this.controller.run();
		assertTrue(this.controller.isRunning());
		assertFalse(this.controller.isTerminated());
		this.controller.terminate();
		assertFalse(this.controller.isRunning());
		assertFalse(this.controller.isTerminated());
	}

	@Test
	public void testReset() throws Exception {
		assertFalse(this.controller.isRunning());
		assertTrue(this.controller.isTerminated());
		this.controller.run();
		assertTrue(this.controller.isRunning());
		assertFalse(this.controller.isTerminated());
		this.controller.reset();
		assertTrue(this.controller.isRunning());
		assertFalse(this.controller.isTerminated());
		this.controller.terminate();
		this.controller.awaitTermination();
		assertFalse(this.controller.isRunning());
		assertTrue(this.controller.isTerminated());
	}
}