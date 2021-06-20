package org.eclipse.gdt.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.dltk.launching.AbstractScriptLaunchConfigurationDelegate;
import org.eclipse.dltk.launching.IInterpreterRunner;
import org.eclipse.dltk.launching.InterpreterConfig;
import org.eclipse.gdt.GoalNature;

public class GoalLaunchConfigurationDelegate extends AbstractScriptLaunchConfigurationDelegate {

	@Override
	protected void runRunner(final ILaunchConfiguration configuration, final IInterpreterRunner runner,
			final InterpreterConfig config, final ILaunch launch, final IProgressMonitor monitor) throws CoreException {
		runner.run(config, launch, monitor);
	}

	@Override
	public String getLanguageId() {
		return GoalNature.GOAL_NATURE;
	}
}