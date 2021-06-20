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
package goal.tools.logging;

import java.util.logging.Logger;

import goal.preferences.LoggingPreferences;
import goal.tools.errorhandling.Warning;

/**
 * Class with references to various static loggers, such as the one for warnings
 * and the one for parser messages. Does not contain references to loggers that
 * are created for each agent's action log and other debug debug output.
 */
public final class Loggers {
	static {
		System.setProperty("java.util.logging.manager", GOALLogManager.class.getName());
	}

	// disable instantiation
	private Loggers() {
	}

	private static GOALLogger infoLogger = new GOALLogger(InfoLog.class.getName(),
			LoggingPreferences.getLogConsolesToFile());
	private static GOALLogger warningLogger = new GOALLogger(Warning.class.getName(),
			LoggingPreferences.getLogConsolesToFile());

	/**
	 * Adds a console logger to all available loggers.
	 */
	public static void addConsoleLogger() {
		for (GOALLogger logger : getAllLoggers()) {
			logger.addConsoleLogger();
		}
	}

	/**
	 * Removes the console logger from all loggers.
	 */
	public static void removeConsoleLogger() {
		for (GOALLogger logger : getAllLoggers()) {
			logger.removeConsoleLogger();
		}
	}

	/**
	 * @return A {@link Logger} for general information
	 */
	public static GOALLogger getInfoLogger() {
		return infoLogger;
	}

	/**
	 * @return The {@link Logger} that logs all {@link Warning}s.
	 */
	public static GOALLogger getWarningLogger() {
		return warningLogger;
	}

	/**
	 * @return An array with all {@link Logger}s obtainable via this class.
	 */
	public static GOALLogger[] getAllLoggers() {
		return new GOALLogger[] { getInfoLogger(), getWarningLogger() };
	}
}
