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
package goal.tools.test.executors;

import goal.core.executors.stack.ActionComboStackExecutor;
import goal.core.executors.stack.CallStack;
import goal.core.runtime.service.agent.RunState;
import goal.tools.errorhandling.exceptions.GOALActionFailedException;
import goal.tools.test.exceptions.TestActionFailed;
import goal.tools.test.executors.testcondition.UntilExecutor;
import krTools.KRInterface;
import languageTools.program.agent.Module.ExitCondition;
import languageTools.program.agent.Module.RuleEvaluationOrder;
import languageTools.program.agent.actions.Action;
import languageTools.program.agent.actions.ModuleCallAction;
import languageTools.program.test.TestAction;

public class TestActionExecutor extends TestObserver {
	private final TestAction action;

	public TestActionExecutor(TestAction action) {
		this.action = action;
	}

	public TestAction getAction() {
		return this.action;
	}

	public void run(RunState runstate) throws TestActionFailed {
		KRInterface kri = runstate.getKRI();
		initialize(runstate);

		if (this.action.getCondition() != null) {
			add(new UntilExecutor(this.action.getCondition(), kri.getSubstitution(null), runstate, this));
		}

		// 'hack' for executing main modules manually
		for (Action<?> action : this.action.getAction().getActions()) {
			if (action instanceof ModuleCallAction) {
				ModuleCallAction moduleCall = (ModuleCallAction) action;
				String module = moduleCall.getTarget().getSignature();
				if (runstate.getMainModule() != null && runstate.getMainModule().getSignature().equals(module)) {
					moduleCall.getTarget().setExitCondition(ExitCondition.NEVER);
				} else if (runstate.getInitModule() != null && runstate.getInitModule().getSignature().equals(module)) {
					moduleCall.getTarget().setRuleEvaluationOrder(RuleEvaluationOrder.LINEARALL);
				} else if (runstate.getEventModule() != null
						&& runstate.getEventModule().getSignature().equals(module)) {
					moduleCall.getTarget().setRuleEvaluationOrder(RuleEvaluationOrder.LINEARALL);
				}
			}
		}

		CallStack stack = new CallStack();
		ActionComboStackExecutor action = new ActionComboStackExecutor(stack, runstate, this.action.getAction(),
				kri.getSubstitution(null));
		stack.push(action);

		try {
			while (stack.canExecute()) {
				stack.pop();
				stack.getPopped().getResult();
			}
		} catch (GOALActionFailedException e) {
			throw new TestActionFailed(this, e);
		}
	}
}
