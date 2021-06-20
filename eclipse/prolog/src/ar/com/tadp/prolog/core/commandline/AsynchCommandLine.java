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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Map;

import org.eclipse.dltk.core.DLTKCore;

/**
 * Execute an external process, and permits interactive communication with the
 * process. To receive the output from the process, we use the
 * {@link ExecEventHandler} call-back interface.
 *
 * @author ccancino
 */
public class AsynchCommandLine implements Runnable {
	// Allocate 1K buffers for Input and Error Streams.
	private final byte[] inBuffer = new byte[1024];
	private final byte[] errBuffer = new byte[1024];
	// Declare internal variables we will need.
	private Process process;
	private final InputStream pErrorStream;
	private final InputStream pInputStream;
	private final OutputStream pOutputStream;
	private final PrintWriter outputWriter;
	private final Thread inReadThread;
	private final Thread errReadThread;
	private final ExecEventHandler handler;

	/**
	 * Do not use this constructor directly unless necessary. Use the static
	 * "exec" methods instead
	 */
	public AsynchCommandLine(final ExecEventHandler eventHandler, final Process p) {
		// Save variables.
		this.handler = eventHandler;
		this.process = p;
		// Get the streams.
		this.pErrorStream = this.process.getErrorStream();
		this.pInputStream = this.process.getInputStream();
		this.pOutputStream = this.process.getOutputStream();
		// Create a PrintWriter on top of the output stream.
		this.outputWriter = new PrintWriter(this.pOutputStream, true);
		// Create the threads and start them.
		this.inReadThread = new Thread(this);
		this.errReadThread = new Thread(this);
		new Thread() {
			@Override
			public void run() {
				try {
					// This Thread just waits for the process to end and
					// notifies the handler.
					int returnValue = -1;
					returnValue = AsynchCommandLine.this.process.waitFor();

					AsynchCommandLine.this.inReadThread.join();
					AsynchCommandLine.this.errReadThread.join();

					processEnded(returnValue);
				} catch (final InterruptedException ex) {
					DLTKCore.error(ex);
				} finally {
					// means that the process ended
					AsynchCommandLine.this.process = null;
				}
			}
		}.start();
		this.inReadThread.start();
		this.errReadThread.start();
	}

	private void processEnded(final int exitValue) {
		this.handler.processEnded(exitValue);
	}

	private void processNewInput(final String input) {
		this.handler.processNewInput(input);
	}

	private void processNewError(final String error) {
		this.handler.processNewError(error);
	}

	/** Send the output string through the print writer. */
	public void sendLine(final String output) throws IOException {
		try {
			Thread.sleep(1000);
		} catch (final InterruptedException e) {
		}
		this.outputWriter.println(output);
	}

	/** Send a single byte to the output stream. */
	public void sendByte(final byte b) throws IOException {
		this.pOutputStream.write(b);
	}

	/** Send a single character through the print writer. */
	public void sendChar(final char c) throws IOException {
		this.outputWriter.print(c);
	}

	@Override
	public void run() {
		// Are we on the InputRead Thread?
		if (this.inReadThread == Thread.currentThread()) {
			try {
				// Read the InputStream in a loop until we find no more bytes to
				// read.
				sleep();
				for (int i = 0; i > -1; i = this.pInputStream.read(this.inBuffer)) {
					// We have a new segment of input, so process it as a
					// String.
					processNewInput(CRLFtoLF(new String(this.inBuffer, 0, i)));
					sleep();
				}
			} catch (final IOException ex) {
			}
			// Are we on the ErrorRead Thread?
		} else if (this.errReadThread == Thread.currentThread()) {
			try {
				// Read the ErrorStream in a loop until we find no more bytes to
				// read.
				for (int i = 0; i > -1; i = this.pErrorStream.read(this.errBuffer)) {
					// We have a new segment of error, so process it as a
					// String.
					processNewError(CRLFtoLF(new String(this.errBuffer, 0, i)));
				}
			} catch (final IOException ex) {
			}
		}
	}

	private void sleep() {
		try {
			Thread.sleep(1000);
		} catch (final Exception e) {
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

	/** Remove carriage returns */
	public static String CRLFtoLF(final String str) {
		return str.replace("\r", "");
	}

	/** Run the command and return the ExecHelper wrapper object. */
	public static AsynchCommandLine create(final ExecEventHandler handler, final String command) throws IOException {
		return new AsynchCommandLine(handler, Runtime.getRuntime().exec(command));
	}

	/**
	 * Create a new instance of ExecHelper while giving an already created
	 * process
	 */
	public static AsynchCommandLine create(final ExecEventHandler handler, final Process process) throws IOException {
		return new AsynchCommandLine(handler, process);
	}

	/** Run the command and return the ExecHelper wrapper object. */
	public static AsynchCommandLine create(final ExecEventHandler handler, final String command[], final String[] envp,
			final File dir) throws IOException {
		return new AsynchCommandLine(handler, Runtime.getRuntime().exec(command, envp, dir));
	}

	/**
	 * Start a process and merge its error and output streams
	 *
	 * @see AsynchCommandLine#exec()
	 */
	public static AsynchCommandLine createAndMerge(final ExecEventHandler handler, final String command[])
			throws IOException {
		final ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.redirectErrorStream(true);

		return new AsynchCommandLine(handler, processBuilder.start());
	}

	/**
	 * Start a process Start a process and merge its error and output streams
	 *
	 * @see AsynchCommandLine#exec()
	 */
	public static AsynchCommandLine createAndMerge(final ExecEventHandler handler, final String command[],
			final Map<String, String> envp, final File dir) throws IOException {
		final ProcessBuilder processBuilder = new ProcessBuilder(command);
		processBuilder.directory(dir);
		processBuilder.redirectErrorStream(true);
		if (envp != null) {
			processBuilder.environment().putAll(envp);
		}

		return new AsynchCommandLine(handler, processBuilder.start());
	}

}
