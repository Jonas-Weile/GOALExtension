package goal.tools.logging;

import java.io.IOException;
import java.util.logging.FileHandler;

/**
 * Specific {@link FileHandler} that logs CSV RFC 4180
 * (https://en.wikipedia.org/wiki/Comma-separated_values) format. Except for the
 * format, this behaves identical to a normal {@link FileHandler}.
 */
public class CsvFileHandler extends FileHandler {
	public CsvFileHandler() throws IOException, SecurityException {
		super();
		setFormatter(new CsvFormatter());
	}

	public CsvFileHandler(String fname) throws SecurityException, IOException {
		super(fname);
		setFormatter(new CsvFormatter());
	}
}
