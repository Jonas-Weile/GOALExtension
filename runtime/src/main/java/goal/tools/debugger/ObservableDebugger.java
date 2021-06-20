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
package goal.tools.debugger;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import events.Channel;
import goal.core.agent.GOALInterpreter;
import goal.core.runtime.RuntimeManager;
import goal.core.runtime.service.environmentport.EnvironmentPort;
import krTools.parser.SourceInfo;
import languageTools.program.agent.AgentId;

public class ObservableDebugger extends SteppingDebugger {
	/**
	 * Maintains a map of which observers have subscribed to which channels.
	 */
	protected final Map<Channel, Set<DebugObserver>> channelObservers;

	public ObservableDebugger(AgentId id,
			RuntimeManager<?, ? extends GOALInterpreter<? extends SteppingDebugger>> manager, EnvironmentPort env) {
		this(id.toString(), manager, env);
	}

	public ObservableDebugger(String id,
			RuntimeManager<?, ? extends GOALInterpreter<? extends SteppingDebugger>> manager, EnvironmentPort env) {
		super(id, manager, env);
		// Initialize channel to observer mapping.
		Channel[] channels = Channel.values();
		this.channelObservers = new LinkedHashMap<>(channels.length);
		for (Channel channel : channels) {
			this.channelObservers.put(channel, new LinkedHashSet<DebugObserver>());
		}
	}

	@Override
	public void breakpoint(Channel channel, Object associateObject, SourceInfo associateSource, String message,
			Object... args) {
		// Only if there are observers for the channel, events need to be send.
		if (!this.channelObservers.get(channel).isEmpty()) {
			DebugEvent event = new DebugEvent(getRunMode(), getName(), channel, associateObject, associateSource,
					message, args);
			if (notifyObservers(channel, event) == false) {
				return;
			}
		}

		super.breakpoint(channel, associateObject, associateSource, message, args);
	}

	/**
	 * Updates observers that subscribed to a channel with the debug information
	 * related to that channel.
	 *
	 * @param channel Channel to send information on.
	 * @param event   Debug event provided to observers subscribed to the channel.
	 */
	protected boolean notifyObservers(Channel channel, DebugEvent event) {
		boolean result = true;
		for (DebugObserver observer : this.channelObservers.get(channel)) {
			result &= observer.notifyBreakpointHit(event);
		}
		return result;
	}

	@Override
	protected void setRunMode(RunMode mode) {
		// notify observers of run mode change.
		if (mode != getRunMode()) {
			notifyObservers(Channel.RUNMODE,
					new DebugEvent(mode, getName(), Channel.RUNMODE, mode, null, "run mode is now '%s'.", mode));
		}
		super.setRunMode(mode);
	}

	/**
	 * add channel to viewed channels. Your observer will be notified when debug
	 * event happens on that channel.
	 *
	 * @param observer
	 * @param channel
	 *
	 * @throws NullPointerException If the given observer is not subscribed to this
	 *                              {@link SteppingDebugger} (or the given observer
	 *                              is a {@link BreakpointObserver}).
	 */
	public void subscribe(DebugObserver observer, Channel channel) {
		this.channelObservers.get(channel).add(observer);
	}

	/**
	 * <p>
	 * Removes the observer from the list of registered observers.
	 * </p>
	 *
	 * @param observer observer to be removed from registered observer list.
	 */
	public void unsubscribe(DebugObserver observer) {
		for (Channel channel : this.channelObservers.keySet()) {
			this.channelObservers.get(channel).remove(observer);
		}
	}

	/**
	 * remove channel from viewed channels. Your observer not will be notified
	 * anymore when debug event happens on that channel.
	 *
	 * @param observer is observer that wants to stop viewing the channel
	 * @param channel  is channel to be removed from view.
	 *
	 * @throws NullPointerException If the given observer is not subscribed to this
	 *                              {@link SteppingDebugger} (or the given observer
	 *                              is a {@link BreakpointObserver}).
	 */
	public void unsubscribe(DebugObserver observer, Channel channel) {
		this.channelObservers.get(channel).remove(observer);
	}

	/**
	 * Check if observer is viewing given channel.
	 *
	 * @param observer is observer that might be viewing
	 * @param channel  is channel that might be under observation.
	 * @return true if under observation, false if not.
	 *
	 * @throws NullPointerException If the given observer is not subscribed to this
	 *                              {@link SteppingDebugger} (or the given observer
	 *                              is a {@link BreakpointObserver}).
	 */
	public boolean isViewing(DebugObserver observer, Channel channel) {
		return this.channelObservers.get(channel).contains(observer);
	}

	@Override
	protected boolean checkUserBreakpointHit(SourceInfo source, Channel channel) {
		boolean hit = super.checkUserBreakpointHit(source, channel);
		if (hit) {
			DebugEvent event = new DebugEvent(getRunMode(), getName(), Channel.BREAKPOINTS, null, source,
					"hit a user-defined breakpoint.");
			notifyObservers(Channel.BREAKPOINTS, event);
		}
		return hit;
	}

	@Override
	public String toString() {
		return super.toString() + "\nObservers per channel:\n" + this.channelObservers.toString();
	}
}
