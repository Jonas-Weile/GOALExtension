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
package ar.com.tadp.prolog.core.builder;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;

/**
 * <p>
 * checks if the output folder and binary folder exists and creates them, if
 * necessary.
 * </p>
 *
 * @author Claudio
 */
public class CheckOutFoldersOperation implements IWorkspaceRunnable {

	private final IProject project;

	CheckOutFoldersOperation(final IProject project) {
		this.project = project;
	}

	// interface methods of IWorkspaceRunnable
	// ////////////////////////////////////////

	@Override
	public void run(final IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Cleaning output folder", 15);
		try {
			checkBinFolder(monitor); // (50)
		} finally {
			monitor.done();
		}
	}

	// helping methods
	// ////////////////

	private void checkBinFolder(final IProgressMonitor monitor) throws CoreException {
		monitor.subTask("Checking binary folder");
		final IContainer outFolder = ResourceUtil.getBinFolder(this.project);
		create(outFolder, monitor);
	}

	private void create(final IContainer container, final IProgressMonitor monitor) throws CoreException {
		if (mustCreate(container)) {
			final IPath path = container.getProjectRelativePath();
			final IFolder folder = this.project.getFolder(path);
			folder.create(true, true, SubMonitor.convert(monitor, 50));
		}
	}

	private boolean mustCreate(final IContainer container) {
		return container != null && !container.equals(this.project) && !container.exists();
	}
}