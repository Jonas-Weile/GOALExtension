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

import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dltk.core.IDLTKLanguageToolkit;
import org.eclipse.dltk.ui.preferences.BuildPathsPropertyPage;
import org.eclipse.dltk.ui.util.BusyIndicatorRunnableContext;
import org.eclipse.dltk.ui.wizards.BuildpathsBlock;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.preferences.IWorkbenchPreferenceContainer;

import ar.com.tadp.prolog.core.PrologLanguageToolkit;

/**
 * @author ccancino
 *
 */
public class PrologBuildPathPropertyPage extends BuildPathsPropertyPage implements IWorkbenchPropertyPage {

	@Override
	protected BuildpathsBlock createBuildPathBlock(final IWorkbenchPreferenceContainer pageContainer) {
		return new PrologBuildPathBlock(new BusyIndicatorRunnableContext(), this, getSettings().getInt(INDEX), false,
				pageContainer);
	}

	public PrologBuildPathPropertyPage() {
	}

	@Override
	public IDLTKLanguageToolkit getLanguageToolkit() {
		return PrologLanguageToolkit.getDefault();
	}

	@Override
	public boolean performOk() {
		final boolean response = super.performOk();
		final Job job = new Job("Cleaning project: \"" + getProject().getName() + "\"") {
			@Override
			public IStatus run(final IProgressMonitor monitor) {
				try {
					getProject().build(IncrementalProjectBuilder.FULL_BUILD, new NullProgressMonitor());
				} catch (final CoreException e) {
					return e.getStatus();
				}
				return Status.OK_STATUS;
			}
		};
		job.schedule();
		return response;
	}

}
