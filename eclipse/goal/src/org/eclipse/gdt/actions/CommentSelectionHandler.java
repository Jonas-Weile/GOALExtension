package org.eclipse.gdt.actions;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandlerListener;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.text.source.ISourceViewer;

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
		} catch (final Exception e) {
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
		int lineOffset, lineLength;
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