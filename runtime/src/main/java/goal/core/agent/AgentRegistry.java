package goal.core.agent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;

import goal.tools.errorhandling.Warning;
import goal.tools.errorhandling.exceptions.GOALLaunchFailureException;
import languageTools.program.agent.AgentId;
import languageTools.program.agent.msg.Message;

/**
 * A registry is a map of agent ids to agents. It is used to keep track of all
 * the agents in a MAS, whether locally or remotely. Agents that run on the same
 * host as the registry are stored in the map by means of their agent id. If an
 * agent runs remotely, the id of that agent is mapped to the remote registry
 * that runs on the remote machine and is made available to the local host.
 * <p>
 * A registry itself is a (special) agent that only maintains a message queue. A
 * registry can receive messages itself in its message queue but that only
 * happens if another registry has sent it a message. A registry sends a message
 * to another registry if it cannot deliver the message itself locally. In order
 * to be able to deliver messages remotely, a registry needs a messaging
 * capability to deliver the message to a remote machine.
 * </p>
 *
 * TODO: (stuff only needed for remote messaging) - The idea is that an agent no
 * longer has a messaging capability, but that a registry has one instead. The
 * messaging capability still needs to be removed from the Agent class and added
 * to the AgentRegistry class. - make available remote registries to be able to
 * store remote agent ids (the remote registry is needed to send a message to
 * the remove agent). - add the registry to the pool of agents and execute it
 * once every while to handle messages that it has received from other (remote)
 * registries and that it should deliver locally.
 */
public class AgentRegistry<CONTROLLER extends Controller> extends Agent<CONTROLLER> {
	/**
	 * Map of the agents that exist in the MAS.
	 */
	private Map<AgentId, Agent<CONTROLLER>> agents = new HashMap<>();
	/**
	 * Map of channels and which agents are subscribed to them.
	 */
	private Map<String, Set<AgentId>> channels = new HashMap<>();

	/**
	 * Creates a registry as a special agent with only messaging and logging
	 * capabilities.
	 *
	 * @throws GOALLaunchFailureException
	 */
	@SuppressWarnings("unchecked")
	public AgentRegistry(LoggingCapabilities logger) throws GOALLaunchFailureException {
		super(new AgentId("registry"), new NoEnvironmentCapabilities(), logger, (CONTROLLER) new NOPController(), null,
				0);
	}

	public AgentId getAgentid(String name) {
		AgentId id = new AgentId(name);
		int serial = 0;
		synchronized (this.agents) {
			while (this.agents.containsKey(id)) {
				id = new AgentId(name + "_" + serial++);
			}
			this.agents.put(id, null); // make reservation
		}
		return id;
	}

	/**
	 * @param aid An agent id.
	 * @return The agent with the given id, or {@code null} if no such agent has
	 *         registered.
	 */
	public Agent<CONTROLLER> getAgent(AgentId aid) {
		synchronized (this.agents) {
			return this.agents.get(aid);
		}
	}

	/**
	 * @return A set of registered agent ids.
	 */
	public Set<AgentId> getRegisteredAgents() {
		synchronized (this.agents) {
			return ImmutableSet.copyOf(this.agents.keySet());
		}
	}

	/**
	 * Registers an agent and adds it into the registry.
	 *
	 * @param agent An agent.
	 */
	public void register(Agent<CONTROLLER> agent) {
		synchronized (this.agents) {
			this.agents.put(agent.getId(), agent);
		}
	}

	/**
	 * Removes an agent from the registry ánd all channels.
	 *
	 * @param aid An agent id.
	 */
	public void unregister(AgentId aid) {
		synchronized (this.agents) {
			this.agents.remove(aid);
		}
		for (String channel : getAllChannels()) {
			unsubscribe(aid, channel);
		}
	}

	/**
	 * Posts a message and inserts it into the mailbox of the agent(s) that should
	 * receive it. Agent might be another registry.
	 *
	 * @param message A message.
	 * @return {@link Warning} if something went wrong, or null if all OK.
	 */
	public Warning postMessage(Message message) {
		List<AgentId> succeeded = new LinkedList<>();
		List<AgentId> failed = new LinkedList<>();
		for (AgentId aid : message.getReceivers()) {
			Agent<?> agent = getAgent(aid);
			if (agent != null) {
				agent.receiveMessage(message);
				succeeded.add(aid);
			} else {
				failed.add(aid);
			}
		}
		if (!failed.isEmpty()) {
			return new Warning("failed to deliver the message '" + message.getContent() + "' to " + failed + ".");
		} else if (succeeded.isEmpty()) {
			return new Warning("there were no receivers for the message '" + message.getContent() + "'.");
		}
		return null;
	}

	// TODO: DOCUMENT THE BELOW FUNCTIONS

	/**
	 * @return A set of registered agent ids.
	 */
	public Set<String> getAllChannels() {
		synchronized (this.channels) {
			return ImmutableSet.copyOf(this.channels.keySet());
		}
	}

	public boolean subscribe(AgentId aid, String channel) {
		synchronized (this.channels) {
			Set<AgentId> current = this.channels.get(channel);
			if (current == null) {
				Set<AgentId> newchannel = new HashSet<>();
				newchannel.add(aid);
				this.channels.put(channel, newchannel);
				return true;
			} else {
				return current.add(aid);
			}
		}
	}

	public boolean unsubscribe(AgentId aid, String channel) {
		synchronized (this.channels) {
			Set<AgentId> current = this.channels.get(channel);
			boolean removed = current != null && current.remove(aid);
			if (removed && current.isEmpty()) {
				this.channels.remove(channel);
			}
			return removed;
		}
	}

	public boolean isChannel(String channel) {
		synchronized (this.channels) {
			return this.channels.containsKey(channel);
		}
	}

	public Set<AgentId> getSubscribers(String channel) {
		synchronized (this.channels) {
			Set<AgentId> current = this.channels.get(channel);
			return (current == null) ? new HashSet<>(0) : ImmutableSet.copyOf(current);
		}
	}
}