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

import java.io.File;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import events.Channel;
import goal.core.agent.Agent;
import goal.preferences.CorePreferences;
import goal.preferences.DebugPreferences;
import goal.preferences.LoggingPreferences;
import goal.tools.IDEGOALInterpreter;
import goal.tools.debugger.DebugEvent;
import goal.tools.debugger.DebugObserver;
import goal.tools.debugger.IDEDebugger;
import goal.tools.debugger.SteppingDebugger.RunMode;
import goal.tools.eclipse.DebugCommand.Command;
import krTools.language.DatabaseFormula;
import krTools.language.Substitution;
import krTools.language.Var;
import krTools.parser.SourceInfo;
import languageTools.program.agent.AgentId;
import languageTools.program.agent.Module;
import languageTools.program.agent.actions.Action;
import languageTools.program.agent.actions.ModuleCallAction;
import languageTools.program.agent.actions.UserSpecCallAction;
import mentalState.error.MSTTranslationException;
import mentalState.translator.Translator;
import msFactory.InstantiationFailedException;
import msFactory.translator.TranslatorFactory;

public class EclipseDebugObserver implements DebugObserver {
	private final Agent<IDEGOALInterpreter> agent;
	private final InputReaderWriter writer;
	private final EclipseStepper stepper;
	private final Deque<StackInfo> stack;
	private SourceInfo source;

	/**
	 * Handles events from an {@link Agent} to put pre-defined output on a
	 * {@link InputReaderWriter}
	 *
	 * @param agent
	 *            The {@link Agent}.
	 * @param writer
	 *            The {@link InputReaderWriter}.
	 */
	public EclipseDebugObserver(final Agent<IDEGOALInterpreter> agent, final InputReaderWriter writer) {
		this.agent = agent;
		this.writer = writer;
		this.stepper = new EclipseStepper(agent);
		this.stack = new LinkedList<>();
		this.source = agent.getController().getProgram().getSourceInfo();
	}

	public void processCommand(final DebugCommand c) {
		this.stepper.processCommand(c);
	}

	/**
	 * Subscribe to everything we want to listen to
	 */
	public void subscribe() {
		final IDEDebugger debugger = this.agent.getController().getDebugger();
		for (final Channel channel : Channel.values()) {
			// Listen to all channels to update the agent's source position
			debugger.subscribe(this, channel);
		}
		this.writer.write(new DebugCommand(Command.LAUNCHED, this.agent.getId(), debugger.getRunMode().name()));
	}

	@Override
	public String getObserverName() {
		return getClass().getSimpleName();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean notifyBreakpointHit(final DebugEvent event) {
		if (event.getChannel().getLevel() > 0 && event.getChannel().getLevel() < Integer.MAX_VALUE
				&& event.getAssociatedSource() != null) {
			this.source = event.getAssociatedSource();
			updateStack(event);
		} else if (event.getChannel() == Channel.MODULE_EXIT || event.getChannel() == Channel.ACTION_END) {
			updateStack(event);
		}
		final AgentId agentId = this.agent.getId();
		if (DebugPreferences.getChannelState(event.getChannel()).canView()
				&& LoggingPreferences.getEclipseAgentConsoles()) {
			this.writer.write(new DebugCommand(Command.LOG, agentId, event.toString()));
		}
		switch (event.getChannel()) {
		case RUNMODE:
			final RunMode runmode = event.getRunMode();
			switch (runmode) {
			case RUNNING:
				this.writer.write(new DebugCommand(Command.RUNMODE, agentId, runmode.name()));
				break;
			case PAUSED:
				this.writer.write(new DebugCommand(Command.RUNMODE, agentId, runmode.name()));
				suspendAtSource();
				break;
			case KILLED:
				this.writer.write(new DebugCommand(Command.RUNMODE, agentId, runmode.name()));
				if (!CorePreferences.getRemoveKilledAgent()) {
					suspendAtSource();
				}
				break;
			default:
				break;
			}
			break;
		case CLEARSTATE:
			this.writer.write(new DebugCommand(Command.CLEAR, agentId));
			break;
		case BREAKPOINTS:
			this.source = event.getAssociatedSource();
			break;
		case MODULE_ENTRY:
			final List<String> mAsList1 = new ArrayList<>(2);
			final Module module1 = (Module) event.getRawArguments()[0];
			mAsList1.add(module1.toString());
			final Substitution subst1 = (Substitution) event.getRawArguments()[1];
			mAsList1.add(subst1.toString());
			this.writer.write(new DebugCommand(Command.MODULE_ENTRY, agentId, mAsList1));
			break;
		case MODULE_EXIT:
			final List<String> mAsList2 = new ArrayList<>(1);
			final Module module2 = (Module) event.getRawArguments()[0];
			mAsList2.add(module2.toString());
			this.writer.write(new DebugCommand(Command.MODULE_EXIT, agentId, mAsList2));
			break;
		case BB_UPDATES:
			final mentalState.Result beliefs = (mentalState.Result) event.getAssociatedObject();
			for (final DatabaseFormula removed : beliefs.getRemoved()) {
				this.writer.write(new DebugCommand(Command.DELETED_BEL, agentId, removed.toString()));
			}
			for (final DatabaseFormula added : beliefs.getAdded()) {
				this.writer.write(new DebugCommand(Command.INSERTED_BEL, agentId, added.toString()));
			}
			break;
		case GB_UPDATES:
			final mentalState.Result goals = (mentalState.Result) event.getAssociatedObject();
			final String focus2 = " [" + goals.getFocus() + "]";
			for (final DatabaseFormula removed : goals.getRemoved()) {
				this.writer.write(new DebugCommand(Command.DROPPED, agentId, removed + focus2));
			}
			for (final DatabaseFormula added : goals.getAdded()) {
				this.writer.write(new DebugCommand(Command.ADOPTED, agentId, added + focus2));
			}
			break;
		case GOAL_ACHIEVED:
			final mentalState.Result achieved = (mentalState.Result) event.getAssociatedObject();
			final String focus1 = " [" + achieved.getFocus() + "]";
			for (final DatabaseFormula achieve : achieved.getRemoved()) {
				this.writer.write(new DebugCommand(Command.ACHIEVED, agentId, achieve + focus1));
			}
			break;
		case PERCEPTS_CONDITIONAL_VIEW:
			final mentalState.Result percept = (mentalState.Result) event.getAssociatedObject();
			try {
				final Translator translator = TranslatorFactory
						.getTranslator(this.agent.getController().getProgram().getKRInterface());
				for (final DatabaseFormula removed : percept.getRemoved()) {
					this.writer.write(new DebugCommand(Command.DELETED_PERCEPT, agentId,
							translator.convertPercept(removed).toProlog()));
				}
				for (final DatabaseFormula added : percept.getAdded()) {
					this.writer.write(new DebugCommand(Command.INSERTED_PERCEPT, agentId,
							translator.convertPercept(added).toProlog()));
				}
			} catch (InstantiationFailedException | MSTTranslationException e) {
				this.writer.write(e);
			}
			break;
		case MAILS_CONDITIONAL_VIEW:
			final mentalState.Result message = (mentalState.Result) event.getAssociatedObject();
			try {
				final Translator translator = TranslatorFactory
						.getTranslator(this.agent.getController().getProgram().getKRInterface());
				for (final DatabaseFormula removed : message.getRemoved()) {
					this.writer.write(new DebugCommand(Command.DELETED_MAIL, agentId,
							translator.convertMessage(removed).toString()));
				}
				for (final DatabaseFormula added : message.getAdded()) {
					this.writer.write(new DebugCommand(Command.INSERTED_MAIL, agentId,
							translator.convertMessage(added).toString()));
				}
			} catch (InstantiationFailedException | MSTTranslationException e) {
				this.writer.write(e);
			}
			break;
		case GB_CHANGES:
			final mentalState.Result base = (mentalState.Result) event.getAssociatedObject();
			if (this.agent.getController().getRunState().getMentalState().isFocussedOn(base.getFocus())) {
				this.writer.write(new DebugCommand(Command.FOCUS, agentId, base.getFocus()));
			} else {
				this.writer.write(new DebugCommand(Command.DEFOCUS, agentId, base.getFocus()));
			}
			break;
		case RULE_CONDITION_EVALUATION:
		case ACTION_PRECOND_EVALUATION:
			final List<String> rAsList = new LinkedList<>();
			if (event.getRawArguments().length > 1) {
				final Set<Substitution> substset = (Set<Substitution>) event.getRawArguments()[1];
				switch (substset.size()) {
				case 0:
					rAsList.add("[]");
					break;
				case 1:
					final Substitution rSub = substset.iterator().next();
					for (final Var var : rSub.getVariables()) {
						rAsList.add(var + "/" + rSub.get(var));
					}
					if (rAsList.isEmpty()) {
						rAsList.add("[]");
					}
					break;
				default:
					for (final Substitution sub : substset) {
						rAsList.add(sub.toString());
					}
					break;

				}
			} else {
				rAsList.add("no solutions");
			}
			this.writer.write(new DebugCommand(Command.RULE_EVALUATION, agentId, rAsList));
			break;
		case CALL_ACTION_OR_MODULE:
		case ACTION_POSTCOND_EVALUATION:
			final List<String> cAsList = new LinkedList<>();
			final Substitution cSub = (Substitution) event.getRawArguments()[event.getRawArguments().length - 1];
			for (final Var var : cSub.getVariables()) {
				cAsList.add(var + "/" + cSub.get(var));
			}
			if (cAsList.isEmpty()) {
				cAsList.add("[]");
			}
			this.writer.write(new DebugCommand(Command.RULE_EVALUATION, agentId, cAsList));
			break;
		case ACTION_EXECUTED_USERSPEC:
			if (LoggingPreferences.getEclipseActionHistory()) {
				final Action<?> executed = (Action<?>) event.getRawArguments()[0];
				if (!(executed instanceof ModuleCallAction)) {
					String actionlog = executed.toString();
					if (LoggingPreferences.getIncludeStackInLogs()) {
						final List<String> actionstack = new LinkedList<>();
						for (StackInfo call : this.stack.toArray(new StackInfo[this.stack.size()])) {
							final File source = new File(call.getSource().getSource());
							actionstack.add(source.getName() + ":" + call.getSource().getLineNumber());
						}
						actionlog += " " + actionstack;
					}
					this.writer.write(new DebugCommand(Command.EXECUTED, agentId, actionlog));
				}
			}
			break;
		default:
			break;
		}
		// Let the EclipseStepper process the event
		return this.stepper.processEvent(event);
	}

	private void updateStack(final DebugEvent event) {
		switch (event.getChannel()) {
		case MODULE_ENTRY:
			final String module = "Module " + event.getAssociatedObject();
			this.stack.push(new StackInfo(module, event.getAssociatedSource()));
			break;
		case MODULE_EXIT:
			final Module main = this.agent.getController().getProgram().getMainModule();
			if (main == null || !event.getAssociatedObject().toString().equals(main.toString())) {
				this.stack.pop();
			}
			break;
		case ACTION_PRECOND_EVALUATION:
			if (event.getRawArguments()[0] instanceof UserSpecCallAction) {
				final String action = "Action " + event.getRawArguments()[0];
				this.stack.push(new StackInfo(action, event.getAssociatedSource()));
			}
			break;
		case ACTION_END:
			if (event.getRawArguments()[0] instanceof UserSpecCallAction) {
				this.stack.pop();
			}
			break;
		default:
			if (!this.stack.isEmpty()) {
				final StackInfo toUpdate = this.stack.pop();
				this.stack.push(new StackInfo(toUpdate.getName(), this.source));
			}
			break;
		}
	}

	/**
	 * Send a message to the stream to suspend the agent at its last known source
	 * position (code that has been run)
	 */
	public void suspendAtSource() {
		final List<String> params = new LinkedList<>();
		for (StackInfo call : this.stack.toArray(new StackInfo[this.stack.size()])) {
			final SourceInfo source = call.getSource();
			final int end = source.getCharacterPosition() + source.getStopIndex() - source.getStartIndex();
			final String param = call.getName() + "#" + source.getSource() + "#" + source.getLineNumber() + "#"
					+ source.getCharacterPosition() + "#" + end;
			params.add(param);
		}
		this.writer.write(new DebugCommand(Command.SUSPEND, this.agent.getId(), params));
	}

	/**
	 * Send a message to the stream to suspend the agent at the given source
	 * position
	 */
	public void suspendAtSource(String name, SourceInfo source) {
		final List<String> params = new ArrayList<>(1);
		final int end = source.getCharacterPosition() + source.getStopIndex() - source.getStartIndex();
		final String param = name + "#" + source.getSource() + "#" + source.getLineNumber() + "#"
				+ source.getCharacterPosition() + "#" + end;
		params.add(param);
		this.writer.write(new DebugCommand(Command.SUSPEND, this.agent.getId(), params));
	}
}