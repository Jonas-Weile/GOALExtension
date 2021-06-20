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

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import goal.core.runtime.service.agent.RunState;

public class LoggingPreferences {
	public enum Pref {
		stackdump, logtofile, logconsoles, overwritelogfiles, showlogtime, logdirectory, showLogsInConsole,
		/* */
		includeStackInLogs, eclipseActionHistory, eclipseAgentConsoles, eclipseDebug, printStats,
		/**
		 * Store action history, for rollback to previous GOAL {@link RunState}
		 */
		enableHistory
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

		init(Pref.overwritelogfiles, false);
		init(Pref.logtofile, false);
		init(Pref.logconsoles, false);
		init(Pref.logdirectory, System.getProperty("user.home") + File.separator + "GOAL");
		init(Pref.showlogtime, false);
		init(Pref.stackdump, false);
		init(Pref.showLogsInConsole, false);
		init(Pref.includeStackInLogs, true);
		init(Pref.eclipseActionHistory, true);
		init(Pref.eclipseAgentConsoles, true);
		init(Pref.eclipseDebug, false);
		init(Pref.printStats, false);
		init(Pref.enableHistory, false);
	}

	public static Map<String, Object> getPrefs() {
		return Collections.unmodifiableMap(preferences);
	}

	/**
	 * User preference whether logging should overwrite old files
	 *
	 * @return true if logging should overwrite old files. Default off/false
	 */
	public static boolean getOverwriteFile() {
		return (Boolean) get(Pref.overwritelogfiles);
	}

	/**
	 * User preference whether logging should be put into files
	 *
	 * @return true if logging to file was requested. Default off/false
	 */
	public static boolean getLogToFile() {
		return (Boolean) get(Pref.logtofile);
	}

	/**
	 * User preference whether the default consoles should be logged to file by
	 * default
	 *
	 * @return true if logging the console logging was requested. Default
	 *         off/false
	 */
	public static boolean getLogConsolesToFile() {
		return (Boolean) get(Pref.logconsoles);
	}

	/**
	 * Get the current log directory (as string, which is compatible with the
	 * preferences system).
	 *
	 * @return log directory
	 */
	public static String getLogDirectory() {
		return (String) get(Pref.logdirectory);
	}

	/**
	 * User preference whether printout of logs should show time
	 *
	 * @return true if logging of time was requested. default off/false.
	 */
	public static boolean getShowTime() {
		return (Boolean) get(Pref.showlogtime);
	}

	/**
	 * User preference whether stack traces should be shown with warning
	 * messages. False by default, initialized by IDE (see SimpleIDE).
	 *
	 * @return true if stack dump should be shown with warnings.
	 */
	public static boolean getShowStackdump() {
		return (Boolean) get(Pref.stackdump);
	}

	/**
	 * Routes logging output to console. This option is not offered as a choice
	 * to a user via the preferences menu because this is supposed to be used by
	 * developers only. Change by means of settings file; false by default.
	 *
	 * @return {@code true} if logs should be displayed in console.
	 */
	public static boolean getShowLogsInConsole() {
		return (Boolean) get(Pref.showLogsInConsole);
	}

	public static boolean getIncludeStackInLogs() {
		return (Boolean) get(Pref.includeStackInLogs);
	}

	public static boolean getEclipseActionHistory() {
		return (Boolean) get(Pref.eclipseActionHistory);
	}

	public static boolean getEclipseAgentConsoles() {
		return (Boolean) get(Pref.eclipseAgentConsoles);
	}

	public static boolean getEclipseDebug() {
		return (Boolean) get(Pref.eclipseDebug);
	}

	public static boolean getPrintStats() {
		return (Boolean) get(Pref.printStats);
	}

	/**
	 * 
	 * @return true if runtime should store action history, for rollback to
	 *         previous GOAL {@link RunState}
	 */
	public static boolean getEnableHistory() {
		return (Boolean) get(Pref.enableHistory);
	}

	/**
	 * User preference whether logging should overwrite old files. False by
	 * default.
	 */
	public static void setOverwriteFile(boolean overwritelogfiles) {
		put(Pref.overwritelogfiles, overwritelogfiles);
	}

	/**
	 * User preference whether logging should be put into files. True by
	 * default.
	 */
	public static void setLogToFile(boolean logtofile) {
		put(Pref.logtofile, logtofile);
	}

	/**
	 * User preference whether the consoles should be logged to file. False by
	 * default.
	 */
	public static void setLogConsolesToFile(boolean logconsoles) {
		put(Pref.logconsoles, logconsoles);
	}

	/**
	 * Current log directory (as string, which is compatible with the
	 * preferences system).
	 */
	public static void setLogDirectory(String logdirectory) {
		put(Pref.logdirectory, logdirectory);
	}

	/**
	 * User preference whether printout of logs should show time. False by
	 * default.
	 */
	public static void setShowTime(boolean showlogtime) {
		put(Pref.showlogtime, showlogtime);
	}

	/**
	 * User preference whether stack traces should be shown with warning
	 * messages. False by default.
	 */
	public static void setShowStackdump(boolean stackdump) {
		put(Pref.stackdump, stackdump);
	}

	public static void setIncludeStackInLogs(boolean include) {
		put(Pref.includeStackInLogs, include);
	}

	public static void setEclipseActionHistory(boolean enable) {
		put(Pref.eclipseActionHistory, enable);
	}

	public static void setEclipseAgentConsoles(boolean enable) {
		put(Pref.eclipseAgentConsoles, enable);
	}

	public static void setEclipseDebug(boolean debug) {
		put(Pref.eclipseDebug, debug);
	}

	public static void setPrintStats(boolean printStats) {
		put(Pref.printStats, printStats);
	}

	public static void setEnableHistory(boolean enable) {
		put(Pref.enableHistory, enable);
	}

	// 3 helper functions...
	private static Object get(Pref pref) {
		if (preferences == null) {
			Preferences.initializeAllPrefs();
		}
		return preferences.get(pref.name());
	}

	private static void put(Pref pref, Object value) {
		if (preferences == null) {
			Preferences.initializeAllPrefs();
		}
		preferences.put(pref.name(), value);
	}

	private static void init(Pref pref, Object defaultValue) {
		Object current = get(pref);
		if (current == null || !current.getClass().equals(defaultValue.getClass())) {
			put(pref, defaultValue);
		}
	}

	/**
	 * Hide constructor.
	 */
	private LoggingPreferences() {
	}
}
