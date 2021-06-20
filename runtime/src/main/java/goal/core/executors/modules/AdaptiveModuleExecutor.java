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

import java.util.LinkedList;
import java.util.List;

import goal.core.executors.stack.ActionComboStackExecutor;
import goal.core.executors.stack.CallStack;
import goal.core.executors.stack.RuleStackExecutor;
import goal.core.runtime.service.agent.Result;
import goal.core.runtime.service.agent.RunState;
import goal.tools.adapt.ModuleID;
import goal.tools.errorhandling.exceptions.GOALActionFailedException;
import krTools.language.Substitution;
import languageTools.program.agent.Module;
import languageTools.program.agent.Module.RuleEvaluationOrder;
import languageTools.program.agent.rules.Rule;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;

public class AdaptiveModuleExecutor extends ModuleExecutor {
	/**
	 * The action combo to execute.
	 */
	private ActionComboStackExecutor actionCombo = null;

	/**
	 * Create an executor for an adaptive {@link Module}.
	 *
	 * @param parent           The {@link CallStack} that we are working in.
	 * @param runstate         The {@link RunState} (i.e. agent) that we are working
	 *                         for.
	 * @param module           The {@link Module} that is to be executed.
	 * @param substitution     The {@link Substitution} to be used for instantiating
	 *                         parameters of the module.
	 * @param defaultRuleOrder the order in which the rules in this module are to be
	 *                         evaluated if the module didn't specify an order.
	 */
	AdaptiveModuleExecutor(CallStack parent, RunState runstate, Module module, Substitution substitution,
			RuleEvaluationOrder defaultRuleOrder) {
		super(parent, runstate, module, substitution, defaultRuleOrder);
	}

	/**
	 * @return the recommended action from the list of enabled rules, or null if
	 *         none is available or enabled.
	 * @throws GOALActionFailedException
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	private ActionComboStackExecutor pickAction()
			throws MSTQueryException, MSTDatabaseException, GOALActionFailedException {
		List<ActionComboStackExecutor> enabledActions = new LinkedList<>();
		for (Rule rule : getModule().getRules()) {
			RuleStackExecutor stub = new RuleStackExecutor(this.parent, this.runstate, rule, getSubstitution());
			stub.setContext(getModule());
			enabledActions.addAll(stub.generateExecutors());
		}

		if (enabledActions.isEmpty()) {
			return null;
		} else {
			return this.runstate.getLearner().act(new ModuleID(getModule().getSignature()),
					this.runstate.getMentalState(), enabledActions);
		}
	}

	@Override
	public void popped() {
		try {
			if (this.failure == null) {
				execute();
			}
		} catch (GOALActionFailedException e) {
			this.failure = e;
		} catch (MSTDatabaseException | MSTQueryException e) {
			this.failure = new GOALActionFailedException("failed to execute module", e);
		}
	}

	private void execute() throws GOALActionFailedException, MSTDatabaseException, MSTQueryException {
		// Check if this is the first call to this module.
		if (!hasPreparedMentalState()) {
			prepareMentalState();
			this.result = Result.START;
		}

		// Check if we have just finished executing an actioncombo;
		// if so we need to check if we need to exit the module.
		ActionComboStackExecutor previous = (getPrevious() instanceof ActionComboStackExecutor)
				? (ActionComboStackExecutor) getPrevious()
				: null;
		boolean exit = (previous == null) ? false : this.actionCombo == null;
		if (previous != null) {
			this.result = this.result.merge(previous.getResult());
			exit = isModuleTerminated(exit || this.result.justPerformedAction());
			// Here justPerformedAction does become true, since it is called after the
			// module has been run (after the 'delete' action)
		}

		// Clean up if we should exit the module;
		// and execute it otherwise.
		if (exit) {
			this.actionCombo = null;
			terminateModule();
		} else {
			executeModule();
		}
	}

	private void executeModule() throws GOALActionFailedException, MSTDatabaseException, MSTQueryException {
		Result previousResult = this.result;
		// If we just executed an actionCombo, remove it and go to the event
		// module first before doing anything else here.
		// TODO: if pickAction did not find an enabled action
		// (this.actionCombo=null as well), it will prevent the event module
		// from being fired right now.

		if (this.result.justPerformedAction()) {
			this.result = Result.START;
			this.actionCombo = null;
			if (doEvent(true, previousResult)) {
				return;
			}
		}

		// We came back from the event module,
		// so we need to pick a new actioncombo now,
		// after updating the learned state first.
		if (this.actionCombo == null) {
			Double reward = this.runstate.getReward();
			if (reward == null) {
				reward = 0d; // no env reward, fix some value.
			}
			this.runstate.getLearner().update(new ModuleID(getModule().getSignature()), this.runstate.getMentalState(),
					reward);
			this.actionCombo = pickAction(); // Actual action that is executed and that determines the reward in the
												// next round
		}

		// Put the module itself back on the stack,
		// and add the next actioncombo to execute to it.
		if (this.actionCombo != null) {
			select(this);
			select(null); // stub for rule
			select(this.actionCombo);
			this.actionCombo = null;
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