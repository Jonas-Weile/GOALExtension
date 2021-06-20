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

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;

import org.eclipse.dltk.core.DLTKCore;

/**
 * @author ccancino
 *
 */
public class ProcessOutputReaderThread implements Runnable {
	private static final long SLEEPING_TIME = 100;
	private final byte[] inBuffer = new byte[5120];
	private final InputStream inputStream;
	private String output;
	private boolean stop = false;

	public ProcessOutputReaderThread(final InputStream inputStream) {
		this.inputStream = inputStream;
	}

	@Override
	public void run() {
		this.output = "";
		try {
			int i = 0;
			while (i > -1 && !this.stop) {
				if (this.inputStream.available() > 0) {
					i = this.inputStream.read(this.inBuffer);
					processNewInput(CRLFtoLF(new String(this.inBuffer, 0, i)));
				} else {
					try {
						Thread.sleep(SLEEPING_TIME);
					} catch (final Exception e) {
					}
				}
			}
		} catch (final IOException ex) {
		}
	}

	private void processNewInput(final String str) {
		this.output += str;
	}

	public void reset() {
		this.output = "";
	}

	public String getString() {
		return this.output;
	}

	/** Remove carriage returns */
	public static String CRLFtoLF(final String str) {
		return str.replace("\r", "");
	}

	public void kill() {
		this.stop = true;
	}

	/*
	 * unravels all layers of FilterInputStream wrappers to get to the core
	 * InputStream
	 */
	public static InputStream extract(InputStream in) {
		try {
			final Field f = FilterInputStream.class.getDeclaredField("in");
			f.setAccessible(true);

			while (in instanceof FilterInputStream) {
				in = (InputStream) f.get(in);
			}

		} catch (final Exception e) {
			DLTKCore.error(e);
		}
		return in;
	}
}
