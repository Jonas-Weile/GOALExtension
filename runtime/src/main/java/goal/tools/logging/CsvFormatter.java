package goal.tools.logging;

import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.TimeZone;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;
import java.util.regex.Pattern;

import goal.tools.errorhandling.StackHelper;

/**
 * Formats a log record in CSV style. Assumes that the filename contains already
 * the date, so we only log time (H:M:S:MS).
 */
public class CsvFormatter extends Formatter {
	private static final char COMMA = ',';
	private static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");

	@Override
	public String format(LogRecord record) {
		StringBuilder sb = new StringBuilder(500);

		sb.append(sdf.format(new Date(record.getMillis())));
		sb.append(COMMA);

		if (record.getMessage() != null) {
			sb.append(escape(formatMessage(record)));
		}
		sb.append(COMMA);

		if (record.getThrown() != null) {
			sb.append(escape(StackHelper.getFullStackTraceInfo(record.getThrown())));
		}
		sb.append("\n");

		// we ignore resourcebundle, seems we're not using it.
		// If this later shows needed, check how Formatter does it.
		// same with parameters, faik we use these only for the message fields.

		return sb.toString();
	}

	/**
	 * Perform RFC 4180 escape of string. If the string contains special
	 * characters (double quote, comma, tab, newline, return) then the string is
	 * wrapped in double quotes; and all double quotes are replaced with
	 * double-double quotes ("").
	 * 
	 * @param formatMessage
	 * @return string, if necessary escaped for csv logging
	 */
	public static String escape(String string) {
		// comma, quote, newline, return trigger escapement
		Pattern pattern = Pattern.compile(".*[,\"\\n\\r\\t].*", Pattern.DOTALL);
		if (pattern.matcher(string).find()) {
			return "\"" + string.replaceAll("\"", "\"\"") + "\"";
		}
		return string;
	}

	/**
	 * For testing we need to fix the timezone
	 * 
	 * @param zone
	 *            the {@link TimeZone} we're in.
	 */
	protected void setTimeZone(TimeZone zone) {
		sdf.setTimeZone(zone);
	}
}
