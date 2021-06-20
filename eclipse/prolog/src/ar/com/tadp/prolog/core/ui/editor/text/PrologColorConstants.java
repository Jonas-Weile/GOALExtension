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

import org.eclipse.dltk.ui.text.DLTKColorConstants;

public interface PrologColorConstants {
	public static final String PROLOG_STRING = DLTKColorConstants.DLTK_STRING;
	public static final String PROLOG_COMMENT = DLTKColorConstants.DLTK_SINGLE_LINE_COMMENT;
	public static final String PROLOG_KEYWORD = DLTKColorConstants.DLTK_KEYWORD;
	public static final String PROLOG_DEFAULT = DLTKColorConstants.DLTK_DEFAULT;
	public static final String PROLOG_OPERATOR = "PROLOG_operators";
	public static final String PROLOG_VARIABLE = "PROLOG_variables";

	public static final String EDITOR_MATCHING_BRACKETS = "matchingBrackets";
	public static final String EDITOR_MATCHING_BRACKETS_COLOR = "matchingBracketsColor";
}
