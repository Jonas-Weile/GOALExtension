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
package goal.tools.test;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;

import events.Channel;
import goal.core.agent.Agent;
import goal.core.agent.AgentRegistry;
import goal.tools.IDEGOALInterpreter;
import goal.tools.TestRun;
import goal.tools.adapt.Learner;
import goal.tools.debugger.IDEDebugger;
import goal.tools.profiler.Profiles;
import goal.tools.test.executors.AgentTestExecutor;
import goal.tools.test.result.AgentTestResult;
import goal.tools.test.result.TestInterpreterResult;
import languageTools.program.mas.AgentDefinition;
import languageTools.program.test.AgentTest;

/**
 * Interpreter that will run test programs. Once completed test results can be
 * retrieved. When no test is provided the AgentDefinition will be interpreted
 * as normal instead.
 */
public class TestInterpreter extends IDEGOALInterpreter {
	private final TestRun testRun;
	private final AgentTest agentTest;
	private AgentTestResult agentTestResult;

	/**
	 * Constructs a new test interpreter.
	 *
	 * @param program   of the agent under test
	 * @param registry  the general agent registry
	 * @param debugger  used when running the program or test
	 * @param learner   used evaluate adaptive rules
	 * @param testRun   the owner of the agentTest
	 * @param agentTest to run, may be null when no tests should be ran
	 * @param profiles
	 */
	public TestInterpreter(AgentDefinition program, AgentRegistry<?> registry, IDEDebugger debugger, Learner learner,
			TestRun testRun, AgentTest agentTest, Profiles profiles) {
		super(program, registry, debugger, learner, profiles);
		this.testRun = testRun;
		this.agentTest = agentTest;
	}

	/**
	 * @return the agent test
	 */
	public AgentTest getTest() {
		return this.agentTest;
	}

	/**
	 * Returns the results of the tests executed by the interpreter. The test
	 * results are not valid until the interpreter has stopped.
	 *
	 * @return the testResults.
	 */
	public TestInterpreterResult getTestResults() {
		return (this.agentTest == null) ? null
				: new TestInterpreterResult(this.agentTest, this.agentTestResult, getUncaughtThrowable(),
						(getRunState() == null) ? false : getRunState().timedOut());
	}

	@Override
	protected Runnable getRunnable(final ExecutorService executor, final Callable<Callable<?>> in) {
		if (this.agentTest == null) {
			// Just run the agent itself when no test for it is present;
			// it might just be there for another agent in the MAS.
			return super.getRunnable(executor, in);
		} else {
			return new Runnable() {
				@SuppressWarnings("unchecked")
				@Override
				public void run() {
					try {
						// Run the whole test
						createRunstate();
						getRunState().getEventGenerator().event(Channel.REASONING_CYCLE_SEPARATOR, 0, null,
								"started test.", TestInterpreter.this.agent.getId());
						AgentTestExecutor testExecutor = new AgentTestExecutor(TestInterpreter.this.agentTest,
								TestInterpreter.this.testRun.getTestProgram());
						TestInterpreter.this.agentTestResult = testExecutor
								.run((Agent<TestInterpreter>) TestInterpreter.this.agent);
					} catch (Exception e) {
						TestInterpreter.this.throwable = e;
					} finally {
						try {
							setTerminated();
						} catch (InterruptedException e) {
							TestInterpreter.this.throwable = e;
						}
						// Check if we are still actually running any test
						boolean alive = false;
						for (TestInterpreter runner : TestInterpreter.this.testRun.getTestRunners()) {
							if (runner.getTest() != null && runner.isRunning()) {
								alive = true;
								break;
							}
						}
						if (!alive && TestInterpreter.this.testRun.getManager() != null) {
							for (Agent<TestInterpreter> agent : TestInterpreter.this.testRun.getManager()
									.getAliveAgents()) {
								agent.stop();
							}
						}
					}
				}
			};
		}
	}
}
