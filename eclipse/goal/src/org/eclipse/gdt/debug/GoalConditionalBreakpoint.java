package org.eclipse.gdt.debug;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.model.IBreakpoint;

public class GoalConditionalBreakpoint extends GoalLineBreakpoint {
	private static final String MARKER_ID = "org.eclipse.gdt.debug.GoalConditionalBreakpointMarker";

	/**
	 * Constructs a GOAL conditional breakpoint on the given resource at the
	 * given line number.
	 *
	 * @param resource
	 *            file on which to set the breakpoint
	 * @param lineNumber
	 *            1-based line number of the breakpoint
	 * @throws CoreException
	 *             if unable to create the breakpoint
	 */
	public GoalConditionalBreakpoint(final IResource resource, final int lineNumber) throws CoreException {
		final IWorkspaceRunnable runnable = new IWorkspaceRunnable() {
			@Override
			public void run(final IProgressMonitor monitor) throws CoreException {
				final IMarker marker = resource.createMarker(MARKER_ID);
				marker.setAttribute(IBreakpoint.REGISTERED, Boolean.FALSE);
				marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
				marker.setAttribute(IBreakpoint.ID, getModelIdentifier());
				marker.setAttribute(IMarker.MESSAGE, "Conditional Breakpoint");
				setMarker(marker);
				setRegistered(true);
				setEnabled(true);
			}
		};
		run(getMarkerRule(resource), runnable);
	}
}
