package org.eclipse.gdt.launch;

import org.eclipse.debug.core.ILaunch;
import org.eclipse.dltk.launching.IInterpreterInstall;
import org.eclipse.dltk.launching.InterpreterConfig;
import org.eclipse.gdt.launching.AbstractRunnableInterpreterRunner;
import org.eclipse.gdt.launching.RunnableProcess;

public class GoalInterpreterRunner extends AbstractRunnableInterpreterRunner {

	public GoalInterpreterRunner(final IInterpreterInstall install) {
		super(install);
	}

	@Override
	protected RunnableProcess createRunnableProcess(final ILaunch launch, final InterpreterConfig config) {
		return new GoalRunnableProcess(getInstall(), launch, config);
	}
}