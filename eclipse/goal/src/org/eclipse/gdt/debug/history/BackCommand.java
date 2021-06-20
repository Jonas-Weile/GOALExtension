package org.eclipse.gdt.debug.history;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.dltk.internal.debug.core.model.ScriptStackFrame;
import org.eclipse.dltk.internal.debug.core.model.ScriptThread;
import org.eclipse.gdt.debug.dbgp.DebuggerCollection;
import org.eclipse.gdt.debug.history.BackCommandHandler.IBackHandler;

import languageTools.program.agent.AgentId;

public class BackCommand extends HistoryCommand implements IBackHandler {
	@Override
	protected boolean isSteppable(final Object target) {
		if (target instanceof ScriptStackFrame) {
			return ((ScriptStackFrame) target).canResume(); // FIXME
		} else if (target instanceof ScriptThread) {
			return ((ScriptThread) target).canResume(); // FIXME
		} else {
			return false;
		}
	}

	@Override
	protected void step(final Object target) throws CoreException {
		if (target instanceof ScriptStackFrame) {
			final ScriptStackFrame context = (ScriptStackFrame) target;
			context.resume(); // hacky...
			final DebuggerCollection debuggerCollection = getDebuggerCollection(context);
			final AgentId agent = debuggerCollection.getAgentForThread(context.getScriptThread());
			debuggerCollection.getMainDebugger().historyBack(agent);
		}
	}

	@Override
	protected Object getEnabledStateJobFamily(final IDebugCommandRequest request) {
		return IBackHandler.class;
	}
}