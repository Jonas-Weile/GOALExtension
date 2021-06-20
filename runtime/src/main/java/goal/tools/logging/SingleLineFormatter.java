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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import goal.preferences.LoggingPreferences;
import goal.tools.errorhandling.Resources;
import goal.tools.errorhandling.StackHelper;
import goal.tools.errorhandling.WarningStrings;

public class SingleLineFormatter extends Formatter {
	private static DateFormat format = new SimpleDateFormat("HH:mm:ss.SSS");

	@Override
	public String format(LogRecord record) {
		final StringBuilder sb = new StringBuilder();

		sb.append(formatHeader(record)).append("\n");
		if (record.getThrown() != null) {
			sb.append(format(record.getThrown()));
		}

		return sb.toString();
	}

	/**
	 * Format just the heaader, don't append the stackdump. Useful for classes
	 * extending this as the default format() may attach stacktraces too
	 * eagerly.
	 *
	 * @param record
	 * @return
	 */
	public String formatHeader(LogRecord record) {
		final StringBuilder sb = new StringBuilder();

		if (LoggingPreferences.getShowTime()) {
			sb.append(format.format(new Date(record.getMillis()))).append(" ");
		}
		if (record.getLevel().equals(Level.WARNING) || record.getLevel().equals(Level.SEVERE)) {
			sb.append(record.getLevel().getLocalizedName()).append(": ");
		}
		sb.append(formatMessage(record));

		return sb.toString();
	}

	protected String format(Throwable error) {
		StringBuilder message = new StringBuilder();
		message.append(" ").append(error.getClass().getSimpleName());

		String cause = StackHelper.getAllCauses(error);
		if (!cause.isEmpty()) {
			message.append(" ").append(cause);
		}
		message.append("\n");

		// show the stack dump if so desired or if runtime exception
		if (LoggingPreferences.getShowStackdump() || error instanceof RuntimeException) {
			message.append(Resources.get(WarningStrings.STACKDUMP));
			message.append(StackHelper.getFullStackTraceInfo(error));
			// show more java details afterwards, if so desired
		}

		return message.toString();
	}
}