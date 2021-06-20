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
package goal.tools.errorhandling;

import java.util.logging.Formatter;
import java.util.logging.Level;

import goal.preferences.LoggingPreferences;
import goal.tools.debugger.Debugger;
import goal.tools.logging.GOALLogRecord;
import goal.tools.logging.GOALLogger;
import goal.tools.logging.Loggers;

/**
 * <p>
 * Contains a log record for an exception. This log record will be passed to the
 * logger. The logger then will request the ExceptionFormatter to format the
 * exception for display.
 *
 * <br>
 *
 * This LogRecord can also be (ab)used to just show general warning messages. In
 * that case, pass stacktrace null.
 */
public class ExceptionLogRecord extends GOALLogRecord {
	private static final long serialVersionUID = 7973818628779319163L;

	enum ShowMode {
		/* just show it */
		SHOW,
		/* don't show it */
		SUPPRESS
	};

	private ShowMode showmode = ShowMode.SHOW;

	@Override
	public Formatter getFormatter() {
		return new ExceptionLogFormatter();
	}

	/**
	 * Creates a new log record for given exception. The log record is then
	 * pushed to the logging system for reporting.
	 *
	 * @param warning
	 *            The message to be printed. If this is null, the message "(no
	 *            explanation available)" will be used. (Trac 1048)
	 * @param error
	 *            The exception that was the cause of this warning.
	 */
	public ExceptionLogRecord(String warning, Throwable error) {
		super(error instanceof RuntimeException ? Level.SEVERE : Level.WARNING, warning, error);
		// handle null cases (trac #1048)
		if (warning == null) {
			warning = "(no explanation available)";
		}
		if (!(error instanceof RuntimeException)) {
			this.showmode = checkOccurences(warning);
		}
	}

	/**
	 * Check if this warning should be shown. Notice that runtime exceptions
	 * should always be shown anyway and for those this function should not be
	 * called.
	 *
	 * @param message
	 *            the general message to show
	 * @return ShowMode what to do with this warning message.
	 */
	private ShowMode checkOccurences(String message) {
		// do not try to log a warning when nothing is going to be displayed
		if (message.length() == 0 && !LoggingPreferences.getShowStackdump()) {
			return ShowMode.SUPPRESS;
		} else {
			return ShowMode.SHOW;
		}
	}

	/**
	 * get the show mode for this log record.
	 *
	 * @return {@link ShowMode}.
	 */
	public ShowMode getShowMode() {
		return this.showmode;
	}

	/**
	 * Logs a warning message if the message has not already been printed more
	 * often than suppression threshold. Also supports printing stack trace
	 * information of the related cause (only if the user preferences indicate
	 * this information should be printed).
	 *
	 * @param debugger
	 *            The debugger in control of the call.
	 * @param warning
	 *            The message to be printed. If this is null, the message "(no
	 *            explanation available)" will be used. (Trac 1048) The message
	 *            will be prefixed with [DEBUGGERNAME].
	 * @param cause
	 *            The original stack trace.
	 * @param error
	 *            The exception that was the cause of this warning.
	 */
	public ExceptionLogRecord(Debugger debugger, String warning, Throwable cause) {
		this(String.format("[%1$s] %2$s", debugger.getName(), warning == null ? "(no explanation available)" : warning),
				cause);
	}

	/**
	 * Emit to given {@link GOALLogger}s. If none are given this logs to the
	 * WarningLogger
	 * 
	 * @param goalLoggers
	 */
	@Override
	public void emit() {
		Loggers.getWarningLogger().log(this);
	}
}