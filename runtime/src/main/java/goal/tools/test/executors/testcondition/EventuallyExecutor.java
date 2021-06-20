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
import languageTools.program.test.testcondition.Eventually;
import languageTools.program.test.testcondition.TestCondition;

public class EventuallyExecutor extends TestConditionExecutor {
	private final Eventually eventually;

	public EventuallyExecutor(Eventually eventually, Substitution substitution, RunState runstate,
			ModuleTestExecutor parent) {
		super(substitution, runstate, parent);
		this.eventually = eventually;
	}

	@Override
	public void evaluate(TestEvaluationChannel channel, SourceInfo info) {
		final Set<Substitution> evaluation = evaluate();
		if (this.eventually.hasNestedCondition()) {
			if (!evaluation.isEmpty()) {
				for (Substitution subst : evaluation) {
					this.parent.add(TestConditionExecutor.getTestConditionExecutor(this.eventually.getNestedCondition(),
							subst, this.runstate, this.parent));
				}
				setPassed(true);
			}
		} else if (!evaluation.isEmpty()) {
			setPassed(true);
		} else if (channel == TestEvaluationChannel.STOPTEST) {
			setPassed(false);
			throw new ConditionFailed(this.eventually, getContext(), this.substitution, evaluation, info,
					this.runstate.timedOut());
		}
	}

	@Override
	public TestCondition getCondition() {
		return this.eventually;
	}
}
