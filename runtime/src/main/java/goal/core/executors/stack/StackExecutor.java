package goal.core.executors.stack;

import goal.core.executors.modules.ModuleExecutor;
import goal.core.runtime.service.agent.Result;
import goal.core.runtime.service.agent.RunState;
import goal.tools.errorhandling.exceptions.GOALActionFailedException;
import krTools.language.Substitution;
import languageTools.program.agent.Module;
import languageTools.program.agent.actions.Action;
import languageTools.program.agent.actions.ActionCombo;
import languageTools.program.agent.rules.Rule;

/**
 * This is the general interface for any executable that can be pushed onto a
 * {@link CallStack}. Basically, an executor is notified when it is popped from
 * the callstack. When popped, an executor can execute some arbitrary code,
 * and/or use {@link #select(StackExecutor)} to schedule new
 * {@link StackExecutor}s for execution, or even itself again (i.e. when the
 * result from an executor that has been selected is needed to continue
 * executing the current executor). When finished (successfully or not), it
 * should ensure a certain result is returned through {@link #getResult()}.
 */
public abstract class StackExecutor {
	/**
	 * The {@link CallStack} this executor has been created for. Usually, this field
	 * should not be accessed directly; instead {@link #select(StackExecutor)} and
	 * {@link #getPrevious()} should be used.
	 */
	protected final CallStack parent;
	/**
	 * The {@link RunState} this executor works under (i.e. an agent)
	 */
	protected final RunState runstate;

	/**
	 * Constructor for implementors: registers the callstack and the runstate, and
	 * sets the position of execution in the source code for this executor.
	 */
	protected StackExecutor(CallStack parent, RunState runstate) {
		this.parent = parent;
		this.runstate = runstate;
	}

	/**
	 * Push the given executor on the {@link CallStack}.
	 *
	 * @param executor
	 */
	protected void select(StackExecutor executor) {
		this.parent.push(executor);
	}

	/**
	 * @return the previously popped (executed) executor from the {@link CallStack}
	 */
	protected StackExecutor getPrevious() {
		return this.parent.getPopped();
	}

	/**
	 * Called immediately after the executor has been popped from the
	 * {@link CallStack}. An implementation should perform any necessary
	 * initialization here, and do the actual execution, i.e. executing certain code
	 * or using {@link #select(StackExecutor)} to push new {@link StackExecutor}s to
	 * the {@link CallStack}. It is possible for an executor to select itself again
	 * as well. When an executor has finished (i.e. it does not select itself
	 * again), it should makes sure that {@link #getResult()} will then return
	 * something informative.
	 *
	 * <p>
	 * If a failure occured before, this action does nothing. If a failure occurs in
	 * this call, the failure will be remembered and returned from
	 * {@link #getResult()}.
	 */
	abstract public void popped();

	/**
	 * @return When the execution was successful, i.e. it has been {@link #popped()}
	 *         and did not select itself again, the relevant {@link Result} should
	 *         be returned here.
	 * @throws GOALActionFailedException
	 *             This exception should be thrown when anything went wrong during
	 *             either initializing or executing the executor.
	 */
	abstract public Result getResult() throws GOALActionFailedException;

	/**
	 * Factory method for creating {@link StackExecutor}s.
	 *
	 * @param object
	 *            Some object to get an {@link StackExecutor} for.
	 * @param substitution
	 *            The substitution to use when creating the object.
	 * @return An instantiated {@link StackExecutor} for a object if this is
	 *         possible; null otherwise.
	 */
	protected StackExecutor getExecutor(Object object, Substitution substitution) {
		if (object instanceof Action<?>) {
			return new ActionStackExecutor(this.parent, this.runstate, (Action<?>) object, substitution, false);
		} else if (object instanceof ActionCombo) {
			return new ActionComboStackExecutor(this.parent, this.runstate, (ActionCombo) object, substitution);
		} else if (object instanceof Rule) {
			return new RuleStackExecutor(this.parent, this.runstate, (Rule) object, substitution);
		} else if (object instanceof Module) {
			Module module = (Module) object;
			ModuleExecutor parent = this.parent.getParentModuleExecutor();
			return ModuleExecutor.getModuleExecutor(this.parent, this.runstate, module, substitution,
					(parent == null) ? module.getRuleEvaluationOrder() : parent.getRuleOrder());
		} else {
			return null;
		}
	}

	@Override
	abstract public String toString();
}
