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

import org.eclipse.dltk.ui.text.ScriptSourceViewerConfiguration;
import org.eclipse.dltk.ui.text.ScriptTextTools;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.rules.IPartitionTokenScanner;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @author ccancino
 *
 */
public class PrologTextTools extends ScriptTextTools {
	private final static String[] LEGAL_CONTENT_TYPES = new String[] { PrologPartitions.PROLOG_STRING,
			PrologPartitions.PROLOG_COMMENT, PrologPartitions.PROLOG_VARIABLE };

	private final IPartitionTokenScanner fPartitionScanner;

	public PrologTextTools(final boolean autoDisposeOnDisplayDispose) {
		super(PrologPartitions.PROLOG_PARTITIONING, LEGAL_CONTENT_TYPES, autoDisposeOnDisplayDispose);
		this.fPartitionScanner = new PrologPartitionScanner();
	}

	@Override
	public ScriptSourceViewerConfiguration createSourceViewerConfiguraton(final IPreferenceStore preferenceStore,
			final ITextEditor editor, final String partitioning) {
		return new PrologSourceViewerConfiguration(getColorManager(), preferenceStore, editor, partitioning);
	}

	@Override
	public IPartitionTokenScanner getPartitionScanner() {
		return this.fPartitionScanner;
	}

}
