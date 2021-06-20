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

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.builder.IBuildContext;
import org.eclipse.dltk.core.builder.IBuildParticipant;
import org.eclipse.dltk.core.builder.IBuildParticipantExtension;
import org.eclipse.dltk.launching.ScriptRuntime;
import org.eclipse.ui.texteditor.MarkerUtilities;

import ar.com.tadp.prolog.core.PrologCorePlugin;
import ar.com.tadp.prolog.core.compiler.CompilerOutput;
import ar.com.tadp.prolog.core.compiler.CompilerOutputItem;
import ar.com.tadp.prolog.core.interpreter.PrologInstall;

/**
 * @author ccancino
 *
 */
public class PrologBuildParticipant implements IBuildParticipant, IBuildParticipantExtension {

	private final IScriptProject project;

	public PrologBuildParticipant(final IScriptProject project) {
		this.project = project;
	}

	@Override
	public boolean beginBuild(final int buildType) {
		deleteResourceMarkers(getProject());
		if (buildType == IBuildContext.INCREMENTAL_BUILD) {
			checkOutFolders();
			// Do I need to clean the output file of the file(s) being compiled
			// currently (??).
			return hasInterpreter();
		}
		if (buildType == IBuildContext.FULL_BUILD) {
			checkOutFolders();
			final IWorkspaceRunnable op = new CleanOutFoldersOperation(getProject());
			try {
				if (hasInterpreter()) {
					ResourcesPlugin.getWorkspace().run(op, SubMonitor.convert(new NullProgressMonitor(), 15));
				} else {
					cleanProjectMarkers();
					addLackOfInterpreterMarker();
					return false;
				}
			} catch (final CoreException e) {
				PrologCorePlugin.log("Error starting compilation", e);
			}
			return true;
		}
		return false;
	}

	private boolean hasInterpreter() {
		try {
			return getProjectInterpreter() != null;
		} catch (final CoreException e) {
			return false;
		}
	}

	@Override
	public void endBuild(final IProgressMonitor monitor) {
	}

	@Override
	public void build(final IBuildContext context) throws CoreException {
		switch (context.getBuildType()) {
		case IBuildContext.RECONCILE_BUILD:
			break;
		case IBuildContext.INCREMENTAL_BUILD:
		case IBuildContext.FULL_BUILD:
			buildFile(context.getFile());
			break;
		default:
			break;
		}
		scheduleRefresh();
		// displayInfo(context);
	}

	private void scheduleRefresh() {
		// TODO see if I can reduce the directory scope of this method...
		final Job job = new Job("Refreshing resources...") {
			@Override
			public IStatus run(final IProgressMonitor monitor) {
				IStatus result = Status.OK_STATUS;
				try {
					getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
				} catch (final CoreException cex) {
					final String msg = "Problem during resource refresh after build.";
					PrologCorePlugin.log(msg, cex);
					result = cex.getStatus();
				}
				return result;
			}
		};
		job.schedule();
	}

	private void checkOutFolders() {
		final IWorkspaceRunnable op = new CheckOutFoldersOperation(getProject());
		try {
			ResourcesPlugin.getWorkspace().run(op, new NullProgressMonitor());
		} catch (final CoreException cex) {
			final String msg = "Problem while checking out and bin folder existence.";
			PrologCorePlugin.log(msg, cex);
		}
	}

	private void buildFile(final IFile file) {
		if (isPrologFile(file)) {
			compileFile(file);
		}
	}

	private void compileFile(final IFile file) {
		try {
			String relPath = file.getProjectRelativePath().makeRelative().toOSString();
			relPath = relPath.substring(relPath.indexOf(File.separator));
			final String outputFile = ResourceUtil.getBinFolder(this.project.getProject()).getLocation().toOSString()
					+ relPath.substring(0, relPath.lastIndexOf(".")) + ".o";

			deleteResourceMarkers(file);
			final CompilerOutput out = getProjectInterpreter().compile(file, outputFile, this.project);
			markResource(file, out.getErrors());
		} catch (final Exception e) {
			PrologCorePlugin.log("Error compiling file: " + file.getLocation().toOSString(), e);
		}
	}

	private PrologInstall getProjectInterpreter() throws CoreException {
		return (PrologInstall) ScriptRuntime.getInterpreterInstall(this.project);
	}

	private void addLackOfInterpreterMarker() throws CoreException {
		final Map<String, Object> marker = new HashMap<String, Object>();
		marker.put(IMarker.MESSAGE, "No interpreter defined for this project.");
		marker.put(IMarker.SEVERITY, Integer.valueOf(IMarker.SEVERITY_ERROR));
		marker.put(IMarker.PRIORITY, Integer.valueOf(IMarker.PRIORITY_HIGH));
		marker.put(IMarker.LOCATION, getProject().getFullPath().toOSString());
		MarkerUtilities.createMarker(getProject(), marker, IMarker.PROBLEM);
	}

	protected boolean isPrologFile(final IFile file) {
		return ResourceUtil.isPrologFile(file);
	}

	protected void cleanProjectMarkers() {
		try {
			getProject().deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
		} catch (final CoreException cex) {
			PrologCorePlugin.log("Could not delete markers.", cex);
		}
	}

	protected void deleteResourceMarkers(final IResource resource) {
		try {
			resource.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_ZERO);
		} catch (final CoreException cex) {
			PrologCorePlugin.log("Could not delete markers.", cex);
		}
	}

	protected void markResource(final IFile file, final Collection<CompilerOutputItem> items) {
		try {
			for (final CompilerOutputItem item : items) {
				if (file.getLocation().toString().equalsIgnoreCase(item.getFileName().trim())) {
					MarkerUtilities.createMarker(file, item.getProperties(), IMarker.PROBLEM);
				}
			}
		} catch (final CoreException cex) {
			PrologCorePlugin.log("Could not create markers for compiler output.", cex);
		}
	}

	private IProject getProject() {
		return this.project.getProject();
	}

}
