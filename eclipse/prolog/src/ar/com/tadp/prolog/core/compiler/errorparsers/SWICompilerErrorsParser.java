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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;

import ar.com.tadp.prolog.core.builder.ResourceUtil;
import ar.com.tadp.prolog.core.compiler.CompilerOutput;
import ar.com.tadp.prolog.core.compiler.CompilerOutputItem;

/**
 * This class is in charge of parsing the compiler output strings in order to
 * create the object representation needed by eclipse to display an
 * error/warning {@link IMarker} Unfortunally there is not a fix error message
 * format in swi-prolog, so parsing is a little messy.
 *
 * Some error format I've found are:
 *
 * 1) Warning: (<filepath>:<lineNumber>): <messageString>
 *
 * 2) Warning: (<filepath>:<lineNumber>:<number>): <messageString>
 *
 * 3) ERROR: <filepath>:<lineNumber>:<number>: <messageString>
 *
 * 4) ERROR: (<filepath>:<lineNumber>): <messageString>
 *
 * 5) ERROR: <messageString>
 *
 * Since information is separated by ":" it is logical to tokenize the compiler
 * output by it, but it must be taken into account the fact that
 * <filepath> might contain ":" when working on Window, also
 * <messageString> might contain many ":", which makes tokenization a little
 * difficult because you can't guarantee each element will be always in the same
 * array's position, besides some messages have <number> and some others
 * haven't. Thus, messages haven't got a fixed format but a variable one.
 *
 * @author Claudio
 */
public class SWICompilerErrorsParser extends CompilerErrorsParser {

	@Override
	public void doParse(final IFile file, final String output, final CompilerOutput compilerOutput) {
		final String[] lines = output.split(ResourceUtil.LINE_SEPARATOR);

		for (int i = 0; i < lines.length; i++) {
			final String line = lines[i];
			try {
				if (line.startsWith("Warning")) {
					addWarning(compilerOutput, line + lines[++i]);
				}
				if (line.startsWith("ERROR")) {
					if (lines.length > i + 1) {
						final String nextln = lines[i + 1];
						if (nextln.startsWith("\t")) {
							addError(compilerOutput, line + nextln);
						} else {
							addError(compilerOutput, line);
						}
					} else {
						addError(compilerOutput, line);

					}
				}
			} catch (final Exception e) {
				// TODO this error format cannot be parsed yet...
			}
		}
	}

	/*
	 * private void addErrorOld(final CompilerOutput compilerOutput, final
	 * String line) { final List<String> tokens = getTokens(line);
	 *
	 * final CompilerOutputItem item = new CompilerOutputItem(
	 * IMarker.SEVERITY_ERROR); item.setFileName(tokens.get(1));
	 *
	 * if (tokens.size() == 3) { item.setLine(0); item.setStartColumn(0);
	 * item.setEndColumn(0); item.setComment(tokens.get(2)); } else {
	 * item.setLine(Integer.valueOf(tokens.get(2))); try { final int intToken =
	 * Integer.parseInt(tokens.get(3)); item.setStartColumn(intToken);
	 * item.setEndColumn(intToken); item.setComment(getFullComment(tokens, 4));
	 *
	 * } catch (final Exception e) { item.setStartColumn(0);
	 * item.setEndColumn(0); item.setComment(getFullComment(tokens, 3));
	 *
	 * } } compilerOutput.addError(item); }
	 */

	private void addError(final CompilerOutput compilerOutput, final String line) {
		final List<String> tokens = getTokens(line);

		final CompilerOutputItem item = new CompilerOutputItem(IMarker.SEVERITY_ERROR);
		item.setFileName(tokens.get(1));

		if (tokens.size() == 3) {
			item.setLine(0);
			item.setStartColumn(0);
			item.setEndColumn(0);
			item.setComment(tokens.get(2));
		} else {
			try {
				item.setLine(Integer.valueOf(tokens.get(2)));
				item.setStartColumn(getOffset(item.getLine()));
				item.setEndColumn(getLength(item.getLine()));
				item.setComment(getFullComment(tokens, 4));
			} catch (final Exception e) {
				try {
					item.setComment(getFullComment(tokens, 3));
				} catch (final Exception e1) {
					// TODO this error format cannot be parsed yet...
				}
			}
		}
		compilerOutput.addError(item);
	}

	private void addWarning(final CompilerOutput compilerOutput, final String line) {
		final List<String> tokens = getTokens(line);

		final CompilerOutputItem item = new CompilerOutputItem(IMarker.SEVERITY_WARNING);
		item.setFileName(tokens.get(1));
		item.setLine(Integer.valueOf(tokens.get(2)));
		item.setStartColumn(getOffset(item.getLine()));
		item.setEndColumn(getLength(item.getLine()));
		item.setComment(getFullComment(tokens, 3));

		compilerOutput.addError(item);
	}

	/**
	 * Returns a {@link List<String>} spliting <line> by the token ":". It also
	 * joins the 1 and 2 {@link List} elements if they represent a Windows path,
	 * and removes some garbage characters.
	 *
	 */
	private List<String> getTokens(final String line) {
		final List<String> list = new ArrayList<String>(Arrays.asList(line.split(":")));

		if (list.get(2).startsWith("/")) {
			list.set(1, list.get(1) + ":" + list.remove(2));
		}

		list.set(1, list.get(1).replace("(", ""));
		if (list.size() > 2) {
			list.set(2, list.get(2).replace(")", ""));
			if (list.size() >= 4) {
				list.set(3, list.get(3).replace("\t", ""));
			}
		}
		return list;
	}

	/**
	 * Returns a string joining the elements of the {@link List} starting from
	 * the index <b>startIndex</b>
	 */
	private String getFullComment(final List<String> tokens, final int startIndex) {
		if (startIndex > tokens.size()) {
			return tokens.get(startIndex - 1);
		}
		String comment = tokens.get(startIndex).replace("\t", "").trim();
		for (int i = startIndex + 1; i < tokens.size(); i++) {
			comment += ":" + tokens.get(i);
		}
		return comment;
	}

}
