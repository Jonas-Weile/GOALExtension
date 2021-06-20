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

import java.util.List;

import languageTools.program.test.AgentTest;

public class AgentTestResult implements TestResult {
	private final boolean passed;
	private final AgentTest test;
	private final List<ModuleTestResult> results;

	public AgentTestResult(AgentTest test, List<ModuleTestResult> results) {
		this.test = test;
		this.results = results;
		this.passed = checkTestPassed();
	}

	private boolean checkTestPassed() {
		for (ModuleTestResult result : this.results) {
			if (!result.isPassed()) {
				return false;
			}
		}
		return true;
	}

	public AgentTest getTest() {
		return this.test;
	}

	public boolean isPassed() {
		return this.passed;
	}

	public List<ModuleTestResult> getTestResults() {
		return this.results;
	}

	@Override
	public <T> T accept(ResultFormatter<T> formatter) {
		return formatter.visit(this);
	}
}
