package org.eclipse.gdt.launch;

import java.io.IOException;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dltk.core.environment.IDeployment;
import org.eclipse.dltk.core.environment.IFileHandle;
import org.eclipse.dltk.internal.launching.AbstractInterpreterInstallType;
import org.eclipse.dltk.launching.EnvironmentVariable;
import org.eclipse.dltk.launching.IInterpreterInstall;
import org.eclipse.dltk.launching.LibraryLocation;
import org.eclipse.gdt.Activator;
import org.eclipse.gdt.GoalNature;

public class GoalInterpreterInstallType extends AbstractInterpreterInstallType {

	@Override
	protected IInterpreterInstall doCreateInterpreterInstall(final String id) {
		return new GoalInterpreterInstall(this, id);
	}

	@Override
	protected ILog getLog() {
		return Activator.getDefault().getLog();
	}

	@Override
	protected String getPluginId() {
		return Activator.PLUGIN_ID;
	}

	@Override
	protected String[] getPossibleInterpreterNames() {
		return new String[] { "goal" };
	}

	@Override
	public String getName() {
		return "GOAL Installation Type";
	}

	@Override
	public LibraryLocation[] getDefaultLibraryLocations(final IFileHandle installLocation,
			final EnvironmentVariable[] variables, final IProgressMonitor monitor) {
		return new LibraryLocation[0];
	}

	@Override
	public String getNatureId() {
		return GoalNature.GOAL_NATURE;
	}

	@Override
	public IStatus validateInstallLocation(final IFileHandle installLocation) {
		return Status.OK_STATUS;
	}

	@Override
	protected IPath createPathFile(final IDeployment deployment) throws IOException {
		throw new RuntimeException("This method should not be used");
	}

}
