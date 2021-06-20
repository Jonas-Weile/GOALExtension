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

import goal.tools.logging.GOALLogRecord;
import languageTools.program.agent.actions.LogAction;

/**
 * Provides an abstract representation of the logging capabilities of the agent.
 * Mainly meant for the {@link LogAction}.
 *
 * Implementing classes can provide this functionality as they see fit.
 *
 */
public interface LoggingCapabilities {
	/**
	 * Returns the name of the logger.
	 *
	 * @return The name of the logger.
	 */
	public String getName();

	/**
	 * Logs a {@link GOALLogrecord}.
	 *
	 * @param record The log record to be logged.
	 */
	public void log(GOALLogRecord record);

	/**
	 * Logs a plain text message.
	 *
	 * @param message The string to be logged.
	 */
	void log(String message);

	/**
	 * Dispose the logger (clean-up resources)
	 */
	void dispose();
}
