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
package org.eclipse.gdt.debug.dbgp;

import java.net.InetAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.dltk.dbgp.debugger.AbstractDbgpDebuggerEngine;
import org.eclipse.dltk.dbgp.debugger.DbgpContext;
import org.eclipse.dltk.dbgp.debugger.debugger.IDebugger;
import org.eclipse.dltk.launching.InterpreterConfig;
import org.eclipse.gdt.launching.RunnableDebuggingEngineRunner;

public class DbgpDebugger extends AbstractDbgpDebuggerEngine {
	private static final String GOAL_APPID = "goal_debugger";
	private static final String GOAL_LANGUAGE = "goal";
	private static final String GOAL_PROTOCOL_VERSION = "1.0";
	private static final Map<String, String> SUPPORTED_FEATURES = new HashMap<String, String>(11);

	static {
		SUPPORTED_FEATURES.put("language_supports_threads", "1");
		SUPPORTED_FEATURES.put("language_name", "GOAL");
		SUPPORTED_FEATURES.put("language_version", "1.0");
		SUPPORTED_FEATURES.put("encoding", "UTF-8");
		SUPPORTED_FEATURES.put("protocol_version", "1");
		SUPPORTED_FEATURES.put("supports_async", "1");
		SUPPORTED_FEATURES.put("breakpoint_types", "line");
		SUPPORTED_FEATURES.put("multiple_sessions", "1");
		SUPPORTED_FEATURES.put("max_children", "32");
		SUPPORTED_FEATURES.put("max_data", "1024");
		SUPPORTED_FEATURES.put("max_depth", "1");
	}

	private final RunnableDebuggingEngineRunner runner;

	public DbgpDebugger(final InetAddress ideAdress, final int port, final String ideKey, final URI fileURI,
			final boolean debugDbgpProtocol, final RunnableDebuggingEngineRunner runner) {
		super(ideAdress, port, ideKey, fileURI, debugDbgpProtocol);
		this.runner = runner;
	}

	public RunnableDebuggingEngineRunner getRunner() {
		return this.runner;
	}

	@Override
	protected IDebugger createDebugger(final Thread thread) {
		final InterpreterConfig last = this.runner.getLastConfig();
		if (last != null && last.hasInterpreterArg(ThreadDebugger.AGENT)) {
			return new ThreadDebugger(this, thread);
		} else if (last != null && last.hasInterpreterArg(EnvDebugger.ENVIRONMENT)) {
			return new EnvDebugger(this, thread);
		} else {
			return new LocalDebugger(this, thread);
		}
	}

	@Override
	protected String getInitPacketAppid() {
		return GOAL_APPID;
	}

	@Override
	protected String getInitPacketLanguage() {
		return GOAL_LANGUAGE;
	}

	@Override
	protected String getInitPacketProtocolVersion() {
		return GOAL_PROTOCOL_VERSION;
	}

	@Override
	public String getDbgpFeature(final String featureName) {
		return SUPPORTED_FEATURES.get(featureName);
	}

	@Override
	public Collection<DbgpContext> createDbgpContexts() {
		final Collection<DbgpContext> contexts = new ArrayList<DbgpContext>(3);
		contexts.add(DbgpContext.DBGP_CONTEXT_LOCAL);
		contexts.add(DbgpContext.DBGP_CONTEXT_GLOBAL);
		contexts.add(DbgpContext.DBGP_CONTEXT_CLASS);
		return contexts;
	}
}
