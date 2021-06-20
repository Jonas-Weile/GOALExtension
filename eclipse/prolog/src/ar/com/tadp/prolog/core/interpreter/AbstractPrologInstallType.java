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
package ar.com.tadp.prolog.core.interpreter;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.environment.IDeployment;
import org.eclipse.dltk.core.environment.IFileHandle;
import org.eclipse.dltk.internal.launching.AbstractInterpreterInstallType;
import org.eclipse.dltk.launching.EnvironmentVariable;
import org.eclipse.dltk.launching.IInterpreterInstall;
import org.eclipse.dltk.launching.LibraryLocation;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;

import ar.com.tadp.prolog.core.PrologCorePlugin;
import ar.com.tadp.prolog.core.PrologNature;
import ar.com.tadp.prolog.core.compiler.CompilerOutput;

/**
 * @author ccancino
 *
 */
public abstract class AbstractPrologInstallType extends AbstractInterpreterInstallType {
	private MessageConsole console;

	public Process createProcess(final String command, final String[] envp) {
		try {
			return Runtime.getRuntime().exec(command, envp);
		} catch (final IOException e) {
			PrologCorePlugin.log("Cannot execute Prolog process.", e);
		}
		return null;
	}

	public abstract CompilerOutput compile(IFile sourceFile, String outputFile, IScriptProject project,
			PrologInstall prologInstall);

	@Override
	public String getNatureId() {
		return PrologNature.PROLOG_NATURE;
	}

	@Override
	protected String getPluginId() {
		return PrologCorePlugin.PLUGIN_ID;
	}

	@Override
	protected IInterpreterInstall doCreateInterpreterInstall(final String id) {
		return new PrologInstall(this, id);
	}

	/**
	 * TODO see if it is true I should not use libraries, or if I should
	 */
	@Override
	public synchronized LibraryLocation[] getDefaultLibraryLocations(final IFileHandle installLocation,
			final EnvironmentVariable[] variables, final IProgressMonitor monitor) {
		return new LibraryLocation[] {};
	}

	@Override
	protected IPath createPathFile(final IDeployment deployment) throws IOException {
		return deployment.getAbsolutePath();
		// Bundle bundle = PrologCorePlugin.getDefault().getBundle();
		// deployment.add(bundle, ".");
	}

	@Override
	protected ILog getLog() {
		return PrologCorePlugin.getDefault().getLog();
	}

	protected MessageConsoleStream getConsole() {
		if (this.console == null) {
			this.console = new MessageConsole(getName(), null);
			this.console.activate();
			ConsolePlugin.getDefault().getConsoleManager().addConsoles(new IConsole[] { this.console });
		}
		return this.console.newMessageStream();
	}

	protected CompilerOutput parseOutput(final IFile sourceFile, final String result) {
		// this.getConsole().println(result);
		return new CompilerOutput();
	}

}
