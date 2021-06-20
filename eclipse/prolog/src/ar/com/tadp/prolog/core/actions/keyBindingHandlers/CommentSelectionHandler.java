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
package ar.com.tadp.prolog.core.actions.keyBindingHandlers;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.ISourceViewer;

/**
 * Is in charge of put selection as comment, or uncomment selected comment
 * lines.
 *
 * @author Claudio
 */
public class CommentSelectionHandler extends SelectionHandler {

	private static final String COMMENT_CHARACTER = "%";
	private ISourceViewer sourceViewer;
	private IDocument document;

	@Override
	public void addHandlerListener(final IHandlerListener handlerListener) {
	}

	@Override
	public void dispose() {
	}

	@Override
	public Object execute(final ExecutionEvent event) throws ExecutionException {

		try {
			this.sourceViewer = Utils.getActiveEditor().getViewer();
			this.document = this.sourceViewer.getDocument();

			final TextSelection selection = getTextSelection();

			final int lineOffset = this.document.getLineOffset(selection.getStartLine());
			final int lineLength = this.document.getLineLength(selection.getStartLine());

			final String firstLine = this.document.get(lineOffset, lineLength);
			if (firstLine.startsWith(COMMENT_CHARACTER)) {
				uncomment(selection);
			} else {
				comment(selection);
			}

		} catch (final BadLocationException e) {
			DLTKCore.error(e);
		}

		return null;
	}

	private void comment(final TextSelection selection) throws BadLocationException {
		String temporal = "";
		int lines = 0;
		for (int currentLineNumber = selection.getStartLine(); currentLineNumber <= selection
				.getEndLine(); currentLineNumber++) {
			final int lineOffset = this.document.getLineOffset(currentLineNumber);
			final int lineLength = this.document.getLineLength(currentLineNumber);

			final String replacementText = this.document.get(lineOffset, lineLength);
			temporal += COMMENT_CHARACTER + replacementText;
			lines++;
		}
		replaceLines(selection, temporal, lines, COMMENT_CHARACTER.length());
	}

	private void uncomment(final TextSelection selection) throws BadLocationException {
		String temporal = "";
		int lines = 0;

		for (int currentLineNumber = selection.getStartLine(); currentLineNumber <= selection
				.getEndLine(); currentLineNumber++) {
			final int lineOffset = this.document.getLineOffset(currentLineNumber);
			final int lineLength = this.document.getLineLength(currentLineNumber);

			final String textLine = this.document.get(lineOffset, lineLength);
			if (textLine.trim().startsWith(COMMENT_CHARACTER)) {
				temporal += textLine.substring(COMMENT_CHARACTER.length(), textLine.length());
			} else {
				temporal += textLine;
			}
			lines++;
		}

		replaceLines(selection, temporal, lines, -COMMENT_CHARACTER.length());
	}

	private void replaceLines(final TextSelection selection, final String temporal, final int lines,
			final int offsetsOffset) throws BadLocationException {
		int lineOffset;
		int lineLength;
		if (lines != 1) {
			lineOffset = selection.getOffset();
			lineLength = selection.getLength();
		} else {
			lineOffset = this.document.getLineOffset(selection.getStartLine());
			lineLength = this.document.getLineLength(selection.getStartLine());
		}
		this.document.replace(lineOffset, lineLength, temporal);

		if (selection.getLength() != 0) {
			this.sourceViewer.setSelectedRange(lineOffset, temporal.length());
		} else {
			this.sourceViewer.setSelectedRange(selection.getOffset() + offsetsOffset, selection.getLength());
		}
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public boolean isHandled() {
		return true;
	}

	@Override
	public void removeHandlerListener(final IHandlerListener handlerListener) {
	}

}
