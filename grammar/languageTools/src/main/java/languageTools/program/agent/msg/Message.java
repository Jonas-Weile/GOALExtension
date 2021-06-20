/**
 * The GOAL Grammar Tools. Copyright (C) 2014 Koen Hindriks.
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

package languageTools.program.agent.msg;

import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import krTools.language.Substitution;
import krTools.language.Update;
import krTools.language.Var;
import languageTools.program.agent.AgentId;

/**
 * A message consists of a sender, a receiver, a {@link SentenceMood} of the
 * message, and the content.
 */
public class Message implements Serializable {
	private static final long serialVersionUID = 1776221600909495302L;
	/**
	 * The name of the agent that sends this message.
	 */
	private AgentId sender = null;
	/**
	 * The name(s) of the agent(s) that should receive this message.
	 */
	private Set<AgentId> receivers = new LinkedHashSet<>(0);
	/**
	 * The mood of the message; either <i>indicative</i>, <i>imperative</i> or
	 * <i>interrogative</i>.
	 */
	private SentenceMood mood;
	/**
	 * Content of the message to be sent.
	 */
	private Update content;
	/**
	 * A cache of free variables in the content of the message.
	 */
	private Set<Var> free = new LinkedHashSet<>(0);

	/**
	 * Creates a {@link Message} with content and mood. Sender and receiver cannot
	 * be established at compile time and are determined at runtime.
	 *
	 * @param content
	 *            The content of this message.
	 * @param mood
	 *            The mood of this message.
	 */
	public Message(Update content, SentenceMood mood) {
		setContent(content);
		setMood(mood);
	}

	/**
	 * Creates a {@link Message} with sender, receiver, content and mood.
	 *
	 * @param sender
	 *            The name of the sender of this message.
	 * @param receiver
	 *            The name of the receiver of this message.
	 * @param content
	 *            The content of this message.
	 * @param mood
	 *            The mood of this message.
	 */
	private Message(AgentId sender, Set<AgentId> receivers, Update content, SentenceMood mood) {
		setSender(sender);
		setReceivers(receivers);
		setContent(content);
		setMood(mood);
	}

	/**
	 * Returns the name of the agent who sends this {@link Message}.
	 *
	 * @return The name of the sender of this message.
	 */
	public AgentId getSender() {
		return this.sender;
	}

	/**
	 * Sets the name of the agent who sends this {@link Message}.
	 *
	 * @param sender
	 *            A {@link String} representing the name of the sender of this
	 *            message.
	 */
	public void setSender(AgentId sender) {
		this.sender = sender;
	}

	/**
	 * Returns the name of the agent who should receive this {@link Message}.
	 *
	 * @return The name of the receiver of this message.
	 */
	public Set<AgentId> getReceivers() {
		return Collections.unmodifiableSet(this.receivers);
	}

	/**
	 * Sets the names of agents who should receive this {@link Message}.
	 *
	 * @param receivers
	 *            The set of names of receivers of this message.
	 */
	public void setReceivers(Set<AgentId> receivers) {
		this.receivers = receivers;
	}

	/**
	 * @return The (sendtence) mood of this message.
	 */
	public SentenceMood getMood() {
		return this.mood;
	}

	/**
	 * @param mood
	 *            The (sentence) mood of this message.
	 */
	public void setMood(SentenceMood mood) {
		this.mood = mood;
	}

	/**
	 * @return The content of this message.
	 */
	public Update getContent() {
		return this.content;
	}

	/**
	 *
	 * @param content
	 */
	public void setContent(Update content) {
		this.content = content;
		this.free = this.content.getFreeVar();
	}

	/**
	 * Returns the (free) variables that occur in the content of this message.
	 *
	 * @return The (free) variables that occur in the content of the message.
	 */
	public Set<Var> getFreeVar() {
		return Collections.unmodifiableSet(this.free);
	}

	/**
	 * A message is considered to be closed if its content is closed, or if it is an
	 * interrogative (and variables are allowed in the message content).
	 *
	 * @return {@code true} if the content of the message does not have any free
	 *         variables, or the message is an interrogative; {@code false}
	 *         otherwise.
	 */
	public boolean isClosed() {
		return this.content.isClosed() || this.mood == SentenceMood.INTERROGATIVE;
	}

	/**
	 * Applies a substitution to the content of this message.
	 *
	 * @param substitution
	 *            Substitution for instantiating (free) variables in the message
	 *            content.
	 * @return A message with content instantiated by applying the substitution.
	 */
	public Message applySubst(Substitution substitution) {
		return new Message(getSender(), getReceivers(),
				(getContent() == null) ? null : getContent().applySubst(substitution), getMood());
	}

	@Override
	public String toString() {
		// Construct string from content and mood.
		String sender = this.sender + ": ";
		switch (this.mood) {
		case IMPERATIVE:
			return sender + "!" + getContent();
		case INTERROGATIVE:
			return sender + "?" + getContent();
		default: // suppress mood expression for indicatives.
			return sender + getContent();
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.content == null) ? 0 : this.content.hashCode());
		result = prime * result + ((this.mood == null) ? 0 : this.mood.hashCode());
		result = prime * result + ((this.receivers == null) ? 0 : this.receivers.hashCode());
		result = prime * result + ((this.sender == null) ? 0 : this.sender.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || !(obj instanceof Message)) {
			return false;
		}
		Message other = (Message) obj;
		if (this.mood != other.mood) {
			return false;
		}
		if (this.sender == null) {
			if (other.sender != null) {
				return false;
			}
		} else if (!this.sender.equals(other.sender)) {
			return false;
		}
		if (this.receivers == null) {
			if (other.receivers != null) {
				return false;
			}
		} else if (!this.receivers.equals(other.receivers)) {
			return false;
		}
		if (this.content == null) {
			if (other.content != null) {
				return false;
			}
		} else if (!this.content.equals(other.content)) {
			return false;
		}
		return true;
	}

	/**
	 * Returns a copy of this message.
	 *
	 * @return A copy of this message.
	 */
	@Override
	public Message clone() {
		return new Message(this.sender, this.receivers, this.content, this.mood);
	}
}
