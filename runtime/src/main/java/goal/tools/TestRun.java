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
package goal.tools;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import goal.core.agent.AbstractAgentFactory;
import goal.core.agent.AgentFactory;
import goal.tools.adapt.Learner;
import goal.tools.debugger.IDEDebugger;
import goal.tools.debugger.LoggingObserver;
import goal.tools.debugger.NOPObserver;
import goal.tools.errorhandling.exceptions.GOALLaunchFailureException;
import goal.tools.errorhandling.exceptions.GOALRunFailedException;
import goal.tools.profiler.Profiles;
import goal.tools.test.TestInterpreter;
import goal.tools.test.result.AgentTestResult;
import goal.tools.test.result.ModuleTestResult;
import goal.tools.test.result.TestInterpreterResult;
import languageTools.program.test.AgentTest;
import languageTools.program.test.TestProgram;

/**
 * Runs a {@link TestProgram} program. During the test agent are created with a
 * {@link TestInterpreter}. This interpreter will execute a {@link AgentTest}
 * provided by the {@link TestProgram}. The results can be collected by using
 * the {@link TestResultInspector}.
 */
public class TestRun extends AbstractRun<IDEDebugger, TestInterpreter> {
	/**
	 * Creates the agents used when running the test program. The agents are created
	 * with a {@link TestInterpreter} controller. The agents base name is used to
	 * look up the {@link AgentTest} for the agent. If no test could be found, the
	 * agent will only check for runtime errors.
	 */
	protected class TestRunAgentFactory extends AbstractAgentFactory<IDEDebugger, TestInterpreter> {
		public TestRunAgentFactory() throws GOALLaunchFailureException {
			super(TestRun.this.timeout);
		}

		@Override
		protected IDEDebugger provideDebugger() {
			IDEDebugger debugger = new IDEDebugger(getAgentId(), getManager(), getEnvironmentPort());
			debugger.setKeepRunning(!TestRun.this.debug);
			if (TestRun.this.debuggerOutput) {
				new LoggingObserver(debugger).subscribe();
			} else if (!TestRun.this.debug) {
				new NOPObserver(debugger).subscribe();
			}
			return debugger;
		}

		@Override
		protected TestInterpreter provideController(IDEDebugger debugger, Learner learner, Profiles profiles) {
			AgentTest test = TestRun.this.testProgram.getAgentTest(getAgentDf().getName());
			TestInterpreter controller = new TestInterpreter(getAgentDf(), getRegistry(), debugger, learner,
					TestRun.this, test, profiles);
			if (TestRun.this.debug) {
				controller.keepDataOnTermination();
			}
			TestRun.this.testRunners.add(controller);
			return controller;
		}
	}

	/**
	 * The test program.
	 */
	private final TestProgram testProgram;
	private final boolean debug;
	private final List<TestInterpreter> testRunners;

	public TestRun(TestProgram program, boolean debug) throws GOALRunFailedException {
		super(program.getMAS());
		setTimeOut(program.getTimeout());
		setResultInspector(new TestResultInspector(program));
		this.testProgram = program;
		this.debug = debug;
		this.testRunners = new LinkedList<>();
	}

	public TestProgram getTestProgram() {
		return this.testProgram;
	}

	public List<TestInterpreter> getTestRunners() {
		return Collections.unmodifiableList(this.testRunners);
	}

	@Override
	protected AgentFactory<IDEDebugger, TestInterpreter> buildAgentFactory() throws GOALLaunchFailureException {
		return new TestRunAgentFactory();
	}

	@Override
	public void cleanup() {
		if (this.debug) {
			// Do nothing; runtime is manually cleaned
		} else {
			super.cleanup();
		}
	}

	/**
	 * Calculates and returns the percentage of tests that have passed.
	 *
	 * @return The percentage (in percentage form, e.g. 66.7) of the amount of tests
	 *         that passed during the TestRun.
	 */
	public double calculatePercentage() {
		double tests = 0;
		double success = 0;
		for (TestInterpreter interpreter : this.testRunners) {
			TestInterpreterResult testResult = interpreter.getTestResults();
			AgentTestResult results;
			if ((results = testResult.getResult()) == null) {
				continue;
			}
			for (ModuleTestResult result : results.getTestResults()) {
				++tests;
				if (result.isPassed()) {
					++success;
				}
			}
		}
		return (tests == 0) ? 0 : (success / tests * 100);
	}
}
