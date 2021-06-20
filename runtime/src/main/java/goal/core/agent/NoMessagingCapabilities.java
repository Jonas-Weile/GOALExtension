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

import java.util.HashSet;
import java.util.Set;

import languageTools.program.agent.msg.Message;

/**
 * NOP implementation for messaging. No messages will be received, all send
 * messages disappear.
 */
public class NoMessagingCapabilities implements MessagingCapabilities {

	@Override
	public void reset() {
		// Does nothing.
	}

	@Override
	public Set<Message> getAllMessages() {
		return new HashSet<>(0);
	}

	@Override
	public void postMessage(Message message) {
		// Does nothing.
	}

	@Override
	public void dispose() {
		// Does nothing.
	}

}