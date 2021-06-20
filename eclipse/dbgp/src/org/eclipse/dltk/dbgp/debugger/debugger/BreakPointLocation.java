/*******************************************************************************
 * Copyright (c) 2010 Freemarker Team.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.dltk.dbgp.debugger.debugger;

/**
 * Breakpoint location.
 *
 */
public class BreakPointLocation {
	private String fileName;
	private String where;
	private int lineBegin;
	private int columnBegin;
	private int lineEnd;
	private int columnEnd;

	protected BreakPointLocation() {
	}

	public BreakPointLocation(String fileName, String where, int lineBegin, int columnBegin, int lineEnd,
			int columnEnd) {
		this.fileName = fileName;
		this.where = where;
		this.lineBegin = lineBegin;
		this.columnBegin = columnBegin;
		this.lineEnd = lineEnd;
		this.columnEnd = columnEnd;
	}

	public String getFileName() {
		return this.fileName;
	}

	public String getWhere() {
		return this.where;
	}

	public int getLineBegin() {
		return this.lineBegin;
	}

	public int getColumnBegin() {
		return this.columnBegin;
	}

	public int getLineEnd() {
		return this.lineEnd;
	}

	public int getColumnEnd() {
		return this.columnEnd;
	}
}
