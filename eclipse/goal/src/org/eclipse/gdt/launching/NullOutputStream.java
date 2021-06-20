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
import java.io.OutputStream;

/**
 * Empty {@link OutputStream} implementation.
 *
 * This class could interest DLTK project?
 */
public class NullOutputStream extends OutputStream {
	public static final OutputStream INSTANCE = new NullOutputStream();

	public NullOutputStream() {
	}

	@Override
	public void write(final byte abyte0[], final int i, final int j) {
	}

	@Override
	public void write(final int i) {
	}

	@Override
	public void write(final byte abyte0[]) throws IOException {
	}
}