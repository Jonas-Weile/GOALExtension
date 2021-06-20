package org.eclipse.gdt.debug.history;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.dltk.internal.debug.core.model.ScriptStackFrame;
import org.eclipse.dltk.internal.debug.core.model.ScriptThread;
import org.eclipse.gdt.debug.dbgp.DebuggerCollection;
import org.eclipse.gdt.debug.dbgp.LocalDebugger;
import org.eclipse.gdt.debug.history.LookupCommandHandler.ILookupHandler;
import org.eclipse.gdt.parser.GoalSourceParser;
import org.eclipse.swt.widgets.Display;

import goal.tools.history.EventStorage;
import goal.tools.history.events.AbstractEvent;
import languageTools.program.ProgramMap;
import languageTools.program.agent.AgentId;

public class LookupCommand extends HistoryCommand implements ILookupHandler {
	private LookupDialog dialog;

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
			final EventStorage history = debugger.getAgentState(agent).getHistory(debugger);
			final ProgramMap map = GoalSourceParser.getMap(debugger.getPath());
			final Set<String> lookupData = (history == null) ? new HashSet<>(0) : history.getAllLookupData(map);
			Display.getDefault().syncExec(new Runnable() {
				@Override
				public void run() {
					LookupCommand.this.dialog = new LookupDialog(lookupData.toArray(new String[lookupData.size()]));
					LookupCommand.this.dialog.open();
				}
			});
			final Set<String> result = new LinkedHashSet<>();
			if (this.dialog.getResult() != null) {
				for (final Object obj : this.dialog.getResult()) {
					result.add((String) obj);
				}
			}
			switch (this.dialog.getReturnCode()) {
			case LookupDialog.ALL_OCCURENCES:
				final Map<Integer, AbstractEvent> all = history.onlyAllMatching(map, result);
				context.resume();
				GoalHistoryView.setFilter(all);
				int stepto1 = history.getMax();
				for (final Integer key : all.keySet()) {
					if (all.get(key) != null) {
						stepto1 = key;
					}
				}
				debugger.historyStepTo(agent, stepto1);
				break;
			case LookupDialog.FIRST_OCCURENCE:
				final Map<Integer, AbstractEvent> first = history.onlyFirstMatching(map, result);
				context.resume();
				GoalHistoryView.setFilter(first);
				int stepto2 = 0;
				for (final Integer key : first.keySet()) {
					if (first.get(key) != null) {
						stepto2 = key;
						break;
					}
				}
				debugger.historyStepTo(agent, stepto2);
				break;
			case LookupDialog.LAST_OCCURENCE:
				final Map<Integer, AbstractEvent> last = history.onlyLastMatching(map, result);
				context.resume();
				int stepto3 = history.getMax();
				for (final Integer key : last.keySet()) {
					if (last.get(key) != null) {
						stepto3 = key;
					}
				}
				debugger.historyStepTo(agent, stepto3);
				break;
			default:
				break;
			}
		}
	}

	@Override
	protected Object getEnabledStateJobFamily(final IDebugCommandRequest request) {
		return ILookupHandler.class;
	}
}