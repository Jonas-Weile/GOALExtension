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

import java.util.LinkedHashSet;
import java.util.Set;

import goal.core.runtime.service.agent.RunState;
import goal.tools.test.exceptions.ConditionFailed;
import goal.tools.test.executors.ModuleTestExecutor;
import krTools.language.Substitution;
import krTools.parser.SourceInfo;
import languageTools.program.test.testcondition.Always;
import languageTools.program.test.testcondition.TestCondition;

public class AlwaysExecutor extends TestConditionExecutor {
	private final Always always;
	private Set<Substitution> evaluation;

	public AlwaysExecutor(Always always, Substitution substitution, RunState runstate, ModuleTestExecutor parent) {
		super(substitution, runstate, parent);
		this.always = always;
		this.evaluation = new LinkedHashSet<>(0);
	}

	@Override
	public void evaluate(TestEvaluationChannel channel, SourceInfo info) {
		Set<Substitution> prev = new LinkedHashSet<>(this.evaluation);
		this.evaluation = evaluate();

		if (this.always.hasNestedCondition()) {
			if (channel == TestEvaluationChannel.STOPTEST) {
				setPassed(true);
			} else if (!this.evaluation.isEmpty() && !this.evaluation.equals(prev)) {
				for (Substitution subst : this.evaluation) {
					this.parent.add(TestConditionExecutor.getTestConditionExecutor(this.always.getNestedCondition(),
							subst, this.runstate, this.parent));
				}
			}
		} else {
			if (this.evaluation.isEmpty()) {
				setPassed(false);
				throw new ConditionFailed(this.always, getContext(), this.substitution, this.evaluation, info,
						this.runstate.timedOut());
			} else if (channel == TestEvaluationChannel.STOPTEST) {
				setPassed(true);
			}
		}
	}

	@Override
	public TestCondition getCondition() {
		return this.always;
	}
}
