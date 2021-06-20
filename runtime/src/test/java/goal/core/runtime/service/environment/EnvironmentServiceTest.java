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

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import goal.core.runtime.service.environment.events.EnvironmentPortRemovedEvent;
import goal.core.runtime.service.environment.events.EnvironmentServiceEvent;
import languageTools.analyzer.FileRegistry;
import languageTools.analyzer.mas.MASValidator;
import languageTools.program.mas.MASProgram;

public class EnvironmentServiceTest {
	private EnvironmentService environmentService;

	@Before
	public void setUp() throws Exception {
		String filename = "src/test/resources/goal/agents/fibonaciDummyEnv.mas2g";
		FileRegistry registry = new FileRegistry();
		MASValidator mas2g = new MASValidator(filename, registry);
		mas2g.validate();
		MASProgram program = mas2g.getProgram();
		mas2g.process();

		if (registry.hasAnyError()) {
			throw new Exception(registry.getAllErrors().toString());
		}

		this.environmentService = new EnvironmentService(program);
		this.environmentService.start();
	}

	@After
	public void tearDown() throws Exception {
		this.environmentService.shutDown();
		this.event = null;
	}

	private Object event;

	private class TestObserver implements EnvironmentServiceObserver {
		@Override
		public void environmentServiceEventOccured(EnvironmentService environmentService, EnvironmentServiceEvent evt) {
			EnvironmentServiceTest.this.event = evt;
		}
	}

	// @Test
	public void testAddEnvironmentPortMessageBox() throws Exception {
		// FIXME: This doesn't work. It is not possible to add environments
		// later on. Only way to discover new environment is to listen to
		// MessagagingEvent of environment MessageBoxes being created.
		// However at this point none is listening to them yet.

		// TestObserver observer = new TestObserver();
		// environmentService.addObserver(new TestObserver());
		//
		// MessageBoxId id = messaging.getNewUniqueID("secondDummyEnvironment",
		// Type.ENVIRONMENT);
		// MessageBox box = messaging.getNewMessageBox(id);
		//
		// assertTrue(observer.event instanceof EnvironmentPortAddedEvent);
		// assertNotNull(((EnvironmentPortAddedEvent)observer.event).getPort());
	}

	@Test
	public void testRemoveEnvironmentPortDirect() throws Exception {
		this.environmentService.addObserver(new TestObserver());
		this.environmentService.shutDown();

		assertTrue(this.event instanceof EnvironmentPortRemovedEvent);
		assertNotNull(((EnvironmentPortRemovedEvent) this.event).getPort());
	}

	@Test
	public void testGetEnvironmentPort() throws Exception {
		assertNotNull(this.environmentService.getEnvironmentPort());
	}
}
