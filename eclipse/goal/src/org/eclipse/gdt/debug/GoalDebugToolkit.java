package org.eclipse.gdt.debug;

import org.eclipse.dltk.debug.core.AbstractDLTKDebugToolkit;

public class GoalDebugToolkit extends AbstractDLTKDebugToolkit {

	@Override
	public boolean isAccessWatchpointSupported() {
		return false;
	}
}