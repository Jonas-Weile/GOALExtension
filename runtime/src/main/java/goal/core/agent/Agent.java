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

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

import com.google.common.collect.ImmutableSet;

import goal.tools.errorhandling.exceptions.GOALLaunchFailureException;
import languageTools.program.agent.AgentId;
import languageTools.program.agent.actions.LogAction;
import languageTools.program.agent.msg.Message;

/**
 * Generic representation of an agent in the GOAL runtime environment.
 *
 * An agent consist of an identity, the ability to act and perceive its an
 * environment through and the ability to communicate with other agents. What
 * the agent does is controlled by a {@link Controller}.
 *
 * The {@link Controller} starts a process that can be used by subclasses to
 * drive the agents actions. This process can be stopped, started and reset
 * through the agent. During these operations the agent should stay connected to
 * to the environment and messaging.
 *
 * At the end of the Agents life the dispose method should be called to clean up
 * any resources claimed by the agent.
 *
 *
 * @param <CONTROLLER> a subclass of {@link Controller} used to control the
 *                     agent.
 */
public class Agent<CONTROLLER extends Controller> {
	private final CONTROLLER controller;
	private final EnvironmentCapabilities environment;
	private final AgentId id;
	private final LoggingCapabilities logging;

	/**
	 * Message queue for storing received messages.
	 */
	private final Queue<Message> messageInQueue = new ConcurrentLinkedQueue<>();

	/**
	 * Creates new agent.
	 *
	 * @param id          unique identity of the agent
	 * @param environment capabilities for the environment in which the agent is
	 *                    situated
	 * @param logger      a logger for {@link LogAction}s.
	 * @param controller  that controls the agent
	 * @param executor    for the controller to use
	 * @param timeout     A timestamp (in millisecond precision) at which the agent
	 *                    should be terminated; 0 means run indefinately.
	 * @throws GOALLaunchFailureException
	 */
	public Agent(AgentId id, EnvironmentCapabilities environment, LoggingCapabilities logger, CONTROLLER controller,
			ExecutorService executor, long timeout) throws GOALLaunchFailureException {
		this.id = id;
		this.environment = environment;
		this.logging = logger;
		this.controller = controller;
		this.controller.initalizeController(this, executor, timeout);
	}

	/**
	 * Waits for the agents process to terminate.
	 *
	 * @throws InterruptedException when interrupted while waiting for the agent to
	 *                              terminate
	 */
	public void awaitTermination() throws InterruptedException {
		this.controller.awaitTermination();
	}

	/**
	 * Disposes any resources held by the agent.
	 */
	public void dispose(boolean controller) {
		/*
		 * we catch all exceptions here so that we can at least try to close everything.
		 * We wrap caught exceptions in a CHECKED Exception() to avoid bugging the user
		 * with stack traces if unchecked exceptions come out (eg, with BW4T3).
		 */
		if (controller) {
			try {
				this.controller.dispose();
			} catch (Exception e) {
				// new Warning(Resources.get(WarningStrings.FAILED_FREE_AGENT),
				// new Exception(e));
			}
		}
		try {
			this.environment.dispose();
		} catch (Exception e) {
			// new Warning(Resources.get(WarningStrings.FAILED_FREE_ENV), new
			// Exception(e));
		}
		this.logging.dispose();
	}

	/**
	 *
	 * @return the agents controller
	 */
	public CONTROLLER getController() {
		return this.controller;
	}

	/**
	 * @return the environment capabilities of the agent.
	 */
	public EnvironmentCapabilities getEnvironment() {
		return this.environment;
	}

	/**
	 * Returns the name of the {@link Agent}.
	 *
	 * @return The name of the agent.
	 */
	public AgentId getId() {
		return this.id;
	}

	/**
	 * Add message received to message queue.
	 *
	 * @param message A message (sent by another agent).
	 */
	public void receiveMessage(Message message) {
		this.messageInQueue.add(message);
	}

	/**
	 * Returns the messages that have been received in the agent's mailbox (message
	 * queue).
	 *
	 * @return A list of messages that have been received (since last time that this
	 *         method was called).
	 */
	public Set<Message> getMessages() {
		List<Message> messages = new LinkedList<>();
		Iterator<Message> queue = this.messageInQueue.iterator();
		while (queue.hasNext()) {
			Message message = queue.next();
			messages.add(message);
			queue.remove();
		}
		return ImmutableSet.copyOf(messages);
	}

	/**
	 * @return the agent's {@link LoggingCapabilities}.
	 */
	public LoggingCapabilities getLogging() {
		return this.logging;
	}

	/**
	 * Checks if agents process is running. Returns {@code true} if it has been
	 * started and is running.
	 *
	 * @return {@code true} if the agents proccess has been started and is running
	 */

	public boolean isRunning() {
		return this.controller.isRunning();
	}

	/**
	 * Resets the agents controller. The agents procces is stopped, once the agent
	 * has stopped its internal state will be reset.
	 *
	 * @throws GOALLaunchFailureException
	 */
	public void reset() throws GOALLaunchFailureException {
		try {
			this.controller.reset();
		} catch (InterruptedException e) {
			throw new GOALLaunchFailureException("reset of agent controller unexpectedly interrupted.", e);
		}
	}

	/**
	 * Starts the agents process.
	 */
	public void start() {
		this.controller.run();
	}

	/**
	 * Immediately stops the agents process. If possible, terminate the agent's
	 * debugger instead of calling this function.
	 */
	public void stop() {
		this.controller.terminate();
	}

	/**
	 * Returns the name of the agent. Useful for debugging purposes.
	 *
	 * @return the name of the agent
	 */
	@Override
	public String toString() {
		return getId().toString();
	}
}