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
package ar.com.tadp.prolog.core.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.dltk.ui.CodeFormatterConstants;
import org.eclipse.dltk.ui.PreferenceConstants;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.ui.editors.text.EditorsUI;

import ar.com.tadp.prolog.core.PrologCorePlugin;
import ar.com.tadp.prolog.core.ui.editor.text.PrologColorConstants;

/**
 * @author ccancino
 *
 */
public class PrologUIPreferenceInitializer extends AbstractPreferenceInitializer {

	@Override
	public void initializeDefaultPreferences() {
		final IPreferenceStore store = PrologCorePlugin.getDefault().getPreferenceStore();

		EditorsUI.useAnnotationsPreferencePage(store);
		EditorsUI.useQuickDiffPreferencePage(store);

		// Initialize DLTK default values
		PreferenceConstants.initializeDefaultValues(store);

		// Initialize prolog constants
		PreferenceConverter.setDefault(store, PrologColorConstants.PROLOG_COMMENT, new RGB(63, 127, 95));
		PreferenceConverter.setDefault(store, PrologColorConstants.PROLOG_KEYWORD, new RGB(127, 0, 85));
		PreferenceConverter.setDefault(store, PrologColorConstants.PROLOG_VARIABLE, new RGB(0, 0, 192));
		PreferenceConverter.setDefault(store, PrologColorConstants.PROLOG_STRING, new RGB(0, 128, 155));
		PreferenceConverter.setDefault(store, PrologColorConstants.PROLOG_OPERATOR, new RGB(150, 150, 150));

		store.setDefault(PrologColorConstants.PROLOG_COMMENT + PreferenceConstants.EDITOR_BOLD_SUFFIX, false);
		store.setDefault(PrologColorConstants.PROLOG_COMMENT + PreferenceConstants.EDITOR_ITALIC_SUFFIX, false);

		store.setDefault(PrologColorConstants.PROLOG_KEYWORD + PreferenceConstants.EDITOR_BOLD_SUFFIX, true);
		store.setDefault(PrologColorConstants.PROLOG_KEYWORD + PreferenceConstants.EDITOR_ITALIC_SUFFIX, true);

		store.setDefault(PrologColorConstants.PROLOG_OPERATOR + PreferenceConstants.EDITOR_BOLD_SUFFIX, true);
		store.setDefault(PrologColorConstants.PROLOG_OPERATOR + PreferenceConstants.EDITOR_ITALIC_SUFFIX, false);

		store.setDefault(PreferenceConstants.EDITOR_TAB_WIDTH, 8);
		store.setDefault(PreferenceConstants.EDITOR_SYNC_OUTLINE_ON_CURSOR_MOVE, true);

		store.setDefault(CodeFormatterConstants.FORMATTER_TAB_CHAR, CodeFormatterConstants.TAB);
		store.setDefault(CodeFormatterConstants.FORMATTER_TAB_SIZE, "8");
		store.setDefault(CodeFormatterConstants.FORMATTER_INDENTATION_SIZE, "8");

		store.setDefault(PrologColorConstants.EDITOR_MATCHING_BRACKETS, true);
		PreferenceConverter.setDefault(store, PrologColorConstants.EDITOR_MATCHING_BRACKETS_COLOR,
				new RGB(192, 192, 192));

		store.setDefault(PreferenceConstants.SRCBIN_FOLDERS_IN_NEWPROJ, true);
		store.setDefault(PreferenceConstants.SRC_SRCNAME, "src");
	}

}
