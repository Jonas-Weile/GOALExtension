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
package ar.com.tadp.prolog.core.ui.editor;

import org.eclipse.jface.text.IDocument;

public interface PrologPartitions {
	String PROLOG_STRING = "__prolog_string";;
	String PROLOG_COMMENT = "__prolog_comment";
	String PROLOG_VARIABLE = "__prolog_variable";
	String PROLOG_PARTITIONING = "__prolog_partitioning";

	public final static String[] PROLOG_PARITION_TYPES = new String[] { PrologPartitions.PROLOG_STRING,
			PrologPartitions.PROLOG_COMMENT, PrologPartitions.PROLOG_VARIABLE, IDocument.DEFAULT_CONTENT_TYPE };

}
