package goal.tools.history;

import events.Channel;
import goal.core.agent.Agent;
import goal.core.agent.GOALInterpreter;
import goal.core.runtime.service.agent.RunState;
import goal.tools.debugger.DebugEvent;
import goal.tools.debugger.DebugObserver;
import goal.tools.debugger.ObservableDebugger;
import goal.tools.history.events.ActionEvent;
import goal.tools.history.events.CallEvent;
import goal.tools.history.events.EnterEvent;
import goal.tools.history.events.InspectionEvent;
import goal.tools.history.events.LeaveEvent;
import goal.tools.history.events.ModificationAction;
import goal.tools.history.events.ModificationEvent;
import krTools.language.Substitution;
import krTools.parser.SourceInfo;
import languageTools.program.agent.Module;
import languageTools.program.agent.actions.Action;
import languageTools.program.agent.actions.NonMentalAction;
import languageTools.program.agent.msc.MentalStateCondition;
import languageTools.program.agent.selector.Selector.SelectorType;
import mentalState.BASETYPE;
import mentalState.MentalStateWithEvents;

/**
 * Observer to fill the {@link EventStorage}.
 */
public class EventStorageObserver implements DebugObserver {
	private final EventStorage storage;
	private final GOALInterpreter<?> controller;
	private SourceInfo source;

	public EventStorageObserver(final EventStorage storage, final Agent<GOALInterpreter<?>> agent) {
		this.storage = storage;
		this.controller = agent.getController();
		this.source = this.controller.getProgram().getSourceInfo();
	}

	@Override
	public String getObserverName() {
		return getClass().getSimpleName();
	}

	public void subscribe() {
		if (this.controller.getDebugger() instanceof ObservableDebugger) {
			final ObservableDebugger debugger = (ObservableDebugger) this.controller.getDebugger();
			for (final Channel channel : Channel.values()) {
				// Listen to all channels to update the agent's source position
				debugger.subscribe(this, channel);
			}
		}
	}

	@Override
	public boolean notifyBreakpointHit(final DebugEvent event) {
		final RunState runState = this.controller.getRunState();
		if (event.getChannel().getLevel() > 0 && event.getChannel().getLevel() < Integer.MAX_VALUE
				&& event.getAssociatedSource() != null) {
			this.source = event.getAssociatedSource();
		}
		switch (event.getChannel()) {
		case BB_UPDATES:
		case GB_UPDATES:
		case GOAL_ACHIEVED:
		case PERCEPTS_CONDITIONAL_VIEW:
		case MAILS_CONDITIONAL_VIEW:
			final mentalState.Result result = (mentalState.Result) event.getAssociatedObject();
			final BASETYPE type = getType(event);
			String selector = result.getFocus();
			if (type == BASETYPE.GOALBASE) {
				final MentalStateWithEvents mentalState = runState.getMentalState();
				if (mentalState.isFocussedOn(selector)) {
					selector = SelectorType.THIS.name();
				} else {
					selector = SelectorType.SELF.name();
				}
			}
			final ModificationAction modification = new ModificationAction(runState, type, selector, result.getAdded(),
					result.getRemoved(), this.source);
			this.storage.write(new ModificationEvent(modification));
			break;
		case MODULE_ENTRY:
			final Module module1 = (Module) event.getAssociatedObject();
			this.storage.write(new EnterEvent(runState, module1));
			break;
		case MODULE_EXIT:
			final Module module2 = (Module) event.getAssociatedObject();
			this.storage.write(new LeaveEvent(runState, module2, null));
			break;
		case RULE_CONDITION_EVALUATION:
		case ACTION_PRECOND_EVALUATION:
			final MentalStateCondition condition = (MentalStateCondition) event.getAssociatedObject();
			this.storage.write(new InspectionEvent(runState, condition));
			break;
		case CALL_ACTION_OR_MODULE:
			final Action<?> called = (Action<?>) event.getRawArguments()[0];
			final Substitution subst = (Substitution) event.getRawArguments()[1];
			if (called instanceof NonMentalAction) {
				this.storage.write(new CallEvent(runState, called, subst));
			}
			break;
		case ACTION_EXECUTED_USERSPEC:
		case ACTION_EXECUTED_BUILTIN:
			final Action<?> executed = (Action<?>) event.getAssociatedObject();
			if (executed instanceof NonMentalAction) {
				this.storage.write(new ActionEvent(runState, executed));
			}
			break;
		default:
			break;
		}
		return true;
	}

	private static BASETYPE getType(final DebugEvent event) {
		switch (event.getChannel()) {
		case BB_UPDATES:
			return BASETYPE.BELIEFBASE;
		case GB_UPDATES:
		case GOAL_ACHIEVED:
			return BASETYPE.GOALBASE;
		case PERCEPTS_CONDITIONAL_VIEW:
			return BASETYPE.PERCEPTBASE;
		case MAILS_CONDITIONAL_VIEW:
			return BASETYPE.MESSAGEBASE;
		default:
			return null;
		}
	}
}
