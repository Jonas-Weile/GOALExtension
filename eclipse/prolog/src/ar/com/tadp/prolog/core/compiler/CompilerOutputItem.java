/*****************************************************************************
 * This file is part of the Prolog Development Tools (ProDT)
 *
 * Author: Claudio Cancinos
 * WWW: https://sourceforge.net/projects/prodevtools
 * Copyright (C): 2008, Claudio Cancinos
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; If not, see <http://www.gnu.org/licenses/>
 ****************************************************************************/
package ar.com.tadp.prolog.core.compiler;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;

/**
 * <p>
 * Default implementation for the information that was parsed out of the
 * compiler output.
 * </p>
 *
 * @author Claudio
 */
public class CompilerOutputItem {
	private String fileName;
	private int lineNumber;
	private String comment;
	private int startColumn;
	private int endColumn;
	private int severity;

	public CompilerOutputItem() {
		// placeholder constructor
	}

	public CompilerOutputItem(final int severity) {
		this.severity = severity;
	}

	public CompilerOutputItem(final String fileName, final int lineNumber, final String comment) {
		this.fileName = fileName;
		this.lineNumber = lineNumber;
		this.comment = comment.trim();
	}

	public CompilerOutputItem(final String fileName, final int lineNumber, final int startColumn, final int endColumn,
			final String comment) {
		this.fileName = fileName;
		this.lineNumber = lineNumber;
		this.startColumn = startColumn;
		this.endColumn = endColumn;
		this.comment = comment;
	}

	public void addToComment(final String commentAddition) {
		final String start = (this.comment.equals("")) ? this.comment : this.comment + "\n";
		this.comment = start + commentAddition.trim();
	}

	@Override
	public String toString() {
		return "CompilerOutputItem:" + "\n  file   : " + this.fileName + "\n  line   : " + this.lineNumber
				+ "\n  comment: " + this.comment;
	}

	public int getLine() {
		return this.lineNumber;
	}

	public int getStartColumn() {
		return this.startColumn;
	}

	public int getEndColumn() {
		return this.endColumn;
	}

	public String getComment() {
		return this.comment;
	}

	public String getFileName() {
		return this.fileName;
	}

	public Map<String, Object> getProperties() {
		final Map<String, Object> marker = new HashMap<String, Object>();
		marker.put(IMarker.MESSAGE, this.comment);
		marker.put(IMarker.SEVERITY, Integer.valueOf(this.severity));
		marker.put(IMarker.PRIORITY, Integer.valueOf(IMarker.PRIORITY_HIGH));
		marker.put(IMarker.LINE_NUMBER, Integer.valueOf(this.lineNumber));
		marker.put(IMarker.CHAR_START, Integer.valueOf(this.startColumn));
		marker.put(IMarker.CHAR_END, Integer.valueOf(this.startColumn + this.endColumn));
		return marker;
	}

	public void populateMarker(final IMarker marker) throws CoreException {
		marker.setAttribute(IMarker.MESSAGE, this.comment);
		marker.setAttribute(IMarker.SEVERITY, this.severity);
		marker.setAttribute(IMarker.PRIORITY, Integer.valueOf(IMarker.PRIORITY_HIGH));
		marker.setAttribute(IMarker.LINE_NUMBER, Integer.valueOf(this.lineNumber));
		if (this.endColumn != 0) {
			marker.setAttribute(IMarker.CHAR_START, Integer.valueOf(this.startColumn));
			marker.setAttribute(IMarker.CHAR_END, Integer.valueOf(this.startColumn + this.endColumn));
		}
	}

	public void setFileName(final String fileName) {
		this.fileName = fileName;
	}

	public void setLine(final int line) {
		this.lineNumber = line;
	}

	public void setStartColumn(final int column) {
		this.startColumn = column;
	}

	public void setEndColumn(final int column) {
		this.endColumn = column;
	}

	public void setComment(final String comment) {
		this.comment = comment;
	}

}