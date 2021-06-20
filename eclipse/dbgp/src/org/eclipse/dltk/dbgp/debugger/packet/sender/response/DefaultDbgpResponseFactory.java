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
package org.eclipse.dltk.dbgp.debugger.packet.sender.response;

import java.util.Collection;
import java.util.Deque;

import org.eclipse.dltk.dbgp.DbgpRequest;
import org.eclipse.dltk.dbgp.debugger.DbgpContext;
import org.eclipse.dltk.dbgp.debugger.IDbgpDebuggerEngine;
import org.eclipse.dltk.dbgp.debugger.debugger.BreakPointLocation;
import org.eclipse.dltk.dbgp.debugger.packet.sender.DbgpXmlPacket;
import org.eclipse.dltk.dbgp.debugger.packet.sender.status.RunningPacket;
import org.eclipse.dltk.dbgp.debugger.packet.sender.status.StoppedPacket;
import org.eclipse.dltk.dbgp.debugger.packet.sender.status.StoppingPacket;

/**
 * Default implementation for {@link IDbgpResponseFactory}.
 *
 */
public class DefaultDbgpResponseFactory implements IDbgpResponseFactory {

	public static final IDbgpResponseFactory INSTANCE = new DefaultDbgpResponseFactory();

	@Override
	public DbgpXmlResponsePacket processCommandFeatureSet(IDbgpDebuggerEngine engine, DbgpRequest command) {
		return new FeatureSetPacket(command);
	}

	@Override
	public DbgpXmlResponsePacket processCommandFeatureGet(IDbgpDebuggerEngine engine, DbgpRequest command) {
		return new FeatureGetPacket(command, engine);
	}

	@Override
	public DbgpXmlResponsePacket processCommandStdout(IDbgpDebuggerEngine engine, DbgpRequest command) {
		return new StdoutPacket(command);
	}

	@Override
	public DbgpXmlResponsePacket processCommandStderr(IDbgpDebuggerEngine engine, DbgpRequest command) {
		return new StderrPacket(command);
	}

	public DbgpXmlResponsePacket processCommandStop(IDbgpDebuggerEngine engine, DbgpRequest command) {
		// TODO : manage stopping, stopped status
		return null;
	}

	public DbgpXmlResponsePacket processCommandRunning(IDbgpDebuggerEngine engine, DbgpRequest command) {
		return new RunningPacket(command);
	}

	public DbgpXmlResponsePacket processCommandStopping(IDbgpDebuggerEngine engine, DbgpRequest command) {
		return new StoppingPacket(command);
	}

	public DbgpXmlResponsePacket processCommandStopped(IDbgpDebuggerEngine engine, DbgpRequest command) {
		return new StoppedPacket(command);
	}

	@Override
	public BreakPointSetPacket processCommandBreakPointSet(IDbgpDebuggerEngine engine, DbgpRequest command) {
		return new BreakPointSetPacket(command);
	}

	@Override
	public BreakPointRemovePacket processCommandBreakPointRemove(IDbgpDebuggerEngine engine, DbgpRequest command) {
		return new BreakPointRemovePacket(command);
	}

	@Override
	public DbgpXmlResponsePacket processCommandStackGet(IDbgpDebuggerEngine engine, DbgpRequest command,
			Deque<BreakPointLocation> locations) {
		String data = "";
		int level = 0;
		for (BreakPointLocation location : locations) {
			final String filename = location.getFileName();
			final String where = location.getWhere();
			final int lineno = location.getLineBegin();
			final String cmdbegin = location.getLineBegin() + ":" + location.getColumnBegin();
			final String cmdend = location.getLineEnd() + ":" + location.getColumnEnd();

			DbgpXmlPacket stack = new DbgpXmlPacket("stack");
			stack.addAttribute("level", level++);
			stack.addAttribute("type", "file");
			stack.addAttribute("filename", filename);
			stack.addAttribute("where", where);
			stack.addAttribute("lineno", lineno);
			stack.addAttribute("cmdbegin", cmdbegin);
			stack.addAttribute("cmdend", cmdend);
			data += stack.toXml();
		}
		StackGetPacket packet = new StackGetPacket(command);
		packet.setData(data);
		return packet;
	}

	@Override
	public DbgpXmlResponsePacket processCommandBreak(IDbgpDebuggerEngine engine, DbgpRequest request) {
		DbgpXmlResponsePacket response = new DbgpXmlResponsePacket(request);
		response.addAttribute("success", "1");
		return response;
	}

	@Override
	public DbgpXmlResponsePacket processCommandBreakPointGet(IDbgpDebuggerEngine engine, DbgpRequest request) {
		DbgpXmlResponsePacket response = new DbgpXmlResponsePacket(request);
		response.addAttribute("success", "0");
		return response;
	}

	@Override
	public ContextGetPacket processCommandContextGet(IDbgpDebuggerEngine engine, DbgpRequest command) {
		return new ContextGetPacket(command);
	}

	@Override
	public DbgpXmlResponsePacket processCommandContextNames(IDbgpDebuggerEngine engine, DbgpRequest command) {
		ContextNamesPacket packet = new ContextNamesPacket(command);
		Collection<DbgpContext> contexts = engine.getDbgpContexts();
		for (DbgpContext context : contexts) {
			packet.addContext(context.getId(), context.getName());
		}
		return packet;
	}

	@Override
	public DbgpXmlResponsePacket processCommandStep(IDbgpDebuggerEngine engine, DbgpRequest request) {
		DbgpXmlResponsePacket response = new DbgpXmlResponsePacket(request);
		response.addAttribute("status", "break");
		response.addAttribute("reason", "ok");
		return response;
	}

	@Override
	public EvalPacket processCommandEval(IDbgpDebuggerEngine engine, DbgpRequest request) {
		return new EvalPacket(request);
	}

}
