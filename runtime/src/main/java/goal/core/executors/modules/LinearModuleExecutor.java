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
package goal.core.executors.modules;

import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;

import goal.core.executors.stack.CallStack;
import goal.core.executors.stack.RuleStackExecutor;
import goal.core.runtime.service.agent.Result;
import goal.core.runtime.service.agent.RunState;
import goal.tools.errorhandling.exceptions.GOALActionFailedException;
import krTools.language.Substitution;
import languageTools.program.agent.Module;
import languageTools.program.agent.Module.RuleEvaluationOrder;
import languageTools.program.agent.rules.Rule;

/**
 * Executor for a regular {@link Module}. Keeps its own stack of
 * {@link RuleStackExecutor}s as initially determined by {@link #pushed()}, but
 * then modified by {@link #popped()}, i.e. whilst the module is running. Not
 * all rules in this stack will be pushed to the {@link CallStack} (and thus
 * executed), as the module might terminate at any point in the execution. If
 * there is an event module, it will be 'automatically' put on the stack by this
 * executor when a module has finished evaluation all rules but none applied.
 */
public class LinearModuleExecutor extends ModuleExecutor {
	/**
	 * The rules to execute as initially determined by {@link #pushed()}, but
	 * then modified by {@link #popped()}, i.e. whilst the module is running.
	 */
	private Deque<RuleStackExecutor> rules;

	/**
	 * Create an executor for a linear {@link Module}.
	 *
	 * @param parent
	 *            The {@link CallStack} that we are working in.
	 * @param runstate
	 *            The {@link RunState} (i.e. agent) that we are working for.
	 * @param module
	 *            The {@link Module} that is to be executed.
	 * @param substitution
	 *            The {@link Substitution} to be used for instantiating
	 *            parameters of the module.
	 * @param defaultRuleOrder
	 *            the order in which the rules in this module are to be
	 *            evaluated if the module didn't specify an order.
	 */
	LinearModuleExecutor(CallStack parent, RunState runstate, Module module, Substitution substitution,
			RuleEvaluationOrder defaultRuleOrder) {
		super(parent, runstate, module, substitution, defaultRuleOrder);
	}

	/**
	 * loads this.rules with executors for all rules in this module.
	 */
	@SuppressWarnings("unchecked")
	private void setRules() {
		this.result = Result.START;
		// Create all initial rule executors, and shuffle them if needed.
		this.rules = new LinkedList<>();
		Module module = getModule();
		for (Rule rule : module.getRules()) {
			RuleStackExecutor executor = (RuleStackExecutor) getExecutor(rule, getSubstitution());
			executor.setContext(module);
			this.rules.add(executor);
		}
		if (getRuleOrder() == RuleEvaluationOrder.RANDOM || getRuleOrder() == RuleEvaluationOrder.RANDOMALL) {
			Collections.shuffle((List<RuleStackExecutor>) this.rules);
		}
	}

	@Override
	public void popped() {
		if (this.failure != null) {
			return;
		}
		Module module = getModule();

		try {
			if (!hasPreparedMentalState()) {
				prepareMentalState();
				setRules();
			}
			// Check if we have just finished executing a rule;
			// if so we need to check if we need to exit the module.
			RuleStackExecutor previous = (getPrevious() instanceof RuleStackExecutor)
					? (RuleStackExecutor) getPrevious() : null;
			boolean exit = (previous == null) ? module.getRules().isEmpty() : this.rules.isEmpty();
			boolean all = (getRuleOrder() == RuleEvaluationOrder.LINEARALL
					|| getRuleOrder() == RuleEvaluationOrder.RANDOMALL
					|| getRuleOrder() == RuleEvaluationOrder.LINEARALLRANDOM);
			if (previous != null) {
				this.result = this.result.merge(previous.getResult());
				exit = isModuleTerminated(exit || (!all && this.result.justPerformedAction()));
			}
			Result previousResult = this.result;
			boolean reset = this.rules.isEmpty() || (!all && this.result.justPerformedAction());
			// Clean up if we should exit the module
			if (exit) {
				this.rules.clear();
				terminateModule();
			} else {
				// Re-initialize the set of rules if we are
				// executing them all sequentially but have no more left OR if
				// the previous rule succeeded (for a linear module)
				if (reset) {
					setRules();
				}
				// Check whether we need to start a new cycle. We do so if we do
				// NOT exit this module, some action has been performed while
				// evaluating the module's rules, or a new percept or message
				// has arrived. We also need to be running within the main
				// module's context (never start a new cycle when running the
				// init/event or a module called from either of these modules).
				// Otherwise, put the module itself back on the stack,
				// and add the next rule to execute to it.
				if (!doEvent(reset, previousResult) && !this.rules.isEmpty()) {
					select(this);
					select(this.rules.remove());
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
}