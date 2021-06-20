package tuprolog.parser;

import java.io.File;

import krTools.parser.SourceInfo;

public class SourceInfoObject implements SourceInfo {
	private final File sourceFile;
	private final int lineNr;
	private final int charPos;
	private final int start;
	private final int end;
	private final String msg = new String();

	public SourceInfoObject(File file, int lineNr, int charPos, int start, int end) {
		this.sourceFile = file;
		this.lineNr = lineNr;
		this.charPos = charPos;
		this.start = start;
		this.end = end;
	}

	@Override
	public File getSource() {
		return this.sourceFile;
	}

	@Override
	public int getLineNumber() {
		return this.lineNr;
	}

	@Override
	public int getCharacterPosition() {
		return this.charPos;
	}

	@Override
	public int getStartIndex() {
		return this.start;
	}

	@Override
	public int getStopIndex() {
		return this.end;
	}

	@Override
	public String getMessage() {
		return this.msg;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("line ");
		builder.append(this.lineNr);
		builder.append(", position ");
		builder.append(this.charPos);
		if (this.sourceFile != null) {
			builder.append(" in ");
			builder.append(this.sourceFile.getName());
		}
		return builder.toString();
	}

	@Override
	public int hashCode() {
		int hash = (31 * this.lineNr) << 16 + this.charPos;
		if (this.sourceFile != null) {
			hash += this.sourceFile.hashCode();
		}
		return hash;
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		} else if (!(other instanceof SourceInfoObject)) {
			return false;
		}
		SourceInfoObject that = (SourceInfoObject) other;
		if (this.lineNr != that.lineNr) {
			return false;
		} else if (this.charPos != that.charPos) {
			return false;
		}
		if (this.sourceFile == null) {
			return that.sourceFile == null;
		} else {
			return this.sourceFile.getAbsoluteFile().equals(that.sourceFile.getAbsoluteFile());
		}
	}

	@Override
	public int compareTo(SourceInfo o) {
		if (getSource() == null) {
			if (o.getSource() != null) {
				return -1;
			}
		} else {
			if (o.getSource() == null) {
				return 1;
			}
			// both files not null.
			int filecompare = getSource().compareTo(o.getSource());
			if (filecompare != 0) {
				return filecompare;
			}
		}
		// files are equal (or both null).
		int linecompare = getLineNumber() - o.getLineNumber();
		if (linecompare != 0) {
			return linecompare;
		}
		// lines are equal
		return getCharacterPosition() - o.getCharacterPosition();
	}
}
