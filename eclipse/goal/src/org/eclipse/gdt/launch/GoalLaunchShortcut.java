package org.eclipse.gdt.launch;

import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.dltk.internal.debug.ui.launcher.AbstractScriptLaunchShortcut;
import org.eclipse.gdt.GoalNature;

public class GoalLaunchShortcut extends AbstractScriptLaunchShortcut {

	@Override
	protected ILaunchConfigurationType getConfigurationType() {
		return getLaunchManager().getLaunchConfigurationType("org.eclipse.gdt.launch.GoalLaunchConfigurationType");
	}

	@Override
	protected String getNatureId() {
		return GoalNature.GOAL_NATURE;
	}
}
