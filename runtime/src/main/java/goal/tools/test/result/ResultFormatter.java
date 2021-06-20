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
import goal.tools.test.exceptions.EvaluationFailed;
import goal.tools.test.exceptions.TestActionFailed;

public interface ResultFormatter<T> {
	T visit(TestInterpreterResult result);

	T visit(TestProgramResult result);

	T visit(AgentTestResult result);

	T visit(ModuleTestResult result);

	T visit(TestConditionResult result);

	T visit(ConditionFailed result);

	T visit(TestActionFailed result);

	T visit(EvaluationFailed result);
}
