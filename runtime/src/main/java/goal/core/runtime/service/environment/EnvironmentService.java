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

import java.io.File;
import java.util.AbstractMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import eis.EILoader;
import eis.EnvironmentInterfaceStandard;
import eis.exceptions.EnvironmentInterfaceException;
import eis.iilang.Function;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Parameter;
import eis.iilang.ParameterList;
import goal.core.runtime.service.environment.events.EnvironmentPortAddedEvent;
import goal.core.runtime.service.environment.events.EnvironmentPortRemovedEvent;
import goal.core.runtime.service.environment.events.EnvironmentServiceEvent;
import goal.core.runtime.service.environmentport.EnvironmentPort;
import goal.tools.errorhandling.Resources;
import goal.tools.errorhandling.Warning;
import goal.tools.errorhandling.WarningStrings;
import goal.tools.errorhandling.exceptions.GOALLaunchFailureException;
import languageTools.program.mas.MASProgram;

/**
 * Launches the environment in the MAS and maintains a registry of other
 * environments in the system. This can either be a remote environment or a
 * local environment started by this environment service.
 * <p>
 * The connection is provided in the form of a {@link EnvironmentPort} that can
 * be used as an interface for a the environment. The environment service
 * creates an environment port for every environment that is known to the
 * messaging service when the environment service is started.
 * <p>
 * FIXME: It is currently not possible to create environment ports for
 * environments that are created after the environment service starts.
 */
public class EnvironmentService {
	/**
	 * MAS program used to launch environments or connect to remote
	 * environments.
	 */
	private final MASProgram masProgram;
	/**
	 * The environment port that connects to an environment.
	 */
	private EnvironmentPort environmentPort;
	/**
	 * The list of observers.
	 */
	private final List<EnvironmentServiceObserver> observers = new LinkedList<>();

	/**
	 * Creates environment services.
	 *
	 * @param masProgram
	 *            The MAS to create the service for.
	 */
	public EnvironmentService(MASProgram masProgram) {
		this.masProgram = masProgram;
	}

	/**
	 * Starts the environment service. Depending on the mas program this may
	 * create and initialize a local environment, for which a
	 * {@link EnvironmentPort} is created.
	 *
	 * @throws GOALLaunchFailureException
	 */
	public void start() throws GOALLaunchFailureException {
		// Get environment name, file (if it exists), and initialization
		// parameters.
		File environmentFile = this.masProgram.getEnvironmentfile();
		if (environmentFile != null) {
			try {
				EnvironmentInterfaceStandard eis = EILoader.fromJarFile(environmentFile);
				String environmentName = environmentFile.getName().substring(0,
						environmentFile.getName().lastIndexOf(".jar"));
				Map<String, Parameter> initialization = convertMapToEIS(this.masProgram.getInitParameters());
				this.environmentPort = new EnvironmentPort(eis, environmentName, initialization);
			} catch (Exception e) {
				throw new GOALLaunchFailureException(Resources.get(WarningStrings.FAILED_LOAD_ENV), e);
			}
		}

		// Start environment only after observers have had a chance to
		// subscribe. This is important because the environment may start
		// threads of its own. In combination with the late listener pattern
		// this may cause race conditions where events happen twice.
		if (this.environmentPort != null) {
			try {
				notifyObservers(new EnvironmentPortAddedEvent(this.environmentPort));
				this.environmentPort.startPort();
			} catch (EnvironmentInterfaceException e) {
				shutDown();
				throw new GOALLaunchFailureException("Failed to start the environment port", e);
			}
		}
	}

	private static Map<String, Parameter> convertMapToEIS(Map<String, Object> init) {
		Map<String, Parameter> result = new LinkedHashMap<>(init.size());
		for (final String key : init.keySet()) {
			final Object value = init.get(key);
			result.put(key, convertValueToEIS(value));
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	private static Parameter convertValueToEIS(Object value) {
		if (value instanceof Number) {
			return new Numeral((Number) value);
		} else if (value instanceof AbstractMap.SimpleEntry) {
			AbstractMap.SimpleEntry<String, Object[]> map = (AbstractMap.SimpleEntry<String, Object[]>) value;
			Parameter[] params = new Parameter[map.getValue().length];
			for (int i = 0; i < params.length; ++i) {
				params[i] = convertValueToEIS(map.getValue()[i]);
			}
			return new Function(map.getKey(), params);
		} else if (value instanceof List) {
			List<Object> oldlist = (List<Object>) value;
			Parameter[] newlist = new Parameter[oldlist.size()];
			for (int i = 0; i < newlist.length; ++i) {
				newlist[i] = convertValueToEIS(oldlist.get(i));
			}
			return new ParameterList(newlist);
		} else {
			return new Identifier(value.toString());
		}
	}

	/**
	 * @return the environment port connecting to the environment.
	 */
	public EnvironmentPort getEnvironmentPort() {
		return this.environmentPort;
	}

	/**
	 * Stops the environment service by closing all environment ports and
	 * shutting down the local environment.
	 */
	public void shutDown() {
		try {
			this.environmentPort.shutDown();
			notifyObservers(new EnvironmentPortRemovedEvent(this.environmentPort));
		} catch (Exception ignore) {
			// don't care on shutdown
		}
	}

	/**********************************************/

	/*********** observer pattern *****************/
	/**********************************************/

	private void notifyObservers(EnvironmentServiceEvent evt) {
		for (EnvironmentServiceObserver obs : this.observers
				.toArray(new EnvironmentServiceObserver[this.observers.size()])) {
			try {
				obs.environmentServiceEventOccured(this, evt);
			} catch (Exception e) { // Callback exception handling
				new Warning(
						String.format(Resources.get(WarningStrings.INTERNAL_PROBLEM), obs.toString(), evt.toString()),
						e).emit();
			}
		}
	}

	public void addObserver(EnvironmentServiceObserver obs) {
		this.observers.add(obs);
	}
}
