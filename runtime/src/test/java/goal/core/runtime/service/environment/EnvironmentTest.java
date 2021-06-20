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

import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import eis.EIDefaultImpl;
import eis.EnvironmentInterfaceStandard;
import eis.PerceptUpdate;
import eis.exceptions.ActException;
import eis.exceptions.NoEnvironmentException;
import eis.exceptions.PerceiveException;
import eis.iilang.Action;
import eis.iilang.Parameter;
import goal.core.runtime.service.environmentport.EnvironmentPort;

public class EnvironmentTest {
	private final EnvironmentInterfaceStandard eis = new EIDefaultImpl() {
		private static final long serialVersionUID = 8942811457865949545L;

		@Override
		public String requiredVersion() {
			return null;
		}

		@Override
		protected void performEntityAction(Action arg1, String arg0) throws ActException {
		}

		@Override
		protected boolean isSupportedByType(Action arg0, String arg1) {
			return false;
		}

		@Override
		protected boolean isSupportedByEnvironment(Action arg0) {
			return false;
		}

		@Override
		protected boolean isSupportedByEntity(Action arg0, String arg1) {
			return false;
		}

		@Override
		protected PerceptUpdate getPerceptsForEntity(String arg0) throws PerceiveException, NoEnvironmentException {
			return null;
		}
	};

	private EnvironmentPort environment;

	@Before
	public void setUp() throws Exception {
		this.environment = new EnvironmentPort(this.eis, "dummyEnvironment", new HashMap<String, Parameter>(0));
	}

	@After
	public void tearDown() throws Exception {
		this.environment.shutDown();
	}

	@Test(timeout = 5000)
	public void testShutDown() throws Exception {
		this.environment.shutDown();
	}

	@Test(timeout = 5000)
	public void testDelayedShutDown() throws Exception {
		TimeUnit.SECONDS.sleep(1);
		this.environment.shutDown();
	}

	@Test
	public void testGetMessageBoxId() {
		assertFalse(this.environment.getEnvironmentName().isEmpty());
	}
}
