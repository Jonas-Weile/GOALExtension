/*******************************************************************************
 * Copyright (c) 2010 Freemarker Team.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Angelo Zerr <angelo.zerr@gmail.com> - initial API and implementation
 *******************************************************************************/
package org.eclipse.gdt.launching;

import java.io.IOException;
import java.io.InputStream;

/**
 * {@link InputStream} delegate.
 *
 * This class could interest DLTK project?
 */
public class InputStreamWrapper extends InputStream {
	private InputStream inputStream;
	private volatile boolean terminated = false;

	@Override
	public int available() throws IOException {
		return this.inputStream.available();
	}

	@Override
	public void close() throws IOException {
		if (isInitialized()) {
			this.inputStream.close();
		}
	}

	@Override
	public boolean equals(final Object obj) {
		if (isInitialized()) {
			return this.inputStream.equals(obj);
		} else {
			return super.equals(obj);
		}
	}

	@Override
	public int hashCode() {
		if (isInitialized()) {
			return this.inputStream.hashCode();
		} else {
			return super.hashCode();
		}
	}

	@Override
	public void mark(final int readlimit) {
		if (isInitialized()) {
			this.inputStream.mark(readlimit);
		}
	}

	@Override
	public boolean markSupported() {
		if (isInitialized()) {
			return this.inputStream.markSupported();
		} else {
			return false;
		}
	}

	@Override
	public int read() throws IOException {
		waitInitialized();
		if (isInitialized()) {
			return this.inputStream.read();
		} else {
			return -1;
		}
	}

	/**
	 * Wait until Inpustream is filled or terminated.
	 */
	private void waitInitialized() {
		synchronized (this) {
			while (!isInitialized() && !this.terminated) {
				try {
					wait();
				} catch (final InterruptedException e) {
					break;
				}
			}
		}
	}

	@Override
	public int read(final byte[] b, final int off, final int len) throws IOException {
		waitInitialized();
		if (isInitialized()) {
			return this.inputStream.read(b, off, len);
		} else {
			return -1;
		}
	}

	@Override
	public int read(final byte[] b) throws IOException {
		waitInitialized();
		if (isInitialized()) {
			return this.inputStream.read(b);
		} else {
			return -1;
		}
	}

	@Override
	public void reset() throws IOException {
		if (isInitialized()) {
			this.inputStream.reset();
		}
	}

	@Override
	public long skip(final long n) throws IOException {
		if (isInitialized()) {
			return this.inputStream.skip(n);
		} else {
			return 0;
		}
	}

	@Override
	public String toString() {
		if (isInitialized()) {
			return this.inputStream.toString();
		} else {
			return super.toString();
		}
	}

	/**
	 * Set the inputstream to wrap.
	 *
	 * @param inputStream
	 */
	public void setInputStream(final InputStream inputStream) {
		synchronized (this) {
			this.inputStream = inputStream;
			notifyAll();
		}
	}

	/**
	 * Return true if there is inputStream to wrap and false otherwise.
	 *
	 * @return
	 */
	public boolean isInitialized() {
		return (this.inputStream != null);
	}

	/**
	 * Mark the wrapper as terminated (means that the wrapper will not used
	 * again).
	 */
	public void markAsTerminated() {
		synchronized (this) {
			this.terminated = true;
			notifyAll();
		}
	}
}
