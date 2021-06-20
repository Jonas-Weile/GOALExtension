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
package goal.tools.test.exceptions;

import goal.tools.test.executors.TestActionExecutor;
import goal.tools.test.result.ResultFormatter;
import goal.tools.test.result.TestResult;

public class TestActionFailed extends Exception implements TestResult {
	/** Generated serialVersionUID */
	private static final long serialVersionUID = 2119184965021739086L;
	private final TestActionExecutor testAction;

	public TestActionExecutor getTestAction() {
		return this.testAction;
	}

	public TestActionFailed(TestActionExecutor testAction) {
		this(testAction, null);
	}

	public TestActionFailed(TestActionExecutor testAction, Exception cause) {
		super(cause);
		this.testAction = testAction;

	}

	@Override
	public <T> T accept(ResultFormatter<T> formatter) {
		return formatter.visit(this);
	}
}
