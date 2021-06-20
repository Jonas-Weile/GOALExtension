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
package goal.tools.test.executors.testcondition;

import java.util.Set;

import goal.core.runtime.service.agent.RunState;
import goal.tools.test.exceptions.ConditionFailed;
import goal.tools.test.executors.ModuleTestExecutor;
import krTools.language.Substitution;
import krTools.parser.SourceInfo;
import languageTools.program.test.testcondition.Never;
import languageTools.program.test.testcondition.TestCondition;

public class NeverExecutor extends TestConditionExecutor {
	private final Never never;

	public NeverExecutor(Never never, Substitution substitution, RunState runstate, ModuleTestExecutor parent) {
		super(substitution, runstate, parent);
		this.never = never;
	}

	@Override
	public void evaluate(TestEvaluationChannel channel, SourceInfo info) throws ConditionFailed {
		if (this.never.hasNestedCondition()) {
			// NOT POSSIBLE?!
		} else {
			final Set<Substitution> evaluation = evaluate();
			if (!evaluation.isEmpty()) {
				setPassed(false);
				throw new ConditionFailed(this.never, getContext(), this.substitution, evaluation, info,
						this.runstate.timedOut());
			} else if (channel == TestEvaluationChannel.STOPTEST) {
				setPassed(true);
			}
		}
	}

	@Override
	public TestCondition getCondition() {
		return this.never;
	}
}
