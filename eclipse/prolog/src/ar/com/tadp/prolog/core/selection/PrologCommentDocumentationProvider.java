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
package ar.com.tadp.prolog.core.selection;

import java.io.Reader;
import java.io.StringReader;

import org.eclipse.dltk.core.IBuffer;
import org.eclipse.dltk.core.IMember;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.ISourceRange;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.ui.documentation.IScriptDocumentationProvider;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;

/**
 * @author ccancino
 *
 */
public class PrologCommentDocumentationProvider implements IScriptDocumentationProvider {

	private static final String SINGLE_LINE_COMMENT_PREFIX = "%";

	protected String getLine(final Document d, final int line) throws BadLocationException {
		return d.get(d.getLineOffset(line), d.getLineLength(line));
	}

	protected String getHeaderComment(final IMember member) {
		try {
			final ISourceRange range = member.getSourceRange();
			if (range == null) {
				return null;
			}

			IBuffer buf = null;

			final ISourceModule compilationUnit = member.getSourceModule();
			if (!compilationUnit.isConsistent()) {
				return null;
			}

			buf = compilationUnit.getBuffer();

			final int start = range.getOffset();

			final String contents = buf.getContents();

			String result = "";

			final Document doc = new Document(contents);
			try {
				int line = doc.getLineOfOffset(start);
				line--;
				if (line < 0) {
					return null;
				}
				boolean emptyEnd = true;
				while (line >= 0) {
					final String curLine = getLine(doc, line);
					final String curLineTrimmed = curLine.trim();
					if ((curLineTrimmed.length() == 0 && emptyEnd)
							|| curLineTrimmed.startsWith(SINGLE_LINE_COMMENT_PREFIX)) {
						if (curLineTrimmed.length() != 0) {
							emptyEnd = false;
						}
						result = curLine + result;
					} else {
						break;
					}

					line--;
				}
			} catch (final BadLocationException e) {
				return null;
			}

			return result;

		} catch (final ModelException e) {
		}
		return null;
	}

	@Override
	public Reader getInfo(final IMember member, final boolean lookIntoParents, final boolean lookIntoExternal) {
		final String header = getHeaderComment(member);
		return new StringReader(convertToHTML(header));
	}

	protected String convertToHTML(final String header) {
		final StringBuffer result = new StringBuffer();
		// result.append("<p>\n");
		final Document d = new Document(header);
		for (int line = 0;; line++) {
			try {
				String str = getLine(d, line).trim();
				if (str == null) {
					break;
				}
				while (str.length() > 0 && str.startsWith(SINGLE_LINE_COMMENT_PREFIX)) {
					str = str.substring(1);
				}
				while (str.length() > 0 && str.endsWith(SINGLE_LINE_COMMENT_PREFIX)) {
					str = str.substring(0, str.length() - 1);
				}
				if (str.length() == 0) {
					result.append("<p>");
				} else {
					if (str.trim().matches("\\w*:")) {
						result.append("<h4>");
						result.append(str);
						result.append("</h4>");
					} else {
						result.append(str + "<br>");
					}
				}
			} catch (final BadLocationException e) {
				break;
			}

		}
		return result.toString();
	}

	@Override
	public Reader getInfo(final String content) {
		return null;
	}

}
