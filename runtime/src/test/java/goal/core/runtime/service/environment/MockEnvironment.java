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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import eis.EIDefaultImpl;
import eis.PerceptUpdate;
import eis.exceptions.ActException;
import eis.exceptions.EntityException;
import eis.exceptions.ManagementException;
import eis.exceptions.NoEnvironmentException;
import eis.exceptions.PerceiveException;
import eis.iilang.Action;
import eis.iilang.EnvironmentState;
import eis.iilang.Parameter;
import eis.iilang.Percept;

final class MockEnvironment extends EIDefaultImpl {
	private static final long serialVersionUID = 8751467335725577307L;

	@Override
	public void init(Map<String, Parameter> parameters) throws ManagementException {
		super.init(parameters);

		setState(EnvironmentState.PAUSED);
		setState(EnvironmentState.RUNNING);

		try {
			addEntity("existingEntity");
		} catch (EntityException e) {
			// FIXME: add entity is internal to the EIS.
			// Should not throw exceptions.
			throw new ManagementException("...", e);
		}
	}

	@Override
	public void reset(Map<String, Parameter> parameters) throws ManagementException {
		super.reset(parameters);
		setState(EnvironmentState.RUNNING);
	}

	@Override
	public String queryProperty(String property) {
		if (property.startsWith("REWARD")) {
			return Double.toString(42.0);
		} else {
			return null;
		}
	}

	@Override
	public String requiredVersion() {
		return null;
	}

	@Override
	protected void performEntityAction(Action action, String entity) throws ActException {
		if (entity.equals("existingEntity")) {
			return;
		} else {
			throw new ActException("No such entity");
		}
	}

	@Override
	protected boolean isSupportedByType(Action arg0, String arg1) {
		return false;
	}

	@Override
	protected boolean isSupportedByEnvironment(Action arg0) {
		return true;
	}

	@Override
	protected boolean isSupportedByEntity(Action arg0, String arg1) {
		return true;
	}

	@Override
	protected PerceptUpdate getPerceptsForEntity(String arg0) throws PerceiveException, NoEnvironmentException {
		return new PerceptUpdate(Arrays.asList(new Percept(arg0)), new ArrayList<>(0));
	}
}