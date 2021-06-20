package org.eclipse.gdt.launch;

import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.dltk.launching.AbstractInterpreterInstall;
import org.eclipse.dltk.launching.IInterpreterInstallType;
import org.eclipse.dltk.launching.IInterpreterRunner;
import org.eclipse.gdt.GoalNature;

public class GoalInterpreterInstall extends AbstractInterpreterInstall {

	public GoalInterpreterInstall(final IInterpreterInstallType type, final String id) {
		super(type, id);
	}

	@Override
	public IInterpreterRunner getInterpreterRunner(final String mode) {
		final IInterpreterRunner runner = super.getInterpreterRunner(mode);
		if (runner != null) {
			return runner;
		}
		if (mode.equals(ILaunchManager.RUN_MODE)) {
			return new GoalInterpreterRunner(this);
		}
		return null;
	}

	@Override
	public String getNatureId() {
		return GoalNature.GOAL_NATURE;
	}
}
