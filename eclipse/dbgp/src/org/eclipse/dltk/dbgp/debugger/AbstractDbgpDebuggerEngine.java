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
package org.eclipse.dltk.dbgp.debugger;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;

import org.eclipse.dltk.dbgp.DbgpRequest;
import org.eclipse.dltk.dbgp.debugger.debugger.DebuggerState;
import org.eclipse.dltk.dbgp.debugger.debugger.IDebugger;
import org.eclipse.dltk.dbgp.debugger.debugger.event.IDebuggerEvent;
import org.eclipse.dltk.dbgp.debugger.debugger.event.IDebuggerEventListener;
import org.eclipse.dltk.dbgp.debugger.debugger.event.IStreamEvent;
import org.eclipse.dltk.dbgp.debugger.debugger.event.ISuspendedEvent;
import org.eclipse.dltk.dbgp.debugger.internal.packet.receiver.DbgpAsciiPacketReceiver;
import org.eclipse.dltk.dbgp.debugger.internal.packet.receiver.IDbgpCommandListener;
import org.eclipse.dltk.dbgp.debugger.internal.packet.sender.DbgpXmlPacketSender;
import org.eclipse.dltk.dbgp.debugger.packet.sender.DbgpXmlPacket;
import org.eclipse.dltk.dbgp.debugger.packet.sender.InitPacket;
import org.eclipse.dltk.dbgp.debugger.packet.sender.response.BreakPointRemovePacket;
import org.eclipse.dltk.dbgp.debugger.packet.sender.response.BreakPointSetPacket;
import org.eclipse.dltk.dbgp.debugger.packet.sender.response.ContextGetPacket;
import org.eclipse.dltk.dbgp.debugger.packet.sender.response.DbgpXmlResponsePacket;
import org.eclipse.dltk.dbgp.debugger.packet.sender.response.DefaultDbgpResponseFactory;
import org.eclipse.dltk.dbgp.debugger.packet.sender.response.EvalPacket;
import org.eclipse.dltk.dbgp.debugger.packet.sender.response.IDbgpResponseFactory;
import org.eclipse.dltk.dbgp.debugger.packet.sender.status.DefaultDbgpStatusFactory;
import org.eclipse.dltk.dbgp.debugger.packet.sender.status.IDbgpStatusFactory;
import org.eclipse.dltk.dbgp.debugger.packet.sender.stream.StdoutStreamPacket;
import org.eclipse.dltk.dbgp.internal.DbgpTermination;

/**
 * Abstract class DBGp debugger enigne, wich implements several methods of
 * {@link IDbgpDebuggerEngine}.
 */
public abstract class AbstractDbgpDebuggerEngine extends DbgpTermination
		implements IDbgpDebuggerEngine, IDbgpConstants, IDbgpCommandListener, IDebuggerEventListener {
	private final InetAddress ideAdress;
	private final int port;
	private final String ideKey;
	private final URI fileURI;
	private IDbgpResponseFactory responseFactory = DefaultDbgpResponseFactory.INSTANCE;
	private IDbgpStatusFactory statusFactory = DefaultDbgpStatusFactory.INSTANCE;

	private boolean debugDbgpProtocol;

	private DbgpAsciiPacketReceiver receiver;
	private DbgpXmlPacketSender sender;
	private IDebugger debugger;
	private boolean debuggerStarted;

	private DbgpRequest lastContinuationCommand;
	private DbgpRequest lastStackGetCommand;
	private ISuspendedEvent lastSuspendedEvent;
	private Thread thread;
	private boolean forceStop;

	private Collection<DbgpContext> contexts = null;

	public AbstractDbgpDebuggerEngine(InetAddress ideAdress, int port, String ideKey, URI fileURI,
			boolean debugDbgpProtocol) {
		this.ideAdress = ideAdress;
		this.port = port;
		this.ideKey = ideKey;
		this.fileURI = fileURI;
		this.debugDbgpProtocol = debugDbgpProtocol;
		this.contexts = createDbgpContexts();
	}

	@Override
	public void start() throws IOException {
		try {
			// Connect to DBGP Client Eclipse IDE
			Socket client = new Socket(this.ideAdress, this.port);

			// Receive messages from DBGP Client Eclipse IDE
			this.receiver = new DbgpAsciiPacketReceiver(this, new DataInputStream(client.getInputStream()));
			// Response messages to DBGP Client Eclipse IDE
			this.sender = new DbgpXmlPacketSender(this, new DataOutputStream(client.getOutputStream()));

			// add a listener IDbgpCommandListener to call
			// DbgpFreemarkerDebugger#commandReceived
			// which call notify() to not wait (see wait() call into
			// workingCycle)
			this.receiver.addCommandListener(this);

			// Create debugger
			this.debugger = createDebugger(Thread.currentThread());
			if (this.debugger != null) {
				// debugger.addTerminationListener(this);
				this.debugger.addDebuggerEventListener(this);
				// debugger.connect();
			}
			// Start receiver to get ASCII messages from the IDE DBGP debugger
			this.receiver.start();
			// Start sender to send XML messages to the IDE DBGP debugger
			this.sender.start();

			// Send XML init packet to manage Connection Initialization. See
			// specification at
			// http://xdebug.org/docs-dbgp.php#connection-initialization
			InitPacket initPacket = createInitPacket(this.ideKey, Thread.currentThread().getId() + "", this.fileURI);
			this.sender.send(initPacket);

			while (true) {
				try {
					synchronized (this) {
						while (!this.receiver.hasAvailableCommand()) {
							wait();
							DebuggerState state = this.debugger.getState();
							if (state == DebuggerState.STOPPING || state == DebuggerState.STOPPED) {
								break;
							}
						}
					}

					DbgpRequest request = this.receiver.retrieveCommand();
					if (request != null) {
						DbgpXmlResponsePacket response = processCommand(request);
						if (response != null) {
							this.sender.send(response);
						}
					} else {
						DebuggerState state = this.debugger.getState();
						if (state == DebuggerState.STOPPED) {
							break;
						}
					}
				} catch (InterruptedException e) {
					break;
				}
			}
		} catch (Exception e) {
			terminate(e);
			return;
		}
		terminate(null);
	}

	private void terminate(Exception e) {
		if (!this.forceStop) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
			}
		}
		while (this.receiver != null && this.receiver.hasAvailableCommand()) {
			try {
				Thread.sleep(1);
			} catch (InterruptedException e2) {
			}
		}
		if (this.debugger != null) {
			this.debugger.removeDebuggerEventListener(this);
		}
		this.thread = null;
		fireObjectTerminated(e);
	}

	@Override
	public void stop() {
		DebuggerState state = DebuggerState.INITIAL;
		if (this.debugger != null) {
			state = this.debugger.getState();
		}
		if (state != DebuggerState.STOPPED && this.debugger != null) {
			this.debugger.stop();
		}
		commandReceived();
	}

	@Override
	public synchronized void commandReceived() {
		notifyAll();
	}

	private synchronized DbgpXmlResponsePacket processCommand(final DbgpRequest request) {
		String command = request.getCommand();
		if (command.equals(COMMAND_FEATURE_SET)) {
			return this.responseFactory.processCommandFeatureSet(this, request);
		}
		if (command.equals(COMMAND_FEATURE_GET)) {
			return this.responseFactory.processCommandFeatureGet(this, request);
		}
		if (command.equals(COMMAND_STDOUT)) {
			return this.responseFactory.processCommandStdout(this, request);
		}
		if (command.equals(COMMAND_STDERR)) {
			return this.responseFactory.processCommandStderr(this, request);
		}
		if (command.equals(COMMAND_RUN)) {
			return processCommandRun(request);
		}
		if (command.equals(COMMAND_STOP)) {
			return processCommandStop(request);
		}
		if (command.equals(COMMAND_BREAK)) {
			return processCommandBreak(request);
		}
		if (command.equals(COMMAND_STACK_GET)) {
			return processCommandStackGet(request);
		}
		if (command.equals(COMMAND_BREAKPOINT_SET)) {
			return processCommandBreakPointSet(request);
		}
		if (command.equals(COMMAND_BREAKPOINT_GET)) {
			return this.responseFactory.processCommandBreakPointGet(this, request);
		}
		if (command.equals(COMMAND_BREAKPOINT_REMOVE)) {
			return processCommandBreakRemove(request);
		}
		if (command.equals(COMMAND_CONTEXT_NAMES)) {
			return this.responseFactory.processCommandContextNames(this, request);
		}
		if (command.equals(COMMAND_CONTEXT_GET)) {
			return processCommandContextGet(request);
		}
		if (command.equals(COMMAND_STEP_OVER) || command.equals(COMMAND_STEP_INTO)
				|| command.equals(COMMAND_STEP_OUT)) {
			return this.responseFactory.processCommandStep(this, request);
		}
		if (command.equals(COMMAND_EVAL)) {
			return processCommandEval(request);
		}
		return null;
	}

	private synchronized DbgpXmlResponsePacket processCommandBreakPointSet(final DbgpRequest request) {
		BreakPointSetPacket packet = this.responseFactory.processCommandBreakPointSet(this, request);
		int lineno = packet.getLineno();
		String filename = packet.getFilename();

		// FIXME : manage error when breakpoint fire error
		String breakPointId = this.debugger.addBreakPoint(lineno, filename);
		packet.setBreakPointId(breakPointId);
		return packet;
	}

	private synchronized DbgpXmlResponsePacket processCommandRun(final DbgpRequest request) {
		this.lastContinuationCommand = request;
		if (!isDebuggerStarted()) {
			runDebugger();
		} else {
			this.debugger.resume();
		}
		return null;
	}

	private synchronized DbgpXmlResponsePacket processCommandStop(final DbgpRequest request) {
		this.forceStop = true;
		this.lastContinuationCommand = request;
		this.debugger.stop();
		fireObjectTerminated(null);
		return null;
	}

	private synchronized DbgpXmlResponsePacket processCommandBreak(DbgpRequest request) {
		this.lastContinuationCommand = request;
		this.debugger.suspend();
		return this.responseFactory.processCommandBreak(this, request);
	}

	private synchronized DbgpXmlResponsePacket processCommandBreakRemove(DbgpRequest command) {
		BreakPointRemovePacket packet = this.responseFactory.processCommandBreakPointRemove(this, command);
		String breakPointId = packet.getBreakPointId();
		this.debugger.removeBreakPoint(breakPointId);
		return packet;
	}

	private synchronized DbgpXmlResponsePacket processCommandStackGet(DbgpRequest request) {
		this.lastStackGetCommand = request;
		processStackRequestAndSuspendedMesage();
		return null;
	}

	private synchronized DbgpXmlResponsePacket processCommandContextGet(DbgpRequest request) {
		ContextGetPacket packet = this.responseFactory.processCommandContextGet(this, request);
		this.debugger.collectVariables(packet.getContextId(), packet);
		return packet;
	}

	private synchronized DbgpXmlResponsePacket processCommandEval(DbgpRequest request) {
		EvalPacket packet = this.responseFactory.processCommandEval(this, request);
		this.debugger.evaluate(request.getData(), packet);
		return packet;
	}

	public void setResponseFactory(IDbgpResponseFactory responseFactory) {
		this.responseFactory = responseFactory;
	}

	public void setStatusFactory(IDbgpStatusFactory statusFactory) {
		this.statusFactory = statusFactory;
	}

	@Override
	public boolean isTraceDbgpProtocol() {
		return this.debugDbgpProtocol;
	}

	@Override
	public void requestTermination() {
	}

	@Override
	public void waitTerminated() throws InterruptedException {
	}

	// --------------- Debugger

	protected abstract IDebugger createDebugger(Thread thread);

	public IDebugger getDebugger() {
		return this.debugger;
	}

	public boolean isDebuggerStarted() {
		return this.debuggerStarted;
	}

	private void runDebugger() {
		if (this.debugger == null) {
			return;
		}

		this.debuggerStarted = true;

		this.thread = new Thread() {
			@Override
			public void run() {
				AbstractDbgpDebuggerEngine.this.debugger.run();
			};
		};
		this.thread.start();
	}

	@Override
	public synchronized void handleDebuggerEvent(IDebuggerEvent event) {
		DbgpXmlPacket packet = null;
		IDebuggerEvent.Type type = event.getType();
		switch (type) {
		case STDOUT_STREAM:
			String out = ((IStreamEvent) event).getContent();
			this.sender.send(new StdoutStreamPacket(out));
			break;
		case STDERR_STREAM:
			String err = ((IStreamEvent) event).getContent();
			this.sender.send(new StdoutStreamPacket(err));
			break;
		case SUSPENDED:
			// Debugger is suspended, send packet
			final ISuspendedEvent sm = (ISuspendedEvent) event;
			this.lastSuspendedEvent = sm;
			new Thread(new Runnable() {
				@Override
				public void run() {
					while (lastContinuationCommand == null) {
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							break;
						}
					}
					processStackRequestAndSuspendedMesage();
					sender.send(statusFactory.processStatusBreak(AbstractDbgpDebuggerEngine.this,
							lastContinuationCommand, sm));
				}
			}).start();
			break;
		case STOPPED:
			commandReceived();
			break;
		default:
			break;
		}
	}

	// --------------- DBGp Init Packet

	/**
	 *
	 * @param ideKey
	 * @param threadId
	 * @param fileURI
	 * @return
	 */
	protected InitPacket createInitPacket(String ideKey, String threadId, URI fileURI) {
		String appid = getInitPacketAppid();
		String session = getInitPacketSession();
		String parent = getInitPacketParent();
		String language = getInitPacketLanguage();
		String protocolVersion = getInitPacketProtocolVersion();
		return new InitPacket(appid, ideKey, session, threadId, parent, language, protocolVersion, fileURI);
	}

	private String getInitPacketSession() {
		return null;
	}

	protected String getInitPacketParent() {
		return "";
	}

	protected abstract String getInitPacketAppid();

	protected abstract String getInitPacketLanguage();

	protected abstract String getInitPacketProtocolVersion();

	public boolean isTerminated() {
		DebuggerState state = this.debugger.getState();
		return state == DebuggerState.STOPPED;
	}

	@Override
	public URI getFileURI() {
		return this.fileURI;
	}

	private synchronized void processStackRequestAndSuspendedMesage() {
		if (this.lastStackGetCommand == null || this.lastSuspendedEvent == null) {
			return;
		}

		// send an answer to the stack_get command
		DbgpXmlResponsePacket response = this.responseFactory.processCommandStackGet(this, this.lastStackGetCommand,
				this.lastSuspendedEvent.getLocations());
		this.sender.send(response);

		this.lastSuspendedEvent = null;
		this.lastStackGetCommand = null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.eclipse.dltk.dbgp.debugger.IDbgpDebuggerEngine#getDbgpContexts()
	 */
	@Override
	public final Collection<DbgpContext> getDbgpContexts() {
		return this.contexts;
	}

	/**
	 * By default none DBGp context is supported.
	 */
	protected Collection<DbgpContext> createDbgpContexts() {
		return Collections.emptyList();
	}
}
