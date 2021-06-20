package goal.core.executors.stack;

import java.util.Deque;
import java.util.LinkedList;

import events.Channel;
import events.ExecutionEventGeneratorInterface;
import goal.core.runtime.service.agent.Result;
import goal.core.runtime.service.agent.Result.RunStatus;
import goal.core.runtime.service.agent.RunState;
import goal.tools.errorhandling.exceptions.GOALActionFailedException;
import krTools.language.Substitution;
import languageTools.program.actionspec.UserSpecAction;
import languageTools.program.agent.actions.Action;
import languageTools.program.agent.actions.ActionCombo;
import languageTools.program.agent.msc.MentalStateCondition;

/**
 * Executor for an {@link ActionCombo}. Keeps its own stack of
 * {@link ActionStackExecutor}s as determined by {@link #pushed()}. Not all
 * possible actioncombo instantiations that result from the initialization will
 * be pushed to the {@link CallStack} (and thus executed), for example when an
 * exit-module action is called or an action has failed. If there is an event
 * module, it will be 'automatically' put on the stack by this executor when any
 * {@link UserSpecAction} has been executed.
 */
public class ActionComboStackExecutor extends StackExecutor {
	/**
	 * The combo action to be executed.
	 */
	private final ActionCombo actions;
	/**
	 * Substitution to be used for instantiating action parameters.
	 */
	private final Substitution substitution;
	/**
	 * A goal that is focused on (if any).
	 */
	private MentalStateCondition focus;
	/**
	 * A list of executors for the actions in the combo action as initially
	 * determined by {@link #pushed()}
	 */
	private Deque<ActionStackExecutor> executors;
	/**
	 * The last result of an execute-call
	 */
	private Result result;
	/**
	 * A possible exception (instead of a result)
	 */
	private GOALActionFailedException failure;

	/**
	 * Create an executor for a list of one or more actions.
	 *
	 * @param parent
	 *            The {@link CallStack} that we are working in.
	 * @param runstate
	 *            The {@link RunState} (i.e. agent) that we are working for.
	 * @param actioncombo
	 *            The {@link ActionCombo} that is to be executed.
	 * @param substitution
	 *            The {@link Substitution} that is to be used for instantiating
	 *            parameters of the actions part of the combo action.
	 */
	public ActionComboStackExecutor(CallStack parent, RunState runstate, ActionCombo actions,
			Substitution substitution) {
		super(parent, runstate);
		this.actions = actions;
		this.substitution = substitution;
	}

	// For learner...
	public ActionCombo getAction() {
		return this.actions;
	}

	// For learner...
	public Substitution getParameters() {
		return this.substitution;
	}

	/**
	 * Passes a focus goal on to all modules that need to be executed as part of
	 * this executor. It is left to the module to actually do something with the
	 * focus goal.
	 *
	 * @param focus
	 *            The goal that is focused on.
	 */
	public void setFocus(MentalStateCondition focus) {
		this.focus = focus;
	}

	@Override
	public void popped() {
		if (this.failure != null) {
			return;
		}
		ExecutionEventGeneratorInterface generator = this.runstate.getEventGenerator();
		if (this.executors == null) {
			generator.event(Channel.ACTIONCOMBO_START, this.actions, this.actions.getSourceInfo(),
					"performing actioncombo '%s'.", this.actions);
			this.result = Result.START;
			this.executors = new LinkedList<>();
			for (Action<?> action : this.actions) {
				ActionStackExecutor executor = (ActionStackExecutor) getExecutor(action, this.substitution);
				executor.setFocus(this.focus);
				this.executors.add(executor);
			}
		}

		try {
			// Check if we have just finished executing an action because
			// we might need to stop executing any other actions right now.
			ActionStackExecutor previous = (getPrevious() instanceof ActionStackExecutor)
					? (ActionStackExecutor) getPrevious() : null;
			if (previous != null && !previous.isGenerated()) {
				this.result = this.result.merge(previous.getResult());
				if (!previous.isModuleAction()) {
					// If module needs to be terminated then stop executing the
					// combo. Stop executing the combo if the last action we
					// tried to perform failed and that actions was not a module
					// call too.
					if (previous.getResult().getStatus() != RunStatus.RUNNING
							|| !previous.getResult().justPerformedAction()) {
						this.executors.clear();
					}
				}
			}
			// Put the combo itself back on the stack,
			// and add the next action to execute to it.
			if (!this.executors.isEmpty()) {
				select(this);
				select(this.executors.remove());
			} else {
				generator.event(Channel.ACTIONCOMBO_END, this.actions, this.actions.getSourceInfo(),
						"completed actioncombo '%s'.", this.actions);
			}
		} catch (GOALActionFailedException e) {
			this.failure = e;
		}
	}

	@Override
	public Result getResult() throws GOALActionFailedException {
		if (this.failure == null) {
			return this.result;
		} else {
			throw this.failure;
		}
	}

	@Override
	public String toString() {
		return "ActionComboStackExecutor for " + this.actions + " with " + this.substitution;
	}
}
