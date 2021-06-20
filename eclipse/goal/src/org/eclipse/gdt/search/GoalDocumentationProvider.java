package org.eclipse.gdt.search;

import java.io.Reader;
import java.io.StringReader;

import org.eclipse.dltk.core.IBuffer;
import org.eclipse.dltk.core.IMember;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.core.ISourceRange;
import org.eclipse.dltk.ui.documentation.IScriptDocumentationProvider;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.Document;

public class GoalDocumentationProvider implements IScriptDocumentationProvider {

	@Override
	public Reader getInfo(final IMember member, final boolean lookIntoParents, final boolean lookIntoExternal) {
		final String header = getHeaderComment(member);
		return new StringReader(convertToHTML(header));
	}

	protected String getLine(final Document d, final int line) throws BadLocationException {
		return d.get(d.getLineOffset(line), d.getLineLength(line));
	}

	protected String getHeaderComment(final IMember member) {
		try {
			final ISourceRange range = member.getSourceRange();
			if (range == null) {
				return null;
			}
			final ISourceModule compilationUnit = member.getSourceModule();
			if (!compilationUnit.isConsistent()) {
				return null;
			}
			final IBuffer buf = compilationUnit.getBuffer();
			final int start = range.getOffset();
			final String contents = buf.getContents();
			final Document doc = new Document(contents);
			int line = doc.getLineOfOffset(start);
			line--;
			if (line < 0) {
				return null;
			}
			String result = "";
			boolean emptyEnd = true;
			while (line >= 0) {
				final String curLine = getLine(doc, line);
				final String curLineTrimmed = curLine.trim();
				if ((curLineTrimmed.length() == 0 && emptyEnd) || curLineTrimmed.startsWith("%")) {
					if (curLineTrimmed.length() != 0) {
						emptyEnd = false;
					}
					result = curLine + result;
				} else {
					break;
				}
				line--;
			}
			if (result.isEmpty()) {
				return null;
			} else {
				return result;
			}
		} catch (final Exception e) {
			return null;
		}
	}

	protected String convertToHTML(final String header) {
		final StringBuffer result = new StringBuffer();
		final Document d = new Document(header);
		for (int line = 0;; line++) {
			try {
				String str = getLine(d, line).trim();
				if (str == null) {
					break;
				}
				while (str.length() > 0 && str.startsWith("%")) {
					str = str.substring(1);
				}
				while (str.length() > 0 && str.endsWith("%")) {
					str = str.substring(0, str.length() - 1);
				}
				if (str.length() > 0) {
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