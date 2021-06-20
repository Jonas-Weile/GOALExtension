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

import java.util.logging.LogRecord;

import goal.tools.logging.SingleLineFormatter;

/**
 * Formatter for a {@link ExceptionLogRecord}. Replaces old GOALBugReport and
 * WarningFormatter.
 */
public class ExceptionLogFormatter extends SingleLineFormatter {
	@Override
	public String format(LogRecord record) {
		if (!(record instanceof ExceptionLogRecord)) {
			throw new IllegalArgumentException("ExceptionFormatter can only format ExceptionLogRecord but received '"
					+ record.getClass().getCanonicalName() + "'.");
		}
		// Get content of warning.
		ExceptionLogRecord elRecord = (ExceptionLogRecord) record;
		StringBuilder message = new StringBuilder();
		message.append(formatHeader(elRecord));
		if (elRecord.getCause() != null) {
			message.append(format(elRecord.getCause()));
		}
		message.append("\n");
		return message.toString();
	}
}