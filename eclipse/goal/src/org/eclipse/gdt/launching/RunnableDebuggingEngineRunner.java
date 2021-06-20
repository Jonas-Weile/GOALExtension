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

import java.text.DateFormat;
import java.util.Date;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.RuntimeProcess;
import org.eclipse.dltk.compiler.util.Util;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.PreferencesLookupDelegate;
import org.eclipse.dltk.debug.core.IDbgpService;
import org.eclipse.dltk.debug.core.model.IScriptDebugTarget;
import org.eclipse.dltk.internal.launching.DLTKLaunchingPlugin;
import org.eclipse.dltk.launching.DebuggingEngineRunner;
import org.eclipse.dltk.launching.IInterpreterInstall;
import org.eclipse.dltk.launching.InterpreterConfig;
import org.eclipse.dltk.launching.LaunchingMessages;
import org.eclipse.osgi.util.NLS;

/**
 * Abstract implementation of a DLTK debugging engine runner which works with
 * {@link RunnableProcess} instead of classic Java Lang {@link Process}.
 *
 * This class could interest DLTK project?
 */
public abstract class RunnableDebuggingEngineRunner extends DebuggingEngineRunner {
	private InterpreterConfig lastConfig;
	private ILaunch lastLaunch;

	public RunnableDebuggingEngineRunner(final IInterpreterInstall install) {
		super(install);
	}

	@Override
	protected IScriptDebugTarget createDebugTarget(final ILaunch launch, final IDbgpService dbgpService)
			throws CoreException {
		return new GoalDebugTarget(getDebugModelId(), dbgpService, getSessionId(launch.getLaunchConfiguration()),
				launch);
	}

	public IInterpreterInstall getPublicInstall() {
		return getInstall();
	}

	public InterpreterConfig getLastConfig() {
		return this.lastConfig;
	}

	public ILaunch getLastLaunch() {
		return this.lastLaunch;
	}

	public void startSubProcess(final InterpreterConfig config, final ILaunch launch) throws CoreException {
		try {
			checkConfig(config, getInstall().getEnvironment());
			this.lastConfig = config;
			this.lastLaunch = launch;

			final IProgressMonitor monitor = new NullProgressMonitor();
			final PreferencesLookupDelegate delegate = createPreferencesLookupDelegate(launch);
			startProcess(config, launch, monitor, delegate);
		} catch (final CoreException e) {
			launch.terminate();
			throw e;
		}
	}

	@Override
	protected IProcess rawRun(final ILaunch launch, final InterpreterConfig config) throws CoreException {
		checkConfig(config, getInstall().getEnvironment());
		this.lastConfig = config;
		this.lastLaunch = launch;

		final String[] cmdLine = renderCommandLine(config);
		final IPath workingDirectory = config.getWorkingDirectoryPath();
		final String[] environment = getEnvironmentVariablesAsStrings(config);

		final String cmdLineLabel = renderCommandLineLabel(cmdLine);
		final String processLabel = renderProcessLabel(cmdLine);

		if (DLTKLaunchingPlugin.TRACE_EXECUTION) {
			traceExecution(processLabel, cmdLineLabel, workingDirectory, environment);
		}

		final RunnableProcess p = createRunnableProcess(launch, config);

		launch.setAttribute(DLTKLaunchingPlugin.LAUNCH_COMMAND_LINE, cmdLineLabel);
		final IProcess process[] = new IProcess[] { null };
		DebugPlugin.getDefault().addDebugEventListener(new IDebugEventSetListener() {
			@Override
			public void handleDebugEvents(final DebugEvent[] events) {
				for (final DebugEvent event : events) {
					if (event.getSource().equals(process[0])) {
						if (event.getKind() == DebugEvent.CHANGE || event.getKind() == DebugEvent.TERMINATE) {
							updateProcessLabel(launch, cmdLineLabel, process[0]);
							if (event.getKind() == DebugEvent.TERMINATE) {
								DebugPlugin.getDefault().removeDebugEventListener(this);
							}
						}
					}
				}
			}
		});
		process[0] = new RuntimeProcess(launch, p, processLabel, null);

		process[0].setAttribute(IProcess.ATTR_CMDLINE, cmdLineLabel);
		updateProcessLabel(launch, cmdLineLabel, process[0]);
		return process[0];
	}

	/**
	 * String representation of the command line
	 *
	 * @param commandLine
	 * @return
	 */
	private static String renderCommandLineLabel(final String[] commandLine) {
		if (commandLine.length == 0) {
			return Util.EMPTY_STRING;
		}
		final StringBuffer buf = new StringBuffer();
		for (int i = 0; i < commandLine.length; i++) {
			if (i != 0) {
				buf.append(' ');
			}
			final char[] characters = commandLine[i].toCharArray();
			final StringBuffer command = new StringBuffer();
			boolean containsSpace = false;
			for (final char character : characters) {
				if (character == '\"') {
					command.append('\\');
				} else if (character == ' ') {
					containsSpace = true;
				}
				command.append(character);
			}
			if (containsSpace) {
				buf.append('\"');
				buf.append(command.toString());
				buf.append('\"');
			} else {
				buf.append(command.toString());
			}
		}
		return buf.toString();
	}

	private static String renderProcessLabel(final String[] commandLine) {
		final String format = LaunchingMessages.StandardInterpreterRunner;
		final String timestamp = DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.MEDIUM)
				.format(new Date(System.currentTimeMillis()));
		return NLS.bind(format, commandLine[0], timestamp);
	}

	private void traceExecution(final String processLabel, final String cmdLineLabel, final IPath workingDirectory,
			final String[] environment) {
		final StringBuffer sb = new StringBuffer();
		sb.append("-----------------------------------------------\n");
		sb.append("Running ").append(processLabel).append('\n');
		sb.append("Command line: ").append(cmdLineLabel).append('\n');
		sb.append("Working directory: ").append(workingDirectory).append('\n');
		sb.append("Environment:\n");
		for (final String element : environment) {
			sb.append('\t').append(element).append('\n');
		}
		sb.append("-----------------------------------------------\n");
		System.out.println(sb);
	}

	private void updateProcessLabel(final ILaunch launch, final String cmdLineLabel, final IProcess process) {
		final StringBuffer buffer = new StringBuffer();
		int exitValue = 0;
		try {
			exitValue = process.getExitValue();
		} catch (final DebugException e1) {
			exitValue = 0;
		}
		if (exitValue != 0) {
			buffer.append("<abnormal exit code:" + exitValue + "> ");
		}
		String type = null;
		final ILaunchConfiguration launchConfiguration = launch.getLaunchConfiguration();
		if (launchConfiguration != null) {
			try {
				final ILaunchConfigurationType launchConfigType = launchConfiguration.getType();
				if (launchConfigType != null) {
					type = launchConfigType.getName();
				}
			} catch (final CoreException e) {
				DLTKCore.error(e);
			}
			buffer.append(launchConfiguration.getName());
		}
		if (type != null) {
			buffer.append(" [");
			buffer.append(type);
			buffer.append("] ");
		}
		buffer.append(process.getLabel());
		process.setAttribute(IProcess.ATTR_PROCESS_LABEL, buffer.toString());
	}

	/**
	 * Create {@link RunnableProcess}.
	 *
	 * @param launch
	 * @param config
	 * @return
	 */
	protected abstract RunnableProcess createRunnableProcess(ILaunch launch, InterpreterConfig config);
}