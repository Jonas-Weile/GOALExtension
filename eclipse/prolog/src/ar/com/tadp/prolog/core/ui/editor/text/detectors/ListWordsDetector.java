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
package ar.com.tadp.prolog.core.ui.editor.text.detectors;

import org.eclipse.jface.text.rules.IWordDetector;

/**
 *
 * @author ccancino
 */
public class ListWordsDetector implements IWordDetector {
	private final String[] words;

	public ListWordsDetector(final String[] operators) {
		this.words = operators;
	}

	@Override
	public boolean isWordPart(final char c) {
		for (final String word : this.words) {
			final String currentWord = word;
			if (!isEmpty(currentWord) && currentWord.indexOf(c) != -1) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isWordStart(final char c) {
		for (final String word : this.words) {
			final String currentWord = word;
			if (!isEmpty(currentWord) && currentWord.charAt(0) == c) {
				return true;
			}
		}
		return false;
	}

	private boolean isEmpty(final String str) {
		return str == null || str.length() == 0;
	}

}