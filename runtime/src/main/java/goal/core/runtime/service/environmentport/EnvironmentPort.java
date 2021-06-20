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
package goal.core.runtime.service.environmentport;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import eis.AgentListener;
import eis.EnvironmentInterfaceStandard;
import eis.EnvironmentListener;
import eis.PerceptUpdate;
import eis.exceptions.EntityException;
import eis.exceptions.EnvironmentInterfaceException;
import eis.exceptions.ManagementException;
import eis.iilang.Action;
import eis.iilang.EnvironmentState;
import eis.iilang.Parameter;
import eis.iilang.Percept;
import goal.core.runtime.service.agent.AgentService;
import goal.core.runtime.service.environmentport.environmentport.events.DeletedEntityEvent;
import goal.core.runtime.service.environmentport.environmentport.events.EnvironmentEvent;
import goal.core.runtime.service.environmentport.environmentport.events.FreeEntityEvent;
import goal.core.runtime.service.environmentport.environmentport.events.NewEntityEvent;
import goal.core.runtime.service.environmentport.environmentport.events.NewPerceptEvent;
import goal.core.runtime.service.environmentport.environmentport.events.StateChangeEvent;
import goal.tools.errorhandling.Resources;
import goal.tools.errorhandling.Warning;
import goal.tools.errorhandling.WarningStrings;

/**
 * Tool to handle the connection with an {@link LocalMessagingEnvironment}. It
 * runs on the client side and communicates with a
 * {@link LocalMessagingEnvironment} which runs on another computer.
 * <p>
 * Basic functionality is to track the run state of the {@link EnvMsgConnector}.
 * <p>
 * This also handles events like newEntity and passes them on to the
 * {@link AgentService} if necessary.
 */
public class EnvironmentPort implements EnvironmentListener, AgentListener {
	private final EnvironmentInterfaceStandard environment;
	private final String name;
	private final Map<String, eis.iilang.Parameter> initialization;
	private final List<EnvironmentPortObserver> observers = new LinkedList<>();

	/**
	 * <p>
	 * Create environment port to control environment.
	 * </p>
	 * <p>
	 * The EnvironmentPort must be immediately ready to handle state requests, even
	 * if no environment has been connected yet.
	 * </p>
	 *
	 * TODO: looks like a round about construction to get environment info in...
	 *
	 * @param environment
	 *            The EIS environment
	 * @param name
	 *            The name of the environment
	 * @param initialization
	 *            The init parameters to pass to the environment
	 */
	public EnvironmentPort(EnvironmentInterfaceStandard environment, String name,
			Map<String, Parameter> initialization) {
		this.environment = environment;
		this.name = name;
		this.initialization = initialization;
	}

	public String getEnvironmentName() {
		return this.name;
	}

	/**
	 * @return The current state of the environment.
	 */
	public EnvironmentState getEnvironmentState() {
		return this.environment.getState();
	}

	/**
	 * Start the environment port. This only connects this class to the environment,
	 * and passes all the init parameters to the environment.
	 *
	 * @throws ManagementException
	 */
	public void startPort() throws ManagementException {
		// GOAL-3941 attach environmentlistener before init so that we see all
		// new entities
		this.environment.attachEnvironmentListener(this);
		this.environment.init(this.initialization);

		// List<EnvironmentEvent> events = new LinkedList<>();
		// for (String entity : this.environment.getFreeEntities()) {
		// String type = null;
		// try {
		// type = this.environment.getType(entity);
		// } catch (EntityException e) {
		// new Warning(Resources.get(WarningStrings.FAILED_EIS_GETTYPE), e);
		// }
		// events.add(new NewEntityEvent(entity, type));
		// }
		// events.add(new StateChangeEvent(getEnvironmentState()));
		//
		// for (EnvironmentEvent event : events) {
		// notifyObservers(event);
		// }

	}

	/**
	 * Shut down the environment port (closes the remote environment).
	 *
	 * @throws EnvironmentInterfaceException
	 */
	public void shutDown() throws EnvironmentInterfaceException {
		try {
			if (this.environment.getState() != EnvironmentState.KILLED) {
				this.environment.kill();
			}
		} catch (Exception e) { // takedown protection
		} finally {
			this.environment.detachEnvironmentListener(this);
		}
	}

	/**
	 * requests the environment to start. NOTE: The env may not be running when this
	 * call returns.
	 *
	 * @throws EnvironmentInterfaceException
	 */
	public void start() throws EnvironmentInterfaceException {
		if (this.environment.getState() != EnvironmentState.RUNNING) {
			this.environment.start();
		}
	}

	/**
	 * requests the environment to pause. NOTE: the env may not yet be paused when
	 * the call returns.
	 *
	 * @throws EnvironmentInterfaceException
	 */
	public void pause() throws EnvironmentInterfaceException {
		if (this.environment.getState() != EnvironmentState.PAUSED) {
			this.environment.pause();
		}
	}

	/**
	 * Reset the environment. Note, this is a parameterless reset because the
	 * EnvMsgConnector remembers the original init parameters and re-uses them. See
	 * {@link EnvMsgConnector#reset()}. Note2: the env may not yet be reset when
	 * this call returns.
	 *
	 * @throws EnvironmentInterfaceException
	 */
	public void reset() throws EnvironmentInterfaceException {
		this.environment.reset(this.initialization);
	}

	/**
	 * Support to register agent with environment? TODO: why? we want to associate
	 * agent with entity or leave environment alone?
	 *
	 * @param agentName
	 *
	 * @throws EnvironmentInterfaceException
	 */
	public void registerAgent(String agentName) throws EnvironmentInterfaceException {
		this.environment.registerAgent(agentName);
		this.environment.attachAgentListener(agentName, this);
	}

	/**
	 * Frees an agent from the agents-entities-relation.
	 *
	 * @param agentName
	 *            agent name in EIS.
	 *
	 * @throws EnvironmentInterfaceException
	 */
	public void freeAgent(String agentName) throws EnvironmentInterfaceException {
		this.environment.unregisterAgent(agentName);
		this.environment.detachAgentListener(agentName, this);
	}

	/**
	 * Support to associate agent to entity.
	 *
	 * @param agentName
	 *            The agent.
	 * @param newEntity
	 *            The entity.
	 *
	 * @throws EnvironmentInterfaceException
	 */
	public void associateEntity(String agentName, String newEntity) throws EnvironmentInterfaceException {
		this.environment.associateEntity(agentName, newEntity);
	}

	public void performAction(String agentName, Action action) throws EnvironmentInterfaceException {
		this.environment.performAction(agentName, action);
	}

	public PerceptUpdate getPercepts(String agentName) throws EnvironmentInterfaceException {
		Map<String, PerceptUpdate> map = this.environment.getPercepts(agentName);
		if (map.size() == 1) {
			return map.values().iterator().next();
		} else {
			PerceptUpdate percepts = new PerceptUpdate();
			for (PerceptUpdate sub : map.values()) {
				percepts.merge(sub);
			}
			return percepts;
		}
	}

	public Double getReward(String agentName) throws EnvironmentInterfaceException {
		try {
			Object result = this.environment.queryProperty("REWARD " + agentName);
			return Double.valueOf(result.toString());
		} catch (Exception e) {
			throw new EnvironmentInterfaceException("failed to retrieve a reward from the environment.", e);
		}
	}

	@Override
	public void handlePercept(String agent, Percept percept) {
		notifyObservers(new NewPerceptEvent(agent, percept));
	}

	@Override
	public void handleStateChange(EnvironmentState newState) {
		notifyObservers(new StateChangeEvent(newState));
	}

	@Override
	public void handleFreeEntity(String entity, Collection<String> agents) {
		// FIXME: handleFreeEntity should also have type parameter...
		// Now we have to catch exceptions here which we cannot handle...
		// Rien: Seems to me eis.getType should just return null. What ever
		// thread we're on just came out of eis so we can assume that the
		// entity is present.
		String type = null;
		try {
			type = this.environment.getType(entity);
		} catch (EntityException e) {
			new Warning(String.format(Resources.get(WarningStrings.FAILED_EIS_GETTYPE1), entity), e).emit();
		}
		notifyObservers(new FreeEntityEvent(entity, agents, type));
	}

	@Override
	public void handleDeletedEntity(String entity, Collection<String> agents) {
		notifyObservers(new DeletedEntityEvent(entity, agents));
	}

	@Override
	public void handleNewEntity(String entity) {
		// FIXME: handleFreeEntity should also have type parameter...
		// Now we have to catch exceptions here which we cannot handle...
		String type = null;
		try {
			type = this.environment.getType(entity);
		} catch (EntityException e) {
			new Warning(String.format(Resources.get(WarningStrings.FAILED_EIS_GETTYPE1), entity), e).emit();
		}
		notifyObservers(new NewEntityEvent(entity, type));
	}

	/**********************************************/
	/*********** observer handler *****************/
	/**********************************************/

	/**
	 * notify all our observers of an event
	 *
	 * @param e
	 *            The event to notify.
	 */
	public void notifyObservers(EnvironmentEvent e) {
		for (EnvironmentPortObserver obs : this.observers.toArray(new EnvironmentPortObserver[this.observers.size()])) {
			try {
				obs.EnvironmentPortEventOccured(this, e);
			} catch (Exception e1) { // Callback failure handling
				new Warning(String.format(Resources.get(WarningStrings.FAILED_CALLBACK), obs.toString(), e.toString()),
						e1).emit();
			}
		}
	}

	/**
	 * add new observer to this port.
	 *
	 * @param o
	 *            The observer to add.
	 */
	public void addObserver(EnvironmentPortObserver o) {
		this.observers.add(o);
	}

	/**
	 * remove an observer from this port. Nothing happens if given observer was not
	 * one of our observers
	 *
	 * @param o
	 *            The observer to delete.
	 */
	public void deleteObserver(EnvironmentPortObserver o) {
		this.observers.remove(o);
	}

	/**
	 * string that is also used in GOAL in the Process panel. FIXME it's not nice to
	 * have the name of env as string for this {@link EnvironmentPort}.
	 */
	@Override
	public String toString() {
		return this.name;
	}

	@Override
	public int hashCode() {
		return ((this.name == null) ? 0 : this.name.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || !(obj instanceof EnvironmentPort)) {
			return false;
		}
		EnvironmentPort other = (EnvironmentPort) obj;
		if (this.name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!this.name.equals(other.name)) {
			return false;
		}
		return true;
	}
}