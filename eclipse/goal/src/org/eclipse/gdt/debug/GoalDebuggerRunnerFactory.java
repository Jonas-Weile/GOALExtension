package org.eclipse.gdt.debug;

import org.eclipse.dltk.launching.IInterpreterInstall;
import org.eclipse.dltk.launching.IInterpreterRunner;
import org.eclipse.dltk.launching.IInterpreterRunnerFactory;

public class GoalDebuggerRunnerFactory implements IInterpreterRunnerFactory {

	@Override
	public IInterpreterRunner createRunner(final IInterpreterInstall install) {
		return new GoalDebuggerRunner(install);
	}
}
