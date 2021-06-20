/**
 * The GOAL Grammar Tools. Copyright (C) 2014 Koen Hindriks.
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
package languageTools.parser;

import org.antlr.v4.runtime.Token;

import krTools.parser.SourceInfo;

/**
 * Container that stores a position in a file or stream by means of a line
 * number index and character position on that line.
 */
public class InputStreamPosition implements SourceInfo {
	/**
	 * The name or description of the file or other stream this
	 * {@link InputStreamPosition} points into.
	 */
	private final String source;
	/**
	 * On what line the input stream's pointer is located (0-based)
	 */
	private final int lineNumber;
	/**
	 * At which character on the given line the input stream's pointer is
	 * located (0-based)
	 */
	private final int characterPosition;
	/**
	 * The current (token) startindex of the stream's pointer
	 */
	private final int startIndex;
	/**
	 * The current (token) stopindex of the stream's pointer
	 */
	private int stopIndex;

	/**
	 * TODO
	 *
	 * @param start
	 * @param stop
	 * @param source
	 */
	public InputStreamPosition(Token start, Token stop, String source) {
		this(start.getLine(), start.getCharPositionInLine(), start.getStartIndex(), stop.getStopIndex(), source);
	}

	/**
	 * TODO
	 *
	 * @param token
	 * @param index
	 * @param source
	 */
	public InputStreamPosition(Token token, int index, String source) {
		this(token.getLine(), token.getCharPositionInLine(), index,
				token.getText() == null ? index : index + token.getText().length(), source);
	}

	/**
	 * constructor.
	 *
	 * @param lineNumber
	 *            the line number. 1 is first line.
	 * @param characterPosition
	 * @param startIndex
	 * @param stopIndex
	 * @param source
	 */
	public InputStreamPosition(int lineNumber, int characterPosition, int startIndex, int stopIndex, String source) {
		this.source = source;
		this.lineNumber = lineNumber;
		this.characterPosition = characterPosition;
		this.startIndex = startIndex;
		this.stopIndex = stopIndex;
	}

	/**
	 * @return The source file this input stream position is associated with.
	 */
	@Override
	public String getSource() {
		return this.source;
	}

	/**
	 * @return The line number this marker marks.
	 */
	@Override
	public int getLineNumber() {
		return this.lineNumber;
	}

	/**
	 * @return The index of the character in its line that this marker marks.
	 */
	@Override
	public int getCharacterPosition() {
		return this.characterPosition;
	}

	/**
	 * @return The (token) startindex of the character that this marker marks.
	 */
	@Override
	public int getStartIndex() {
		return this.startIndex;
	}

	/**
	 * @return The (token) stopindex of the character that this marker marks.
	 */
	@Override
	public int getStopIndex() {
		return this.stopIndex;
	}

	/**
	 * @param stopIndex
	 *            The (token) stopindex of the character that this marker marks.
	 */
	public void setStopIndex(int stopIndex) {
		this.stopIndex = stopIndex;
	}

	/**
	 * @return A short representation of this {@link InputStreamPosition}. The
	 *         returned value is of the format <code>L&lt;LINE&gt;,
	 * C&lt;COL&gt;</code>.
	 */
	public String toShortString() {
		return "L" + this.lineNumber + ", C" + this.characterPosition;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("line ");
		builder.append(this.lineNumber);
		builder.append(", position ");
		builder.append(this.characterPosition);
		if (this.source != null) {
			builder.append(" in ");
			builder.append(this.source);
		}
		return builder.toString();
	}

	@Override
	public int hashCode() {
		int hash = (31 * this.lineNumber) << 16 + this.characterPosition;
		if (this.source != null) {
			hash += this.source.hashCode();
		}
		return hash;
	}

	@Override
	public boolean equals(Object other) {
		if (other == this) {
			return true;
		} else if (!(other instanceof InputStreamPosition)) {
			return false;
		}
		InputStreamPosition that = (InputStreamPosition) other;
		if (this.lineNumber != that.lineNumber) {
			return false;
		} else if (this.characterPosition != that.characterPosition) {
			return false;
		}
		if (this.source == null) {
			return that.source == null;
		} else {
			return this.source.equals(that.source);
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

	@Override
	public String getMessage() {
		return new String();
	}
}