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

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import events.Channel;
import events.ExecutionEventGeneratorInterface;
import events.NoEventGenerator;
import goal.core.runtime.service.agent.RunState;
import goal.preferences.CorePreferences;
import goal.tools.debugger.DebugEvent;
import goal.tools.errorhandling.Warning;
import goal.tools.test.exceptions.ConditionFailed;
import goal.tools.test.exceptions.EvaluationFailed;
import goal.tools.test.executors.ModuleTestExecutor;
import goal.tools.test.executors.TestActionExecutor;
import goal.tools.test.executors.TestObserver;
import goal.tools.test.result.TestResultFormatter;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.parser.SourceInfo;
import languageTools.program.actionspec.UserSpecAction;
import languageTools.program.agent.Module.FocusMethod;
import languageTools.program.agent.actions.Action;
import languageTools.program.agent.actions.ActionCombo;
import languageTools.program.agent.msc.MentalStateCondition;
import languageTools.program.test.TestMentalStateCondition;
import languageTools.program.test.testcondition.Always;
import languageTools.program.test.testcondition.Eventually;
import languageTools.program.test.testcondition.Never;
import languageTools.program.test.testcondition.TestCondition;
import languageTools.program.test.testcondition.Until;
import mentalState.MentalStateWithEvents;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;
import mentalState.executors.MentalStateConditionExecutor;

/**
 * Abstract base for any test condition. Test conditions are evaluated in the
 * context of a running agent and need to provide an evaluator that can do so.
 */
public abstract class TestConditionExecutor {
	/**
	 * A test condition can have one out of three evaluations:
	 * {@link TestConditionEvaluation#PASSED},
	 * {@link TestConditionEvaluation#FAILED}, or
	 * {@link TestConditionEvaluation#UNKNOWN}.
	 */
	public enum TestConditionEvaluation {
	/**
	 * PASSED means that the test condition has been passed (holds).
	 */
	PASSED,
	/**
	 * FAILED means that the test condition failed during the test (did not hold).
	 */
	FAILED,
	/**
	 * UNKNOWN means that the test has not yet been completed and the test condition
	 * has not yet been completely evaluated.
	 */
	UNKNOWN;

		@Override
		public String toString() {
			switch (this) {
			case FAILED:
				return "failed";
			case PASSED:
				return "passed";
			case UNKNOWN:
				// if we don't know whether test condition was passed or not,
				// this means the test must have been interrupted.
				return "interrupted";
			default:
				// should never happen...
				return "unknown";
			}
		}
	}

	public enum TestEvaluationChannel {
		MODULE_ENTRY, MODULE_EXIT, ACTION_EXECUTED, STOPTEST;

		public static TestEvaluationChannel fromDebugChannel(Channel debug) {
			switch (debug) {
			case ACTION_EXECUTED_BUILTIN:
			case ACTION_EXECUTED_MESSAGING:
			case ACTION_EXECUTED_USERSPEC:
				return ACTION_EXECUTED;
			case MODULE_ENTRY:
				return MODULE_ENTRY;
			case MODULE_EXIT:
				return MODULE_EXIT;
			default:
				return STOPTEST;
			}
		}
	}

	private TestConditionEvaluation passed = TestConditionEvaluation.UNKNOWN;
	private ConditionFailed failure = null;
	protected final Substitution substitution;
	protected final RunState runstate;
	protected final TestObserver parent;
	protected final ExecutionEventGeneratorInterface noevents = new NoEventGenerator();
	protected DebugEvent current;

	public TestConditionExecutor(Substitution substitution, RunState runstate, TestObserver parent) {
		this.substitution = substitution;
		this.runstate = runstate;
		this.parent = parent;
	}

	public String getContext() {
		if (this.parent instanceof ModuleTestExecutor) {
			ModuleTestExecutor executor = (ModuleTestExecutor) this.parent;
			return (executor.getTest() == null) ? "unknown module"
					: ("module '" + executor.getTest().getModuleSignature() + "'");
		} else if (this.parent instanceof TestActionExecutor) {
			TestActionExecutor executor = (TestActionExecutor) this.parent;
			return (executor.getAction() == null) ? "unknown action" : ("action '" + executor.getAction() + "'");
		} else {
			return "";
		}
	}

	public Substitution getSubstitution() {
		return this.substitution;
	}

	/**
	 * Get the parsed {@link TestCondition}.
	 *
	 * @return {@link TestCondition}
	 */
	abstract public TestCondition getCondition();

	/**
	 * Evaluates a mental state query on the agent's {@link RunState}.
	 *
	 * @param runstate     of the agent.
	 * @param substitution the current substitution.
	 * @param query        the mental state query.
	 * @return result of evaluating the mental state query.
	 */
	protected Set<Substitution> evaluate() {
		TestMentalStateCondition testquery = getCondition().getQuery();

		Substitution temp = this.substitution.clone();
		Action<?> prev = this.runstate.getLastAction();
		if (prev == null) {
			prev = new UserSpecAction("", new ArrayList<Term>(0), false, null, null, null, null);
		}

		Set<Substitution> result = new LinkedHashSet<>();
		if (testquery.isActionFirst()) {
			ActionCombo actions = testquery.getAction().getAction();
			for (Action<?> action : actions.getActions()) {
				Substitution check = action.applySubst(temp).mgu(prev, this.runstate.getKRI());
				if (testquery.getAction().isPositive()) {
					if (check == null) {
						return new LinkedHashSet<>(0);
					} else {
						temp = temp.combine(check);
					}
				} else if (check != null) {
					return new LinkedHashSet<>(0);
				}
				if (testquery.getCondition() == null) {
					result.add(check);
					return result;
				}
			}
		}
		if (testquery.getCondition() != null) {
			MentalStateWithEvents mentalState = this.runstate.getMentalState();
			try {
				MentalStateCondition msc = testquery.getCondition();
				result = new MentalStateConditionExecutor(msc, temp)
						.evaluate(mentalState, FocusMethod.NONE, this.noevents).getAnswers();
			} catch (MSTQueryException | MSTDatabaseException e) {
				new Warning("testcondition evaluation of '" + testquery + "' failed.", e).emit();
			}
			if (!result.isEmpty() && testquery.getAction() != null) {
				Substitution[] copy = result.toArray(new Substitution[result.size()]);
				result = new LinkedHashSet<>(result.size());
				for (Substitution sub : copy) {
					ActionCombo actions = testquery.getAction().getAction();
					for (Action<?> action : actions.getActions()) {
						Substitution check = action.applySubst(sub).mgu(prev, this.runstate.getKRI());
						if ((testquery.getAction().isPositive() && check != null)
								|| (!testquery.getAction().isPositive() && check == null)) {
							result.add(sub);
						}
					}
				}
			}
		}

		return result;
	}

	public void evaluate(DebugEvent event) {
		if (this.passed == TestConditionEvaluation.UNKNOWN) {
			this.current = event;
			TestEvaluationChannel channel = (event == null || event.getChannel() == null)
					? TestEvaluationChannel.STOPTEST
					: TestEvaluationChannel.fromDebugChannel(event.getChannel());
			try {
				// calls setPassed when applicable
				evaluate(channel, (event == null) ? null : event.getAssociatedSource());
			} catch (ConditionFailed failure) {
				this.failure = failure;
				if (CorePreferences.getAbortOnTestFailure()) {
					throw failure;
				} else {
					EvaluationFailed exception = new EvaluationFailed(
							"the test condition '" + getCondition() + "' failed.", failure);
					this.runstate.getEventGenerator().event(Channel.TESTFAILURE, null, null,
							exception.accept(new TestResultFormatter()));
				}
			}
		}
	}

	abstract void evaluate(TestEvaluationChannel channel, SourceInfo info);

	/**
	 * Use this method for setting evaluation of test condition to either
	 * {@link TestConditionEvaluation#PASSED} or
	 * {@link TestConditionEvaluation#FAILED}.
	 *
	 * @param passed {@code true} to set evaluation to
	 *               {@link TestConditionEvaluation#PASSED}; {@code false} to set
	 *               evaluation to {@link TestConditionEvaluation#FAILED}.
	 */
	protected void setPassed(boolean passed) {
		if (passed) {
			this.passed = TestConditionEvaluation.PASSED;
			this.parent.remove(this);
		} else {
			this.passed = TestConditionEvaluation.FAILED;
		}
	}

	/**
	 * @return true iff the test condition is passed
	 */
	public boolean isPassed() {
		return this.passed == TestConditionEvaluation.PASSED;
	}

	public TestConditionEvaluation getState() {
		return this.passed;
	}

	public ConditionFailed getFailure() {
		return this.failure;
	}

	public static TestConditionExecutor getTestConditionExecutor(TestCondition condition, Substitution substitution,
			RunState runstate, TestObserver parent) {
		if (condition instanceof Always) {
			return new AlwaysExecutor((Always) condition, substitution, runstate, (ModuleTestExecutor) parent);
		} else if (condition instanceof Eventually) {
			return new EventuallyExecutor((Eventually) condition, substitution, runstate, (ModuleTestExecutor) parent);
		} else if (condition instanceof Never) {
			return new NeverExecutor((Never) condition, substitution, runstate, (ModuleTestExecutor) parent);
		} else if (condition instanceof Until) {
			return new UntilExecutor((Until) condition, substitution, runstate, (TestActionExecutor) parent);
		} else {
			return null;
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = getClass().hashCode();
		result = prime * result + ((getContext() == null) ? 0 : getContext().hashCode());
		result = prime * result + ((getCondition() == null) ? 0 : getCondition().hashCode());
		result = prime * result + ((this.substitution == null) ? 0 : this.substitution.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		TestConditionExecutor other = (TestConditionExecutor) obj;
		if (getContext() == null) {
			if (other.getContext() != null) {
				return false;
			}
		} else if (!getContext().equals(other.getContext())) {
			return false;
		}
		if (getCondition() == null) {
			if (other.getCondition() != null) {
				return false;
			}
		} else if (!getCondition().equals(other.getCondition())) {
			return false;
		}
		if (this.substitution == null) {
			if (other.getSubstitution() != null) {
				return false;
			}
		} else if (!this.substitution.equals(other.getSubstitution())) {
			return false;
		}
		return true;
	}
}
