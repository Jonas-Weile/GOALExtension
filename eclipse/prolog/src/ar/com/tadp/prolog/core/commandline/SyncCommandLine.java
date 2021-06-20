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
package ar.com.tadp.prolog.core.commandline;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.eclipse.dltk.core.DLTKCore;

import ar.com.tadp.prolog.core.ThreadExecutorService;

/**
 * It wrap a {@link Process} to be used as if it was executed using an OS
 * command line. The result of the execution of a command will be returned
 * synchronously right after the execution of the command.
 *
 * @author ccancino
 */
public class SyncCommandLine {
	private static final int WAIT_RESPONSE = 200;

	private Process process;
	private final PrintWriter processInputWriter;
	private final ExecEventHandler handler;
	private final ProcessOutputReaderThread outputReaderProcess;
	private final ProcessOutputReaderThread errorReaderProcess;

	protected SyncCommandLine(final ExecEventHandler eventHandler, final Process p) {
		this.handler = eventHandler;
		this.process = p;

		this.processInputWriter = new PrintWriter(this.process.getOutputStream(), true);
		this.outputReaderProcess = new ProcessOutputReaderThread(this.process.getInputStream());
		this.errorReaderProcess = new ProcessOutputReaderThread(this.process.getErrorStream());

		ThreadExecutorService.getInstance().execute(new Runnable() {
			@Override
			public void run() {
				try {
					int returnValue = -1;
					returnValue = SyncCommandLine.this.process.waitFor();
					SyncCommandLine.this.outputReaderProcess.kill();
					SyncCommandLine.this.errorReaderProcess.kill();
					processEnded(returnValue);
				} catch (final InterruptedException ex) {
					DLTKCore.error(ex);
				} finally {
					// means that the process ended
					SyncCommandLine.this.process = null;
				}
			}
		});

		ThreadExecutorService.getInstance().execute(this.outputReaderProcess);
		ThreadExecutorService.getInstance().execute(this.errorReaderProcess);
	}

	private void processEnded(final int exitValue) {
		this.handler.processEnded(exitValue);
	}

	/** Send the output string through the print writer. */
	public CommandResponse executeCommand(final String command) throws IOException {
		this.processInputWriter.println(command);
		waitResponse();

		return new CommandResponse(getOutput(), getError());
	}

	/**
	 * Returns the current content of the process output stream clearing it
	 * after.
	 *
	 * @return
	 */
	public String getOutput() {
		final String output = this.outputReaderProcess.getString();
		this.outputReaderProcess.reset();
		return output;
	}

	/**
	 * Returns the current content of the process output stream clearing it
	 * after.
	 *
	 * @return
	 */
	public String getError() {
		final String error = this.errorReaderProcess.getString();
		this.errorReaderProcess.reset();
		return error;
	}

	private void waitResponse() {
		try {
			ThreadExecutorService.getInstance().awaitTermination(WAIT_RESPONSE, TimeUnit.MILLISECONDS);
			if (this.outputReaderProcess.getString().length() == 0
					|| this.outputReaderProcess.getString().length() == 0) {
				ThreadExecutorService.getInstance().awaitTermination(WAIT_RESPONSE, TimeUnit.MILLISECONDS);
			}
		} catch (final InterruptedException e) {
		}
	}

	/** Kill the process */
	public void kill() {
		if (this.process != null) {
			this.process.destroy();
		}
		this.process = null;
	}

	/** Is the process still running? */
	public boolean isRunning() {
		return this.process != null;
	}

	/** Wait for the process to end */
	public void join() {
		try {
			if (this.process != null) {
				this.process.waitFor();
			}
			this.process = null;
		} catch (final InterruptedException e) {
			DLTKCore.error(e);
		}
	}

	/** Run the command and return the ExecHelper wrapper object. */
	public static SyncCommandLine create(final ExecEventHandler handler, final String command) throws IOException {
		return new SyncCommandLine(handler, Runtime.getRuntime().exec(command));
	}

	/**
	 * Create a new instance of ExecHelper while giving an already created
	 * process
	 */
	public static SyncCommandLine create(final ExecEventHandler handler, final Process process) throws IOException {
		return new SyncCommandLine(handler, process);
	}

	/** Run the command and return the ExecHelper wrapper object. */
	public static SyncCommandLine create(final ExecEventHandler handler, final String command[], final String[] envp,
			final File dir) throws IOException {
		return new SyncCommandLine(handler, Runtime.getRuntime().exec(command, envp, dir));
	}

	/**
	 * Start a process and merge its error and output streams
	 *
	 * @see SyncCommandLine#exec()
	 */
	public static SyncCommandLine createAndMerge(final ExecEventHandler handler, final String command[])
			throws IOException {
		final ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.redirectErrorStream(true);

		return new SyncCommandLine(handler, processBuilder.start());
	}

	/**
	 * Start a process Start a process and merge its error and output streams
	 *
	 * @see SyncCommandLine#exec()
	 */
	public static SyncCommandLine createAndMerge(final ExecEventHandler handler, final String command[],
			final Map<String, String> envp, final File dir) throws IOException {
		final ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.directory(dir);
		processBuilder.redirectErrorStream(true);
		if (envp != null) {
			processBuilder.environment().putAll(envp);
		}

		return new SyncCommandLine(handler, processBuilder.start());
	}

}
