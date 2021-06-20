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

import java.text.CharacterIterator;
import java.text.StringCharacterIterator;

import org.eclipse.core.commands.IHandler;
import org.eclipse.jface.text.TextSelection;

/**
 *
 * @author Claudio
 */
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

	private String getStringAt(final int offset) {
		final int relativeOffset = offset;
		int start = 0, end = 0;
		final String content = Utils.getActiveEditor().getViewer().getDocument().get();
		final StringCharacterIterator iter = new StringCharacterIterator(content);
		char c;

		for (c = iter.setIndex(relativeOffset); c != CharacterIterator.DONE
				&& isFullIdentifierPart(c); c = iter.previous()) {
		}
		start = isFullIdentifierPart(iter.current()) ? iter.getIndex() : iter.getIndex() + 1;// iter.getIndex()
																								// !=
																								// 0
																								// ?
																								// iter.getIndex()
																								// +
																								// 1
																								// :
																								// iter.getIndex();

		for (c = iter.setIndex(relativeOffset); c != CharacterIterator.DONE
				&& isFullIdentifierPart(c); c = iter.next()) {
		}
		end = iter.getIndex();

		return (start <= end) ? content.substring(start, end) : "";
	}

	private boolean isFullIdentifierPart(final char c) {
		return c != '\"' && c != ' ' && c != '\n' && c != '\r' && c != '\t' && c != '(' && c != ')' && c != ','
				&& c != '.' && c != ';';
	}

}
