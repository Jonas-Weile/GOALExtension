package goal.core.executors.stack;

import java.util.Iterator;

import events.Channel;
import events.ExecutionEventGeneratorInterface;
import goal.core.executors.actions.ActionExecutor;
import goal.core.executors.actions.ModuleCallActionExecutor;
import goal.core.executors.actions.UserSpecActionExecutor;
import goal.core.executors.modules.ModuleExecutor;
import goal.core.runtime.service.agent.Result;
import goal.core.runtime.service.agent.RunState;
import goal.tools.errorhandling.exceptions.GOALActionFailedException;
import krTools.KRInterface;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;
import languageTools.program.agent.actions.Action;
import languageTools.program.agent.actions.ModuleCallAction;
import languageTools.program.agent.msc.MentalStateCondition;
import mentalState.MSCResult;

/**
 * Executor for an {@link Action}. Being 'in the end of the execution chain',
 * this executor does not keep a stack of any other executors. However, when the
 * action is a {@link ModuleCallAction}, a {@link ModuleStackExecutor} can still
 * be pushed on the callstack by this executor. Otherwise, the action will
 * simply be performed (in the {@link #popped()} function) by using a
 * corresponding {@link ActionExecutor}.
 */
public class ActionStackExecutor extends StackExecutor {
	/**
	 * The action to be executed.
	 */
	private final Action<?> action;
	/**
	 * Substitution to be used for instantiating parameters of action.
	 */
	private final Substitution substitution;
	/**
	 * Indicates if the action was generated (i.e. not originating from the source
	 * code, for example a call to the event module)
	 */
	private final boolean generated;
	/**
	 * A goal that is focused on (if any).
	 */
	private MentalStateCondition focus;
	/**
	 * The last result of an execute-call. Protected so that we can test this
	 */
	protected Result result;
	/**
	 * A possible exception (instead of a result)
	 */
	private GOALActionFailedException failure;

	/**
	 * Create an executor for an actions.
	 *
	 * @param parent       The {@link CallStack} that we are working in.
	 * @param runstate     The {@link RunState} (i.e. agent) that we are working
	 *                     for.
	 * @param action       The {@link Action} that is to be executed. This is the
	 *                     action on the caller's side, with the variables also
	 *                     exactly as on the caller's side. These still have to be
	 *                     fitted into the variables as in the actual actionspec.
	 * @param substitution The {@link Substitution} that holds on the caller's side.
	 *                     These still have to be applied to the action.
	 * @param generated    True if the action was generated (i.e. not originating
	 *                     from the source code, for example a call to the event
	 *                     module)
	 */
	public ActionStackExecutor(CallStack parent, RunState runstate, Action<?> action, Substitution substitution,
			boolean generated) {
		super(parent, runstate);
		this.action = action;
		this.substitution = substitution;
		this.generated = generated;
	}

	/**
	 * @param focus The goal to focus on (if any, can be null)
	 */
	public void setFocus(MentalStateCondition focus) {
		this.focus = focus;
	}

	/**
	 * @return True iff the (to be) executed action is an instance of
	 *         {@link ModuleCallAction}
	 */
	public boolean isModuleAction() {
		return (this.action instanceof ModuleCallAction);
	}

	/**
	 * @return True iff the (to be) executed action was generated (i.e. not
	 *         originating from the source code, for example a call to the event
	 *         module)
	 */
	public boolean isGenerated() {
		return this.generated;
	}

	@Override
	public void popped() {
		if (this.failure != null) {
			return;
		}

		ExecutionEventGeneratorInterface generator = this.runstate.getEventGenerator();
		boolean anonymous = (this.action instanceof ModuleCallAction)
				&& ((ModuleCallAction) this.action).getTarget().isAnonymous();

		try {
			// Check if we have just finished executing a module call, as it
			// is the only type of action that actually uses the stack to
			// execute. If it was, we use the result of that module as the
			// result of this action, and do not execute anything else anymore.
			ModuleExecutor previous = (getPrevious() instanceof ModuleExecutor) ? (ModuleExecutor) getPrevious() : null;
			if (this.result != null && previous != null) {
				// action has been completed.
				if (!anonymous && !this.generated) {
					generator.event(Channel.ACTION_EXECUTED_USERSPEC, this.action, this.action.getSourceInfo(),
							"performed '%s'.", this.action.applySubst(this.substitution));
				}
				Result prevResult = previous.getResult();
				// copy prev result, but status only if it was anonymous module
				this.result = new Result(prevResult.justPerformedAction(), prevResult.justPerformedRealAction(),
						anonymous ? prevResult.getStatus() : result.getStatus());
				generator.event(Channel.ACTION_END, this.action, this.action.getSourceInfo(), "executed action '%s'.",
						this.action);
			} else {
				// action needs still to be executed.
				generator.event(Channel.ACTION_START, this.action, this.action.getSourceInfo(), "selected action '%s'.",
						this.action);
				if (!anonymous && !this.generated) {
					generator.event(Channel.CALL_ACTION_OR_MODULE, this.action.applySubst(this.substitution),
							this.action.getSourceInfo(), "trying '%s' with %s.", this.action, this.substitution);
				}
				this.result = Result.START;
				ActionExecutor executor = ActionExecutor.getActionExecutor(this.action, this.substitution);

				// The pre/post of a user-specified action are deeper in the
				// callstack specification, but they are not separate executors,
				// so we need to raise the stack index manually in this case.
				if (executor instanceof UserSpecActionExecutor) {
					select(null);
				}

				// Evaluate the action's precondition.
				MSCResult precondition = executor.evaluatePrecondition(this.runstate);
				// If the precondition holds, get the generated action
				// executor; don't do anything otherwise (default result).
				if (precondition.holds()) {
					// If executor is a module executor, push it (and the rule
					// itself) on the stack; the module result is handled above,
					// i.e. when this rule is popped again.
					if (executor instanceof ModuleCallActionExecutor) {
						if (executor.canBeExecuted()) {
							ModuleExecutor moduleExec = (ModuleExecutor) getExecutor(
									((ModuleCallAction) this.action).getTarget(), filteredParameters());
							moduleExec.setFocus(this.focus);
							select(this);
							select(moduleExec);
						} else {
							this.failure = new GOALActionFailedException("attempt to execute '" + this.action
									+ "' with " + this.substitution + " left free variables: "
									+ this.action.applySubst(this.substitution).getFreeVar() + ".");
						}
					} else {
						// Just perform the action and use its result otherwise.
						this.result = executor.perform(this.runstate);
						this.runstate.getEventGenerator().event(Channel.ACTION_END, this.action,
								this.action.getSourceInfo(), "executed action '%s'.", this.action);
						if (executor instanceof UserSpecActionExecutor) {
							generator.event(Channel.ACTION_EXECUTED_USERSPEC, this.action, this.action.getSourceInfo(),
									"performed '%s'.", this.action.applySubst(this.substitution));
						}
					}
				} else {
					this.runstate.getEventGenerator().event(Channel.ACTION_END, this.action,
							this.action.getSourceInfo(), "precondition of action '%s' did not hold", this.action);
				}
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

	/**
	 * The formal and actual parameters of a user-defined module act as a filter on
	 * the parameter bindings that were passed on to the executor when evaluating a
	 * rule's condition. Any bindings from the given substitution for variables that
	 * do not occur in the module's parameters are removed. An anonymous module does
	 * not have any parameters and does not filter the given substitution but simply
	 * passes this on as is.
	 *
	 * @param substitution A substitution.
	 * @return A substitution where all bindings for variables that do not occur in
	 *         the module's parameters have been removed, or the given substitution
	 *         in case the module is anonymous.
	 *
	 *         TODO: move part of this to the language tools project, and have
	 *         validator compute the mgu of formal and actual parameters.
	 */
	private Substitution filteredParameters() {
		ModuleCallAction action = (ModuleCallAction) this.action;
		if (action.getTarget().isAnonymous()) {
			return this.substitution;
		} else {
			KRInterface kri = this.runstate.getKRI();
			Substitution newsubst = kri.getSubstitution(null);
			Iterator<Var> formalParameters = action.getTarget().getParameters().iterator();
			Iterator<Term> actualParameters = action.getParameters().iterator();
			while (formalParameters.hasNext()) {
				Var nextVar = formalParameters.next();
				Term nextTerm = actualParameters.next();
				newsubst = newsubst.combine(nextVar.mgu(nextTerm.applySubst(this.substitution)));
			}
			return newsubst;
		}
	}

	@Override
	public String toString() {
		return "ActionStackExecutor for " + this.action + " with " + this.substitution;
	}
}
