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

/**
 * A {@link GOALLogger} that waits with creating a log file till the first log
 * is actually made. This logger ALWAYS writes to file, it ignores the
 * LoggingPreferences.getLogToFile() settings #3618.
 */
public class GOALLoggerDelayed extends GOALLogger {
	private final boolean eligibleForLogToFile;

	public GOALLoggerDelayed(String name, boolean eligibleForLogToFile) {
		super(name, false);
		this.eligibleForLogToFile = eligibleForLogToFile;
	}

	private void checkAttachLogFile() {
		if (this.eligibleForLogToFile && this.fileHandler == null) {
			addLogToFileHandler();
		}
	}

	/**
	 * Logs a {@link GOALLogrecord}.
	 *
	 * @param record
	 *            The log record to be logged.
	 */
	@Override
	public void log(GOALLogRecord record) {
		checkAttachLogFile();
		super.log(record);
	}

	@Override
	public void log(String message) {
		checkAttachLogFile();
		super.log(message);
	}
}
