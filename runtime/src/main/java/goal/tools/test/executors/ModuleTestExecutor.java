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
package goal.tools.test.executors;

import goal.core.runtime.service.agent.RunState;
import goal.tools.debugger.DebugEvent;
import goal.tools.debugger.DebuggerKilledException;
import goal.tools.errorhandling.Warning;
import goal.tools.test.executors.testcondition.AlwaysExecutor;
import goal.tools.test.executors.testcondition.TestConditionExecutor;
import krTools.KRInterface;
import languageTools.program.test.ModuleTest;
import languageTools.program.test.testcondition.Always;
import languageTools.program.test.testcondition.TestCondition;

public class ModuleTestExecutor extends TestObserver {
	private final ModuleTest test;
	private boolean didPost = false;

	public ModuleTestExecutor(ModuleTest test) {
		this.test = test;
	}

	public ModuleTest getTest() {
		return this.test;
	}

	public void install(RunState runstate) {
		initialize(runstate);

		// Evaluate the pre-condition
		evaluatePre(runstate);

		// Install the in-conditions
		KRInterface kr = runstate.getKRI();
		for (TestCondition condition : this.test.getIn()) {
			add(TestConditionExecutor.getTestConditionExecutor(condition, kr.getSubstitution(null), runstate, this));
		}
	}

	public void evaluatePre(RunState runstate) {
		if (this.test.getPre() != null) {
			KRInterface kr = runstate.getKRI();
			TestConditionExecutor pre = new AlwaysExecutor(
					new Always(this.test.getPre(), "pre{ " + this.test.getPre() + " }"), kr.getSubstitution(null),
					runstate, this);
			add(pre);
			pre.evaluate(new DebugEvent(null, null, null, null, null, ""));
		}
	}

	public void evaluatePost(RunState runstate) {
		if (this.test.getPost() != null) {
			KRInterface kr = runstate.getKRI();
			TestConditionExecutor post = new AlwaysExecutor(
					new Always(this.test.getPost(), "post{ " + this.test.getPost() + " }"), kr.getSubstitution(null),
					runstate, this);
			add(post);
			post.evaluate(new DebugEvent(null, null, null, null, null, ""));
			this.didPost = true;
		}
	}

	/**
	 * Will be called explicitly from the ModuleTestExecutor and
	 * AgentTestExecutor at takedown.
	 *
	 * @param runstate
	 */
	public void destroy(RunState runstate) {
		// Allow in-conditions to clean-up after themselves (determine final
		// results, e.g. fail eventually or succeed always/never conditions)
		try {
			for (TestConditionExecutor executor : getExecutors()) {
				executor.evaluate((DebugEvent) null);
			}
		} catch (DebuggerKilledException e) {
			new Warning("Did not finish evaluating in-conditions of '" + getTest().getModuleName() + "' for agent '"
					+ runstate.getId() + "' because a timeout was reached.", e).emit();
		}
		if (this.test.getPost() != null && !this.didPost) {
			new Warning("Did not evaluate post-condition of '" + getTest().getModuleName() + "' for agent '"
					+ runstate.getId() + "' because a timeout or until-condition was reached.").emit();
		}
	}

	@Override
	public int hashCode() {
		return (this.test == null) ? 0 : this.test.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || !(obj instanceof ModuleTestExecutor)) {
			return false;
		}
		ModuleTestExecutor other = (ModuleTestExecutor) obj;
		if (this.test == null) {
			if (other.test != null) {
				return false;
			}
		} else if (!this.test.equals(other.test)) {
			return false;
		}
		return true;
	}
}
