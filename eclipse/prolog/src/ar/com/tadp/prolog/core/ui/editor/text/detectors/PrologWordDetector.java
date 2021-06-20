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
public class PrologWordDetector implements IWordDetector {

	@Override
	public boolean isWordPart(final char character) {
		return Character.isJavaIdentifierPart(character) || character == ':';
	}

	@Override
	public boolean isWordStart(final char character) {
		return Character.isJavaIdentifierStart(character);
	}

}