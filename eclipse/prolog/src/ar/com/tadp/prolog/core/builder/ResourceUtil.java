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
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;

import ar.com.tadp.prolog.core.PrologNature;

/**
 * @author Claudio
 *
 */
public class ResourceUtil {

	public static final String LINE_SEPARATOR = System.getProperty("line.separator");

	public static IContainer getSourcesFolder(final IProject project) {
		return project.getFolder("src");
	}

	public static IContainer getBinFolder(final IProject project) {
		return project.getFolder("bin");// .getProjectRelativePath();
	}

	/**
	 * finds the corresponding resource for the specified element. This is
	 * element itself, if it is an IResource, or an adapter. Returns null, if no
	 * resource could be found.
	 */
	public static IResource findResource(final Object element) {
		IResource result = null;
		if (element instanceof IResource) {
			result = (IResource) element;
		} else if (element instanceof IAdaptable) {
			final Object adapter = ((IAdaptable) element).getAdapter(IResource.class);
			if (adapter instanceof IResource) {
				result = (IResource) adapter;
			}
		}
		return result;
	}

	public static boolean isPrologFile(final IFile file) {
		return file.exists() && ("pl".equals(file.getFileExtension()) || "pro".equals(file.getFileExtension()));
	}

	public static void addPrologNature(final IProject project) {
		try {
			final IProjectDescription description = project.getDescription();

			final String[] natures = description.getNatureIds();
			final String[] newNatures = new String[natures.length + 1];
			System.arraycopy(natures, 0, newNatures, 0, natures.length);
			newNatures[natures.length] = PrologNature.PROLOG_NATURE;
			description.setNatureIds(newNatures);

			project.setDescription(description, IResource.FORCE, null);
		} catch (final CoreException e) {
			// DO NOTHING
		}
	}

	public static void removePrologNature(final IProject project) {
		try {
			final IProjectDescription description = project.getDescription();
			final String[] natures = description.getNatureIds();
			for (int i = 0; i < natures.length; ++i) {
				if (natures[i].equals(PrologNature.PROLOG_NATURE)) {
					final String[] newNatures = new String[natures.length - 1];
					System.arraycopy(natures, 0, newNatures, 0, i);
					System.arraycopy(natures, i + 1, newNatures, i, natures.length - i - 1);
					description.setNatureIds(newNatures);
					project.setDescription(description, IResource.FORCE, null);
					return;
				}
			}
		} catch (final CoreException e) {
			// DO NOTHING
		}
	}

}
