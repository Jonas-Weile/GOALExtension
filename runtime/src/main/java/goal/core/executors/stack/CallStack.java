package goal.core.executors.stack;

import java.util.Deque;
import java.util.LinkedList;

import goal.core.agent.Controller;
import goal.core.executors.modules.ModuleExecutor;

/**
 * Stack containing {@link StackExecutor}s. This stack is usually managed by an
 * agent {@link Controller}. Everything that is pushed to this stack will be
 * executed; when the stack is empty the corresponding agent is finished. An
 * ordinary callstack will always follow this sequence of executors:
 * {@link ModuleStackExecutor} > {@link RuleStackExecutor} >
 * {@link ActionComboStackExecutor} > {@link ActionStackExecutor} (and then
 * possibly on to a module again). Sometimes 'null' might be added in place of
 * any of these executors (e.g. for event module execution) to ensure that this
 * ordering (and thus the corresponding indexes) is still present.
 */
public class CallStack {
	/**
	 * The actual stack of executors
	 */
	private final Deque<StackExecutor> stack;
	/**
	 * The previously executed executor
	 */
	private StackExecutor popped;

	/**
	 * Initialize an empty stack
	 */
	public CallStack() {
		this.stack = new LinkedList<>();
	}

	/**
	 * Clear the stack immediately (i.e., without executing any of the elements)
	 */
	public void clear() {
		this.stack.clear();
	}

	/**
	 * @return true iff there are executors left on the stack
	 */
	public boolean canExecute() {
		return !this.stack.isEmpty();
	}

	/**
	 * @return the number of executors that are currently on the stack, also
	 *         referred to as the 'stack level' or 'stack depth'; used by for
	 *         example the debugging mechanism to determine the result of different
	 *         stepping actions (into, over, out) in a generic way.
	 */
	public int getIndex() {
		return this.stack.size();
	}

	/**
	 * Push an executor on the stack.
	 *
	 * @param executor
	 *            the {@link StackExecutor} to be pushed. Can be null in order to
	 *            ensure a certain stack index (i.e. for {@link #getIndex()})
	 */
	public void push(StackExecutor executor) {
		this.stack.push(executor);
	}

	/**
	 * Pop an executor from the stack. If the popped executor is not null, the
	 * executor is informed that it has been popped. After this function has
	 * finished, the popped executor (if not null) will be retrievable by
	 * {@link #getPopped()} until the next executor has been fully processed.
	 */
	public void pop() {
		StackExecutor executor = this.stack.pop();
		if (executor != null) {
			executor.popped();
			this.popped = executor;
		}
	}

	/**
	 * @return the last {@link #pop()}-ped executor (if any already).
	 */
	public StackExecutor getPopped() {
		return this.popped;
	}

	/**
	 * @return the first {@link ModuleExecutor} in the stack, or null if no such
	 *         module.
	 */
	public ModuleExecutor getParentModuleExecutor() {
		for (StackExecutor executor : this.stack) {
			if (executor instanceof ModuleExecutor) {
				return (ModuleExecutor) executor;
			}
		}
		return null;
	}
}
