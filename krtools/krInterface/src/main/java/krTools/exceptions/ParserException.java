/**
 * Knowledge Representation Tools. Copyright (C) 2014 Koen Hindriks.
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

package krTools.exceptions;

import java.io.File;
import java.io.IOException;

import krTools.parser.SourceInfo;

/**
 * Exception to catch exceptions thrown by a parser.
 *
 * <p>
 * A parser exception should be thrown in case anything went wrong during:
 * initialization of the parser, e.g., due to a problem with the source, parsing
 * encountered a serious error and could not construct any expression, or if
 * parsing was interrupted for some reason.
 * </p>
 */
public class ParserException extends Exception implements SourceInfo {
	private static final long serialVersionUID = 8224464835000074458L;

	/**
	 * The source of this error. Will not be printed when null.
	 */
	private SourceInfo info = null;

	/**
	 * Creates a new {@link ParserException} using the message provided. Do not
	 * use this constructor if you can pass a SourceInfo.
	 *
	 * @param msg
	 *            Informative message about the exception.
	 * @param file
	 *            the file that caused the error. Must be not null.
	 */
	public ParserException(String msg, File file) {
		this(msg, new FileSourceInfo(msg, file));
	}

	/**
	 * Creates a new {@link ParserException} using the message, and source that
	 * are provided.
	 *
	 * @param msg
	 *            Informative message about the exception.
	 * @param info
	 *            The source that was being parsed.
	 */
	public ParserException(String msg, SourceInfo info) {
		this(msg, info, null);
	}

	/**
	 * Creates a new {@link ParserException} using the message, source, and
	 * throwable that are provided.
	 *
	 * @param msg
	 *            Informative message about the exception.
	 * @param info
	 *            The source that was being parsed.
	 * @param e
	 *            cause.
	 */
	public ParserException(String msg, SourceInfo info, Throwable e) {
		super(msg, e);
		this.info = info;
	}

	/**
	 * @return The source of this exception, or {@code null} if no source is
	 *         available.
	 */
	@Override
	public String getSource() {
		if (this.info != null) {
			return this.info.getSource();
		} else {
			return null;
		}
	}

	/**
	 * @return Line number where exception occurred, or {@code -1} if no line nr
	 *         is available.
	 */
	@Override
	public int getLineNumber() {
		if (this.info != null) {
			return this.info.getLineNumber();
		} else {
			return -1;
		}
	}

	/**
	 * @return Character position where exception occurred, or {@code -1} if no
	 *         position is available.
	 */
	@Override
	public int getCharacterPosition() {
		if (this.info != null) {
			return this.info.getCharacterPosition();
		} else {
			return -1;
		}
	}

	@Override
	public int getStartIndex() {
		if (this.info != null) {
			return this.info.getStartIndex();
		} else {
			return -1;
		}
	}

	@Override
	public int getStopIndex() {
		if (this.info != null) {
			return this.info.getStopIndex();
		} else {
			return -1;
		}
	}

	/**
	 * public getter for Source Info
	 *
	 * @return the source info attached to this parser exception
	 **/
	public SourceInfo getSourceInfo() {
		return this.info;
	}

	@Override
	public String toString() {
		return "ParserException: " + getMessage() + " " + this.info;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || !(obj instanceof ParserException)) {
			return false;
		} else {
			ParserException other = (ParserException) obj;
			if (this.info == null) {
				if (other.getSourceInfo() != null) {
					return false;
				}
			} else if (!this.info.equals(other.getSourceInfo())) {
				return false;
			}
			if (getMessage() == null) {
				if (other.getMessage() != null) {
					return false;
				}
			} else if (!getMessage().equals(other.getMessage())) {
				return false;
			}
			return true;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.info == null) ? 0 : this.info.hashCode());
		result = prime * result + ((getMessage() == null) ? 0 : getMessage().hashCode());
		return result;
	}

	@Override
	public int compareTo(SourceInfo o) {
		ParserException other = (ParserException) o;
		if (equals(other)) {
			return 0;
		} else if (other.getSourceInfo() == null) {
			return (this.info == null) ? 0 : -1;
		} else if (this.info == null) {
			return 1;
		} else {
			return before(this.info, other.getSourceInfo()) ? -1 : 1;
		}
	}

	/**
	 * Checks wether the position if the first source info is before the second
	 * one.
	 *
	 * @param info1
	 *            A source info object.
	 * @param info2
	 *            A source info object.
	 * @return {@code true} if source position of info1 object occurs before
	 *         position of info2 object.
	 */
	private static boolean before(SourceInfo info1, SourceInfo info2) {
		boolean source = info1.getSource() != null && info2.getSource() != null
				&& (info1.getSource().compareTo(info2.getSource()) < 0);
		boolean sourceEqual = info1.getSource() != null && info2.getSource() != null
				&& (info1.getSource().compareTo(info2.getSource()) == 0);
		boolean lineNr = sourceEqual && (info1.getLineNumber() < info2.getLineNumber());
		boolean lineNrEqual = (info1.getLineNumber() == info2.getLineNumber());
		boolean position = sourceEqual && lineNrEqual && (info1.getCharacterPosition() < info2.getCharacterPosition());
		return source || lineNr || position;
	}
}

/**
 * Substitute SourceInfo if we receive only file .
 *
 */
class FileSourceInfo implements SourceInfo {
	private String source;
	private String message;

	public FileSourceInfo(String mess, File file) {
		try {
			this.source = (file == null) ? null : file.getCanonicalPath();
		} catch (IOException e) {
			this.source = null; // TODO
		}
	}

	@Override
	public int compareTo(SourceInfo o) {
		return this.source.compareTo(o.getSource());
	}

	@Override
	public String getSource() {
		return this.source;
	}

	@Override
	public int getLineNumber() {
		return 0;
	}

	@Override
	public int getCharacterPosition() {
		return 0;
	}

	@Override
	public int getStartIndex() {
		return 0;
	}

	@Override
	public int getStopIndex() {
		return 0;
	}

	@Override
	public String getMessage() {
		return this.message;
	}
}
