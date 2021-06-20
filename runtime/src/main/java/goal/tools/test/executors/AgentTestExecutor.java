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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import events.Channel;
import events.ExecutionEventGeneratorInterface;
import goal.core.agent.Agent;
import goal.core.executors.modules.ModuleExecutor;
import goal.core.executors.stack.CallStack;
import goal.core.runtime.service.agent.RunState;
import goal.tools.debugger.DebuggerKilledException;
import goal.tools.errorhandling.exceptions.GOALActionFailedException;
import goal.tools.test.TestInterpreter;
import goal.tools.test.exceptions.BoundaryException;
import goal.tools.test.exceptions.ConditionFailed;
import goal.tools.test.exceptions.EvaluationFailed;
import goal.tools.test.exceptions.TestActionFailed;
import goal.tools.test.executors.testcondition.TestConditionExecutor;
import goal.tools.test.result.AgentTestResult;
import goal.tools.test.result.ModuleTestResult;
import goal.tools.test.result.TestConditionResult;
import goal.tools.test.result.TestResult;
import goal.tools.test.result.TestResultFormatter;
import krTools.parser.SourceInfo;
import languageTools.program.agent.Module;
import languageTools.program.agent.Module.RuleEvaluationOrder;
import languageTools.program.test.AgentTest;
import languageTools.program.test.ModuleTest;
import languageTools.program.test.TestAction;
import languageTools.program.test.TestProgram;

public class AgentTestExecutor extends TestObserver {
	private final AgentTest test;
	private final TestProgram parent;
	private RunState runstate;
	private Map<String, ModuleTestExecutor> testMap;
	private Map<ModuleTestExecutor, TestResult> tests;

	public AgentTestExecutor(AgentTest test, TestProgram parent) {
		this.test = test;
		this.parent = parent;
	}

	public AgentTest getTest() {
		return this.test;
	}

	/**
	 * Runs this test on the given run state.
	 *
	 * @param agent
	 *            the agent executing the test
	 * @return a list of {@link AssertTestResult}s containing the results of the
	 *         tests.
	 */
	public AgentTestResult run(Agent<TestInterpreter> agent) throws GOALActionFailedException {
		this.testMap = new LinkedHashMap<>();
		this.tests = new LinkedHashMap<>();
		this.runstate = agent.getController().getRunState();

		initialize(this.runstate);

		CallStack stack = new CallStack();
		this.runstate.startCycle(false);

		// If we have an event module, add it to the stack as
		// well, i.e. execute it before the main module.
		Module event = this.runstate.getEventModule();
		if (event != null) {
			ModuleExecutor eventExec = ModuleExecutor.getModuleExecutor(stack, this.runstate, event,
					event.getKRInterface().getSubstitution(null), RuleEvaluationOrder.LINEARALL);
			// eventExec.setSource(UseCase.EVENT);
			stack.push(eventExec);
		}
		// If we have an init module, add it to the stack as
		// well, i.e. execute it once before the other modules.
		Module init = this.runstate.getInitModule();
		if (init != null) {
			ModuleExecutor initExec = ModuleExecutor.getModuleExecutor(stack, this.runstate, init,
					init.getKRInterface().getSubstitution(null), RuleEvaluationOrder.LINEARALL);
			// initExec.setSource(UseCase.INIT);
			stack.push(initExec);
		}
		// Execute any possible init/event stuff.
		while (stack.canExecute()) {
			stack.pop();
			stack.getPopped().getResult();
		}

		// Execute all actions specified in the test.
		ExecutionEventGeneratorInterface events = this.runstate.getEventGenerator();
		for (TestAction action : this.test.getActions()) {
			TestResult exception = null;
			try {
				TestActionExecutor execute = new TestActionExecutor(action);
				execute.run(this.runstate);
			} catch (TestActionFailed e) {
				exception = e;
				events.event(Channel.TESTFAILURE, null, null, exception.accept(new TestResultFormatter()));
				break;
			} catch (ConditionFailed e) {
				exception = new EvaluationFailed("'" + action.getAction() + "' did not complete successfully.", e);
				events.event(Channel.TESTFAILURE, null, null, exception.accept(new TestResultFormatter()));
				break;
			} catch (DebuggerKilledException | BoundaryException passthrough) {
			} finally {
				ModuleTestExecutor current = this.testMap
						.get(agent.getController().getRunState().getActiveModule().getSignature());
				if (current != null) {
					this.tests.put(current, exception);
				}
			}
		}
		AgentTestResult result = new AgentTestResult(this.test, getTestResults());
		agent.dispose(false);
		return result;
	}

	private List<ModuleTestResult> getTestResults() {
		List<ModuleTestResult> results = new ArrayList<>(this.tests.size());
		for (ModuleTestExecutor moduletest : this.tests.keySet()) {
			moduletest.destroy(this.runstate);

			List<TestConditionResult> testResults = new ArrayList<>(moduletest.getExecutors().length);
			List<ConditionFailed> failures = new LinkedList<>();
			for (TestConditionExecutor test : moduletest.getExecutors()) {
				TestConditionResult result = new TestConditionResult(test);
				int existing = failures.indexOf(result.getFailure());
				if (existing != -1) {
					ConditionFailed failure = failures.get(existing);
					failure.addEvaluation(result.getFailure().getFirstEvaluation());
				} else {
					testResults.add(result);
					if (result.getFailure() != null) {
						failures.add(result.getFailure());
					}
				}
			}

			results.add(new ModuleTestResult(moduletest, this.tests.get(moduletest), testResults));
		}
		return results;
	}

	@Override
	public void goalEvent(Channel channel, Object associateObject, SourceInfo associateSource, String message,
			Object... args) {
		switch (channel) {
		case MODULE_ENTRY:
			Module module1 = (Module) associateObject;
			ModuleTest test1 = this.parent.getModuleTest(module1.getSignature());
			if (test1 != null && !this.testMap.containsKey(module1.getSignature())) {
				ModuleTestExecutor executor1 = new ModuleTestExecutor(test1);
				this.testMap.put(module1.getSignature(), executor1);
				executor1.install(this.runstate);
				this.tests.put(executor1, null);
			}
			break;
		case MODULE_EXIT:
			Module module2 = (Module) associateObject;
			ModuleTestExecutor executor2 = this.testMap.get(module2.getSignature());
			if (executor2 != null) {
				executor2.evaluatePost(this.runstate);
			}
			break;
		default:
			break;
		}
	}

	@Override
	public int hashCode() {
		return (this.test == null) ? 0 : this.test.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || !(obj instanceof AgentTestExecutor)) {
			return false;
		}
		AgentTestExecutor other = (AgentTestExecutor) obj;
		if (this.test == null) {
			if (other.test != null) {
				return false;
			}
		} else if (!this.test.equals(other.test)) {
			return false;
		}
		return true;
	}
}
