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
package ar.com.tadp.prolog.core.console.ui;

import java.util.List;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.dltk.console.IScriptInterpreter;
import org.eclipse.dltk.console.ui.IScriptConsole;
import org.eclipse.dltk.console.ui.IScriptConsoleFactory;
import org.eclipse.dltk.console.ui.ScriptConsole;
import org.eclipse.dltk.console.ui.ScriptConsoleFactoryBase;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IScriptProject;

public class PrologConsoleFactory extends ScriptConsoleFactoryBase implements IScriptConsoleFactory {

	@Override
	protected ScriptConsole createConsoleInstance() {
		return createConsole(createPrologInterpreter(null), "Stand Alone");
	}

	private ScriptConsole createConsole(final PrologScriptInterpreter interpreter, final String consoleName) {
		final PrologScriptConsole console = new PrologScriptConsole(interpreter, consoleName);
		interpreter.setConsole(console);
		// HACK this is just to display the "welcome message" of the underlying
		// interpreter.
		console.executeCommand("\n");
		return console;
	}

	@Override
	public IScriptConsole openConsole(final IScriptInterpreter interpreter, final String id, final ILaunch launch) {
		final ScriptConsole console = new PrologScriptConsole(interpreter, id);
		registerAndOpenConsole(console);
		return console;
	}

	public void openConsole(final List<String> files, final IScriptProject project) {
		final PrologScriptInterpreter interpreter = createPrologInterpreter(project);
		final PrologScriptConsole console = (PrologScriptConsole) createConsole(interpreter, files.toString());
		if (console != null) {
			registerAndOpenConsole(console);
			for (final String string : files) {
				final String path = string;
				console.executeCommand("consult(\'" + path + "\').");
			}
		}
	}

	private PrologScriptInterpreter createPrologInterpreter(final IScriptProject project) {
		PrologScriptInterpreter interpreter = null;
		try {
			interpreter = (project != null) ? new PrologScriptInterpreter(project) : new PrologScriptInterpreter();
		} catch (final Exception e) {
			DLTKCore.error(e);
		}
		return interpreter;
	}

}
