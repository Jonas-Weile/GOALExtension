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

import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import goal.tools.errorhandling.Warning;

/**
 * Small wrapper around {@link LogRecord} to also store a {@link Throwable}
 * cause of the record.<br>
 * Known subclasses are {@link Warning} and {@link GOALBugReport}.
 */
public abstract class GOALLogRecord extends LogRecord {
	/** auto-generated serial version UID */
	private static final long serialVersionUID = -1287434316588218924L;
	/**
	 * The cause of this record to be created.
	 */
	private final Throwable cause;

	/**
	 * Creates a new {@link GOALLogRecord}.
	 *
	 * @param level   The level of the record to be created.
	 * @param message The message in the record to be created.
	 * @param cause   The reason why the record is to be created. May be null or a
	 *                DummyException.
	 */
	protected GOALLogRecord(Level level, String message, Throwable cause) {
		super(level, message);
		this.cause = cause;
		setThrown(cause); // default logger uses this to show causes. #3078
	}

	/**
	 * Returns the cause of the logging record.
	 *
	 * @return The cause of this record to be created. May be {@code null}.
	 */
	public Throwable getCause() {
		return this.cause;
	}

	/**
	 * @return A {@link Formatter} to properly format this record.
	 */
	public abstract Formatter getFormatter();

	/**
	 * Emit to given {@link GOALLogger}s. If none are given this logs to the
	 * InfoLogger
	 * 
	 * @param goalLoggers
	 */
	public void emit() {
		Loggers.getInfoLogger().log(this);
	}

	@Override
	public String toString() {
		return getFormatter().formatMessage(this);
	}
}
