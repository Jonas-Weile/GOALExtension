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

import goal.tools.test.exceptions.ConditionFailed;
import goal.tools.test.executors.testcondition.TestConditionExecutor;
import languageTools.program.test.testcondition.TestCondition;

public class TestConditionResult implements TestResult {
	private final TestConditionExecutor test;
	private final ConditionFailed result;
	private final boolean passed;

	public TestConditionResult(TestConditionExecutor test) {
		this.test = test;
		this.result = test.getFailure();
		this.passed = (this.result == null);
	}

	public TestCondition getTest() {
		return (this.test == null) ? null : this.test.getCondition();
	}

	public boolean isPassed() {
		return this.passed;
	}

	public ConditionFailed getFailure() {
		return this.result;
	}

	@Override
	public <T> T accept(ResultFormatter<T> formatter) {
		return formatter.visit(this);
	}
}
