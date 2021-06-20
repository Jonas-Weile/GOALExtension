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

import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.dltk.launching.IInterpreterInstall;
import org.eclipse.dltk.launching.InterpreterConfig;

/**
 * {@link RunnableProcess} with DLTK launching data.
 *
 * This class could interest DLTK project?
 */
public abstract class DLTKRunnableProcess extends RunnableProcess {
	protected final IInterpreterInstall install;
	protected final InterpreterConfig config;
	protected final ILaunch launch;

	public DLTKRunnableProcess(final IInterpreterInstall install, final ILaunch launch,
			final InterpreterConfig config) {
		super(null, false);
		this.install = install;
		this.config = config;
		this.launch = launch;
		super.start();
	}

	protected IPath getPath() {
		return this.config.getScriptFilePath();
	}
}