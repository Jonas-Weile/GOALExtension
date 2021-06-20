package org.eclipse.gdt.debug;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.LineBreakpoint;
import org.eclipse.gdt.Metrics;
import org.eclipse.gdt.Metrics.Event;
import org.eclipse.gdt.debug.dbgp.DebuggerCollection;

public class GoalLineBreakpoint extends LineBreakpoint {
	private static final String MODEL_ID = "org.eclipse.gdt.debug.core";
	private static final String MARKER_ID = "org.eclipse.gdt.debug.GoalLineBreakpointMarker";

	public static GoalLineBreakpoint[] getAll() {
		final IBreakpoint[] list = DebugPlugin.getDefault().getBreakpointManager().getBreakpoints(MODEL_ID);
		final GoalLineBreakpoint[] returned = new GoalLineBreakpoint[list.length];
		for (int i = 0; i < list.length; ++i) {
			returned[i] = (GoalLineBreakpoint) list[i];
		}
		return returned;
	}

	/**
	 * Constructs a GOAL line breakpoint on the given resource at the given line
	 * number.
	 *
	 * @param resource
	 *            file on which to set the breakpoint
	 * @param lineNumber
	 *            1-based line number of the breakpoint
	 * @throws CoreException
	 *             if unable to create the breakpoint
	 */
	public GoalLineBreakpoint(final IResource resource, final int lineNumber) throws CoreException {
		final IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(final IProgressMonitor monitor) throws CoreException {
				Metrics.event(Event.BREAKPOINT);
				final IMarker marker = resource.createMarker(MARKER_ID);
				marker.setAttribute(IBreakpoint.REGISTERED, Boolean.FALSE);
				marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
				marker.setAttribute(IBreakpoint.ID, getModelIdentifier());
				marker.setAttribute(IMarker.MESSAGE, "Regular Breakpoint");
				setMarker(marker);
				setRegistered(true);
				setEnabled(true);
			}
		};
		run(getMarkerRule(resource), runnable);
	}

	protected GoalLineBreakpoint() {
		// For GoalConditionalBreakpoint...
	}

	@Override
	public String getModelIdentifier() {
		return MODEL_ID;
	}

	@Override
	public void setEnabled(boolean isEnabled) throws CoreException {
		boolean wasEnabled = isEnabled();
		super.setEnabled(isEnabled);
		if (wasEnabled != isEnabled) {
			final DebuggerCollection collection = DebuggerCollection
					.getCollection(getMarker().getResource().getProject().getName());
			if (collection != null && collection.getMainDebugger() != null) {
				collection.getMainDebugger().updateBreakpoints();
			}
		}
	}
}
