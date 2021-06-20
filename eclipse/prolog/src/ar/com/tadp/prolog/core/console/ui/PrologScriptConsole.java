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

import org.eclipse.dltk.console.IScriptInterpreter;
import org.eclipse.dltk.console.ScriptConsolePrompt;
import org.eclipse.dltk.console.ui.ScriptConsole;

/**
 * @author ccancino
 *
 */
public class PrologScriptConsole extends ScriptConsole {
	public static final String CONSOLE_TYPE = "prolog_console";
	public static final String CONSOLE_NAME = "Prolog Console";

	public PrologScriptConsole(final IScriptInterpreter interpreter, final String id) {
		super(CONSOLE_NAME + " [" + id + "]", CONSOLE_TYPE);

		setInterpreter(interpreter);
		// setTextHover(new JavaScriptConsoleTextHover(interpreter));
		setContentAssistProcessor(new PrologScriptConsoleCompletionProcessor(interpreter));
		setPrompt(new ScriptConsolePrompt(":-", "|"));
	}

}
