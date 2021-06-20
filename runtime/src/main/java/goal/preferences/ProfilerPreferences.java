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

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import goal.tools.profiler.InfoType;

public class ProfilerPreferences {
	public enum Pref {
		profiling, profilingToFile, logNodeID
	}

	private static Map<String, Object> preferences;

	/**
	 * Initializes the preference settings. If no initial preference settings
	 * are provided, the default preference settings are used (as if user did
	 * not change any settings).
	 *
	 * @param init
	 *            The settings for initializing the preferences.
	 */
	public static void initPrefs(Map<String, Object> init) {
		if (init == null) {
			preferences = new TreeMap<>();
		} else {
			preferences = init;
		}

		init(Pref.profiling, false);
		init(Pref.profilingToFile, false);
		init(Pref.logNodeID, false);
		for (InfoType type : InfoType.values()) {
			init(type, true);
		}
	}

	public static Map<String, Object> getPrefs() {
		return Collections.unmodifiableMap(preferences);
	}

	/**
	 * @return true iff we should generate a profile
	 */
	public static boolean getProfiling() {
		return (boolean) get(Pref.profiling);
	}

	/**
	 * 
	 * @return true iff generated profile should be saved to a file (instead of
	 *         dumped into the console)
	 */
	public static boolean getProfilingToFile() {
		return (boolean) get(Pref.profilingToFile);
	}

	/**
	 * @return true iff we should log the node IDs
	 */
	public static boolean getLogNodeId() {
		return (boolean) get(Pref.logNodeID);
	}

	/**
	 * @param type
	 *            the {@link InfoType} that might be selected for display
	 * @return true iff the given {@link InfoType} has been selected (for
	 *         display)
	 */
	public static boolean isTypeSelected(InfoType type) {
		return get(type);
	}

	/**
	 * Set the given type to be displayed or not
	 *
	 * @param type
	 *            the {@link InfoType}
	 * @param display
	 *            true to display the given type, false to not display it.
	 */
	public static void setTypeSelected(InfoType type, boolean display) {
		put(type, display);
	}

	/**
	 * Set profiling on or off
	 *
	 * @param enable
	 *            true to enable, false to disable.
	 */
	public static void setProfiling(boolean enable) {
		put(Pref.profiling, enable);
	}

	/**
	 * Store the profiling results to file or not
	 *
	 * @param enable
	 *            true to enable, false to disable.
	 */
	public static void setProfilingToFile(boolean enable) {
		put(Pref.profilingToFile, enable);
	}

	/**
	 * Set logging of node IDs on or off
	 * 
	 * @param enable
	 *            true to enable, false to disable
	 */
	public static void setLogNodeId(boolean enable) {
		put(Pref.logNodeID, enable);
	}

	private static Object get(Pref pref) {
		if (preferences == null) {
			Preferences.initializeAllPrefs();
		}
		return preferences.get(pref.name());
	}

	private static Boolean get(InfoType type) {
		if (preferences == null) {
			Preferences.initializeAllPrefs();
		}
		return (Boolean) preferences.get(type.name());
	}

	private static void put(Pref pref, Object value) {
		if (preferences == null) {
			Preferences.initializeAllPrefs();
		}
		preferences.put(pref.name(), value);
	}

	private static void put(InfoType type, Boolean value) {
		if (preferences == null) {
			Preferences.initializeAllPrefs();
		}
		preferences.put(type.name(), value);
	}

	private static void init(Pref pref, Object defaultValue) {
		Object current = get(pref);
		if (current == null || !current.getClass().equals(defaultValue.getClass())) {
			put(pref, defaultValue);
		}
	}

	private static void init(InfoType type, Boolean defaultValue) {
		Boolean current = get(type);
		if (current == null || !current.getClass().equals(defaultValue.getClass())) {
			put(type, defaultValue);
		}
	}

	/**
	 * Hide constructor.
	 */
	private ProfilerPreferences() {
	}

}
