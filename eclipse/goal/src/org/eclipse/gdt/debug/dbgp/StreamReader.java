
package org.eclipse.gdt.debug.dbgp;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.dltk.dbgp.debugger.debugger.BreakPointLocation;
import org.eclipse.dltk.dbgp.debugger.debugger.DebuggerState;
import org.eclipse.dltk.debug.core.model.IScriptThread;
import org.eclipse.dltk.launching.InterpreterConfig;
import org.eclipse.gdt.debug.history.ExplanationDialog.Explanation;
import org.eclipse.gdt.debug.ui.GoalAgentConsole;
import org.eclipse.gdt.launching.GoalDebugTarget;
import org.eclipse.gdt.launching.RunnableDebuggingEngineRunner;

import goal.core.runtime.service.environmentport.EnvironmentPort;
import goal.preferences.CorePreferences;
import goal.preferences.LoggingPreferences;
import goal.tools.eclipse.DebugCommand;
import goal.tools.eclipse.DebugCommand.Command;
import languageTools.program.agent.AgentId;

public class StreamReader extends Thread {
	private final Map<AgentId, DebuggerState> agentdebugstates;
	private final Map<AgentId, AgentState> agentstates;
	private final Map<EnvironmentPort, DebuggerState> envdebugstates;
	private final Map<EnvironmentPort, Boolean> envstates;
	private final Map<AgentId, GoalAgentConsole> consoles;
	private final LocalDebugger parent;
	private final BufferedReader input;
	private volatile EvalReceiver eval;
	private volatile StateReceiver state;
	private volatile ExplanationReceiver expl;
	private volatile boolean run;

	protected StreamReader(final LocalDebugger parent, final InputStream in) {
		this.parent = parent;
		this.input = new BufferedReader(new InputStreamReader(in));
		this.agentdebugstates = new ConcurrentHashMap<>();
		this.agentstates = new ConcurrentHashMap<>();
		this.envdebugstates = new ConcurrentHashMap<>();
		this.envstates = new ConcurrentHashMap<>();
		this.consoles = new ConcurrentHashMap<>();
		if (LoggingPreferences.getEclipseActionHistory()) {
			this.consoles.put(new AgentId(""), parent.createAgentConsole(null));
		}
		this.run = true;
	}

	public DebuggerState getEnvironmentDebugState(final EnvironmentPort environment) {
		return (environment == null) ? null : this.envdebugstates.get(environment);
	}

	public void setEnvironmentDebugState(final EnvironmentPort environment, final DebuggerState state) {
		if (environment != null) {
			this.envdebugstates.put(environment, state);
		}
	}

	public DebuggerState getAgentDebugState(final AgentId agent) {
		return (agent == null) ? null : this.agentdebugstates.get(agent);
	}

	public void setAgentDebugState(final AgentId agent, final DebuggerState state) {
		if (agent != null) {
			this.agentdebugstates.put(agent, state);
		}
	}

	public AgentState getAgentState(final AgentId agent) {
		return (agent == null) ? null : this.agentstates.get(agent);
	}

	public boolean getEnvironmentState(final EnvironmentPort environment) {
		return (environment != null && this.envstates.containsKey(environment)) ? this.envstates.get(environment)
				: false;
	}

	public GoalAgentConsole getConsole(final AgentId agent) {
		return (agent == null) ? null : this.consoles.get(agent);
	}

	public Collection<GoalAgentConsole> getConsoles() {
		return Collections.unmodifiableCollection(this.consoles.values());
	}

	public EvalReceiver getEvalReceiver(final String expression, final AgentId agent) {
		final EvalReceiver eval = new EvalReceiver(expression, agent);
		this.eval = eval;
		return eval;
	}

	public StateReceiver getStateReceiver(final AgentId agent) {
		final StateReceiver state = new StateReceiver(agent);
		this.state = state;
		return state;
	}

	public ExplanationReceiver getExplanationReceiver(final AgentId agent, final Explanation explanation,
			final String content) {
		final ExplanationReceiver expl = new ExplanationReceiver(agent, explanation, content);
		this.expl = expl;
		return expl;
	}

	@Override
	public void run() {
		while (this.run) {
			try {
				final String line = this.input.readLine();
				if (line != null) {
					final DebugCommand read = DebugCommand.fromString(line);
					final boolean handled = (read == null) ? false : processCommand(read);
					if (!handled || LoggingPreferences.getEclipseDebug()) {
						this.parent.out(line + "\n");
					}
				}
			} catch (final Exception e) {
				this.parent.err(e);
			}
		}
		try {
			this.input.close();
		} catch (final Exception ignore) {
		}
	}

	private boolean processCommand(final DebugCommand command) {
		final AgentId agent = command.getAgent();
		AgentState agentstate = null;
		if (agent != null) {
			agentstate = this.agentstates.get(agent);
			if (agentstate == null) {
				agentstate = new AgentState(agent);
				this.agentstates.put(agent, agentstate);
			}
		}
		final EnvironmentPort environment = command.getEnvironment();
		// System.out.println("REC: " + command);
		switch (command.getCommand()) {
		case LAUNCHED:
			if (this.agentdebugstates.containsKey(agent)) {
				return true;
			}
			this.agentdebugstates.put(agent, DebuggerState.STARTING);
			if (LoggingPreferences.getEclipseAgentConsoles()) {
				final GoalAgentConsole agentconsole = this.parent.createAgentConsole(agent);
				this.consoles.put(agent, agentconsole);
			}
			if (this.parent.isBound()) {
				final RunnableDebuggingEngineRunner runner = ((DbgpDebugger) this.parent.getDebugger()).getRunner();
				final InterpreterConfig subcfg = runner.getLastConfig();
				if (subcfg.hasInterpreterArg(EnvDebugger.ENVIRONMENT)) {
					subcfg.removeInterpreterArg(EnvDebugger.ENVIRONMENT);
				}
				if (!subcfg.hasInterpreterArg(ThreadDebugger.AGENT)) {
					subcfg.addInterpreterArg(ThreadDebugger.AGENT);
				}
				subcfg.addEnvVar(ThreadDebugger.AGENT, agent.toString());
				try {
					runner.startSubProcess(subcfg, runner.getLastLaunch());
					while (!this.parent.hasDebugger(agent)) {
						Thread.sleep(1); // FIXME
					}
				} catch (final Exception e) {
					this.parent.err(e);
				}
			} else {
				this.parent.setAgent(agent);
			}
			final DebuggerState initial1 = command.getData().equals("RUNNING") ? DebuggerState.RUNNING
					: DebuggerState.SUSPENDED;
			this.agentdebugstates.put(agent, initial1);
			return true;
		case ENV_CREATED:
			if (this.envdebugstates.containsKey(environment)) {
				return true;
			}
			this.envdebugstates.put(environment, DebuggerState.STARTING);
			final RunnableDebuggingEngineRunner runner = ((DbgpDebugger) this.parent.getDebugger()).getRunner();
			final InterpreterConfig subcfg = runner.getLastConfig();
			if (!subcfg.hasInterpreterArg(EnvDebugger.ENVIRONMENT)) {
				subcfg.addInterpreterArg(EnvDebugger.ENVIRONMENT);
			}
			if (subcfg.hasInterpreterArg(ThreadDebugger.AGENT)) {
				subcfg.removeInterpreterArg(ThreadDebugger.AGENT);
			}
			subcfg.addEnvVar(EnvDebugger.ENVIRONMENT, environment.getEnvironmentName());
			try {
				runner.startSubProcess(subcfg, runner.getLastLaunch());
				while (!this.parent.hasDebugger(environment)) {
					Thread.sleep(1); // FIXME
				}
			} catch (final Exception e) {
				this.parent.err(e);
			}
			final DebuggerState initial2 = command.getData().equals("RUNNING") ? DebuggerState.RUNNING
					: DebuggerState.SUSPENDED;
			this.envdebugstates.put(environment, initial2);
			return true;
		case SUSPEND:
			final Deque<BreakPointLocation> callstack = new LinkedList<>();
			for (final String pos : command.getAllData()) {
				final String[] data = pos.split("#");
				final String breakcmd = data[0];
				final URI breakfile = new File(data[1]).toURI();
				final int breakline = Integer.parseInt(data[2]);
				final int breakstart = Integer.parseInt(data[3]);
				final int breakend = Integer.parseInt(data[4]);
				callstack.add(new BreakPointLocation(breakfile.toASCIIString(), breakcmd, breakline, breakstart,
						breakline, breakend));
			}
			new Thread() {
				@Override
				public void run() {
					try {
						while (!StreamReader.this.parent.hasDebugger(agent)) {
							Thread.sleep(1); // FIXME
						}
						StreamReader.this.parent.suspendByBreakPoint(agent.toString(), callstack);
					} catch (final Exception e) {
						StreamReader.this.parent.err(e);
					}
				}
			}.start();
			return true;
		case CLEAR:
			agentstate.reset();
			return true;
		case LOG:
			final String msg = command.getData();
			final GoalAgentConsole console = this.consoles.get(agent);
			if (console == null) {
				return false;
			} else if (msg.endsWith(" +++++++") && console.getLast().endsWith(" +++++++")) {
				return true;
			} else {
				console.println(msg);
				return true;
			}
		case EXECUTED:
			final String action = command.getData();
			this.consoles.get(new AgentId("")).println(agent + " performed " + action);
			return true;
		case RUNMODE:
			final String mode = command.getData();
			agentstate.setRunMode(mode);
			if (mode.equalsIgnoreCase("killed")) {
				final Set<String> killed = new LinkedHashSet<>(1);
				killed.add("The agent has been terminated");
				agentstate.setCondition(killed);
				if (CorePreferences.getRemoveKilledAgent()) {
					final IScriptThread thread = this.parent.getThread(agent);
					final GoalDebugTarget target = (GoalDebugTarget) thread.getDebugTarget();
					target.getThreadManager().terminateThread(thread);
				}
			}
			return true;
		case MODULE_ENTRY:
			final List<String> module1 = command.getAllData();
			// final UseCase use1 = UseCase.valueOf(module1.get(0));
			final String name1 = module1.get(0);
			final String subst1 = module1.get(1);
			agentstate.setModule(name1);
			final Set<String> single1 = new LinkedHashSet<>(1);
			single1.add("Entered '" + name1 + "' with " + subst1);
			// if (use1 == UseCase.EVENT) {
			// single1.add("Processed messages and percepts");
			// }
			agentstate.setCondition(single1);
			return true;
		case MODULE_EXIT:
			final List<String> module2 = command.getAllData();
			// final UseCase use2 = UseCase.valueOf(module2.get(0));
			final String name2 = module2.get(0);
			agentstate.removeModule();
			final Set<String> single2 = new LinkedHashSet<>(1);
			single2.add("Exited '" + name2 + "'");
			agentstate.setCondition(single2);
			return true;
		case INSERTED_BEL:
			final String belief1 = command.getData();
			agentstate.addBelief(belief1);
			return true;
		case DELETED_BEL:
			final String belief2 = command.getData();
			agentstate.removeBelief(belief2);
			return true;
		case INSERTED_PERCEPT:
			final String percept1 = command.getData();
			agentstate.addPercept(percept1);
			return true;
		case DELETED_PERCEPT:
			final String percept2 = command.getData();
			agentstate.removePercept(percept2);
			return true;
		case INSERTED_MAIL:
			final String mail1 = command.getData();
			agentstate.addMail(mail1.toString());
			return true;
		case DELETED_MAIL:
			final String mail2 = command.getData();
			agentstate.removeMail(mail2.toString());
			return true;
		case ADOPTED:
			final String goal1 = command.getData();
			agentstate.addGoal(goal1);
			return true;
		case DROPPED:
			final String goal2 = command.getData();
			agentstate.removeGoal(goal2);
			return true;
		case ACHIEVED:
			final String goal3 = command.getData();
			agentstate.removeGoal(goal3);
			final Set<String> single3 = new LinkedHashSet<>(1);
			single3.add("Achieved '" + goal3 + "'");
			agentstate.setCondition(single3);
			return true;
		case FOCUS:
			final String focus1 = command.getData();
			agentstate.setFocus(focus1);
			return true;
		case DEFOCUS:
			// final String focus2 = (String)command.getData();
			agentstate.removeFocus();
			return true;
		case RULE_EVALUATION:
			final List<String> condition = command.getAllData();
			agentstate.setCondition(new LinkedHashSet<>(condition));
			return true;
		case EVAL:
			final String result1 = command.getData();
			this.eval.setResult(result1);
			return true;
		case HISTORY_STATE:
			final String result2 = command.getData();
			this.state.setResult(result2);
			return true;
		case WHY_ACTION:
		case WHY_NOT_ACTION:
			final String result3 = command.getData();
			this.expl.setResult(result3);
			return true;
		case ENV_STATE:
			final String envstate = command.getData().toUpperCase();
			switch (envstate) {
			case "PAUSED":
				new Thread() {
					@Override
					public void run() {
						try {
							IScriptThread thread;
							while ((thread = StreamReader.this.parent.getThread(environment)) == null) {
								Thread.sleep(1); // FIXME
							}
							thread.suspend();
						} catch (final Exception e) {
							StreamReader.this.parent.err(e);
						} finally {
							StreamReader.this.envstates.put(environment, true); // set initialized flag
						}
					}
				}.start();
				return true;
			case "RUNNING":
				new Thread() {
					@Override
					public void run() {
						try {
							IScriptThread thread;
							while ((thread = StreamReader.this.parent.getThread(environment)) == null) {
								Thread.sleep(1); // FIXME
							}
							thread.resume();
						} catch (final Exception e) {
							StreamReader.this.parent.err(e);
						} finally {
							StreamReader.this.envstates.put(environment, true); // set initialized flag
						}
					}
				}.start();
				return true;
			default:
				return true;
			}
		default:
			return false;
		}
	}

	public void end() {
		this.run = false;
	}

	public class EvalReceiver {
		private final AgentId agent;
		private volatile String receivedResult;

		protected EvalReceiver(final String expression, final AgentId agent) {
			this.agent = agent;
			String eval = expression.trim();
			if (eval.endsWith(".")) {
				eval = eval.substring(0, eval.length() - 1);
			}
			StreamReader.this.parent.write(new DebugCommand(Command.EVAL, agent, eval));
		}

		public void setResult(final String received) {
			synchronized (this) {
				if (this.agent == null) {
					this.receivedResult = received;
				} else {
					this.receivedResult = this.agent + ": " + received;
				}
				notifyAll();
			}
		}

		public String getResult() {
			synchronized (this) {
				while (this.receivedResult == null) {
					try {
						wait();
					} catch (final Exception ignore) {
						this.receivedResult = ignore.getMessage();
					}
				}
			}
			return this.receivedResult;
		}
	}

	public class StateReceiver {
		private volatile String receivedResult;

		protected StateReceiver(final AgentId agent) {
			StreamReader.this.parent.write(new DebugCommand(Command.HISTORY_STATE, agent));
		}

		public synchronized void setResult(final String received) {
			this.receivedResult = received;
			notifyAll();
		}

		public synchronized String getResult() throws Exception {
			while (this.receivedResult == null) {
				wait();
			}
			return this.receivedResult;
		}
	}

	public class ExplanationReceiver {
		private volatile String receivedResult;

		protected ExplanationReceiver(final AgentId agent, final Explanation explanation, final String content) {
			Command command = null;
			switch (explanation) {
			case WHY_ACTION:
				command = Command.WHY_ACTION;
				break;
			case WHY_NOT_ACTION:
				command = Command.WHY_NOT_ACTION;
				break;
			}
			StreamReader.this.parent.write(new DebugCommand(command, agent, content));
		}

		public synchronized void setResult(final String received) {
			this.receivedResult = received;
			notifyAll();
		}

		public synchronized String getResult() throws Exception {
			while (this.receivedResult == null) {
				wait();
			}
			return this.receivedResult;
		}
	}
}