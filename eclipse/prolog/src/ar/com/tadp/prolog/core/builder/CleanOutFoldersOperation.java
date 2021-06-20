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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

/**
 * <p>
 * Operation for cleaning output and binary folders.
 * </p>
 *
 * @author Claudio
 */
class CleanOutFoldersOperation implements IWorkspaceRunnable {

	private static IResourceProxyVisitor folderCleaner = new FolderCleaner();

	private final IProject project;

	CleanOutFoldersOperation(final IProject project) {
		this.project = project;
	}

	@Override
	public void run(final IProgressMonitor mon) throws CoreException {
		mon.beginTask("Cleaning output folder", 15);
		try {
			deleteExe(mon);
			cleanOutFolder(mon);
		} finally {
			mon.done();
		}
	}

	private void cleanOutFolder(final IProgressMonitor mon) throws CoreException {
		mon.subTask("Shrubbing output folder.");
		final IContainer outFolder = ResourceUtil.getBinFolder(this.project);
		if (outFolder != null && !outFolder.equals(this.project)) {
			outFolder.accept(folderCleaner, IContainer.INCLUDE_PHANTOMS);
		}
		mon.worked(12);
	}

	private void deleteExe(final IProgressMonitor mon) throws CoreException {
	}

	private static class FolderCleaner implements IResourceProxyVisitor {
		@Override
		public boolean visit(final IResourceProxy proxy) throws CoreException {
			if (proxy.getType() == IResource.FILE) {
				final IResource resource = proxy.requestResource();
				// TODO need more general approach here
				final String name = resource.getName();
				if (!name.equals(".project") && !name.equals(".classpath")) {
					resource.delete(IResource.FORCE, null);
				}
			}
			return true;
		}
	}

}