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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.List;

import org.eclipse.dltk.launching.IScriptProcessHandler;

import ar.com.tadp.prolog.core.ThreadExecutorService;

/**
 *
 * @author ccancino
 */
public class PrologProcessHandler implements IScriptProcessHandler {
	private final Object lock = new Object();
	private Process process;
	private ScriptOutputReader stdoutReader;
	private ScriptOutputReader stderrReader;

	@Override
	public ScriptResult handle(final Process process, final char[] stdin) {
		this.process = process;
		this.stdoutReader = new ScriptOutputReader(process.getInputStream());
		this.stderrReader = new ScriptOutputReader(process.getErrorStream());

		try {
			ThreadExecutorService.getInstance().execute(this.stdoutReader);
			ThreadExecutorService.getInstance().execute(this.stderrReader);

			writeToStdin(process.getOutputStream(), stdin);

			synchronized (this.lock) {
				try {
					this.lock.wait();
					// ???
					process.waitFor();
				} catch (final InterruptedException e) {
					// do nothing
				}
			}
		} finally {
			// make sure the process is terminated!
			process.destroy();
		}

		return buildResult(process, this.stdoutReader, this.stderrReader);
	}

	private ScriptResult buildResult(final Process process, final ScriptOutputReader stdoutReader,
			final ScriptOutputReader stderrReader) {
		final ScriptResult result = new ScriptResult();
		result.exitValue = process.exitValue();

		result.stdout = stdoutReader.getBuffer();
		result.stderr = stderrReader.getBuffer();

		result.stdoutLines = stdoutReader.getLines();
		result.stderrLines = stderrReader.getLines();
		return result;
	}

	private void closeWriter(final OutputStreamWriter writer) {
		if (writer != null) {
			try {
				writer.close();
			} catch (final IOException e) {
				// do nothing
			}
		}
	}

	private void writeToStdin(final OutputStream stream, final char[] stdin) {
		final OutputStreamWriter stdinWriter = new OutputStreamWriter(stream);

		try {
			if (stdin != null) {
				stdinWriter.write(stdin);
				stdinWriter.flush();
			}
		} catch (final IOException e) {
			// broken pipe, ignore
		} finally {
			closeWriter(stdinWriter);
		}
	}

	public void kill() {
		this.stderrReader.kill();
		this.stdoutReader.kill();
		this.process.destroy();
	}

	class ScriptOutputReader implements Runnable {
		private final StringBuffer buffer = new StringBuffer();
		private final InputStream stream;
		private boolean stop = false;

		ScriptOutputReader(final InputStream stream) {
			this.stream = stream;
		}

		public void kill() {
			this.stop = true;
		}

		@Override
		public void run() {
			final BufferedReader reader = new BufferedReader(new InputStreamReader(this.stream));

			try {
				int count = 0;
				final char[] bytes = new char[1024];
				String previous = "";
				String current;

				while (!this.stop && (count = reader.read(bytes)) >= 0) {
					current = new String(bytes);
					verifyLooping(previous, current);
					previous = current;
					this.buffer.append(bytes, 0, count);
				}

				synchronized (PrologProcessHandler.this.lock) {
					PrologProcessHandler.this.lock.notifyAll();
				}
			} catch (final IOException e) {
				this.buffer.setLength(0);
			}
		}

		/**
		 * This is here in order to avoid being trapped in compiler's infinite
		 * loop
		 *
		 * @param previous
		 * @param current
		 */
		private void verifyLooping(final String previous, final String current) {
			if (previous.equals(current)) {
				PrologProcessHandler.this.kill();
			}
		}

		public String getBuffer() {
			return this.buffer.toString();
		}

		public List<String> getLines() {
			final String[] lines = this.buffer.toString().split("\n");
			return Arrays.asList(lines);
		}
	}

}
