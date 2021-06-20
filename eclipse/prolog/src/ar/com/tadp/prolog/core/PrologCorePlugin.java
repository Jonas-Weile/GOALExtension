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
package ar.com.tadp.prolog.core;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.launching.IInterpreterInstall;
import org.eclipse.dltk.launching.ScriptRuntime;
import org.eclipse.dltk.ui.text.ScriptTextTools;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import ar.com.tadp.prolog.core.ui.editor.PrologEditor;
import ar.com.tadp.prolog.core.ui.editor.PrologTextTools;

/**
 * The activator class controls the plug-in life cycle
 */
public class PrologCorePlugin extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "ar.com.tadp.prolog";

	// The shared instance
	private static PrologCorePlugin plugin;

	private PrologTextTools prologTextTools;

	/**
	 * The constructor
	 */
	public PrologCorePlugin() {
	}

	@Override
	public void start(final BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(final BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 *
	 * @return the shared instance
	 */
	public static PrologCorePlugin getDefault() {
		return plugin;
	}

	public synchronized ScriptTextTools getTextTools() {
		if (this.prologTextTools == null) {
			this.prologTextTools = new PrologTextTools(true);
		}
		return this.prologTextTools;
	}

	public static IInterpreterInstall getDefaultInterpreter(final IScriptProject scriptProject) {
		IInterpreterInstall interpreterInstall = null;
		try {
			if (scriptProject != null) {
				interpreterInstall = ScriptRuntime.getInterpreterInstall(scriptProject);
			}
			if (interpreterInstall == null) {
				interpreterInstall = ScriptRuntime
						.getDefaultInterpreterInstall(ScriptRuntime.getDefaultInterpreterIDs()[0]);
			}
		} catch (final CoreException e) {
			PrologCorePlugin.log("Error retrieving default interpreter.", e);
		}
		return interpreterInstall;
	}

	public static void log(final String message) {
		getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, message));
	}

	public static void log(final String message, final Throwable thr) {
		getDefault().getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, IStatus.OK, message, thr));
	}

	public static IScriptProject getProject() {
		final IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		final IWorkbenchPage activePage = workbenchWindow.getActivePage();
		final IEditorPart activeEditor = activePage.getActiveEditor();
		if (activeEditor instanceof PrologEditor) {
			return ((PrologEditor) activeEditor).getInputModelElement().getScriptProject();
		}
		return null;
	}

}
