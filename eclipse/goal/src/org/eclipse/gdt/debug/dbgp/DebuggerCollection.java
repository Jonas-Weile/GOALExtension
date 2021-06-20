package org.eclipse.gdt.debug.dbgp;

import java.util.Collection;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.dltk.dbgp.debugger.debugger.AbstractDebugger;
import org.eclipse.dltk.dbgp.debugger.debugger.BreakPointLocation;
import org.eclipse.dltk.debug.core.model.IScriptThread;

import goal.core.runtime.service.environmentport.EnvironmentPort;
import languageTools.program.agent.AgentId;

public class DebuggerCollection {
	private static Map<String, DebuggerCollection> collections = new ConcurrentHashMap<>();
	private final Map<String, AbstractDebugger<?>> debuggers = new ConcurrentHashMap<>();
	private final Map<String, AgentId> agents = new ConcurrentHashMap<>();
	private final Map<String, EnvironmentPort> environments = new ConcurrentHashMap<>();
	private LocalDebugger main;

	public static DebuggerCollection registerCollection(final String project) {
		final DebuggerCollection collection = new DebuggerCollection();
		collections.put(project, collection);
		return collection;
	}

	public static DebuggerCollection getCollection(final String project) {
		return collections.get(project);
	}

	public static void removeCollection(final String project) {
		collections.remove(project);
	}

	public void registerDebugger(final AgentId agent, final AbstractDebugger<?> debugger) {
		final String id = agent.toString();
		final String thread = registerDebugger(id, debugger);
		this.agents.put(thread, agent);
	}

	public void registerDebugger(final EnvironmentPort environment, final AbstractDebugger<?> debugger) {
		final String id = environment.getEnvironmentName();
		final String thread = registerDebugger(id, debugger);
		this.environments.put(thread, environment);
	}

	private String registerDebugger(final String id, final AbstractDebugger<?> debugger) {
		if (id != null && debugger != null) {
			debugger.bind();
			this.debuggers.put(id, debugger);
			if (debugger instanceof LocalDebugger) {
				this.main = (LocalDebugger) debugger;
			}
			return Long.toString(debugger.getThread().getId());
		} else {
			return null;
		}
	}

	public boolean hasDebugger(final AgentId agent) {
		return (agent == null) ? false : hasDebugger(agent.toString());
	}

	public boolean hasDebugger(final EnvironmentPort environment) {
		return (environment == null) ? false : hasDebugger(environment.getEnvironmentName());
	}

	private boolean hasDebugger(final String id) {
		return this.debuggers.containsKey(id);
	}

	public LocalDebugger getMainDebugger() {
		return this.main;
	}

	public Collection<AgentId> getAgents() {
		return this.agents.values();
	}

	public AgentId getAgentForThread(final IScriptThread thread) {
		try {
			return this.agents.get(thread.getName());
		} catch (final Exception e) {
			return null;
		}
	}

	public EnvironmentPort getEnvironmentForThread(final IScriptThread thread) {
		try {
			return this.environments.get(thread.getName());
		} catch (final Exception e) {
			return null;
		}
	}

	public void suspendByBreakPoint(final String id, final Deque<BreakPointLocation> positions) {
		final AbstractDebugger<?> debugger = this.debuggers.get(id);
		if (debugger != null) {
			debugger.suspendByBreakPoint(positions);
		}
	}

	private DebuggerCollection() {
	}
}
