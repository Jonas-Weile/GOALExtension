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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import goal.core.runtime.service.environmentport.EnvironmentPort;
import goal.tools.adapt.FileLearner;
import goal.tools.adapt.Learner;
import goal.tools.debugger.Debugger;
import goal.tools.errorhandling.exceptions.GOALLaunchFailureException;
import goal.tools.logging.GOALLoggerDelayed;
import goal.tools.profiler.Profiles;
import languageTools.program.agent.AgentId;
import languageTools.program.mas.AgentDefinition;
import languageTools.program.mas.LaunchInstruction;

/**
 * Abstract base for building Agents. Implementations can provide different
 * Messaging- and EnvironmentCapabilities, Debuggers, Learners and Controllers.
 * This can be done by overriding or implementing the proper methods.
 *
 * During the construction the class fields will be initialized to assist the
 * creation of different classes.
 *
 * When extending this class and providing constructor withouth messaging
 * subclasses classes should be aware that messageBoxId and messageBox fields
 * are null when building agents. The provideMessagingCapabilities method should
 * be overridden accordingly.
 *
 * It might happen that multiple agents are constructed at the same time.
 * Therefore this class should be thread safe. #2953
 *
 * @param <DEBUGGER>   class of the debugger to provide.
 * @param <CONTROLLER> class of the GOALInterpreter to provide.
 */
public abstract class AbstractAgentFactory<DEBUGGER extends Debugger, CONTROLLER extends GOALInterpreter<DEBUGGER>>
		implements AgentFactory<DEBUGGER, CONTROLLER> {
	/**
	 * The general agent registry.
	 */
	private final AgentRegistry<CONTROLLER> registry;
	/**
	 * The timestamp (in millisecond precision) at which the agent should be
	 * terminated; 0 means run indefinately.
	 */
	private final long timeout;
	/**
	 * The definition of the agent that is created.
	 */
	private AgentDefinition agentDf;
	/**
	 * The (actual) id of the agent that is created. May be different from the id
	 * provided in the agent definition.
	 */
	private AgentId agentId;
	/**
	 * Port to the environment in which the agent that is created will be placed.
	 */
	private EnvironmentPort environmentPort;

	/**
	 * Constructs factory without messaging.
	 *
	 * @throws GOALLaunchFailureException
	 */
	protected AbstractAgentFactory(long timeout) throws GOALLaunchFailureException {
		this.timeout = timeout;
		this.registry = new AgentRegistry<>(provideLoggingCapabilities());
	}

	/**
	 * @return The factory's global agent registry.
	 */
	public AgentRegistry<CONTROLLER> getRegistry() {
		return this.registry;
	}

	/**
	 * @return The id of the agent.
	 */
	public AgentId getAgentId() {
		return this.agentId;
	}

	/**
	 * @return The definition of the agent.
	 */
	public AgentDefinition getAgentDf() {
		return this.agentDf;
	}

	/**
	 * @return A port to the environment.
	 */
	public EnvironmentPort getEnvironmentPort() {
		return this.environmentPort;
	}

	@Override
	public Agent<CONTROLLER> build(LaunchInstruction launch, AgentDefinition agentDf, String agentBaseName,
			EnvironmentPort environment, Profiles profiles) throws GOALLaunchFailureException {
		/*
		 * Initialize variables used in agent construction.
		 */
		this.agentDf = agentDf;
		this.environmentPort = environment;
		this.agentId = this.registry.getAgentid(agentBaseName);

		/*
		 * Construct agent components.
		 */
		EnvironmentCapabilities environmentCapabilities = provideEnvironmentCapabilities();
		LoggingCapabilities loggingCapabilities = provideLoggingCapabilities();
		DEBUGGER debugger = provideDebugger();
		Learner learner = provideLearner(launch);
		CONTROLLER controller = provideController(debugger, learner, profiles);
		ExecutorService executor = provideExecutor(this.agentId);

		/*
		 * Construct agent.
		 */
		Agent<CONTROLLER> agent = new Agent<>(this.agentId, environmentCapabilities, loggingCapabilities, controller,
				executor, this.timeout);
		this.registry.register(agent);
		return agent;
	}

	@Override
	public void remove(AgentId agent) {
		this.registry.unregister(agent);
	}

	/**
	 * Creates the environment capabilities used by the agents. Subclasses can
	 * override this method to provide their own environment capabilities.
	 *
	 * @return environment capabilities used by the agent.
	 */
	protected EnvironmentCapabilities provideEnvironmentCapabilities() {
		if (this.environmentPort == null) {
			return new NoEnvironmentCapabilities();
		} else {
			return new DefaultEnvironmentCapabilities(this.agentId, this.environmentPort);
		}
	}

	/**
	 * Creates the logging capabilities used by the agents. Subclasses can override
	 * this method to provide their own logging capabilities.
	 *
	 * @return logging capabilities used by the agent.
	 */
	protected LoggingCapabilities provideLoggingCapabilities() {
		if (this.agentId == null) {
			return new NoLoggingCapabilities();
		} else {
			return new GOALLoggerDelayed(this.agentId.toString(), true);
		}
	}

	/**
	 * Provides the debugger used by the agent.
	 *
	 * @return the debugger used by the agent
	 */
	protected abstract DEBUGGER provideDebugger();

	/**
	 * Provides the learner used by the agent. Subclasses can override this method
	 * to provide their own learner.
	 *
	 * @return the learner used by the agent
	 */
	protected Learner provideLearner(LaunchInstruction launch) {
		return FileLearner.createFileLearner(launch, this.agentDf);
	}

	/**
	 * Provides a controller for running the agent.
	 *
	 * @param debugger created by {@link #provideDebugger()}
	 * @param learner  created by {@link #provideLearner()}
	 * @param profiles the profiles database
	 *
	 * @return the controller used by the agent
	 */
	protected abstract CONTROLLER provideController(DEBUGGER debugger, Learner learner, Profiles profiles);

	protected ExecutorService provideExecutor(final AgentId agent) {
		return Executors.newSingleThreadExecutor(new ThreadFactory() {
			@Override
			public Thread newThread(final Runnable r) {
				final Thread t = new Thread(r, agent.toString());
				t.setPriority(Thread.MIN_PRIORITY + 1);
				return t;
			}
		});
	}
}
