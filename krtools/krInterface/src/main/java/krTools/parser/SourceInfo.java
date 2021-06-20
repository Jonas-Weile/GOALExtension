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

package krTools.parser;

import java.io.File;

/**
 * Information about a specific position in the source. Provides the position in
 * terms of a line and character position number, as well as its index in a
 * file.
 * <p>
 * For the Comparable, the filename must be considered first. Null files are
 * always smaller than known files. For the known files you can use
 * {@link File#compareTo(File)}. Only if the files are equal, the line number is
 * then compared. If these are also equal, the column numbers are to be
 * compared. This is the reference implementation:<br>
 *
 * <code>
 *
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
	} *
 *           </code>
 */
public interface SourceInfo extends Comparable<SourceInfo> {
	/**
	 * Get the source path of this SourceInfo.
	 *
	 * @return The source that this info is associated with.
	 */
	public String getSource();

	/**
	 * Get the line number of this SourceInfo.
	 *
	 * @return The line number of the line that this info is about.
	 */
	public int getLineNumber();

	/**
	 * Get the character position of this SourceInfo.
	 *
	 * @return The first index of the character(s) in the line that this info is
	 *         about.
	 */
	public int getCharacterPosition();

	/**
	 * Get the start index of this SourceInfo.
	 *
	 * @return The position of the first character that this info is about (in
	 *         relation the entire file that is currently parsed).
	 */
	public int getStartIndex();

	/**
	 * Get the stop index of this SourceInfo.
	 *
	 * @return The position of the last character that this info is about (in
	 *         relation the entire file that is currently parsed).
	 */
	public int getStopIndex();

	/**
	 * Get the info message of this SourceInfo.
	 *
	 * @return The info message.
	 */
	public String getMessage();
}
