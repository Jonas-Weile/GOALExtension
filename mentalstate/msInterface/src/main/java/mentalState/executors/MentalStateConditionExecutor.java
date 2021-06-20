/**
 * The GOAL Mental State. Copyright (C) 2014 Koen Hindriks.
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
package mentalState.executors;

import events.ExecutionEventGeneratorInterface;
import krTools.language.Substitution;
import languageTools.program.agent.Module.FocusMethod;
import languageTools.program.agent.msc.MentalStateCondition;
import mentalState.MSCResult;
import mentalState.MentalStateWithEvents;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;

/**
 * Executor for evaluating a mental state condition.
 */
public class MentalStateConditionExecutor {

	/**
	 * The mental state condition to be evaluated.
	 */
	private final MentalStateCondition condition;
	/**
	 * Substitution for instantiating (free) variables in the mental state
	 * condition.
	 */
	private final Substitution substitution;

	/**
	 * Executor for evaluating a mental state condition.
	 *
	 * @param condition
	 *            The mental state condition to be evaluated.
	 * @param substitution
	 *            Substitution for instantiating (free) variables in the mental
	 *            state condition.
	 */
	public MentalStateConditionExecutor(MentalStateCondition condition, Substitution substitution) {
		this.condition = condition;
		this.substitution = substitution;
	}

	/**
	 * Evaluates a mental state condition using a focus method.
	 *
	 * @param mentalState
	 *            The mental state to evaluate the condition on.
	 * @param focus
	 *            the focus method.
	 * @param generator
	 *            the channel to report executed actions to .
	 * @return The {@link MentalStateConditionExecutor.MSCResult} obtained by
	 *         evaluating the condition.
	 * @throws MSTQueryException
	 *             If the evaluation failed.
	 * @throws MSTDatabaseException
	 */
	public MSCResult evaluate(MentalStateWithEvents mentalState, FocusMethod focus,
			ExecutionEventGeneratorInterface generator) throws MSTQueryException, MSTDatabaseException {
		switch (focus) {
		case FILTER:
			return mentalState.filterEvaluate(this.condition, this.substitution, generator);
		case NEW:
		case NONE:
			return mentalState.evaluate(this.condition, this.substitution, generator);
		case SELECT:
			return mentalState.focusEvaluate(this.condition, this.substitution, generator);
		default:
			throw new MSTQueryException(
					"unknown focus method '" + focus + "' for evaluating '" + this.condition + "'.");
		}
	}

}