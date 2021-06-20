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
package org.eclipse.dltk.dbgp.debugger.debugger;

import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.dltk.dbgp.debugger.IDbgpDebuggerEngine;
import org.eclipse.dltk.dbgp.debugger.debugger.event.IDebuggerEvent;
import org.eclipse.dltk.dbgp.debugger.debugger.event.IDebuggerEventListener;
import org.eclipse.dltk.dbgp.debugger.debugger.event.ISuspendedEvent.SuspendedCause;
import org.eclipse.dltk.dbgp.debugger.internal.debugger.event.RunningEvent;
import org.eclipse.dltk.dbgp.debugger.internal.debugger.event.StartingEvent;
import org.eclipse.dltk.dbgp.debugger.internal.debugger.event.StderrStreamEvent;
import org.eclipse.dltk.dbgp.debugger.internal.debugger.event.StdoutStreamEvent;
import org.eclipse.dltk.dbgp.debugger.internal.debugger.event.StoppedEvent;
import org.eclipse.dltk.dbgp.debugger.internal.debugger.event.StoppingEvent;
import org.eclipse.dltk.dbgp.debugger.internal.debugger.event.SuspendedEvent;

/**
 * Abstract class for debugger.
 *
 * @param <T>
 *            native breakpoint.
 */
public abstract class AbstractDebugger<T> implements IDebugger {
	// The 'real' debugger
	private final IDbgpDebuggerEngine debugger;

	// State of the debugger
	private DebuggerState state = DebuggerState.INITIAL;

	// store debugger event listener
	private final ListenerList eventListeners = new ListenerList(ListenerList.IDENTITY);

	// Native BreakPoint registered into map to retrieve it with breakPointId.
	private Map<String, T> breakPoints = null;

	private StringWriter writer = null;

	private final URI fileURI;

	private String fileURIBaseDir;

	private boolean isBound;

	private final Thread thread;

	public AbstractDebugger(IDbgpDebuggerEngine debuggerEngine, Thread thread) {
		this.debugger = debuggerEngine;
		this.thread = thread;
		this.fileURI = debuggerEngine.getFileURI();
		this.isBound = false;
	}

	public final IDbgpDebuggerEngine getDebugger() {
		return this.debugger;
	}

	public final void bind() {
		this.isBound = true;
	}

	public final boolean isBound() {
		return this.isBound;
	}

	public final Thread getThread() {
		return this.thread;
	}

	/**
	 * Run the debugger.
	 */
	@Override
	public final void run() {
		// running the debugger...
		setState(DebuggerState.RUNNING);
		try {
			// run the debugger
			doRun();
		} finally {
			// stop the debugger...
			stop();
		}
	}

	/**
	 * Stop the debugger.
	 */
	@Override
	public final void stop() {
		// stopping the debugger
		setState(DebuggerState.STOPPING);
		try {
			// stop the debugger
			doStop();
			// flush writer
			flushWriter();
		} finally {
			// stopped
			setState(DebuggerState.STOPPED);
		}
	}

	/**
	 * Add debuger listener to observe change of state of the debugger.s
	 */
	@Override
	public void addDebuggerEventListener(IDebuggerEventListener listener) {
		this.eventListeners.add(listener);
	}

	/**
	 * Remove debugger listener.
	 */
	@Override
	public void removeDebuggerEventListener(IDebuggerEventListener listener) {
		this.eventListeners.remove(listener);
	}

	/**
	 * Notify listeners that a state change.
	 *
	 * @param event
	 */
	protected void notifyListeners(IDebuggerEvent event) {
		Object[] listeners = this.eventListeners.getListeners();
		for (Object listener : listeners) {
			((IDebuggerEventListener) listener).handleDebuggerEvent(event);
		}
	}

	/**
	 * Fire suspended event.
	 *
	 * @param cause
	 * @param locations
	 */
	protected void fireSuspendedEvent(SuspendedCause cause, Deque<BreakPointLocation> locations) {
		notifyListeners(new SuspendedEvent(cause, locations));
	}

	/**
	 * Update the state of the debugger and fire event.
	 *
	 * @param state
	 */
	private void setState(DebuggerState state) {
		this.state = state;
		switch (state) {
		case STARTING:
			notifyListeners(StartingEvent.INSTANCE);
			break;
		case RUNNING:
			notifyListeners(RunningEvent.INSTANCE);
			break;
		case STOPPING:
			notifyListeners(StoppingEvent.INSTANCE);
			break;
		case STOPPED:
			notifyListeners(StoppedEvent.INSTANCE);
			break;
		}
	}

	/**
	 * Display content into Eclipse Console.
	 *
	 * @param content
	 */
	public void out(String content) {
		IDebuggerEvent event = new StdoutStreamEvent(content);
		notifyListeners(event);
	}

	/**
	 * Display exception into Eclipse Console.
	 *
	 * @param e
	 */
	public void err(Exception e) {
		IDebuggerEvent event = new StderrStreamEvent(e);
		notifyListeners(event);
	}

	/**
	 * Display error message into Eclipse Console.
	 *
	 * @param e
	 */
	public void err(String e) {
		IDebuggerEvent event = new StderrStreamEvent(e);
		notifyListeners(event);
	}

	/**
	 * Add native BreakPoint and return the BreakPoint ID.
	 */
	@Override
	public String addBreakPoint(int lineno, String filename) {
		T breakPoint = createBreakpoint(filename, lineno);
		String breakPointID = this.registerBreakPoint(breakPoint);
		return breakPointID;
	}

	/**
	 * Remove the native BreakPoint retrieved by BreakPoint ID.
	 */
	@Override
	public void removeBreakPoint(String breakPointId) {
		T breakPoint = this.getBreakPoint(breakPointId);
		if (breakPoint != null) {
			removeBreakpoint(breakPoint);
		}
	}

	/**
	 * Register the native breakpoint into a Map and return it the ID of this
	 * breakpoint.
	 *
	 * @param breakPoint
	 * @return
	 */
	protected String registerBreakPoint(T breakPoint) {
		if (this.breakPoints == null) {
			this.breakPoints = new HashMap<>();
		}

		String breakPointID = breakPoint.hashCode() + "";
		this.breakPoints.put(breakPointID, breakPoint);
		return breakPointID;
	}

	/**
	 * Unregisters the native breakpoint into a Map
	 *
	 * @param breakPoint
	 * @return
	 */
	protected T unregisterBreakPoint(T breakPoint) {
		String breakPointID = breakPoint.hashCode() + "";
		return this.breakPoints.remove(breakPointID);
	}

	/**
	 * Return the native breakpoint retrieved by breakpointId and null
	 * otherwise.
	 *
	 * @param breakPointId
	 * @return
	 */
	protected T getBreakPoint(String breakPointId) {
		if (this.breakPoints == null) {
			return null;
		}
		return this.breakPoints.get(breakPointId);
	}

	/**
	 * Returns the sate of the debugger.
	 */
	@Override
	public DebuggerState getState() {
		return this.state;
	}

	/**
	 * Suspend the debugger due to a breakpoint.
	 *
	 * @param locations
	 *            The callstack
	 */
	public void suspendByBreakPoint(Deque<BreakPointLocation> locations) {
		fireSuspendedEvent(SuspendedCause.BREAKPOINT, locations);

		flushWriter();
	}

	/**
	 * Flush the writer.
	 */
	private void flushWriter() {
		if (this.writer != null) {
			out(this.writer.toString());
			this.writer.getBuffer().setLength(0);
		}
	}

	/**
	 * Returns the writer.
	 *
	 * @return
	 */
	protected Writer getWriter() {
		if (this.writer == null) {
			this.writer = new StringWriter();
		}
		return this.writer;
	}

	/**
	 * Implement run debugger.
	 */
	protected abstract void doRun();

	/**
	 * Implement stop debugger.
	 */
	protected abstract void doStop();

	/**
	 * Create native breakpoint.
	 *
	 * @param filename
	 * @param lineno
	 * @return
	 */
	public abstract T createBreakpoint(String filename, int lineno);

	/**
	 * Remove native breakpoint.
	 *
	 * @param breakPoint
	 */
	public abstract void removeBreakpoint(T breakPoint);

	public URI getFileURI() {
		return this.fileURI;
	}
}
