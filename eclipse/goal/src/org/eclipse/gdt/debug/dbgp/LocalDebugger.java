
package org.eclipse.gdt.debug.dbgp;

import java.io.BufferedWriter;
import java.io.File;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.Deque;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IWatchExpression;
import org.eclipse.dltk.dbgp.debugger.IDbgpDebuggerEngine;
import org.eclipse.dltk.dbgp.debugger.IVariableAdder;
import org.eclipse.dltk.dbgp.debugger.debugger.AbstractDebugger;
import org.eclipse.dltk.dbgp.debugger.debugger.BreakPointLocation;
import org.eclipse.dltk.dbgp.debugger.debugger.DebuggerState;
import org.eclipse.dltk.debug.core.model.IScriptThread;
import org.eclipse.gdt.Activator;
import org.eclipse.gdt.Messages;
import org.eclipse.gdt.Metrics;
import org.eclipse.gdt.Metrics.Event;
import org.eclipse.gdt.debug.GoalConditionalBreakpoint;
import org.eclipse.gdt.debug.GoalLineBreakpoint;
import org.eclipse.gdt.debug.dbgp.StreamReader.EvalReceiver;
import org.eclipse.gdt.debug.dbgp.StreamReader.ExplanationReceiver;
import org.eclipse.gdt.debug.dbgp.StreamReader.StateReceiver;
import org.eclipse.gdt.debug.history.ExplanationDialog.Explanation;
import org.eclipse.gdt.debug.history.GoalHistoryView;
import org.eclipse.gdt.debug.ui.GoalAgentConsole;
import org.eclipse.gdt.debug.ui.GoalAgentConsoleView;
import org.eclipse.gdt.launch.GoalRunnableProcess;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;

import goal.core.runtime.service.environmentport.EnvironmentPort;
import goal.preferences.LoggingPreferences;
import goal.preferences.Preferences;
import goal.tools.debugger.BreakPoint;
import goal.tools.eclipse.DebugCommand;
import goal.tools.eclipse.DebugCommand.Command;
import goal.tools.eclipse.DebugTool;
import goal.tools.eclipse.EclipseStepper.StepMode;
import goal.tools.eclipse.GoalBreakpointManager;
import languageTools.program.agent.AgentId;

public class LocalDebugger extends AbstractDebugger<GoalLineBreakpoint> {
	private final Map<String, IScriptThread> threads;
	private volatile Thread watcher;
	private IPath path;
	private DebugEventSetListener listener;
	private AgentId agent;
	private Process process;
	private StreamReader reader;
	private StreamWriter writer;
	private String project;
	private DebuggerCollection collection;

	public LocalDebugger(final IDbgpDebuggerEngine debugger, final Thread thread) {
		super(debugger, thread);
		this.threads = new ConcurrentHashMap<>();
		this.watcher = new Thread() {
			@Override
			public void run() {
				while (LocalDebugger.this.watcher != null) {
					for (final IExpression expression : DebugPlugin.getDefault().getExpressionManager()
							.getExpressions()) {
						if (expression instanceof IWatchExpression) {
							((IWatchExpression) expression).evaluate();
						}
					}
					try {
						Thread.sleep(100);
					} catch (final Exception ignore) {
					}
				}
			}
		};
	}

	public IPath getPath() {
		return this.path;
	}

	public DebuggerState getEnvironmentDebugState(final EnvironmentPort environment) {
		return this.reader.getEnvironmentDebugState(environment);
	}

	private void setEnvironmentDebugState(final EnvironmentPort environment, final DebuggerState state) {
		this.reader.setEnvironmentDebugState(environment, state);
	}

	public DebuggerState getAgentDebugState(final AgentId agent) {
		return this.reader.getAgentDebugState(agent);
	}

	private void setAgentDebugState(final AgentId agent, final DebuggerState state) {
		this.reader.setAgentDebugState(agent, state);
	}

	public AgentState getAgentState(final AgentId agent) {
		return this.reader.getAgentState(agent);
	}

	public boolean getEnvironmentState(final EnvironmentPort environment) {
		return this.reader.getEnvironmentState(environment);
	}

	public GoalAgentConsole getConsole(final AgentId agent) {
		return this.reader.getConsole(agent);
	}

	public boolean hasDebugger(final AgentId agent) {
		return this.collection.hasDebugger(agent);
	}

	public boolean hasDebugger(final EnvironmentPort environment) {
		return this.collection.hasDebugger(environment);
	}

	public IScriptThread getThread(final AgentId agent) {
		return this.threads.get(agent.toString());
	}

	public IScriptThread getThread(final EnvironmentPort environment) {
		return this.threads.get(environment.getEnvironmentName());
	}

	public void suspendByBreakPoint(final String id, final Deque<BreakPointLocation> positions) {
		this.collection.suspendByBreakPoint(id, positions);
	}

	public void suspendByBreakPoint(final String id) {
		this.collection.suspendByBreakPoint(id, new LinkedList<BreakPointLocation>());
	}

	public void write(final DebugCommand command) {
		try {
			if (this.writer != null) {
				this.writer.write(command);
			}
		} catch (final Exception e) {
			err(e);
		}
	}

	public void setAgent(final AgentId agent) {
		if (this.agent == null) {
			this.agent = agent;
			this.collection.registerDebugger(agent, this);
		}
	}

	private class DebugEventSetListener implements IDebugEventSetListener {
		@Override
		public void handleDebugEvents(final DebugEvent[] events) {
			for (final DebugEvent event : events) {
				AgentId agentId = null;
				EnvironmentPort envId = null;
				if (event.getSource() instanceof IScriptThread) {
					try {
						final IScriptThread thread = (IScriptThread) event.getSource();
						agentId = LocalDebugger.this.collection.getAgentForThread(thread);
						envId = LocalDebugger.this.collection.getEnvironmentForThread(thread);
						if (agentId != null) {
							LocalDebugger.this.threads.put(agentId.toString(), thread);
						} else if (envId != null) {
							LocalDebugger.this.threads.put(envId.getEnvironmentName(), thread);
						}
					} catch (final Exception ignore) {
					}
				}
				switch (event.getKind()) {
				case DebugEvent.RESUME:
					if (LocalDebugger.this.collection.hasDebugger(agentId)) {
						switch (event.getDetail()) {
						case DebugEvent.STEP_INTO:
							Metrics.event(Event.STEP_IN);
							step(agentId, StepMode.INTO);
							break;
						case DebugEvent.STEP_OVER:
							Metrics.event(Event.STEP_OVER);
							step(agentId, StepMode.OVER);
							break;
						case DebugEvent.STEP_RETURN:
							Metrics.event(Event.STEP_OUT);
							step(agentId, StepMode.OUT);
							break;
						default:
							resume(agentId);
							break;
						}
					} else if (LocalDebugger.this.collection.hasDebugger(envId)) {
						switch (event.getDetail()) {
						case DebugEvent.STEP_INTO:
						case DebugEvent.STEP_OVER:
						case DebugEvent.STEP_RETURN:
							suspendByBreakPoint(envId.getEnvironmentName());
							break;
						default:
							resume(envId);
							break;
						}
					}
					break;
				case DebugEvent.SUSPEND:
					if (LocalDebugger.this.collection.hasDebugger(agentId)) {
						suspend(agentId);
					} else if (LocalDebugger.this.collection.hasDebugger(envId)) {
						suspend(envId);
					}
					break;
				default:
					break;
				}
			}
		}
	}

	@Override
	public void doRun() {
		try {
			final IPath ipath = new Path(getFileURI().getPath());
			final IFile ifile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(ipath);
			this.project = ifile.getProject().getName();
			this.collection = DebuggerCollection.registerCollection(this.project);
			this.listener = new DebugEventSetListener();
			DebugPlugin.getDefault().addDebugEventListener(this.listener);

			this.path = GoalRunnableProcess.getRunnable(ifile);
			if (this.path == null) {
				out("This cannot be run by GOAL; please select a valid MAS2G or TEST2G file.\n");
				return;
			}

			final String[] command = new String[] { "java", // "-Xdebug", "-Xnoagent",
					// "-Xrunjdwp:transport=dt_socket,address=8000,server=y,suspend=n",
					"-cp", Activator.getDefault().getJARpath(), DebugTool.class.getName(),
					Preferences.getSettingsFile().getCanonicalPath(), this.path.toOSString(), getAllBreakpoints() };
			if (Messages.TestFileExtension.equalsIgnoreCase(this.path.getFileExtension())) {
				Metrics.event(Event.DEBUG_TEST);
			} else {
				Metrics.event(Event.DEBUG_MAS);
			}
			this.process = GoalRunnableProcess.getProcess(command, this.path.toFile().getParentFile(), true);
			this.writer = new StreamWriter(this.process.getOutputStream());
			out("Starting debugger for " + this.path.toOSString() + ", please wait...\n");
			this.reader = new StreamReader(this, this.process.getInputStream());
			this.reader.start();
			if (LoggingPreferences.getEclipseDebug()) {
				out(Arrays.asList(command) + "\n");
			}
			this.watcher.start();
			this.process.waitFor();
		} catch (final Exception e) {
			err(e);
		} finally {
			Metrics.event(Event.END);
			doStop();
		}
	}

	@Override
	public void err(final Exception e) {
		if (LoggingPreferences.getShowStackdump()) {
			super.err(e);
		} else {
			super.err(e.getMessage());
		}
	}

	@Override
	public void doStop() {
		if (this.writer != null) {
			try {
				this.writer.write(new DebugCommand(Command.STOP, ""));
			} catch (final Exception ignore) {
			}
			this.writer.close();
			this.writer = null;
			if (this.reader != null) {
				this.reader.end();
			}
		}
		if (this.watcher != null) {
			this.watcher = null;
		}
		DebugPlugin.getDefault().removeDebugEventListener(this.listener);
		DebuggerCollection.removeCollection(this.project);
		Display.getDefault().asyncExec(() -> {
			final IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			for (final GoalAgentConsole console : LocalDebugger.this.reader.getConsoles()) {
				console.destroy();
				page.hideView(page.findViewReference(GoalAgentConsoleView.VIEW_ID, console.getName()));
			}
			final IViewReference ref = page.findViewReference(GoalHistoryView.VIEW_ID);
			final IWorkbenchPart part = (ref == null) ? null : ref.getPart(false);
			if (part instanceof GoalHistoryView) {
				((GoalHistoryView) part).clear();
			}
		});
	}

	@Override
	public void resume() {
		resume(this.agent);
	}

	public void resume(final AgentId agent) {
		if (getAgentState(agent).isInitialized() && getAgentDebugState(agent) != DebuggerState.RUNNING) {
			write(new DebugCommand(Command.RUN, agent));
			setAgentDebugState(agent, DebuggerState.RUNNING);
		}
	}

	public void resume(final EnvironmentPort environment) {
		if (getEnvironmentState(environment) && getEnvironmentDebugState(environment) != DebuggerState.RUNNING) {
			write(new DebugCommand(Command.ENV_RUN, environment));
			setEnvironmentDebugState(environment, DebuggerState.RUNNING);
		}
	}

	@Override
	public void suspend() {
		suspend(this.agent);
	}

	public void suspend(final AgentId agent) {
		if (getAgentState(agent).isInitialized() && getAgentDebugState(agent) != DebuggerState.SUSPENDED) {
			write(new DebugCommand(Command.PAUSE, agent));
			setAgentDebugState(agent, DebuggerState.SUSPENDED);
		}
	}

	public void suspend(final EnvironmentPort environment) {
		if (getEnvironmentState(environment) && getEnvironmentDebugState(environment) != DebuggerState.SUSPENDED) {
			write(new DebugCommand(Command.ENV_PAUSE, environment));
			setEnvironmentDebugState(environment, DebuggerState.SUSPENDED);
		}
	}

	public void step(final AgentId agent, final StepMode mode) {
		write(new DebugCommand(Command.STEP, agent, mode.name()));
	}

	@Override
	public void evaluate(final String expression, final IVariableAdder variableAdder) {
		evaluate(this.agent, expression, variableAdder);
	}

	public void evaluate(final AgentId agent, final String expression, final IVariableAdder variableAdder) {
		final EvalReceiver receiver = this.reader.getEvalReceiver(expression, agent);
		variableAdder.addVariable("", "", receiver.getResult());
	}

	public String evaluate(final AgentId agent, final String expression) {
		final EvalReceiver receiver = this.reader.getEvalReceiver(expression, agent);
		return receiver.getResult();
	}

	public String getHistoryState(final AgentId agent) throws Exception {
		final StateReceiver receiver = this.reader.getStateReceiver(agent);
		return receiver.getResult();
	}

	public String explain(final AgentId agent, final Explanation explanation, final String content) throws Exception {
		final ExplanationReceiver receiver = this.reader.getExplanationReceiver(agent, explanation, content);
		return receiver.getResult();
	}

	@Override
	public void collectVariables(final int contextId, final IVariableAdder variableAdder) {
	}

	@Override
	public GoalLineBreakpoint createBreakpoint(final String filename, final int lineno) {
		try {
			final IPath ipath = Path.fromOSString(new File(filename).getCanonicalPath());
			final IFile ifile = ResourcesPlugin.getWorkspace().getRoot().getFileForLocation(ipath);
			final GoalLineBreakpoint breakpoint = new GoalLineBreakpoint(ifile, lineno);
			updateBreakpoints();
			return breakpoint;
		} catch (final Exception e) {
			err(e);
			return null;
		}
	}

	@Override
	public void removeBreakpoint(final GoalLineBreakpoint breakPoint) {
		try {
			breakPoint.delete();
		} catch (final CoreException e) {
			err(e);
		}
		updateBreakpoints();
	}

	public void updateBreakpoints() {
		write(new DebugCommand(Command.BREAKS, this.agent, getAllBreakpoints()));
	}

	private String getAllBreakpoints() {
		GoalBreakpointManager.clearManagers();
		for (final GoalLineBreakpoint breakpoint : GoalLineBreakpoint.getAll()) {
			try {
				if (breakpoint.isRegistered() && breakpoint.isEnabled()) {
					final File file = breakpoint.getMarker().getResource().getLocation().toFile();
					final BreakPoint.Type type = (breakpoint instanceof GoalConditionalBreakpoint)
							? BreakPoint.Type.CONDITIONAL
							: BreakPoint.Type.ALWAYS;
					GoalBreakpointManager.getOrCreateGoalBreakpointManager(file)
							.addBreakpoint(breakpoint.getLineNumber(), type);
				}
			} catch (final Exception e) {
				err(e);
			}
		}
		return GoalBreakpointManager.saveAll();
	}

	public void historyBack(final AgentId agent) {
		write(new DebugCommand(Command.HISTORY_BACK, agent));
	}

	public void historyForward(final AgentId agent) {
		write(new DebugCommand(Command.HISTORY_FORWARD, agent));
	}

	public void historyStepTo(final AgentId agent, final int index) {
		Metrics.event(Event.HISTORY_STEP);
		write(new DebugCommand(Command.HISTORY_STEP, agent, Integer.toString(index)));
	}

	public GoalAgentConsole createAgentConsole(final AgentId agent) {
		final GoalAgentConsole console = new GoalAgentConsole(agent);
		console.initialize(this);
		return console;
	}

	private class StreamWriter {
		private final BufferedWriter output;

		protected StreamWriter(final OutputStream os) {
			this.output = new BufferedWriter(new OutputStreamWriter(os));
		}

		public void write(final DebugCommand c) throws Exception {
			final String string = c.toString();
			write(string);
			// System.out.println("SENT: " + string);
			if (LoggingPreferences.getEclipseDebug()) {
				out("< " + string + "\n");
			}
		}

		public void write(final String s) throws Exception {
			this.output.write(s);
			this.output.newLine();
			this.output.flush();
		}

		public void close() {
			try {
				this.output.close();
			} catch (final Exception ignore) {
			}
		}
	}
}
