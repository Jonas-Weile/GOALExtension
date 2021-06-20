package org.eclipse.dltk.dbgp.debugger.internal.packet.sender;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.eclipse.dltk.dbgp.debugger.IDbgpDebuggerEngine;
import org.eclipse.dltk.dbgp.debugger.packet.sender.DbgpXmlPacket;
import org.eclipse.dltk.dbgp.internal.DbgpWorkingThread;
import org.eclipse.dltk.dbgp.internal.IDbgpTerminationListener;

public class DbgpXmlPacketSender extends DbgpWorkingThread implements IDbgpTerminationListener {
	private static final String THREAD_NAME = "DBGP Debugger - XML Packet sender";

	final private OutputStream outputStream;
	final private Queue<DbgpXmlPacket> fResponseQueue = new ConcurrentLinkedQueue<DbgpXmlPacket>();

	private final Object fTerminatedLock = new Object();
	private boolean fTerminated = false;

	private IDbgpDebuggerEngine engine;

	public DbgpXmlPacketSender(IDbgpDebuggerEngine engine, OutputStream outputStream) {
		super(THREAD_NAME);
		this.outputStream = outputStream;
		this.engine = engine;
		this.engine.addTerminationListener(this);
	}

	@Override
	protected void workingCycle() throws Exception {
		try {
			while (!hasTerminated() || hasAvailableResponse()) {
				synchronized (this) {
					while (!hasAvailableResponse()) {
						wait();
						if (hasTerminated()) {
							return;
						}
					}
				}

				DbgpXmlPacket xe = this.fResponseQueue.peek();
				String data = xe.toXml();
				String length = "" + data.length();

				this.outputStream.write(length.getBytes());
				this.outputStream.write(0);
				this.outputStream.write(data.getBytes());
				this.outputStream.write(0);
				this.outputStream.flush();
				if (this.engine.isTraceDbgpProtocol()) {
					System.out.println("sent: " + data);
				}
				this.fResponseQueue.remove(xe);
			}
		} catch (InterruptedException ie) {
			if (this.engine.isTraceDbgpProtocol()) {
				System.err.println("Sender exception: " + ie.getMessage());
				System.out.println("Sender exception: terminating responder");
			}
		} catch (IOException ioe) {
			if (this.engine.isTraceDbgpProtocol()) {
				System.err.println("Sender exception: " + ioe.getMessage());
				System.out.println("Sender exception: terminating responder");
			}
		}
	}

	public boolean hasAvailableResponse() {
		return !this.fResponseQueue.isEmpty();
	}

	private boolean hasTerminated() {
		return this.fTerminated;
	}

	public void terminate() {
		this.fTerminated = true;
	}

	public void send(DbgpXmlPacket response) {
		if (!this.fTerminated) {
			synchronized (this) {
				this.fResponseQueue.add(response);
				notifyAll();
			}
		}
	}

	@Override
	public void objectTerminated(Object object, Exception e) {
		synchronized (this.fTerminatedLock) {
			if (hasTerminated()) {
				return;
			}

			try {
				this.outputStream.close();
			} catch (IOException ioe) {
			}
			this.engine.removeTerminationListener(this);
			try {
				this.engine.waitTerminated();
			} catch (InterruptedException ie) {
			}

			terminate();
		}

		synchronized (this) {
			notifyAll();
		}

		fireObjectTerminated(e);
	}
}