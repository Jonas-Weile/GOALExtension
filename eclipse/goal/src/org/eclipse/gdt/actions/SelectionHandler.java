package org.eclipse.gdt.actions;

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.text.TextSelection;

public abstract class SelectionHandler implements IHandler {

	protected String getSelection() {
		final TextSelection selection = getTextSelection();
		if (selection.getLength() != 0) {
			return selection.getText();
		} else {
			final int offset = ((TextSelection) Utils.getActiveEditor().getSelectionProvider().getSelection())
					.getOffset();
			return getStringAt(offset);
		}
	}

	protected TextSelection getTextSelection() {
		return (TextSelection) Utils.getActiveEditor().getViewer().getSelectionProvider().getSelection();
	}

	private static String getStringAt(final int offset) {
		final int relativeOffset = offset;
		int start = 0, end = 0;
		final String content = Utils.getActiveEditor().getViewer().getDocument().get();
		final StringCharacterIterator iter = new StringCharacterIterator(content);

		for (char c = iter.setIndex(relativeOffset); c != CharacterIterator.DONE
				&& isFullIdentifierPart(c); c = iter.previous()) {
		}
		start = isFullIdentifierPart(iter.current()) ? iter.getIndex() : iter.getIndex() + 1;

		for (char c = iter.setIndex(relativeOffset); c != CharacterIterator.DONE
				&& isFullIdentifierPart(c); c = iter.next()) {
		}
		end = iter.getIndex();

		return (start <= end) ? content.substring(start, end) : "";
	}

	private static boolean isFullIdentifierPart(final char c) {
		return c != '\"' && c != ' ' && c != '\n' && c != '\r' && c != '\t' && c != '(' && c != ')' && c != ','
				&& c != '.' && c != ';';
	}
}