/**
 * The GOAL Runtime Environment. Copyright (C) 2015 Koen Hindriks.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package goal.tools.eclipse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

import eis.iilang.EnvironmentState;
import events.ExecutionEventGeneratorInterface;
import goal.core.agent.Agent;
import goal.core.runtime.RuntimeManager;
import goal.core.runtime.service.agent.RunState;
import goal.core.runtime.service.environmentport.EnvironmentPort;
import goal.preferences.LoggingPreferences;
import goal.tools.BreakpointManager;
import goal.tools.IDEGOALInterpreter;
import goal.tools.debugger.IDEDebugger;
import goal.tools.eclipse.DebugCommand.Command;
import goal.tools.eclipse.EclipseEventObserver.EclipseEventListener;
import goal.tools.history.EventStorage;
import goal.tools.history.StorageEventObserver;
import goal.tools.history.events.AbstractEvent;
import goal.tools.history.explanation.DebuggingIsExplaining;
import goal.tools.history.explanation.reasons.Reason;
import languageTools.program.agent.actions.Action;
import languageTools.program.agent.actions.ActionCombo;
import languageTools.program.mas.AgentDefinition;

public class InputReaderWriter extends Thread {
	private final BufferedReader input;
	private final BufferedWriter output;
	private final RuntimeManager<IDEDebugger, IDEGOALInterpreter> runtime;
	private final BreakpointManager mngr;
	private final EclipseEventObserver observer;

	protected InputReaderWriter(final InputStream is, final OutputStream os,
			final RuntimeManager<IDEDebugger, IDEGOALInterpreter> runtime, final BreakpointManager mngr,
			final EclipseEventObserver observer) {
		super("Eclipse reader-writer");
		this.input = new BufferedReader(new InputStreamReader(is));
		this.output = new BufferedWriter(new OutputStreamWriter(os));
		this.runtime = runtime;
		this.mngr = mngr;
		observer.setWriter(this);
		this.observer = observer;
	}

	public void write(final DebugCommand c) {
		write(c.toString());
	}

	public void write(final Exception e) {
		String exc = (e.getMessage() == null) ? e.toString() : e.getMessage();
		StringWriter stringWriter = new StringWriter();
		PrintWriter writer = new PrintWriter(stringWriter);
		e.printStackTrace(writer);
		writer.flush();
		writer.close();
		exc += ": " + stringWriter.getBuffer().toString();
		write(exc.replace('\n', ' '));
	}

	public synchronized void write(final String s) {
		try {
			this.output.write(s);
			this.output.newLine();
			this.output.flush();
		} catch (final Exception e) {
			logFatal(e);
		}
	}

	public static void logFatal(final Exception e) {
		if (LoggingPreferences.getLogToFile()) {
			try {
				final File f = new File(LoggingPreferences.getLogDirectory() + File.separator + "exceptions.log");
				if (!f.exists()) {
					f.createNewFile();
				}
				try (final PrintWriter pw = new PrintWriter(new FileWriter(f, true))) {
					e.printStackTrace(pw);
				}
			} catch (final Exception ex) {
				ex.printStackTrace();
			}
		} else {
			e.printStackTrace();
		}
	}

	@Override
	public void run() {
		while (this.runtime != null) {
			try {
				String line = this.input.readLine();
				if (line != null) {
					final DebugCommand read = DebugCommand.fromString(line);
					final boolean handled = (read == null) ? false : processCommand(read);
					if (!handled) {
						throw new Exception("unhandled command: '" + read + "'.");
					}
				}
			} catch (final Exception e) {
				write(e);
			}
		}
		try {
			this.input.close();
			this.output.close();
		} catch (final Exception ignore) {
		}
	}

	private boolean processCommand(final DebugCommand command) {
		for (final Agent<IDEGOALInterpreter> agent : this.runtime.getAgents()) {
			if (agent.getId().equals(command.getAgent())) {
				this.observer.getObserver(agent).processCommand(command);
				break;
			}
		}
		switch (command.getCommand()) {
		case PAUSE:
			for (final Agent<IDEGOALInterpreter> agent : this.runtime.getAgents()) {
				if (agent.getId().equals(command.getAgent())) {
					try {
						final IDEGOALInterpreter controller = agent.getController();
						final int cycle = controller.getRunState().getRoundCounter();
						if (cycle > 0 && controller.isRunning()) {
							controller.getDebugger().finestep();
							write(new DebugCommand(Command.LOG, agent.getId(),
									"User(event) paused me in cycle " + cycle + "."));
						}
						return true;
					} catch (final Exception e) {
						write(e);
						return true;
					}
				}
			}
			return false;
		case ENV_PAUSE:
			// FIXME: first pause all running agents...
			// for (final Agent<IDEGOALInterpreter> agent : this.runtime.getAgents()) {
			// try {
			// final IDEGOALInterpreter controller = agent.getController();
			// final int cycle = controller.getRunState().getRoundCounter();
			// if (cycle > 0 && controller.isRunning()) {
			// controller.getDebugger().finestep();
			// write(new DebugCommand(Command.LOG, agent.getId(),
			// "Environment paused me in cycle " + cycle + "."));
			// }
			// } catch (final Exception e) {
			// write(e);
			// }
			// }
			// ... and then pause the environment
			EnvironmentPort env1 = this.runtime.getEnvironmentPort();
			if (env1 != null && env1.getEnvironmentName().equals(command.getEnvironment().getEnvironmentName())) {
				try {
					env1.pause();
					return true;
				} catch (final Exception e) {
					write(e);
				}
			}
			return false;
		case RUN:
			// FIXME: first start any paused environment...
			EnvironmentPort env2 = this.runtime.getEnvironmentPort();
			if (env2 != null && env2.getEnvironmentState() != EnvironmentState.RUNNING) {
				try {
					env2.start();
				} catch (final Exception e) {
					write(e);
				}
			}
			for (final Agent<IDEGOALInterpreter> agent : this.runtime.getAgents()) {
				if (agent.getId().equals(command.getAgent())) {
					try {
						final IDEGOALInterpreter controller = agent.getController();
						controller.getDebugger().run();
						if (controller.isRunning()) {
							write(new DebugCommand(Command.LOG, agent.getId(), "User started me."));
						} else if (!LoggingPreferences.getEnableHistory()) {
							agent.reset();
							write(new DebugCommand(Command.LOG, agent.getId(), "User restarted me."));
						}
						return true;
					} catch (final Exception e) {
						write(e);
						return true;
					}
				}
			}
			return false;
		case ENV_RUN:
			EnvironmentPort env3 = this.runtime.getEnvironmentPort();
			if (env3 != null && env3.getEnvironmentName().equals(command.getEnvironment().getEnvironmentName())) {
				try {
					env3.start();
					if (!env3.getEnvironmentState().equals(EnvironmentState.RUNNING)) {
						env3.reset();
					}
					return true;
				} catch (final Exception e) {
					write(e);
				}
			}
			return false;
		case STEP:
			// FIXME: first start any paused environment...
			EnvironmentPort env4 = this.runtime.getEnvironmentPort();
			if (env4 != null && env4.getEnvironmentState() != EnvironmentState.RUNNING) {
				try {
					env4.start();
				} catch (final Exception e) {
					write(e);
				}
			}
			for (final Agent<IDEGOALInterpreter> agent : this.runtime.getAgents()) {
				if (agent.getId().equals(command.getAgent())) {
					try {
						final IDEGOALInterpreter controller = agent.getController();
						if (controller.isRunning()) {
							controller.getDebugger().step();
						} else {
							this.observer.getObserver(agent).suspendAtSource();
						}
						return true;
					} catch (final Exception e) {
						write(e);
						return true;
					}
				}
			}
			return false;
		case EVAL:
			String result = "";
			if (command.getAgent() == null) { // watch expression
				for (final Agent<IDEGOALInterpreter> agent : this.runtime.getAgents()) {
					try {
						final QueryTool query = new QueryTool(agent);
						final String queryresult = query.doquery(command.getData()).replace('\n', ' ');
						result += agent.getId() + ": " + queryresult + "\n";
					} catch (final Exception e) {
						if (LoggingPreferences.getEclipseDebug()) {
							write(e);
						}
					}
				}
				write(new DebugCommand(command.getCommand(), result));
				return true;
			} else { // interactive console
				for (final Agent<IDEGOALInterpreter> agent : this.runtime.getAgents()) {
					if (agent.getId().equals(command.getAgent())) {
						try {
							final QueryTool query = new QueryTool(agent);
							try {
								result = query.doaction(command.getData());
							} catch (final Exception ignore) {
								result = query.doquery(command.getData());
							}
							result = result.replace('\n', ' ');
						} catch (final Exception e) {
							if (LoggingPreferences.getEclipseDebug()) {
								write(e);
							} else {
								result += e.getMessage().replace('\n', ' ');
							}
						}
					}
				}
				write(new DebugCommand(command.getCommand(), command.getAgent(), result));
				return true; // always write result, as the receiver might hang
			}
		case BREAKS:
			GoalBreakpointManager.loadAll(command.getData());
			DebugTool.setFileBreaks(this.mngr);
			for (final Agent<IDEGOALInterpreter> agent : this.runtime.getAgents()) {
				try {
					agent.getController().getDebugger().setBreakpoints(this.mngr.getBreakpoints());
				} catch (final Exception e) {
					write(e);
				}
			}
			return true;
		case STOP:
			try {
				this.runtime.shutDown(true);
			} catch (final Exception ignore) {
			} finally {
				System.exit(0);
			}
			return true;
		case HISTORY_STATE:
			String state = "";
			for (final Agent<IDEGOALInterpreter> agent : this.runtime.getAgents()) {
				if (agent.getId().equals(command.getAgent())) {
					try {
						final EventStorage history = StorageEventObserver.getHistory(command.getAgent());
						if (history != null) {
							state = history.getDataFile().getPath();
						}
					} catch (final Exception ignore) {
					}
				}
			}
			write(new DebugCommand(command.getCommand(), command.getAgent(), state));
			return true; // always write result, as the receiver might hang
		case HISTORY_BACK:
		case HISTORY_FORWARD:
		case HISTORY_STEP:
			for (final Agent<IDEGOALInterpreter> agent : this.runtime.getAgents()) {
				if (agent.getId().equals(command.getAgent())) {
					try {
						final EventStorage history = StorageEventObserver.getHistory(agent.getId());
						if (history == null) {
							throw new Exception("no history found for agent '" + agent.getId() + "'.");
						} else {
							final RunState runState = agent.getController().getRunState();
							final ExecutionEventGeneratorInterface generator = runState.getEventGenerator();
							final EclipseDebugObserver observer = this.observer.getObserver(agent);
							generator.clearListeners();
							generator.addListener(new EclipseEventListener(observer));

							final int from = history.getIndex();
							int to;
							if (command.getCommand() == Command.HISTORY_FORWARD) {
								to = from + 1;
							} else if (command.getCommand() == Command.HISTORY_BACK) {
								to = from - 1;
							} else {
								to = Integer.parseInt(command.getData());
							}
							if (to < 0) {
								to = 0;
							} else if (to >= history.getMax()) {
								to = history.getMax() - 1;
							}
							if (to > from) {
								for (int i = from; i < to; ++i) {
									history.oneStepForward(runState);
								}
							} else {
								for (int i = from; i > to; --i) {
									history.oneStepBack(runState);
								}
							}

							final AbstractEvent current = history.getCurrent();
							final String name = "State " + to + ": " + current.getDescription(runState);
							observer.suspendAtSource(name, current.getSource(runState.getMap()));
						}
						return true;
					} catch (final Exception e) {
						write(e);
						return true;
					}
				}
			}
			return false;
		case WHY_ACTION:
		case WHY_NOT_ACTION:
			String explanation = "";
			for (final Agent<IDEGOALInterpreter> agent : this.runtime.getAgents()) {
				if (agent.getId().equals(command.getAgent())) {
					try {
						final QueryTool tool = new QueryTool(agent);
						final ActionCombo actions = tool.parseAction(command.getData());
						if (actions.size() == 1) {
							final Action<?> action = actions.iterator().next();
							final EventStorage history = StorageEventObserver.getHistory(command.getAgent());
							final AgentDefinition def = agent.getController().getProgram();
							final DebuggingIsExplaining explaining = new DebuggingIsExplaining(history, def.getMap());
							final List<Reason> reasons = (command.getCommand() == Command.WHY_ACTION)
									? explaining.whyAction(action, def.getKRInterface())
									: explaining.whyNotAction(action, def.getKRInterface());
							for (final Reason reason : reasons) {
								explanation += "\n" + reason + "\n";
							}
						} else {
							throw new RuntimeException("input is not a valid (single) action");
						}
					} catch (final Exception ignore) {
						explanation = ignore.getMessage();
					}
				}
			}
			write(new DebugCommand(command.getCommand(), command.getAgent(), explanation));
			return true; // always write result, as the receiver might hang
		default:
			return false;
		}
	}
}