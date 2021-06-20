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
package goal.core.runtime;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import eis.exceptions.EnvironmentInterfaceException;
import goal.core.agent.Agent;
import goal.core.agent.GOALInterpreter;
import goal.core.runtime.RuntimeEvent.EventType;
import goal.core.runtime.service.agent.AgentService;
import goal.core.runtime.service.agent.events.AgentServiceEvent;
import goal.core.runtime.service.environment.EnvironmentService;
import goal.core.runtime.service.environment.EnvironmentServiceObserver;
import goal.core.runtime.service.environment.events.EnvironmentPortAddedEvent;
import goal.core.runtime.service.environment.events.EnvironmentPortRemovedEvent;
import goal.core.runtime.service.environment.events.EnvironmentServiceEvent;
import goal.core.runtime.service.environmentport.EnvironmentPort;
import goal.core.runtime.service.environmentport.EnvironmentPortObserver;
import goal.core.runtime.service.environmentport.environmentport.events.EnvironmentEvent;
import goal.core.runtime.service.environmentport.environmentport.events.StateChangeEvent;
import goal.tools.debugger.Debugger;
import goal.tools.errorhandling.Warning;
import goal.tools.errorhandling.exceptions.GOALBug;
import goal.tools.errorhandling.exceptions.GOALLaunchFailureException;
import goal.tools.logging.GOALLogger;
import goal.tools.logging.InfoLog;
import goal.tools.logging.Loggers;
import goal.util.DefaultObservable;
import goal.util.Observable;
import languageTools.program.agent.AgentId;

/**
 * This is an obligatory class to run a (one) MAS. Running a MAS is not so
 * clearly documented; however there are mechanisms and function calls here that
 * are implicitly assumed to be called while running a MAS.
 * <p>
 * The RuntimeServiceManager is a LOCAL controller. It provides a functionality
 * for managing the GOAL runtime at a high level and can be observed through
 * event listeners.
 * </p>
 * <p>
 * A RuntimeSerManager manages the life cycle of a {@link AgentService},
 * {@link MessagingService} and {@link EnvironmentService}. It also passes
 * events from each service to the other interested servers and any observers of
 * the RuntimeServiceManager itself.
 * </p>
 * <p>
 * External observer are are provided with {@link RuntimeEvent}s to inform them
 * of events in the Runtime.
 * </p>
 * <p>
 * A distributed system can contain several RuntimeServiceManagers. These each
 * have their own set of agents. Communication between Agents is handled through
 * the {@link MessagingService}. The {@link EnvironmentService} provides agents
 * with access to the environment.
 * </p>
 * <p>
 * This class is an important node rerouting various events in the system. The
 * rerouting here looks like this:
 * <ul>
 * <li>RemoteRuntimeManager -- (AgentBorn| AgentRemoved|RuntimeLaunched) -->
 * AgentService
 * <li>RemoteRuntimeManager -- Environment(Added|Removed)(MsgBoxId) -->
 * EnvironmentService
 * <li>EnvironmentService -- EnvironmentPort(Added|Removed) --> (Add|Remove)
 * EnvironmentPort2RuntimeManager Listener.
 * <li>EnvironmentService -- EnvironmentPort(Added|Removed) --> (Add|Remove)
 * EnvironmentPort2Observers Listener.
 * <li>AgentService -- (AgentBornAndReady|LocalAgentRemoved) --> Observers
 * <li>AgentService -- (AgentBornAndReady|LocalAgentRemoved) -->
 * RemoteRuntimeService
 * </ul>
 * <p>
 * The {@link RuntimeManager} has two types of 'observers':
 * <ul>
 * <li>direct observers within this JVM. see
 * {@link #addObserver(RuntimeEventObserver)}.
 * <li>remote observers, working through the {@link Messaging} system. These are
 * not registered here as observers but called directly using the
 * {@link MessagingClient#getMessageBoxes(Type, String)} call.
 * </ul>
 *
 * @param <DEBUGGER>   subclass of {@link Debugger} that agents in this runtime
 *                     will use.
 * @param <CONTROLLER> subclass of {@link GOALInterpreter} that agents in this
 *                     runtime will use.
 */
public class RuntimeManager<DEBUGGER extends Debugger, CONTROLLER extends GOALInterpreter<DEBUGGER>>
		implements Observable<RuntimeEventObserver, RuntimeManager<?, ?>, RuntimeEvent> {
	// wrapper pattern for implementing Observable.
	private final DefaultObservable<RuntimeEventObserver, RuntimeManager<?, ?>, RuntimeEvent> myObservable = new DefaultObservable<>();
	private final static String GOAL_RELAY = "http://ii.tudelft.nl:8080/glrly-1";

	/**
	 * Connects {@link StateChangeEvent}s from the {@link EnvironmentPort} to
	 * external Observers.
	 *
	 */
	private final class EnvironmentPort2Observers implements EnvironmentPortObserver {
		@Override
		public void EnvironmentPortEventOccured(EnvironmentPort environmentPort, EnvironmentEvent event) {
			if (event instanceof StateChangeEvent) {
				RuntimeManager.this.myObservable.notifyObservers(RuntimeManager.this,
						new RuntimeEvent(EventType.ENVIRONMENT_RUNMODE_CHANGED, environmentPort));
			}
		}
	}

	/**
	 * Connects {@link EnvironmentEvent}s from an {@link EnvironmentPort} to the
	 * {@link AgentService}.
	 */
	private final class EnvironmentPort2Runtime implements EnvironmentPortObserver {
		@Override
		public void EnvironmentPortEventOccured(EnvironmentPort environmentPort, EnvironmentEvent event) {
			// FIXME: Handling the event should be done here. RuntimeService
			// should just apply the launch rules for what it is given.
			// RuntimeService may ofcourse provide convenient methods for this.
			RuntimeManager.this.agentService.handleEnvironmentEvent(event, environmentPort);
		}
	}

	/**
	 * When ever an {@link EnvironmentPort} is created or removed this will add or
	 * remove a connection from that EnvironmentPort to external Observers.
	 */
	private final class EnvironmentService2Observers implements EnvironmentServiceObserver {
		private final Map<EnvironmentPort, EnvironmentPortObserver> observers = new ConcurrentHashMap<>();

		private void handle(EnvironmentPortAddedEvent event) {
			EnvironmentPort environmentPort = event.getPort();
			EnvironmentPortObserver observer = new EnvironmentPort2Observers();
			this.observers.put(environmentPort, observer);
			environmentPort.addObserver(observer);

			RuntimeManager.this.myObservable.notifyObservers(RuntimeManager.this,
					new RuntimeEvent(EventType.ENVIRONMENT_LAUNCHED, event.getPort()));
		}

		private void handle(EnvironmentPortRemovedEvent event) {
			EnvironmentPort environmentPort = event.getPort();
			EnvironmentPortObserver observer = this.observers.remove(environmentPort);
			environmentPort.deleteObserver(observer);
			RuntimeManager.this.myObservable.notifyObservers(RuntimeManager.this,
					new RuntimeEvent(EventType.ENVIRONMENT_KILLED, event.getPort()));
		}

		@Override
		public void environmentServiceEventOccured(EnvironmentService environmentService,
				EnvironmentServiceEvent event) {
			if (event instanceof EnvironmentPortAddedEvent) {
				handle((EnvironmentPortAddedEvent) event);
			} else if (event instanceof EnvironmentPortRemovedEvent) {
				handle((EnvironmentPortRemovedEvent) event);
			}
		}
	}

	/**
	 * When ever an {@link EnvironmentPort} is created or removed this will add or
	 * remove a connection from that EnvironmentPort to the {@link AgentService}.
	 */
	private final class EnvironmentService2Runtime implements EnvironmentServiceObserver {
		private final Map<EnvironmentPort, EnvironmentPortObserver> observers = new ConcurrentHashMap<>();

		private void handle(EnvironmentPortAddedEvent event) {
			EnvironmentPort environmentPort = event.getPort();
			EnvironmentPortObserver observer = new EnvironmentPort2Runtime();
			this.observers.put(environmentPort, observer);
			environmentPort.addObserver(observer);
		}

		private void handle(EnvironmentPortRemovedEvent event) {
			EnvironmentPort environmentPort = event.getPort();
			EnvironmentPortObserver observer = this.observers.remove(environmentPort);
			environmentPort.deleteObserver(observer);
		}

		@Override
		public void environmentServiceEventOccured(EnvironmentService environmentService,
				EnvironmentServiceEvent event) {
			if (event instanceof EnvironmentPortAddedEvent) {
				handle((EnvironmentPortAddedEvent) event);
			} else if (event instanceof EnvironmentPortRemovedEvent) {
				handle((EnvironmentPortRemovedEvent) event);
			}
		}
	}

	/**
	 * Connects Agents added / removed events from the {@link AgentService} to
	 * external Observers.
	 */
	private final class Runtime2Observers implements goal.core.runtime.service.agent.AgentServiceEventObserver {
		@Override
		public void agentServiceEvent(AgentService<?, ?> runtimeService, AgentServiceEvent evt) {
			if (evt instanceof goal.core.runtime.service.agent.events.AddedLocalAgent) {
				RuntimeManager.this.myObservable.notifyObservers(RuntimeManager.this,
						new RuntimeEvent(EventType.AGENT_IS_LOCAL_AND_READY,
								((goal.core.runtime.service.agent.events.AddedLocalAgent) evt).getAgent()));
			} else if (evt instanceof goal.core.runtime.service.agent.events.RemovedLocalAgent) {
				RuntimeManager.this.myObservable.notifyObservers(RuntimeManager.this, new RuntimeEvent(
						// removed, AGENT_DIED events go through the debugger
						EventType.AGENT_REMOVED, ((goal.core.runtime.service.agent.events.RemovedLocalAgent) evt)
								.getAgent().getId().toString()));
			} else if (evt instanceof goal.core.runtime.service.agent.events.AddedRemoteAgent) {
				RuntimeManager.this.myObservable.notifyObservers(RuntimeManager.this, new RuntimeEvent(
						EventType.AGENT_BORN,
						((goal.core.runtime.service.agent.events.AddedRemoteAgent) evt).getAgentId().toString()));
			} else if (evt instanceof goal.core.runtime.service.agent.events.RemovedRemoteAgent) {
				RuntimeManager.this.myObservable.notifyObservers(RuntimeManager.this, new RuntimeEvent(
						EventType.AGENT_REMOVED,
						((goal.core.runtime.service.agent.events.RemovedRemoteAgent) evt).getAgentId().toString()));
			} else {
				throw new IllegalArgumentException("unknown event '" + evt + "'.");
			}
		}
	}

	private final EnvironmentService environmentService;
	private final AgentService<DEBUGGER, CONTROLLER> agentService;
	private long startTime;

	/**
	 * Creates a new runtime service manager to manage a MAS.
	 *
	 * @param messagingService     used to facilitate communication between agents
	 *                             and between agents.
	 * @param agentService         used to manage agents in the multi-agent system.
	 * @param environmentService   used to manage the environment.
	 * @param remoteRuntimeService the remote runtime service.
	 * @throws GOALLaunchFailureException when the system could not be launched.
	 */
	public RuntimeManager(AgentService<DEBUGGER, CONTROLLER> agentService, EnvironmentService environmentService) {
		this.agentService = agentService;
		this.environmentService = environmentService;

		// EnvironmentService -- EnvironmentPort(Added|Removed) --> (Add|Remove)
		// EnvironmentPort2RuntimeService.
		environmentService.addObserver(new EnvironmentService2Runtime());

		// EnvironmentService -- EnvironmentPort(Added|Removed) --> (Add|Remove)
		// EnvironmentPort2Observer.
		environmentService.addObserver(new EnvironmentService2Observers());

		// RuntimeService -- (AgentBornAndReady|LocalAgentRemoved) --> Observers
		agentService.addObserver(new Runtime2Observers());
	}

	/**
	 * Reports this usage of GOAL to the GOAL usage (relay) server. Runs in separate
	 * thread and ignores any failures.
	 */
	private static void reportGoalUsage() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				reportGoalUsage1();
			}
		}, "GOAL usage").start();
	}

	/**
	 * Reports this usage of GOAL to the GOAL server.
	 */
	private static void reportGoalUsage1() {
		try {
			String id = System.getProperty("user.name") + "@" + System.getProperty("os.name") + ":"
					+ System.getProperty("os.version") + " java" + System.getProperty("java.version");
			URL url = new URL(GOAL_RELAY + "?id=" + URLEncoder.encode(id, "UTF-8"));

			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.connect();
			// int c = conn.getResponseCode();
		} catch (Throwable e) {
			// ignore completely. reporting is optional.
		}
	}

	/**
	 * Await the launch of the first agent. This method will return once an agent
	 * has been launched.
	 *
	 * @throws InterruptedException
	 */
	public void awaitFirstAgent() throws InterruptedException {
		this.agentService.awaitFirstAgent();
	}

	/**
	 * Waits for all agents to die.
	 *
	 * @throws InterruptedException
	 */
	public void awaitTermination() throws InterruptedException {
		this.agentService.awaitTermination();
	}

	/**
	 * Returns the agent with the given <code>id</code>.
	 *
	 * @param id the id of the agent
	 * @return the agent with the given <code>id</code> or <code>null</code> when no
	 *         such agent is available
	 */
	public Agent<CONTROLLER> getAgent(AgentId id) {
		return this.agentService.getAgent(id);
	}

	/**
	 * Returns the agents that are part of the LOCAL MAS runtime environment.
	 *
	 * @return an array containing all agents that have been launched LOCALLY. Some
	 *         agents might be dead but not yet removed.
	 */
	public Collection<Agent<CONTROLLER>> getAgents() {
		return this.agentService.getAgents();
	}

	/**
	 * Returns all local agents that are running. This collection is thread-safe.
	 *
	 * @return all local agents that are running.
	 */
	public Collection<Agent<CONTROLLER>> getAliveAgents() {
		return this.agentService.getAliveAgents();
	}

	/**
	 * @return True if there is any local agent running.
	 */
	public boolean hasAliveLocalAgents() {
		return this.agentService.hasAliveLocalAgents();
	}

	/**
	 * Returns all local agents that are dead. This collection is thread-safe.
	 *
	 * @return all local agents that are dead.
	 */
	public Collection<Agent<CONTROLLER>> getDeadAgents() {
		return this.agentService.getDeadAgents();
	}

	/**
	 * @return a connected environment (if any).
	 */
	public EnvironmentPort getEnvironmentPort() {
		return this.environmentService.getEnvironmentPort();
	}

	/**
	 * This function MUST BE CALLED when the MAS has finished running. How to detect
	 * a MAS completion is a different question, not answered here, but see
	 * {@link AgentService#awaitTermination()}
	 * 
	 * Shuts down all runtime services, kills and cleans all agents.
	 */
	public void shutDown(boolean dispose) {
		new InfoLog("shutting down the multi-agent system.").emit();

		// Shut down MAS and wait for agents to finish
		try {
			this.agentService.shutDown();
			this.agentService.awaitTermination();
		} catch (Exception e) {
			// new Warning(Resources.get(WarningStrings.INTERRUPT_STOP_RUNTIME),
			// e);
		}

		// Dispose resources
		if (dispose) {
			try {
				this.agentService.dispose();
			} catch (Exception e) {
				new Warning("Failed to stop agent service", e);

			}
		}

		// Shut down environment.
		try {
			this.environmentService.shutDown();
		} catch (Exception e) {
			// new Warning(Resources.get(WarningStrings.FAILED_STOP_ENV), e);
		}

		// TODO: providing runtime service after MAS died looks like a silly
		// thing to do...
		this.myObservable.notifyObservers(this, new RuntimeEvent(EventType.MAS_DIED, this));
		// Print the elapsed time.
		final long elapsedTime = (System.nanoTime() - this.startTime) / 1000000000;
		new InfoLog("ran for " + elapsedTime + " seconds.").emit();

		// Clean up all loggers
		for (GOALLogger logger : Loggers.getAllLoggers()) {
			try {
				logger.dispose();
			} catch (Exception e) {
			}
		}
	}

	/**
	 * Starts all environment and agents that were launched paused (through the
	 * environment). If there is no environment, start all agents anyway.
	 *
	 * @throws MessagingException            when it was not possible to connect to
	 *                                       the environment.
	 * @throws EnvironmentInterfaceException when the environment could not be
	 *                                       started.
	 * @throws GOALLaunchFailureException
	 */
	public void start(boolean startEnvironments) throws GOALLaunchFailureException {
		reportGoalUsage();
		this.myObservable.notifyObservers(this, new RuntimeEvent(EventType.MAS_BORN, this.agentService));

		this.environmentService.start();
		EnvironmentPort environment = getEnvironmentPort();
		if (startEnvironments && environment != null) {
			try {
				environment.start();
			} catch (Exception e) {
				throw new GOALLaunchFailureException("failed to start environment", e);
			}
		}

		this.agentService.start();

		this.startTime = System.nanoTime();
		new InfoLog("start-up complete.").emit();
	}

	/**
	 * Stops agent with the given <code>id</code>.
	 *
	 * @param id of the agent to stop
	 */
	public void stopAgent(AgentId id) {
		this.agentService.stopAgent(id);
	}

	/**
	 * Name of the multi-agent system being run.
	 */
	@Override
	public String toString() {
		return this.agentService.toString();
	}

	/*****************************************/
	/**************** observer ***************/

	/*****************************************/
	/**
	 * Must override but this function should never be used externally. Therefore we
	 * throw. Internally we use {@link #myObservable}
	 */
	@Override
	public void notifyObservers(RuntimeManager<?, ?> e, RuntimeEvent evt) {
		throw new GOALBug("illegal use of RuntimeManager#notifyObservers.");
	}

	@Override
	public void addObserver(RuntimeEventObserver observer) {
		this.myObservable.addObserver(observer);
	}

	@Override
	public void removeObserver(RuntimeEventObserver observer) {
		this.myObservable.removeObserver(observer);
	}

}
