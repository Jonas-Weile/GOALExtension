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

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import events.Channel;
import events.Channel.ChannelState;
import goal.preferences.DebugPreferences;

/**
 * This listener listens to the debug settings in the {@link Preferences} and
 * copies the pause settings to the agent's debugger.
 *
 * This object is owned by the {@link MonitoringService}.
 */
public class DebugSettingSynchronizer implements PropertyChangeListener {
	private final SteppingDebugger debugger;

	/**
	 * Creates a debug observer.
	 *
	 * @param name     name to be assigned to debug observer.
	 * @param debugger debugger to which the observer has been subscribed.
	 */
	public DebugSettingSynchronizer(SteppingDebugger debugger) {
		this.debugger = debugger;

		// Subscribe to debug preferences.
		DebugPreferences.addChangeListener(this);

		/*
		 * Initialize channel settings using user preference settings. Note, this debug
		 * observer is used to control the run mode of the agent, we are not interested
		 * in viewing, except that we need to handle run mode changes.
		 */
		for (Channel channel : Channel.values()) {
			if (DebugPreferences.getChannelState(channel).shouldPause()) {
				debugger.addPause(channel);
			}
		}
	}

	/**
	 *
	 */
	public void stop() {
		DebugPreferences.removeChangeListener(this);
	}

	/**
	 * Catch changes in preferences in DebugPreferencePanel, so that pause actions
	 * can be handled. See
	 * {@link goal.tools.SimpleIDE.preferences.DebugPreferencePane}.
	 */
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		try {
			Channel channel = Channel.valueOf(evt.getPropertyName());
			if (ChannelState.valueOf(evt.getNewValue().toString()).shouldPause()) {
				this.debugger.addPause(channel);
			} else {
				this.debugger.removePause(channel);
			}
		} catch (IllegalArgumentException ignore) {
		}
	}
}
