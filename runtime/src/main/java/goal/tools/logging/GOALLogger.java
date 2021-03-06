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

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

import goal.core.agent.LoggingCapabilities;
import goal.preferences.LoggingPreferences;
import goal.tools.errorhandling.Resources;
import goal.tools.errorhandling.Warning;
import goal.tools.errorhandling.WarningStrings;

/**
 * The GOAL logging tool.
 * <p>
 * The standard {@link java.util.logging.Logger} requires quite a long chain of
 * components requiring piping of the messages through various layers before the
 * user can reading it. This is nice as long as all works but fatal if a part of
 * the chain hangs, or if these layers are not in place to start with (when
 * running a stand-alone GOAL without IDE). See also trac 1163 and 1191.
 * </p>
 * <p>
 * Therefore we use the private field {@link #showLogsInIDE} to turn this all
 * off at right at this point. When that property is true, all logging info is
 * piped through a set of {@link java.util.logging.Logger}s. If that property is
 * false, the log info is printed directly to {@link System.out}.
 * </p>
 * <p>
 * We can't call the constructor of {@link java.util.logging.Logger} therefore
 * we need to wrap the logger as a field of our logger class, deviating from the
 * Java version in this respect.
 * </p>
 * <p>
 * Logging to file is also handled here. The default {@link FileHandler}
 * settings are used. Files are written to the user's home directory. If you
 * turn off the logging-rerouting, log to file is also disabled.
 * </p>
 * <p>
 * Tech note: we don't really care about having unique GOALLoggers for a given
 * name, as all is in the end routed through the unique logging.Logger anyway,
 * and if the output is printed directly instead it also does not matter to have
 * a unique logger.
 * </p>
 */
public class GOALLogger implements LoggingCapabilities {
	/**
	 * The logger used by this {@link GOALLogger}.
	 */
	private final Logger logger;
	/**
	 * null unless we log to file
	 */
	protected FileHandler fileHandler = null;
	/**
	 * Logs messages to the console.
	 */
	private final Handler consoleHandler;

	/**
	 * Creates a logger.
	 *
	 * @param name                 The name of the logger. Should be indicative of
	 *                             its main function.
	 * @param eligibleForLogToFile TODO: make this fully dependent on user's
	 *                             choices. must be set to {@code true} if this
	 *                             logger can write the logs also to file. Whether
	 *                             logs are written to file also depends on the
	 *                             user's preferences.
	 */
	public GOALLogger(String name, boolean eligibleForLogToFile) {
		this.logger = Logger.getLogger(name);
		// do not use any parent handlers, they print too much.
		this.logger.setUseParentHandlers(false);
		if (eligibleForLogToFile && LoggingPreferences.getLogToFile()) {
			addLogToFileHandler();
		}
		this.consoleHandler = new GOALConsoleHandler();
	}

	@Override
	public void dispose() {
		removeConsoleLogger();
		removeLogToFileHandler();
	}

	@Override
	public String getName() {
		return this.logger.getName();
	}

	@Override
	public void log(GOALLogRecord record) {
		if (record.getMessage() != null && !record.getMessage().isEmpty()) {
			this.logger.log(record);
		}
	}

	@Override
	public void log(String message) {
		if (message != null && !message.isEmpty()) {
			this.logger.warning(message);
		}
	}

	/**
	 * Add a log Handler to receive logging messages. see
	 * {@link Logger#addHandler(Handler)}
	 *
	 * @param handler a logging Handler
	 */
	public void addHandler(Handler handler) {
		this.logger.addHandler(handler);
	}

	/**
	 * Adds a handler to this {@link GOALLogger} for writing to a log file.
	 */
	protected void addLogToFileHandler() {
		try {
			String dirname = LoggingPreferences.getLogDirectory();
			new File(dirname).mkdirs();
			DateFormat format = new SimpleDateFormat("yy-MM-dd_HH.mm.ss");
			String fname = dirname + "/" + this.logger.getName()
					+ (LoggingPreferences.getOverwriteFile() ? "" : "_" + format.format(new Date())) + ".csv";
			this.fileHandler = new CsvFileHandler(fname);
			addHandler(this.fileHandler);
		} catch (IOException | SecurityException e) {
			new Warning(String.format(Resources.get(WarningStrings.FAILED_LOG_TO_FILE), this.logger.getName()), e)
					.emit();
		}
	}

	/**
	 * Removes a handler from the list of handlers that receive log messages from
	 * this logger.
	 *
	 * @param handler The logging handler to remove.
	 */
	public void removeHandler(Handler handler) {
		this.logger.removeHandler(handler);
	}

	/**
	 * Do not log to file any more (if we did)
	 */
	public void removeLogToFileHandler() {
		if (this.fileHandler != null) {
			this.logger.removeHandler(this.fileHandler);
			this.fileHandler.close();
			this.fileHandler = null;
		}
	}

	/**
	 * Adds a handler to the logger that will print all messages to the console.
	 */
	public void addConsoleLogger() {
		this.logger.addHandler(this.consoleHandler);
	}

	/**
	 * Stops messages being printed to the console.
	 */
	public void removeConsoleLogger() {
		this.logger.removeHandler(this.consoleHandler);
	}
}