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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.launching.IScriptProcessHandler.ScriptResult;
import org.eclipse.dltk.launching.InternalScriptExecutor;

import ar.com.tadp.prolog.core.PrologCorePlugin;
import ar.com.tadp.prolog.core.PrologNature;
import ar.com.tadp.prolog.core.builder.PrologProcessHandler;
import ar.com.tadp.prolog.core.builder.ResourceUtil;
import ar.com.tadp.prolog.core.compiler.CompilerOutput;
import ar.com.tadp.prolog.core.compiler.errorparsers.SWICompilerErrorsParser;

/**
 * @author ccancino
 *
 */
public class SWIPrologInstallType extends AbstractPrologInstallType {
	private static final String[] INTERPRETER_NAMES = { "plcon", "plcon.exe", "swipl" };
	private final SWICompilerErrorsParser errorsParser = new SWICompilerErrorsParser();;

	@Override
	public String getNatureId() {
		return PrologNature.PROLOG_NATURE;
	}

	@Override
	public String getName() {
		return "SWI-Prolog";
	}

	@Override
	protected String getPluginId() {
		return PrologCorePlugin.PLUGIN_ID;
	}

	@Override
	protected String[] getPossibleInterpreterNames() {
		return INTERPRETER_NAMES;
	}

	@Override
	public CompilerOutput compile(final IFile sourceFile, final String outputFile, final IScriptProject project,
			final PrologInstall prologInstall) {
		try {
			final InternalScriptExecutor executor = new InternalScriptExecutor(prologInstall,
					new PrologProcessHandler());
			final String[] args = new String[] { "-o", outputFile, "-c", sourceFile.getLocation().toOSString() };
			final ScriptResult result = executor.execute(args, null);
			return parseOutput(sourceFile, result.stderr + ResourceUtil.LINE_SEPARATOR + result.stdout);
		} catch (final CoreException e) {
			PrologCorePlugin.log("Error compiling file: " + sourceFile, e);
		}
		return new CompilerOutput();
	}

	@Override
	protected CompilerOutput parseOutput(final IFile sourceFile, final String result) {
		return this.errorsParser.parse(result, sourceFile);
	}

}
