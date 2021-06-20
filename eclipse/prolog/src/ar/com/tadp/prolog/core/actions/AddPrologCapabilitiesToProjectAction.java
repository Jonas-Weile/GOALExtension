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

import java.util.Iterator;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.internal.PluginAction;

import ar.com.tadp.prolog.core.builder.ResourceUtil;

/**
 * @author ccancino
 *
 */
@SuppressWarnings("restriction")
public class AddPrologCapabilitiesToProjectAction implements IObjectActionDelegate {

	@Override
	public void run(final IAction action) {
		final PluginAction pAction = (PluginAction) action;
		final StructuredSelection selection = (StructuredSelection) pAction.getSelection();

		for (final Iterator<?> iterator = selection.iterator(); iterator.hasNext();) {
			try {
				final IProject selectedProject = (IProject) iterator.next();
				if (selectedProject.isOpen()) {
					ResourceUtil.addPrologNature(selectedProject);
				}
			} catch (final Exception e) {
			}
		}
	}

	@Override
	public void setActivePart(final IAction action, final IWorkbenchPart targetPart) {
	}

	@Override
	public void selectionChanged(final IAction action, final ISelection selection) {
	}

}
