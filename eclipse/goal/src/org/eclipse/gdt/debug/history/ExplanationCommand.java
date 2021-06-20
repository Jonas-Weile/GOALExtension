package org.eclipse.gdt.debug.history;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.dltk.internal.debug.core.model.ScriptStackFrame;
import org.eclipse.dltk.internal.debug.core.model.ScriptThread;
import org.eclipse.gdt.debug.dbgp.DebuggerCollection;
import org.eclipse.gdt.debug.dbgp.LocalDebugger;
import org.eclipse.gdt.debug.history.ExplanationCommandHandler.IExplanationHandler;
import org.eclipse.swt.widgets.Display;

import languageTools.program.agent.AgentId;

public class ExplanationCommand extends HistoryCommand implements IExplanationHandler {
	@Override
	protected boolean isSteppable(final Object target) {
		if (target instanceof ScriptStackFrame) {
			return ((ScriptStackFrame) target).isSuspended(); // FIXME
		} else if (target instanceof ScriptThread) {
			return ((ScriptThread) target).isSuspended(); // FIXME

		} else {
			return false;
		}
	}

	@Override
	protected void step(final Object target) throws CoreException {
		if (target instanceof ScriptStackFrame) {
			final ScriptStackFrame context = (ScriptStackFrame) target;
			final DebuggerCollection debuggerCollection = getDebuggerCollection(context);
			final LocalDebugger debugger = debuggerCollection.getMainDebugger();
			final AgentId agent = debuggerCollection.getAgentForThread(context.getScriptThread());
			Display.getDefault().asyncExec(new Runnable() {
				@Override
				public void run() {
					new ExplanationDialog(debugger, agent).open();
				}
			});
		}
	}

	@Override
	protected Object getEnabledStateJobFamily(final IDebugCommandRequest request) {
		return IExplanationHandler.class;
	}
}