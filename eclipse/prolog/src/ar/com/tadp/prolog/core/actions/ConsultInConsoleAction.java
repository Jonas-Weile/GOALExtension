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
package ar.com.tadp.prolog.core.actions;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.internal.core.SourceModule;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreeSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.PluginAction;
import org.eclipse.ui.part.FileEditorInput;

import ar.com.tadp.prolog.core.console.ui.PrologConsoleFactory;
import ar.com.tadp.prolog.core.ui.editor.PrologEditor;

/**
 * @author ccancino
 *
 */
@SuppressWarnings("restriction")
public class ConsultInConsoleAction implements IObjectActionDelegate {

	@Override
	public void setActivePart(final IAction action, final IWorkbenchPart targetPart) {
		// TODO Auto-generated method stub
	}

	@Override
	public void run(final IAction action) {
		final PluginAction pAction = (PluginAction) action;
		final StructuredSelection selection = (StructuredSelection) pAction.getSelection();
		final List<String> files = new LinkedList<String>();
		IScriptProject project = null;

		if (selection instanceof TreeSelection) {
			final TreeSelection treeSelection = (TreeSelection) selection;
			for (final TreePath path : treeSelection.getPaths()) {
				final Object segment = path.getLastSegment();
				if (segment instanceof SourceModule) {
					final SourceModule sourceModule = (SourceModule) segment;
					files.add(sourceModule.getResource().getLocation().toPortableString());
					project = sourceModule.getScriptProject();
				}
			}
		} else {
			final FileEditorInput firstElement = (FileEditorInput) selection.getFirstElement();
			files.add(firstElement.getFile().getLocation().toPortableString());
			project = getProject();
		}

		final PrologConsoleFactory consoleFactory = new PrologConsoleFactory();
		consoleFactory.openConsole(files, project);
	}

	@Override
	public void selectionChanged(final IAction action, final ISelection selection) {
	}

	private IScriptProject getProject() {
		final IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		final IWorkbenchPage activePage = workbenchWindow.getActivePage();
		final IEditorPart activeEditor = activePage.getActiveEditor();
		if (activeEditor instanceof PrologEditor) {
			return ((PrologEditor) activeEditor).getInputModelElement().getScriptProject();
		}
		return null;
	}

}
