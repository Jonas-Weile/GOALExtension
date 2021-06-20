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

import java.io.File;
import java.net.InetAddress;
import java.net.URI;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.dltk.dbgp.debugger.IDbgpDebuggerEngine;
import org.eclipse.dltk.launching.IInterpreterInstall;
import org.eclipse.dltk.launching.InterpreterConfig;
import org.eclipse.dltk.launching.debug.DbgpConnectionConfig;

/**
 * {@link RunnableProcess} with DLTK launching data used to debug.
 *
 * This class could interest DLTK project?
 */
public abstract class DLTKRunnableDebuggingProcess extends DLTKRunnableProcess {
	private IDbgpDebuggerEngine engine;

	public DLTKRunnableDebuggingProcess(final IInterpreterInstall install, final ILaunch launch,
			final InterpreterConfig config) {
		super(install, launch, config);
	}

	@Override
	public void run() {
		final DbgpConnectionConfig dbgpConfig = DbgpConnectionConfig.load(this.config);
		try {
			final File file = new File(this.config.getScriptFilePath().toOSString());
			this.engine = createDbgpDebuggerEngine(InetAddress.getByName(dbgpConfig.getHost()), dbgpConfig.getPort(),
					dbgpConfig.getSessionId(), file.toURI());
			this.engine.start();
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	protected abstract IDbgpDebuggerEngine createDbgpDebuggerEngine(InetAddress ideAdress, int port, String ideKey,
			URI fileURI);

	@Override
	public void destroy() {
		if (this.engine != null) {
			this.engine.stop();
		}
		super.destroy();
	}
}