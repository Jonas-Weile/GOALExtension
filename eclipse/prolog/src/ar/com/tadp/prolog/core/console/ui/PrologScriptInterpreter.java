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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.eclipse.dltk.console.IScriptConsoleIO;
import org.eclipse.dltk.console.IScriptExecResult;
import org.eclipse.dltk.console.IScriptInterpreter;
import org.eclipse.dltk.console.ScriptExecResult;
import org.eclipse.dltk.console.ui.ScriptConsoleManager;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.ui.DLTKPluginImages;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import ar.com.tadp.prolog.core.PrologCorePlugin;
import ar.com.tadp.prolog.core.builder.ResourceUtil;
import ar.com.tadp.prolog.core.commandline.CommandResponse;
import ar.com.tadp.prolog.core.commandline.SyncCommandLine;
import ar.com.tadp.prolog.core.completion.PrologCompletionProposal;
import ar.com.tadp.prolog.core.interpreter.PrologInstall;
import ar.com.tadp.prolog.core.ui.editor.PrologEditor;

/**
 * @author ccancino
 *
 */
public class PrologScriptInterpreter implements IScriptInterpreter {
	private final SyncCommandLine processExecutor;
	private PrologScriptConsole console;
	private int state;

	public PrologScriptInterpreter() {
		final PrologInstall install = (PrologInstall) PrologCorePlugin.getDefaultInterpreter(getProject());
		this.processExecutor = install.executeSynchronousInterpreter();
	}

	public PrologScriptInterpreter(final IScriptProject project) {
		final PrologInstall install = (PrologInstall) PrologCorePlugin.getDefaultInterpreter(project);
		this.processExecutor = install.executeSynchronousInterpreter();
	}

	@Override
	public IScriptExecResult exec(final String command) throws IOException {
		final CommandResponse resultElements = this.processExecutor.executeCommand(command);

		this.state = (command.endsWith(".")) ? WAIT_NEW_COMMAND : WAIT_CONTINUE_COMMAND;

		final String output = resultElements.getOutput();
		final String error = resultElements.getError();

		// To close the console from the console
		if (command.equals("halt.") && output.length() == 0 && error.length() == 0) {
			ScriptConsoleManager.getInstance().close(this.console);
		}

		String result = "";
		boolean isError = false;

		if (!output.isEmpty()) {
			result = output;
		}
		if (!error.isEmpty()) {
			result += !result.isEmpty() ? "\n" : "";
			result += error.trim() + "\n";
		}

		isError = (output.isEmpty() && !error.isEmpty());

		return new ScriptExecResult(result, isError);
	}

	@Override
	public void close() throws IOException {
		this.processExecutor.kill();
	}

	@Override
	public int getState() {
		return this.state;
	}

	@Override
	public void consoleConnected(final IScriptConsoleIO protocol) {
	}

	private IScriptProject getProject() {
		final IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		final IWorkbenchPage activePage = workbenchWindow.getActivePage();
		final IEditorPart activeEditor = activePage.getActiveEditor();
		if (activeEditor instanceof PrologEditor) {
			return ((PrologEditor) activeEditor).getInputModelElement().getScriptProject();
		}
		return null;
	}

	public void setConsole(final PrologScriptConsole console) {
		this.console = console;
	}

	@Override
	public void addInitialListenerOperation(final Runnable runnable) {
	}

	@Override
	public InputStream getInitialOutputStream() {
		return null;
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public List getCompletions(final String commandLine, final int position) throws IOException {
		final CommandResponse response = this.processExecutor.executeCommand("listing.");
		final Collection<String> fields = new ArrayList<String>();
		final Collection<String> methods = new ArrayList<String>();

		final Set<ICompletionProposal> proposals = new HashSet<ICompletionProposal>();
		final StringTokenizer tn = new StringTokenizer(response.getOutput(), ResourceUtil.LINE_SEPARATOR);
		while (tn.hasMoreTokens()) {
			final String line = tn.nextToken();
			try {
				if (line.endsWith(".")) {
					final String name = line.substring(0, line.indexOf("(")).trim();
					if (!fields.contains(name) && name.startsWith(commandLine)) {
						proposals.add(new PrologCompletionProposal(name, position, name.length(),
								DLTKPluginImages.get(DLTKPluginImages.IMG_FIELD_PUBLIC), name, 40));
						fields.add(name);
					}
				}
				if (line.endsWith(":-")) {
					final String name = line.substring(0, line.indexOf("(")).trim();
					if (!methods.contains(name) && name.startsWith(commandLine)) {
						proposals.add(new PrologCompletionProposal(name, position, name.length(),
								DLTKPluginImages.get(DLTKPluginImages.IMG_METHOD_PUBLIC), name, 30));
						methods.add(name);
					}
				}
			} catch (final Exception e) {
			}
		}

		return new ArrayList<ICompletionProposal>(proposals);
	}

	@Override
	public String getDescription(final String commandLine, final int position) throws IOException {
		return null;
	}

	@Override
	public String[] getNames(final String type) throws IOException {
		return null;
	}

}
