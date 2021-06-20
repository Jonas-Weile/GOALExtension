package org.eclipse.gdt.debug;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.dltk.core.PreferencesLookupDelegate;
import org.eclipse.dltk.launching.IInterpreterInstall;
import org.eclipse.dltk.launching.InterpreterConfig;
import org.eclipse.gdt.Activator;
import org.eclipse.gdt.launching.RunnableDebuggingEngineRunner;
import org.eclipse.gdt.launching.RunnableProcess;

public class GoalDebuggerRunner extends RunnableDebuggingEngineRunner {
	private static final String ENGINE_ID = "org.eclipse.gdt.debugger";

	public GoalDebuggerRunner(final IInterpreterInstall install) {
		super(install);
	}

	@Override
	protected RunnableProcess createRunnableProcess(final ILaunch launch, final InterpreterConfig config) {
		return new GoalDebuggerRunnableProcess(this, launch, config);
	}

	@Override
	protected InterpreterConfig addEngineConfig(final InterpreterConfig config,
			final PreferencesLookupDelegate delegate, final ILaunch launch) throws CoreException {
		return config;
	}

	@Override
	protected String getDebuggingEngineId() {
		return ENGINE_ID;
	}

	@Override
	protected String getDebugPreferenceQualifier() {
		return Activator.PLUGIN_ID;
	}

	@Override
	protected String getDebuggingEnginePreferenceQualifier() {
		return Activator.PLUGIN_ID;
	}

	@Override
	protected String getLogFileNamePreferenceKey() {
		return null;
	}

}
