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
package ar.com.tadp.prolog.core.compiler.errorparsers;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;

import ar.com.tadp.prolog.core.builder.ResourceUtil;
import ar.com.tadp.prolog.core.compiler.CompilerOutput;
import ar.com.tadp.prolog.core.compiler.StreamUtils;

/**
 * @author ccancino
 *
 */
public abstract class CompilerErrorsParser {
	protected List<String> fileLines;
	protected Map<Integer, Integer> offsets;
	protected Map<Integer, Integer> lengths;

	public CompilerOutput parse(final String outputString, final IFile file) {
		this.fileLines = readFileLines(file);
		this.offsets = getOffsets(file);
		this.lengths = getLengths(file);
		final CompilerOutput compilerOutput = new CompilerOutput();
		doParse(file, outputString, compilerOutput);
		return compilerOutput;
	}

	protected abstract void doParse(IFile file, String outputString, CompilerOutput compilerOutput);

	private Map<Integer, Integer> getOffsets(final IFile file) {
		final Map<Integer, Integer> offsets = new HashMap<Integer, Integer>();
		int length = 0;
		offsets.put(1, 0);
		for (int i = 0; i < this.fileLines.size(); i++) {
			length += this.fileLines.get(i).length() + ResourceUtil.LINE_SEPARATOR.length();
			offsets.put(i + 2, length);
		}
		return offsets;
	}

	private Map<Integer, Integer> getLengths(final IFile file) {
		final Map<Integer, Integer> lengths = new HashMap<Integer, Integer>();
		for (int i = 1; i < this.fileLines.size() + 1; i++) {
			lengths.put(i, this.fileLines.get(i - 1).length());
		}
		return lengths;
	}

	protected int getOffset(final int line) {
		return this.offsets.get(line);
	}

	protected int getLength(final int line) {
		return this.lengths.get(line);
	}

	protected int getLine(final IFile file, String pattern) {
		pattern = pattern.replaceAll(" ", "");
		int lineIndex = 0;
		for (String fileLine : this.fileLines) {
			lineIndex++;
			fileLine = fileLine.replaceAll(" ", "");
			if (fileLine.startsWith(pattern)) {
				return lineIndex;
			}
		}
		return -1;
	}

	private List<String> readFileLines(final IFile file) {
		try {
			return StreamUtils.readLines(file.getContents());
		} catch (final Exception e) {
			return new LinkedList<String>();
		}
	}

}
