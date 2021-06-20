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

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import goal.core.agent.Agent;
import goal.tools.logging.InfoLog;
import goal.tools.test.TestInterpreter;
import goal.tools.test.result.TestInterpreterResult;
import goal.tools.test.result.TestProgramResult;
import goal.tools.test.result.TestResultFormatter;
import languageTools.program.agent.AgentId;
import languageTools.program.test.TestProgram;

/**
 * Inspects the results of a test. The results are provided as a
 * {@link TestProgramResult}. This inspector can be reused between tests with
 * the same {@link TestProgram}.
 */
public class TestResultInspector implements ResultInspector<TestInterpreter> {
	private final Map<AgentId, TestInterpreterResult> results = new LinkedHashMap<>();
	private final TestProgram testProgram;

	public TestResultInspector(TestProgram testProgram) {
		this.testProgram = testProgram;
	}

	/**
	 * Returns the results of running the unit test. Note that the test result
	 * is only populated after running the test.
	 *
	 * @return the unit test results.
	 */
	public TestProgramResult getResults() {
		return new TestProgramResult(this.testProgram, Collections.unmodifiableMap(this.results));
	}

	@Override
	public void handleResult(Collection<Agent<TestInterpreter>> agents) {
		this.results.clear();
		// Extract results.
		for (Agent<TestInterpreter> a : agents) {
			TestInterpreterResult result = a.getController().getTestResults();
			if (result != null) {
				this.results.put(a.getId(), result);
			}
		}
		// Print results.
		final TestResultFormatter formatter = new TestResultFormatter();
		new InfoLog(formatter.visit(getResults())).emit();
	}
}