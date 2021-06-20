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

import java.util.Set;

import languageTools.program.agent.msg.Message;

/**
 * Provides and abstract representation of the ability of the agent to
 * communicate with other agents.
 *
 * An agent needs to be able to exchanges message with other agents.Implementing
 * classes can provide this functionality as they see fit.
 */
public interface MessagingCapabilities {

	/**
	 * Resets any state stored in agent capabilities.
	 */
	public abstract void reset();

	/**
	 * Get all messages from the queue and empties the queue.
	 *
	 * @return all messages in the queue
	 */
	public abstract Set<Message> getAllMessages();

	/**
	 * Posts a message to another agent.
	 *
	 * @param message
	 *            Message added to the out queue.
	 */
	public abstract void postMessage(Message message);

	/**
	 * Release any resources held.
	 */
	public abstract void dispose();

}