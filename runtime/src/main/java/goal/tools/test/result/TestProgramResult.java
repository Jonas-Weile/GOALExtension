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

import java.io.File;
import java.util.Map;

import languageTools.program.agent.AgentId;
import languageTools.program.mas.MASProgram;
import languageTools.program.test.AgentTest;
import languageTools.program.test.TestProgram;

/**
 * The results of executing a {@link TestProgram}.
 *
 * Because the UnitTest starts a MAS and binds {@link AgentTest}s to agents
 * using their base name there may be multiple {@link AgentTestResult}s for a
 * test. Likewise there may be agents that ran but did not have an associated
 * test.
 *
 * A Unit test is considered to be passed if all {@link TestInterpreterResult}s
 * are passed and if for each {@link AgentTest} defined in the UnitTest there is
 * at least one result.
 */
public class TestProgramResult implements TestResult {
	private final TestProgram testProgram;
	private final Map<AgentId, TestInterpreterResult> results;
	private final boolean passed;

	/**
	 * @param testProgram
	 * @param results
	 */
	public TestProgramResult(TestProgram testProgram, Map<AgentId, TestInterpreterResult> results) {
		this.testProgram = testProgram;
		this.results = results;
		this.passed = checkPassed();
	}

	/**
	 * A unit test is considered to be passed if all tests were successfully passed
	 * (and at least one result was obtained), and failed otherwise, i.e., if one of
	 * the tests failed, was interrupted, or an exception occurred.
	 *
	 * @return {@code true} if the unit test was passed, {@code false} otherwise.
	 */
	private boolean checkPassed() {
		for (TestInterpreterResult tr : this.results.values()) {
			if (!tr.isPassed()) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @return the file containing the {@link TestProgram}
	 */
	public File getUnitTestFile() {
		return this.testProgram.getSourceFile();
	}

	/**
	 * @return the file containing the {@link MASProgram}
	 */
	public File getMasFile() {
		return this.testProgram.getMAS().getSourceFile();
	}

	/**
	 * Returns a map of test and for each test the results. Results without a
	 * associated test are stored with the <code>null</code> key.
	 *
	 * @return the results of the test.
	 */
	public Map<AgentId, TestInterpreterResult> getResults() {
		return this.results;
	}

	/**
	 * A Unit test is considered passed if all TestResults are passed and if for
	 * each test defined in the UnitTest there is at least one result.
	 *
	 * @return true if the test is passed.
	 */
	public boolean isPassed() {
		return this.passed;
	}

	/**
	 * @param formatter
	 * @return
	 */
	@Override
	public <T> T accept(ResultFormatter<T> formatter) {
		return formatter.visit(this);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + (this.passed ? 0 : 1);
		result = prime * result + ((this.testProgram == null) ? 0 : this.testProgram.hashCode());
		result = prime * result + ((this.results == null) ? 0 : this.results.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || !(obj instanceof TestProgramResult)) {
			return false;
		}
		TestProgramResult other = (TestProgramResult) obj;
		if (this.passed != other.passed) {
			return false;
		}
		if (this.testProgram == null) {
			if (other.testProgram != null) {
				return false;
			}
		} else if (!this.testProgram.equals(other.testProgram)) {
			return false;
		}
		if (this.results == null) {
			if (other.results != null) {
				return false;
			}
		} else if (!this.results.equals(other.results)) {
			return false;
		}
		return true;
	}
}
