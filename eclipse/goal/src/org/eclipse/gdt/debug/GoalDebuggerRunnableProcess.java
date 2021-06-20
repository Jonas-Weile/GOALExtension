package org.eclipse.gdt.debug;

import java.net.InetAddress;
import java.net.URI;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.dltk.dbgp.debugger.IDbgpDebuggerEngine;
import org.eclipse.dltk.debug.core.DLTKDebugLaunchConstants;
import org.eclipse.dltk.launching.InterpreterConfig;
import org.eclipse.gdt.debug.dbgp.DbgpDebugger;
import org.eclipse.gdt.launching.DLTKRunnableDebuggingProcess;
import org.eclipse.gdt.launching.RunnableDebuggingEngineRunner;

import goal.preferences.LoggingPreferences;

public class GoalDebuggerRunnableProcess extends DLTKRunnableDebuggingProcess {
	private final RunnableDebuggingEngineRunner runner;

	public GoalDebuggerRunnableProcess(final RunnableDebuggingEngineRunner runner, final ILaunch launch,
			final InterpreterConfig config) {
		super(runner.getPublicInstall(), launch, config);
		launch.setAttribute(DLTKDebugLaunchConstants.ATTR_DEBUG_CONSOLE, "true");
		this.runner = runner;
	}

	@Override
	protected IDbgpDebuggerEngine createDbgpDebuggerEngine(final InetAddress ideAdress, final int port,
			final String ideKey, final URI fileURI) {
		return new DbgpDebugger(ideAdress, port, ideKey, fileURI, LoggingPreferences.getEclipseDebug(), this.runner);
	}
}
