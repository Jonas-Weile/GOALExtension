package org.eclipse.gdt.debug.dbgp;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.dltk.dbgp.debugger.IVariableAdder;
import org.eclipse.dltk.dbgp.debugger.debugger.AbstractDebugger;
import org.eclipse.gdt.debug.GoalLineBreakpoint;

import languageTools.program.agent.AgentId;

public class ThreadDebugger extends AbstractDebugger<GoalLineBreakpoint> {
	public final static String AGENT = "AGENT";
	private final AgentId agent;
	private final DebuggerCollection collection;
	private volatile boolean running;

	public ThreadDebugger(final DbgpDebugger debugger, final Thread thread) {
		super(debugger, thread);
		this.running = true;
		this.agent = new AgentId(debugger.getRunner().getLastConfig().getEnvVar(AGENT));
		final IPath ipath = new Path(getFileURI().getPath());
		final IFile ifile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(ipath);
		this.collection = DebuggerCollection.getCollection(ifile.getProject().getName());
		this.collection.registerDebugger(this.agent, this);
	}

	@Override
	protected void doRun() {
		synchronized (this) {
			while (this.running) {
				try {
					wait();
				} catch (final Exception ignore) {
				}
			}
		}
	}

	@Override
	protected void doStop() {
		synchronized (this) {
			this.running = false;
			notifyAll();
		}
	}

	@Override
	public void resume() {
		if (this.collection.getMainDebugger() != null) {
			this.collection.getMainDebugger().resume(this.agent);
		}
	}

	@Override
	public void suspend() {
		if (this.collection.getMainDebugger() != null) {
			this.collection.getMainDebugger().suspend(this.agent);
		}
	}

	@Override
	public void evaluate(final String expression, final IVariableAdder variableAdder) {
		if (this.collection.getMainDebugger() != null) {
			this.collection.getMainDebugger().evaluate(this.agent, expression, variableAdder);
		}
	}

	@Override
	public void collectVariables(final int contextId, final IVariableAdder variableAdder) {
	}

	@Override
	public GoalLineBreakpoint createBreakpoint(final String filename, final int lineno) {
		return (this.collection.getMainDebugger() == null) ? null
				: this.collection.getMainDebugger().createBreakpoint(filename, lineno);
	}

	@Override
	public void removeBreakpoint(final GoalLineBreakpoint breakPoint) {
		if (this.collection.getMainDebugger() != null) {
			this.collection.getMainDebugger().removeBreakpoint(breakPoint);
		}
	}
}