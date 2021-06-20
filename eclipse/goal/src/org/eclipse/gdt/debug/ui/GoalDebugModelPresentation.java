package org.eclipse.gdt.debug.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.dltk.dbgp.debugger.debugger.DebuggerState;
import org.eclipse.dltk.debug.core.model.IScriptDebugTarget;
import org.eclipse.dltk.debug.core.model.IScriptStackFrame;
import org.eclipse.dltk.debug.core.model.IScriptThread;
import org.eclipse.dltk.debug.ui.ScriptDebugModelPresentation;
import org.eclipse.dltk.internal.ui.editor.EditorUtility;
import org.eclipse.gdt.debug.dbgp.AgentState;
import org.eclipse.gdt.debug.dbgp.DebuggerCollection;
import org.eclipse.gdt.debug.dbgp.LocalDebugger;
import org.eclipse.ui.IEditorInput;

import goal.core.runtime.service.environmentport.EnvironmentPort;
import languageTools.program.agent.AgentId;

public class GoalDebugModelPresentation extends ScriptDebugModelPresentation {

	@Override
	public String getEditorId(final IEditorInput input, final Object element) {
		return EditorUtility.getEditorID(input, element);
	}

	@Override
	protected String getDebugTargetText(final IScriptDebugTarget target) {
		return getDebuggingEngine(target).getName();
	}

	@Override
	protected String getStackFrameText(final IScriptStackFrame stackFrame) {
		try {
			final IPath sourcePath = new Path(stackFrame.getSourceURI().getPath());
			final IFile sourceFile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(sourcePath);
			if (stackFrame.getLineNumber() >= 0) {
				return stackFrame.getWhere() + ", " + sourceFile.getFullPath() + " line " + stackFrame.getLineNumber();
			} else {
				return "";
			}
		} catch (final Exception e) {
			return e.getMessage();
		}
	}

	@Override
	protected String getThreadText(final IScriptThread thread) {
		try {
			final String project = getProject(thread).getName();
			final DebuggerCollection collection = DebuggerCollection.getCollection(project);
			final LocalDebugger debugger = collection.getMainDebugger();
			final AgentId agent = collection.getAgentForThread(thread);
			if (agent != null) {
				final AgentState state = (debugger == null) ? new AgentState(null) : debugger.getAgentState(agent);
				return agent + " [" + state.getRunMode().toLowerCase() + "]";
			}
			final EnvironmentPort environment = collection.getEnvironmentForThread(thread);
			if (environment != null) {
				final DebuggerState state = (debugger == null) ? DebuggerState.STARTING
						: debugger.getEnvironmentDebugState(environment);
				return environment.getEnvironmentName() + " [" + state.toString().toLowerCase() + "]";
			}
			return "Not bound to an agent or environment";
		} catch (final Exception e) {
			return e.getMessage();
		}
	}
}
