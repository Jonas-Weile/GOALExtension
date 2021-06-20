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

package mentalState;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import krTools.language.Substitution;
import languageTools.program.agent.msc.MentalStateCondition;

/**
 * Container for results obtained by evaluating a mental state condition.
 * <p>
 * The evaluation of a mental state condition can give two types of results:
 * <ul>
 * <li>A (possibly empty) set of answers, i.e., substitutions that instantiate
 * the condition such that it holds in a mental state, and</li>
 * <li>A (possibly empty) mapping of substitutions to single goals, where each
 * substitution when applied yields an instantiation of the condition that holds
 * in a mental state where the goal base only contains the associated goal.</li>
 * </ul>
 * </p>
 */
public class MSCResult {
	/**
	 * Mapping of goals to sets of substitutions.
	 */
	private final Map<MentalStateCondition, Set<Substitution>> focusResults = new LinkedHashMap<>();

	/**
	 * Creates a container for the results of a mental state condition evaluation.
	 */
	public MSCResult() {

	}

	/**
	 * Sets the answers obtained by evaluating the mental state condition.
	 *
	 * @param answers
	 *            The answers obtained by evaluating the mental state condition.
	 */
	public void setAnswers(Set<Substitution> answers) {
		this.focusResults.put(null, answers);
	}

	/**
	 * Get the answers obtained by evaluating the mental state condition.
	 *
	 * return The answers obtained by evaluating the mental state condition.
	 */
	public Set<Substitution> getAnswers() {
		if (this.focusResults.size() == 1) {
			return Collections.unmodifiableSet(this.focusResults.values().iterator().next());
		} else {
			Set<Substitution> answers = new LinkedHashSet<>();
			for (Set<Substitution> substitutions : this.focusResults.values()) {
				answers.addAll(substitutions);
			}
			return answers;
		}
	}

	/**
	 * @return {@code true} if the mental state condition holds, i.e., there is at
	 *         least one answer; {@code false} otherwise.
	 */
	public boolean holds() {
		return !getAnswers().isEmpty();
	}

	/**
	 * @return The number of answers obtained by evaluating the condition.
	 */
	public int nrOfAnswers() {
		return getAnswers().size();
	}

	/**
	 * Get the goals that have been focused on while evaluating the condition.
	 * <p>
	 * <b>NB:</b> The typical case actually will be that the set with {@code null}
	 * only is returned, if <i>no</i> goals were focused on while evaluating the
	 * mental state condition.
	 * </p>
	 *
	 * @return The goals focused on while evaluating the mental state condition.
	 */
	public Set<MentalStateCondition> getFocusedGoals() {
		return Collections.unmodifiableSet(this.focusResults.keySet());
	}

	/**
	 * Returns the substitutions obtained by evaluating the condition while focusing
	 * on the goal provided.
	 * <p>
	 * <b>NB:</b> The typical case will be that there is no goal that was focused
	 * on. In that case use {@code null} to obtain the substitution results.
	 * </p>
	 *
	 * @param goal
	 *            The goal that was focused on; use {@code null} if no focus was
	 *            used. See also {@link #focus()}.
	 * @return The substitutions obtained by evaluating the condition.
	 */
	public Set<Substitution> getFocusedResults(MentalStateCondition goal) {
		return this.focusResults.get(goal);
	}

	/**
	 * Adds the answers obtained by focusing on the specific goal.
	 *
	 * @param goal
	 *            The goal that should be focused on.
	 * @param answers
	 *            The answers obtained by focusing on the goal.
	 */
	public void addFocusResult(MentalStateCondition goal, Set<Substitution> answers) {
		this.focusResults.put(goal, answers);
	}

	/**
	 * Removes the answers obtained by focusing on the specified goal.
	 *
	 * @param goal
	 *            The goal used as key to remove the associated answers.
	 */
	public void removeFocusResult(MentalStateCondition goal) {
		this.focusResults.remove(goal);
	}

	/**
	 * @return {@code true} if a focus of attention was (successfully) used to
	 *         evaluate the mental state condition.
	 */
	public boolean focus() {
		return !this.focusResults.containsKey(null);
	}
}