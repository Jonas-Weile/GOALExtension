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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;

import goal.core.agent.Agent;
import goal.core.agent.AgentFactory;
import goal.core.agent.GOALInterpreter;
import goal.core.executors.LaunchRuleExecutor;
import goal.core.runtime.service.agent.events.AddedLocalAgent;
import goal.core.runtime.service.agent.events.AddedRemoteAgent;
import goal.core.runtime.service.agent.events.AgentServiceEvent;
import goal.core.runtime.service.agent.events.RemovedLocalAgent;
import goal.core.runtime.service.agent.events.RemovedRemoteAgent;
import goal.core.runtime.service.environmentport.EnvironmentPort;
import goal.core.runtime.service.environmentport.environmentport.events.DeletedEntityEvent;
import goal.core.runtime.service.environmentport.environmentport.events.EnvironmentEvent;
import goal.core.runtime.service.environmentport.environmentport.events.FreeEntityEvent;
import goal.core.runtime.service.environmentport.environmentport.events.NewEntityEvent;
import goal.preferences.CorePreferences;
import goal.preferences.ProfilerPreferences;
import goal.tools.debugger.Debugger;
import goal.tools.debugger.SteppingDebugger;
import goal.tools.errorhandling.Resources;
import goal.tools.errorhandling.Warning;
import goal.tools.errorhandling.WarningStrings;
import goal.tools.errorhandling.exceptions.GOALLaunchFailureException;
import goal.tools.logging.InfoLog;
import goal.tools.profiler.Profiles;
import languageTools.program.agent.AgentId;
import languageTools.program.mas.LaunchRule;
import languageTools.program.mas.MASProgram;

/**
 * An agent service keeps track of agents in the runtime and supports creating
 * new agents when a new entity appears. An AgentService can handle one MAS
 * only.
 * <p>
 * This class must be thread safe.
 *
 * @param <DEBUGGER>   A debugger type.
 * @param <CONTROLLER> A type of agent controller (interpreter).
 */
public class AgentService<DEBUGGER extends Debugger, CONTROLLER extends GOALInterpreter<DEBUGGER>> {
	private final MASProgram masProgram;
	private final Agents agents = new Agents();
	private final AgentFactory<DEBUGGER, CONTROLLER> factory;
	private final List<AgentServiceEventObserver> observers = new LinkedList<>();
	private final Profiles profiles;

	/**
	 * Executors for the launch rules.
	 */
	private final List<LaunchRuleExecutor<DEBUGGER, CONTROLLER>> launchRuleExecutors = new LinkedList<>();

	/**
	 * @param masProgram
	 * @param factory
	 * @param profiles   the Profiles store for all profile results
	 * @throws GOALLaunchFailureException
	 */
	public AgentService(MASProgram masProgram, AgentFactory<DEBUGGER, CONTROLLER> factory) {
		this.masProgram = masProgram;
		this.factory = factory;
		if (ProfilerPreferences.getProfiling()) {
			this.profiles = new Profiles();
		} else {
			this.profiles = null;
		}

		// Create executors for launch rules.
		for (LaunchRule rule : this.masProgram.getLaunchRules()) {
			this.launchRuleExecutors.add(new LaunchRuleExecutor<>(rule, this.factory));
		}
	}

	/**
	 * Launches a MAS by applying unconditional launch rules. Conditional launch
	 * rules are handled by an environment.
	 *
	 * @throws GOALLaunchFailureException
	 */
	public void start() throws GOALLaunchFailureException {
		for (LaunchRuleExecutor<DEBUGGER, CONTROLLER> executor : this.launchRuleExecutors) {
			for (Agent<CONTROLLER> agent : executor.apply(null, null, null, profiles)) {
				startAgent(agent);
			}
		}
	}

	/**
	 * Kills the entire multi-agent system (all agents).
	 */
	public void shutDown() {
		for (Agent<CONTROLLER> agent : getAgents()) {
			agent.stop();
		}
		if (profiles != null) {
			profiles.getNames().stream().filter(name -> profiles.getProfiles(name).size() > 1)
					.forEach(name -> profiles.getMergedProfile(name).log(new AgentId("All_" + name)));
		}
	}

	/**
	 * Awaits the termination of all agents. Notice, this implementation does NOT
	 * support scenarios where temporarily NO agents run in the system. It's not
	 * clear if that is a bug.
	 *
	 * @throws InterruptedException when interrupted while waiting
	 */
	public void awaitTermination() throws InterruptedException {
		List<Agent<CONTROLLER>> agents;
		if (CorePreferences.getSequentialExecution()) {
			// in sequential mode, iterate over all agents, giving them 'the
			// turn'
			// one-by-one (waiting until each turn has been completed)
			while (!(agents = getAliveAgents()).isEmpty()) {
				for (Agent<CONTROLLER> agent : agents) {
					CONTROLLER controller = agent.getController();
					controller.giveTurn();
					while (controller.isRunning() && controller.hasTurn()) {
						Thread.sleep(1);
					}
				}
			}
		} else {
			while (!(agents = getAliveAgents()).isEmpty()) {
				Agent<CONTROLLER> agent = agents.get(0);
				agent.awaitTermination();
			}
		}
	}

	/**
	 * A list of the agents currently tracked by the {@link AgentService}. The
	 * collection is a non-updating copy.
	 *
	 * @return a list of the agents
	 */
	public Collection<Agent<CONTROLLER>> getAgents() {
		return ImmutableList.copyOf(this.agents.local());
	}

	/**
	 * get all agents, also remote ones.
	 *
	 * @return a set of all agents
	 */
	public Set<AgentId> getAll() {
		return ImmutableSet.copyOf(this.agents.all);
	}

	/**
	 * Returns the GOAL agent with name agentName, if it runs locally.
	 *
	 * @param id the agent to be found.
	 * @return GOAL agent with name agentName, or null if a GOAL agent with that
	 *         name does not exist locally on THIS JVM.
	 */
	public Agent<CONTROLLER> getAgent(AgentId id) {
		return this.agents.getLocal(id);
	}

	/**
	 * Returns all local agents that are running. This collection is thread-safe.
	 *
	 * @return all local agents that are running.
	 */
	public List<Agent<CONTROLLER>> getAliveAgents() {
		List<Agent<CONTROLLER>> aliveAgents = new LinkedList<>();
		for (Agent<CONTROLLER> agent : this.agents.local()) {
			if (agent.isRunning()) {
				aliveAgents.add(agent);
			}
		}
		return aliveAgents;
	}

	/**
	 * Returns all local agents that are dead. This collection is thread-safe.
	 *
	 * @return all local agents that are dead.
	 */
	public List<Agent<CONTROLLER>> getDeadAgents() {
		List<Agent<CONTROLLER>> deadAgents = new LinkedList<>();
		for (Agent<CONTROLLER> agent : this.agents.local()) {
			if (!agent.isRunning()) {
				deadAgents.add(agent);
			}
		}
		return deadAgents;
	}

	/**
	 * @return True if there are any running local agents.
	 */
	public boolean hasAliveLocalAgents() {
		for (Agent<CONTROLLER> agent : this.agents.local()) {
			if (agent.isRunning()) {
				return true;
			}
		}
		return false;
	}

	/**
	 * @return True if there are any local agents (running or not).
	 */
	public boolean hasLocalAgents() {
		return !this.agents.local().isEmpty();
	}

	/**
	 * Stop an agent.
	 *
	 * @param id The agent to stop.
	 */
	public void stopAgent(AgentId id) {
		Agent<CONTROLLER> agent = this.agents.getLocal(id);
		if (agent != null) {
			DEBUGGER debugger = agent.getController().getDebugger();
			if (debugger instanceof SteppingDebugger) {
				((SteppingDebugger) debugger).kill();
			} else {
				agent.stop();
			}
		}
	}

	/**
	 * This is called by remote {@link Runtime} when it is notified that a new agent
	 * has been created. It can be remote or local agent. All local agents are
	 * notified of the new agent.
	 *
	 * @param id of the created message box.
	 */
	public void handleAgentCreated(AgentId id) {
		this.agents.add(id);
		notifyObservers(new AddedRemoteAgent(id));
	}

	/**
	 * If the agent was a local agent it is removed and the observers are notified.
	 *
	 * Note that agents only remove their message box if
	 * {@link PMPreferences#getRemoveKilledAgent()} is true.
	 *
	 * @param id of the created message box.
	 */
	public void handleAgentRemoved(AgentId id) {
		Agent<CONTROLLER> agent = this.agents.getLocal(id);
		this.agents.remove(id);
		this.factory.remove(id);

		// If agent was local notify observers.
		if (agent != null) {
			notifyObservers(new RemovedLocalAgent(agent));
		} else {
			notifyObservers(new RemovedRemoteAgent(id));
		}
	}

	/**
	 * Handles environment events routed via {@link MonitoringService}. FIXME
	 *
	 * @param event    The environment event
	 * @param receiver The port to route the event to
	 * @return True if the event was handled; false otherwise
	 */
	public boolean handleEnvironmentEvent(EnvironmentEvent event, EnvironmentPort receiver) {
		if (event instanceof FreeEntityEvent) {
			FreeEntityEvent f = (FreeEntityEvent) event;
			handleFreeEntity(f.getEntity(), f.getAgents(), f.getType(), receiver);
			return true;
		} else if (event instanceof NewEntityEvent) {
			NewEntityEvent ne = (NewEntityEvent) event;
			handleNewEntity(ne.getEntity(), ne.getType(), receiver);
			return true;
		} else if (event instanceof DeletedEntityEvent) {
			DeletedEntityEvent de = (DeletedEntityEvent) event;
			handleDeletedEntity(de.getEntity(), de.getAgents());
			return true;
		} else {
			return false; // not handled.
		}
	}

	/**
	 * Connects agent to entity in the environment.
	 *
	 * @param newEntity Name of entity available in the environment.
	 * @param agent     An agent.
	 * @param port      A port to an environment.
	 * @throws GOALLaunchFailureException
	 */
	private void connectAgent(String newEntity, Agent<CONTROLLER> agent, EnvironmentPort port)
			throws GOALLaunchFailureException {
		// Connect agent to entity.
		if (port != null) {
			try {
				new InfoLog("connecting agent '" + agent.getId() + "' to entity '" + newEntity + "'.").emit();
				port.registerAgent(agent.getId().toString());
				port.associateEntity(agent.getId().toString(), newEntity);
			} catch (Exception e) {
				agent.dispose(true);
				throw new GOALLaunchFailureException(
						"could not register agent '" + agent.getId() + "' with its environment.", e);
			}
		}
	}

	/**
	 * Starts an agent.
	 *
	 * @param agent An agent.
	 */
	private void startAgent(Agent<CONTROLLER> agent) {
		// INFORM ABOUT EXISTENCE
		// We've created a new agent; inform that agent of the existence of all
		// other agents that we know of.
		this.agents.addLocal(agent);

		// FIXME: remove this notification, agent registry should be updated
		// automatically.
		notifyObservers(new AddedLocalAgent(agent));

		// Start agent.
		agent.start();

		// Wake up sleeping threads in awaitFirstAgent.
		synchronized (this) {
			notifyAll();
		}
	}

	/**
	 * Applies launch rules for launching agent to control entity that is available
	 * and connect the agent to the entity in the environment.
	 *
	 * @param newEntity The name of the entity that has become available in the
	 *                  environment.
	 * @param type      The type of the entity.
	 * @param port      A port to the environment that made the entity available.
	 */
	private void applyLaunchRules(String newEntity, String type, EnvironmentPort port) {
		Iterator<LaunchRuleExecutor<DEBUGGER, CONTROLLER>> iterator = this.launchRuleExecutors.iterator();
		boolean applied = false;
		while (iterator.hasNext() && !applied) {
			// Apply rule and create agents.
			List<Agent<CONTROLLER>> agents = new ArrayList<>(0);
			try {
				agents = iterator.next().apply(newEntity, type, port, profiles);
				// Connect agents to environment.
				for (Agent<CONTROLLER> agent : agents) {
					try {
						connectAgent(newEntity, agent, port);
						startAgent(agent);
					} catch (GOALLaunchFailureException e) {
						new Warning(String.format(Resources.get(WarningStrings.FAILED_LAUNCH_AGENT), newEntity), e)
								.emit();
					}
				}
			} catch (GOALLaunchFailureException e1) {
				new Warning(String.format(Resources.get(WarningStrings.FAILED_LAUNCH_AGENT), newEntity), e1).emit();
			}
			applied = !agents.isEmpty();
		}

		// Issue warning if no rule was applied.
		if (!applied) {
			new Warning(String.format(Resources.get(WarningStrings.NO_APPLICABLE_LAUNCH_RULE), newEntity.toString(),
					type.toString())).emit();
		}
	}

	/**
	 * called when we receive update about a free entity. * Because applyLaunchRules
	 * is called as callback from EIS (via handleFreeEntity etc), and because the
	 * eis callback does not accept throws from its callback functions, we can't
	 * throw an exception here. Therefore we will just print a warning if problems
	 * happen.
	 *
	 * @param agents String names of agents that were connected to entity.
	 */
	private void handleFreeEntity(String entity, Collection<String> agents, String type, EnvironmentPort environment) {
		// Check whether we should print reception
		if (CorePreferences.getPrintEntities()) {
			new InfoLog("received a free entity named '" + entity + "'.").emit();
		}

		// Kill the agents that are no longer bound to the entity.
		for (String name : agents) {
			// Soft kill to be able to inspect mental state thereafter.
			stopAgent(new AgentId(name));
		}

		/*
		 * when an entity becomes free, we can apply the launch rules. This is TRICKY
		 * when we are taking down the runtime service manager.
		 */
		applyLaunchRules(entity, type, environment);
	}

	/**
	 * Called when we receive update about a new entity.
	 */
	private void handleNewEntity(String entity, String type, EnvironmentPort environment) {
		// Check whether we should print reception of new entities.
		if (CorePreferences.getPrintEntities()) {
			new InfoLog("received a new entity named '" + entity + "'.").emit();
		}

		// (Re)connect (new) agent to entity.
		this.applyLaunchRules(entity, type, environment);
	}

	/**
	 * @param agents Called when we receive update about deleted entities.
	 */
	private void handleDeletedEntity(String entity, Collection<String> agents) {
		// Check whether we should print reception
		if (CorePreferences.getPrintEntities()) {
			new InfoLog("entity named '" + entity + "' has been deleted, stopping associated agents " + agents + ".")
					.emit();
		}

		// Remove the agents connected to the entity.
		for (String name : agents) {
			stopAgent(new AgentId(name));
		}
	}

	@Override
	public String toString() {
		return this.masProgram.toString();
	}

	/**
	 * Resets all local agents.
	 *
	 * @throws GOALLaunchFailureException
	 */
	public void reset() throws GOALLaunchFailureException {
		for (Agent<CONTROLLER> agent : this.agents.local()) {
			agent.reset();
		}
	}

	/**
	 * Returns true if the agent id belongs to an agent that is running on this
	 * system.
	 *
	 * @param id the id of the agent to check
	 * @return true if the agent id belongs to a local agent
	 */
	public boolean isLocal(AgentId id) {
		return this.agents.getLocal(id) != null;
	}

	/**
	 * Disposes all agents and any resources held by them.
	 */
	public void dispose() {
		for (Agent<CONTROLLER> agent : getAgents()) {
			agent.dispose(true);
			this.agents.remove(agent.getId());
			notifyObservers(new RemovedLocalAgent(agent));
		}
	}

	/**
	 * Await the launch of the first agent. This method will return once an agent
	 * has been launched.
	 *
	 * @throws InterruptedException
	 */
	public void awaitFirstAgent() throws InterruptedException {
		synchronized (this) {
			while (!hasLocalAgents()) {
				// This will surrender the lock.
				// Wake up call is done in launchAgent().
				wait();
			}
		}
	}

	/******************************************/

	/******** observer handling **************/
	/******************************************/
	public void addObserver(AgentServiceEventObserver o) {
		this.observers.add(o);
	}

	public void notifyObservers(AgentServiceEvent evt) {
		for (AgentServiceEventObserver obs : this.observers
				.toArray(new AgentServiceEventObserver[this.observers.size()])) {
			try {
				obs.agentServiceEvent(this, evt);
			} catch (Exception e) { // Callback protection
				new Warning(
						String.format(Resources.get(WarningStrings.FAILED_CALLBACK), obs.toString(), evt.toString()), e)
								.emit();
			}
		}
	}

	private class Agents {
		/**
		 * Maps agent id's to their corresponding GOAL agent. This means these are the
		 * <i>local</i> agents as we can only associate agent id's with real agents if
		 * these agents are constructed and running locally (on this JVM) here by this
		 * service manager.
		 */
		private final Map<AgentId, Agent<CONTROLLER>> local = new ConcurrentHashMap<>();
		private final Set<AgentId> all = Collections.newSetFromMap(new ConcurrentHashMap<AgentId, Boolean>());

		public void addLocal(Agent<CONTROLLER> agent) {
			this.local.put(agent.getId(), agent);
			this.all.add(agent.getId());
		}

		public void add(AgentId id) {
			this.all.add(id);
		}

		/**
		 * Get the agents running in this JVM
		 *
		 * @return
		 */
		public Set<Agent<CONTROLLER>> local() {
			return ImmutableSet.copyOf(this.local.values());
		}

		public Agent<CONTROLLER> getLocal(AgentId id) {
			return this.local.get(id);
		}

		public void remove(AgentId id) {
			this.all.remove(id);
			this.local.remove(id);
		}
	}
}