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
package goal.tools.test.result;

import goal.tools.debugger.DebuggerKilledException;
import goal.tools.test.TestInterpreter;
import languageTools.program.test.AgentTest;
import languageTools.program.test.TestProgram;

/**
 * Result of running the {@link TestInterpreter} for a single agent. Includes
 * the agent's test and any unexpected exceptions thrown during the execution of
 * the test. Can be considered passed if no exceptions were thrown and the test
 * result passed.
 *
 * Note that an agent may not have had a test.
 */
public class TestInterpreterResult implements TestResult {
	private final AgentTest test;
	private final AgentTestResult testResult;
	private final Throwable uncaughtThrowable;
	private final boolean timeout;
	private final boolean passed;

	/**
	 * @param test
	 * @param id
	 * @param testResult
	 * @param uncaughtThrowable
	 */
	public TestInterpreterResult(AgentTest test, AgentTestResult testResult, Throwable uncaughtThrowable,
			boolean timeout) {
		this.test = test;
		this.testResult = testResult;
		if (uncaughtThrowable instanceof DebuggerKilledException) {
			this.uncaughtThrowable = null; // FIXME?!
		} else {
			this.uncaughtThrowable = uncaughtThrowable;
		}
		this.timeout = timeout;
		this.passed = checkTestPassed();
	}

	/**
	 * Because results and tests are immutable we only compute the result once.
	 *
	 * @return true if agent did not crash and its agent test passed.
	 */
	private boolean checkTestPassed() {
		// Agent did not crash
		if (this.uncaughtThrowable != null) {
			return false;
		}

		// If we had no test. Test passes by default.
		if (this.test == null) {
			return true;
		}

		// If a test was ran, it should produce a result and be successful
		return this.testResult != null && this.testResult.isPassed();
	}

	/**
	 * When an agent terminates in an abnormal manner any exceptions thrown are
	 * stored. These are collected as part of the test.
	 *
	 * @return the exception that caused the agent to stop or null of the agent
	 *         terminated as expected
	 */
	public Throwable getUncaughtThrowable() {
		return this.uncaughtThrowable;
	}

	/**
	 * Returns true if the test passed. The test is considered passed if all
	 * {@link AssertTestResult} are passed no exception was thrown.
	 *
	 * @return true if the test passed
	 */
	public boolean isPassed() {
		return this.passed;
	}

	/**
	 * @return true iff the test timeout was reached (if any)
	 */
	public boolean hasTimedOut() {
		return this.timeout;
	}

	/**
	 * Returns the test that produced this result. Can be <code>null</code> when
	 * ever if agent did not have a test associated with it in the
	 * {@link TestProgram}.
	 *
	 * @return the test that produced this result, can <code>null</code>,
	 */

	public AgentTest getTest() {
		return this.test;
	}

	/**
	 * Returns the result of this tests Can be <code>null</code> if agent did not
	 * have a test associated with it in the {@link TestProgram} or terminated
	 * unexpectedly.
	 *
	 * @see TestInterpreterResult#getUncaughtThrowable()
	 *
	 * @return the result produced by the agent test of this test, can
	 *         <code>null</code>,
	 */
	public AgentTestResult getResult() {
		return this.testResult;
	}

	@Override
	public <T> T accept(ResultFormatter<T> formatter) {
		return formatter.visit(this);
	}
}
