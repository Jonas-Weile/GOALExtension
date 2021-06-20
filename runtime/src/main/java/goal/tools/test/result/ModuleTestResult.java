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

import goal.tools.test.executors.ModuleTestExecutor;
import languageTools.program.test.ModuleTest;

public class ModuleTestResult implements TestResult {
	private final ModuleTestExecutor test;
	private final TestResult exception;
	private final List<TestConditionResult> results;
	private final boolean passed;

	public ModuleTestResult(ModuleTestExecutor test, TestResult exception, List<TestConditionResult> results) {
		this.test = test;
		this.exception = exception;
		this.results = results;
		this.passed = checkTestsPassed();
	}

	private boolean checkTestsPassed() {
		for (TestConditionResult result : this.results) {
			if (!result.isPassed()) {
				return false;
			}
		}
		return (this.exception == null);
	}

	public TestResult getException() {
		return this.exception;
	}

	public ModuleTest getTest() {
		return (this.test == null) ? null : this.test.getTest();
	}

	public boolean isPassed() {
		return this.passed;
	}

	public List<TestConditionResult> getResults() {
		return this.results;
	}

	@Override
	public <T> T accept(ResultFormatter<T> formatter) {
		return formatter.visit(this);
	}
}
