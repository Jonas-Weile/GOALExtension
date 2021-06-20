package org.eclipse.gdt.debug.history;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.internal.core.commands.StepCommand;
import org.eclipse.dltk.internal.debug.core.model.ScriptStackFrame;
import org.eclipse.gdt.debug.dbgp.DebuggerCollection;

@SuppressWarnings("restriction")
abstract class HistoryCommand extends StepCommand {
	protected DebuggerCollection getDebuggerCollection(ScriptStackFrame context) {
		final IPath sourcePath = new Path(context.getSourceURI().getPath());
		final IFile sourceFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(sourcePath);
		return DebuggerCollection.getCollection(sourceFile.getProject().getName());
	}
}
