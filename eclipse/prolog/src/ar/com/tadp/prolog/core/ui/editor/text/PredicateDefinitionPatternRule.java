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
package ar.com.tadp.prolog.core.ui.editor.text;

import org.eclipse.jface.text.rules.ICharacterScanner;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.IWordDetector;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WordPatternRule;

import ar.com.tadp.prolog.core.builder.ResourceUtil;

public class PredicateDefinitionPatternRule extends WordPatternRule {

	public PredicateDefinitionPatternRule(final IWordDetector detector, final String startSequence,
			final String endSequence, final IToken token, final char escapeCharacter) {
		super(detector, startSequence, endSequence, token, escapeCharacter);
	}

	public PredicateDefinitionPatternRule(final IWordDetector detector, final String startSequence,
			final String endSequence, final IToken token) {
		super(detector, startSequence, endSequence, token);
	}

	@Override
	public IToken evaluate(final ICharacterScanner scanner, final boolean resume) {
		final String lineSeparator = ResourceUtil.LINE_SEPARATOR;

		if (((ITokenScanner) scanner).getTokenOffset() == 0) {
			return super.evaluate(scanner, resume);
		}

		scanner.unread();
		String previousCharacter = new String(new byte[] { (byte) scanner.read() });
		if (lineSeparator.contains(previousCharacter)) {
			while (lineSeparator.contains(previousCharacter)) {
				previousCharacter = new String(new byte[] { (byte) scanner.read() });
			}
			scanner.unread();
			final IToken token = super.evaluate(scanner, resume);
			return token;
		}
		return Token.UNDEFINED;
	}

	@Override
	protected IToken doEvaluate(final ICharacterScanner scanner, final boolean resume) {

		if (resume) {

			if (endSequenceDetected(scanner)) {
				return this.fToken;
			}

		} else {

			// final int c =
			scanner.read();
			// if (c == fStartSequence[0]) {
			// if (sequenceDetected(scanner, fStartSequence, false)) {
			if (endSequenceDetected(scanner)) {
				return this.fToken;
				// }
				// }
			}
		}

		scanner.unread();
		return Token.UNDEFINED;
	}

	private final StringBuffer internalBuffer = new StringBuffer();

	@Override
	protected boolean endSequenceDetected(final ICharacterScanner scanner) {
		this.internalBuffer.setLength(0);
		int c = scanner.read();
		while (this.fDetector.isWordPart((char) c)) {
			this.internalBuffer.append((char) c);
			c = scanner.read();
		}
		scanner.unread();

		// if (fBuffer.length() >= fEndSequence.length) {
		// for (int i=fEndSequence.length - 1, j= fBuffer.length() - 1; i >= 0;
		// i--, j--) {
		// if (fEndSequence[i] != fBuffer.charAt(j)) {
		// unreadBuffer(scanner);
		// return false;
		// }
		// }
		return true;
		// }

		// unreadBuffer(scanner);
		// return false;
	}

	// protected void unreadBuffer(ICharacterScanner scanner) {
	// super.unreadBuffer(scanner);
	// internalBuffer.insert(0, fStartSequence);
	// for (int i= internalBuffer.length() - 1; i > 0; i--)
	// scanner.unread();
	// }

}
