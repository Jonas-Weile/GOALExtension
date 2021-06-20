package org.eclipse.gdt.parser;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.builder.IBuildChange;
import org.eclipse.dltk.core.builder.IBuildState;
import org.eclipse.dltk.core.builder.IScriptBuilder;
import org.eclipse.gdt.Activator;
import org.eclipse.gdt.editor.ComboToolbar;

import languageTools.analyzer.FileRegistry;
import languageTools.analyzer.mas.MASValidator;

public class GoalProjectBuilder implements IScriptBuilder {
	@Override
	public boolean initialize(final IScriptProject project) {
		return true;
	}

	@Override
	public void prepare(final IBuildChange change, final IBuildState state, final IProgressMonitor monitor)
			throws CoreException {
	}

	@Override
	public void build(final IBuildChange change, final IBuildState state, final IProgressMonitor monitor)
			throws CoreException {
		final ComboToolbar toolbar = Activator.getDefault().getActiveToolbar();
		if (toolbar != null && toolbar.getActiveItem() != null) {
			final IFile masfile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(toolbar.getActiveItem());
			if (masfile != null && masfile.exists()) {
				masfile.getProject().deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
				final FileRegistry registry = new FileRegistry();
				final MASValidator visitor = new MASValidator(masfile.getLocation().toOSString(), registry);
				visitor.validate();
				visitor.process(); // full process
				GoalSourceParser.markProblems(masfile, registry);
			}
		}
	}

	@Override
	public void endBuild(final IScriptProject project, final IBuildState state, final IProgressMonitor monitor) {
	}

	@Override
	public void clean(final IScriptProject project, final IProgressMonitor monitor) {
	}
}
