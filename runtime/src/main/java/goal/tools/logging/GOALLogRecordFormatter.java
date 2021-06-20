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
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

/**
 * This class is a stop gap measure to deal with the curious use
 * {@link Formatter} by {@link GOALLogRecord}s. LoggingRecords allow a message
 * and parameters to be passed on to the logging system. The {@link Formatter}s
 * then format these by pushing the parameters into the message, adding a time
 * stamp and other info. GOALLogrecords however provide their own formatter
 * which create the message.
 */
public class GOALLogRecordFormatter extends SimpleFormatter {
	/**
	 * Formats a log record. If the record is an instance of {@link GOALLogRecord}
	 * the formatter provided by the record will be used to create the message.
	 * Otherwise SimpleFormatter is used.
	 *
	 */
	@Override
	public String formatMessage(LogRecord record) {
		if (record instanceof GOALLogRecord) {
			return ((GOALLogRecord) record).getFormatter().format(record);
		} else {
			return super.formatMessage(record);
		}
	}
}
