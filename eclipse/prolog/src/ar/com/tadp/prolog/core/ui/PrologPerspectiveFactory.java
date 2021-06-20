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
package ar.com.tadp.prolog.core.ui;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.dltk.ui.DLTKUIPlugin;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

/**
 * @author ccancino
 */
public class PrologPerspectiveFactory implements IPerspectiveFactory {

	@Override
	public void createInitialLayout(final IPageLayout layout) {
		defineactions(layout);
		defineLayout(layout);
	}

	private void defineLayout(final IPageLayout layout) {
		// Editors are placed for free.
		final String editorArea = layout.getEditorArea();

		final IFolderLayout leftUp = layout.createFolder("leftUp", IPageLayout.LEFT, (float) 0.24, editorArea);
		leftUp.addView(DLTKUIPlugin.ID_SCRIPT_EXPLORER);

		final IFolderLayout leftDown = layout.createFolder("leftDown", IPageLayout.BOTTOM, (float) 0.75, editorArea);
		leftDown.addView(IPageLayout.ID_PROBLEM_VIEW);
		leftDown.addView("org.eclipse.ui.console.ConsoleView");
		leftDown.addView(IPageLayout.ID_TASK_LIST);
		leftDown.addView(IPageLayout.ID_BOOKMARKS);

		final IFolderLayout rightUp = layout.createFolder("rightUp", IPageLayout.RIGHT, (float) 0.75, editorArea);
		rightUp.addView(IPageLayout.ID_OUTLINE);
	}

	private void defineactions(final IPageLayout layout) {
		// Add "new wizards".
		layout.addNewWizardShortcut("ar.com.tadp.prolog.NewPrologProjectWizard");
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");
		layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");

		// Add "show views".
		layout.addShowViewShortcut(DLTKUIPlugin.ID_SCRIPT_EXPLORER);
		layout.addShowViewShortcut(IPageLayout.ID_BOOKMARKS);
		layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
		layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
		layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
		layout.addShowViewShortcut("org.eclipse.ui.console.ConsoleView");

		layout.addActionSet("org.eclipse.team.ui.actionSet");
		layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
		layout.addActionSet("ar.com.tadp.prolog.actionSet");
	}

}
