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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eis.EnvironmentInterfaceStandard;
import eis.exceptions.PerceiveException;
import eis.iilang.Action;
import eis.iilang.EnvironmentState;
import eis.iilang.Parameter;
import goal.core.runtime.service.environmentport.EnvironmentPort;

public class EnvironmentPortTest {
	private EnvironmentInterfaceStandard eis;
	private EnvironmentPort environmentPort;

	@Before
	public void setUp() throws Exception {
		this.eis = new MockEnvironment();
		this.environmentPort = new EnvironmentPort(this.eis, "dummyEnvironment", new HashMap<String, Parameter>(0));
		this.environmentPort.startPort();

	}

	/**
	 * wait (max 2 seconds) for env to reach some state.
	 *
	 * @param state
	 */
	private void waitForEnvState(EnvironmentState state) {
		int tries = 0;
		while (this.eis.getState() != state && tries++ < 20) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		assert (this.eis.getState() == state);
	}

	@After
	public void tearDown() throws Exception {
		this.environmentPort.shutDown();
	}

	// @Test
	public void testStartPortShutDown() throws Exception {
		// FIXME: Executing this will hang tear down. Message boxes that have
		// been deleted can still send messages but not receive them.
		// environmentPort.startPort();
		// TimeUnit.SECONDS.sleep(5);
		// environmentPort.shutDown();
	}

	@Test
	public void testStart() throws Exception {
		// FIXME: fails on commandline?
		// this.environmentPort.pause();
		// waitForEnvState(EnvironmentState.PAUSED);
		this.environmentPort.start();
		waitForEnvState(EnvironmentState.RUNNING);
	}

	@Test
	public void testPause() throws Exception {
		assertNotSame(EnvironmentState.PAUSED, this.eis.getState());
		this.environmentPort.pause();
		// FIXME: fails on commandline?
		// waitForEnvState(EnvironmentState.PAUSED);
	}

	@Test
	public void testKill() throws Exception {
		assertNotSame(EnvironmentState.KILLED, this.eis.getState());
		this.environmentPort.shutDown();
		waitForEnvState(EnvironmentState.KILLED);
	}

	@Test(expected = PerceiveException.class)
	public void testGetPerceptsNonExistingAgent() throws Exception {
		this.environmentPort.getPercepts("nonExistingAgent");
	}

	@Test
	public void testGetPercepts() throws Exception {
		this.environmentPort.registerAgent("existingAgent");
		this.environmentPort.associateEntity("existingAgent", "existingEntity");
		assertFalse(this.environmentPort.getPercepts("existingAgent").isEmpty());
	}

	@Test
	public void testGetReward() throws Exception {
		assertNotNull(this.environmentPort.getReward("nonExistingAgent"));
	}

	@Test
	public void testRegisterAgent() throws Exception {
		this.environmentPort.registerAgent("existingAgent");
		assertTrue(this.eis.getAgents().contains("existingAgent"));
	}

	@Test
	public void testAssociateEntity() throws Exception {
		assertFalse(this.eis.getFreeEntities().isEmpty());
		this.environmentPort.registerAgent("existingAgent");
		this.environmentPort.associateEntity("existingAgent", "existingEntity");
		assertTrue(this.eis.getFreeEntities().isEmpty());
	}

	@Test
	public void testPerformAction() throws Exception {
		this.environmentPort.registerAgent("existingAgent");
		this.environmentPort.associateEntity("existingAgent", "existingEntity");
		this.environmentPort.performAction("existingAgent", new Action("act"));
	}
}
