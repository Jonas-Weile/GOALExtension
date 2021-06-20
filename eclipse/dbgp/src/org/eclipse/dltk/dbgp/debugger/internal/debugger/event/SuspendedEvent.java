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
package org.eclipse.dltk.dbgp.debugger.internal.debugger.event;

import java.util.Deque;

import org.eclipse.dltk.dbgp.debugger.debugger.BreakPointLocation;
import org.eclipse.dltk.dbgp.debugger.debugger.event.ISuspendedEvent;

/**
 * Suspended debugger event.
 *
 */
public class SuspendedEvent extends AbstractDebuggerEvent implements ISuspendedEvent {
	private SuspendedCause cause;
	private Deque<BreakPointLocation> locations;

	public SuspendedEvent(SuspendedCause cause, Deque<BreakPointLocation> locations) {
		super(Type.SUSPENDED);
		this.cause = cause;
		this.locations = locations;
	}

	@Override
	public SuspendedCause getCause() {
		return this.cause;
	}

	@Override
	public Deque<BreakPointLocation> getLocations() {
		return this.locations;
	}
}
