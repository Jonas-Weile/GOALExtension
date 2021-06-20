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
package goal.preferences;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import events.Channel;
import events.Channel.ChannelState;

/**
 * Stores the users/application's debug preferences. Some apps have very strict
 * settings (GOAL-Eclipse), others (SimpleIDE) allow modifying most.
 */
public class DebugPreferences {
	private static List<PropertyChangeListener> listeners = new LinkedList<>();
	private static Map<String, Object> preferences;
	private static Map<String, Object> defaultSettings = null;

	/**
	 * This function must be called to initialize GOAL, before running.
	 * 
	 * @param defaultSet the default debug settings for GOAL. Actually
	 *                   Set<String,String>. This is used when there is no available
	 *                   preferences file
	 */

	public static void setDefault(Map<String, Object> defaultSet) {
		for (Channel channel : Channel.values()) {
			String valStr = (String) defaultSet.get(channel.name());
			if (valStr == null) {
				throw new IllegalArgumentException("defaultSet is missing value for key=" + channel.name());
			}
			try {
				ChannelState.valueOf(valStr);
			} catch (IllegalArgumentException e) {
				throw new IllegalArgumentException("defaultSet value " + valStr + "is not a proper value ");
			}
		}
		defaultSettings = defaultSet;
	}

	/**
	 * Force given settings to use in the system. Will be called by Preferences
	 * 
	 * @param init the actual settings, actually a Map<String,String>. If null, we
	 *             load the default settings.
	 */
	public static void initPrefs(Map<String, Object> init) {
		if (defaultSettings == null) {
			throw new IllegalStateException("DebugPreferences.setDefault has not been called");
		}
		if (init == null) {
			preferences = new TreeMap<>(defaultSettings);
		} else {
			preferences = init;
		}
	}

	/**
	 * Reset all channels to default state.
	 */
	public static void reset() {
		for (Channel c : Channel.values()) {
			setChannelState(c, ChannelState.valueOf((String) defaultSettings.get(c.name())));
		}
	}

	public static Map<String, Object> getPrefs() {
		return Collections.unmodifiableMap(preferences);
	}

	/**
	 * Gets the preferred {@link ChannelState} for the given {@link Channel}.
	 *
	 * @param channel The {@link Channel} to get the state for.
	 * @return The preferred {@link ChannelState} of the given {@link Channel}, or
	 *         the default state for the given channel if there is no preference.
	 */
	public static ChannelState getChannelState(Channel channel) {
		return get(channel);
	}

	/**
	 * Sets the {@link ChannelState} for the given {@link Channel}.
	 * 
	 * @param channel The channel to set the state for.
	 * @param state   The desired state of the channel.
	 */
	public static void setChannelState(Channel channel, ChannelState state) {
		String oldValue = getChannelState(channel).name();
		put(channel, state);

		// Notify listeners of changes.
		for (PropertyChangeListener listener : listeners) {
			listener.propertyChange(new PropertyChangeEvent(channel, channel.toString(), oldValue, state.toString()));
		}
	}

	/**
	 * Subscribe a listener to the Debug preferences, so that it will get a
	 * notification via a call to when the preferences are changed.
	 *
	 * @param listener
	 */
	public static void addChangeListener(PropertyChangeListener listener) {
		listeners.add(listener);
	}

	/**
	 * unsubscribe as listener
	 *
	 * @param listener
	 */
	public static void removeChangeListener(PropertyChangeListener listener) {
		listeners.remove(listener);
	}

	// 3 helper functions...
	private static ChannelState get(Channel pref) {
		if (preferences == null) {
			Preferences.initializeAllPrefs();
		}
		String get = (String) preferences.get(pref.name());
		if (get == null || get.isEmpty()) {
			return ChannelState.NONE;
		} else {
			return ChannelState.valueOf(get);
		}
	}

	private static void put(Channel pref, ChannelState value) {
		if (preferences == null) {
			Preferences.initializeAllPrefs();
		}
		preferences.put(pref.name(), value.name());
	}

	/**
	 * Hide constructor.
	 */
	private DebugPreferences() {
	}

}