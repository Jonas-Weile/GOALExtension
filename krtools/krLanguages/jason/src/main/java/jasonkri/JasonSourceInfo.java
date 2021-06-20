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

package jasonkri;

import jason.asSyntax.parser.Token;

import java.io.File;

import krTools.parser.SourceInfo;

/**
 * Information about a specific position in the source. Provides the position in
 * terms of a line and character position number, as well as its index in a
 * file.
 */
public class JasonSourceInfo implements krTools.parser.SourceInfo {

	/**
	 * Jason's source info object.
	 */
	private File file;
	private int lineNr;
	private int column;
	private int startIndex; // first character number in file
	private int stopIndex; // last character number in file

	/**
	 * Default constructor. Takes entire source info from the original
	 * 
	 * @param info
	 *            original {@link SourceInfo} context received from GOAL
	 */
	public JasonSourceInfo(krTools.parser.SourceInfo info) {
		init(info);
	}

	private void init(krTools.parser.SourceInfo info) {
		file = info.getSource();
		lineNr = info.getLineNumber();
		column = info.getCharacterPosition();
		startIndex = info.getStartIndex();
		stopIndex = info.getStopIndex();

	}

	/**
	 * Creates a source info object using Jason's original source info object.
	 * This constructor sets values properly according to the given start point
	 * in the info field. *
	 * 
	 * @param info
	 *            the original {@link SourceInfo} that contains the real start
	 *            line number etc.
	 * @param currentToken
	 *            the token where we found a problem. This is a pointer relative
	 *            to the text that we were parsing, and needs to be offset
	 *            against info
	 */
	public JasonSourceInfo(krTools.parser.SourceInfo info, Token currentToken) {
		init(info);
		lineNr += currentToken.beginLine - 1;
		if (currentToken.beginLine == 1) {
			column += info.getCharacterPosition();
		}
		// FIXME can we also fix start and stop? I don't see required info in
		// currentToken.
	}

	/**
	 * Creates a source info object using Jason's original source info object.
	 * This constructor sets values properly according to the given start point
	 * in the info field. *
	 * 
	 * @param info
	 *            the original {@link SourceInfo} that contains the real start
	 *            line number etc. Must not be null.
	 * @param jasonSrcInfo
	 *            the jason.asSyntax.SourceInfo we have for the object. If null,
	 *            this info is ignored and we use the original info only.
	 */

	public JasonSourceInfo(SourceInfo sourceInfo,
			jason.asSyntax.SourceInfo jasonSrcInfo) {
		init(sourceInfo);
		if (jasonSrcInfo != null) {
			lineNr += jasonSrcInfo.getBeginSrcLine();
			// FIXME can we also fix column, start and stop? I don't see
			// required
			// info in jasonSrcInfo.
		}
	}

	/**
	 * @return The source that this info is associated with.
	 */
	@Override
	public File getSource() {
		return file;
	}

	/**
	 * @return The first line number of the line that this info is about.
	 */
	@Override
	public int getLineNumber() {
		return lineNr;
	}

	/**
	 * @return The first index of the character(s) in the line that this info is
	 *         about.
	 */
	@Override
	public int getCharacterPosition() {
		return column;
	};

	/**
	 * @return The position of the first character that this info is about (in
	 *         relation the entire file that is currently parsed).
	 */
	@Override
	public int getStartIndex() {
		return startIndex;
	};

	/**
	 * @return The position of the last character that this info is about (in
	 *         relation the entire file that is currently parsed).
	 */
	@Override
	public int getStopIndex() {
		return stopIndex;
	};

	/**
	 * @return The info message.
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("line ");
		builder.append(this.lineNr);
		builder.append(", position ");
		builder.append(column);
		if (file != null) {
			builder.append(" in ");
			builder.append(file.getName());
		}
		return builder.toString();
	}

	@Override
	public String getMessage() {
		return toString(); // CHECK what kind of message should this be?
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
