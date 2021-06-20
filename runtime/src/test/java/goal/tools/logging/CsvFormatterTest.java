package goal.tools.logging;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.junit.Before;
import org.junit.Test;

public class CsvFormatterTest {

	private CsvFormatter formatter = new CsvFormatter();

	@Before
	public void before() {
		// set formatter to UTC to ensure conversion timezone
		formatter.setTimeZone(TimeZone.getTimeZone("GMT"));
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testTimeZero() {
		// zero means GMT: Thursday, January 1, 1970 12:00:00 AM
		LogRecord record = new LogRecord(Level.WARNING, "");
		record.setMillis(0l);
		assertEquals("00:00:00.000,,\n", formatter.format(record));
	}

	@Test
	@SuppressWarnings("deprecation")
	public void testTime() {
		LogRecord record = new LogRecord(Level.WARNING, "");
		record.setMillis(1500000000);
		// GMT: Friday, July 14, 2017 2:40:00 AM
		assertEquals("08:40:00.000,,\n", formatter.format(record));
	}

	@Test
	public void testMessage() {
		LogRecord record = new LogRecord(Level.WARNING, "hello");
		assertEquals("hello", getFormattedMessage(record));
	}

	@Test
	public void testMessageQuotes() {
		LogRecord record = new LogRecord(Level.WARNING, "hello \"quotes\"");
		assertEquals("\"hello \"\"quotes\"\"\"", getFormattedMessage(record));
	}

	@Test
	public void testMessageNewline() {
		LogRecord record = new LogRecord(Level.WARNING, "hello\nbye!");
		assertEquals("\"hello\nbye!\"", getFormattedMessage(record));
	}

	@Test
	public void testTabEscapement() {
		LogRecord record = new LogRecord(Level.WARNING, "hello\tbye!");
		assertEquals("\"hello\tbye!\"", getFormattedMessage(record));
	}

	@Test
	public void testStacktrace() {
		LogRecord record = new LogRecord(Level.WARNING, "");
		try {
			throw new IllegalArgumentException("test!");
		} catch (Exception e) {
			record.setThrown(e);
		}
		String exceptionstring = getException(record);
		String[] lines = exceptionstring.split("\\n");
		assertTrue(lines.length > 20);
		String expected = "\"goal.tools.logging.CsvFormatterTest.testStacktrace(CsvFormatterTest.java:";
		assertEquals(expected, lines[0].substring(0, expected.length()));
	}

	private String getFormattedMessage(LogRecord record) {
		return formatter.format(record).split(",")[1];
	}

	private String getException(LogRecord record) {
		return formatter.format(record).trim().split(",")[2];
	}

}
