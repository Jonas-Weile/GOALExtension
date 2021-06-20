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
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.launching.AbstractInterpreterInstall;
import org.eclipse.dltk.launching.IInterpreterInstallType;

import ar.com.tadp.prolog.core.PrologCorePlugin;
import ar.com.tadp.prolog.core.PrologNature;
import ar.com.tadp.prolog.core.commandline.ExecEventHandler;
import ar.com.tadp.prolog.core.commandline.SyncCommandLine;
import ar.com.tadp.prolog.core.compiler.CompilerOutput;

/**
 * @author ccancino
 *
 */
public class PrologInstall extends AbstractInterpreterInstall {

	public PrologInstall(final IInterpreterInstallType type, final String id) {
		super(type, id);
	}

	@Override
	public String getNatureId() {
		return PrologNature.PROLOG_NATURE;
	}

	public SyncCommandLine executeSynchronousInterpreter() {
		try {
			final Process process = getPrologInstallType().createProcess(getInstallLocation().getCanonicalPath(), null);
			return SyncCommandLine.create(new IExecEventsImplementation(), process);
		} catch (final IOException e) {
			PrologCorePlugin.log("Cannot execute Prolog Interpreter.", e);
		}
		return null;
	}

	public CompilerOutput compile(final IFile sourceFile, final String outputFile, final IScriptProject project) {
		return getPrologInstallType().compile(sourceFile, outputFile, project, this);
	}

	private AbstractPrologInstallType getPrologInstallType() {
		return ((AbstractPrologInstallType) getInterpreterInstallType());
	}

	private final class IExecEventsImplementation implements ExecEventHandler {
		@Override
		public void processNewInput(final String input) {
		}

		@Override
		public void processNewError(final String error) {
		}

		@Override
		public void processEnded(final int exitValue) {
		}
	}

}
