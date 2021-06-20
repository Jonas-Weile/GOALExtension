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

package mentalState.converter;

import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import krTools.language.DatabaseFormula;
import krTools.language.Update;
import languageTools.program.agent.Module;
import mentalState.GoalBase;
import mentalState.MentalStateWithEvents;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;

/**
 * Represents a mental state converter. The task of instances of this class is
 * to translate a {@link MentalStateInterface} object to the binary
 * representation, and back. Each instance has a single
 * {@link MentalStateInterface} object associated with it, whose contents often
 * changes.
 * <h1>Serialization</h1>
 * <p>
 * The serialization is currently made to support loading a previously learned
 * behaviour (see {@link FileLearner}) for execution of a GOAL program. A
 * de-serialized {@link GOALMentalStateConverter} is partial: {@link Module} and
 * {@link GoalBase} are not restored fully. Also, we don't save all info stored
 * here that is relevant for learning. Therefore, a de-serialized
 * {@link GOALMentalStateConverter} can not be used to resume model checking or
 * resume learning.
 * </p>
 */
public class GOALMentalStateConverter implements Serializable {
	private static final long serialVersionUID = 6591769350769988803L;

	/**
	 * The conversion universe that this converter has access to, which contains all
	 * beliefs and goals that have occurred in the mental state in the past.
	 */
	private final GOALConversionUniverse universe;

	/************************** Public methods ********************************/

	/**
	 * Creates a converter for the specified mental state.
	 */
	public GOALMentalStateConverter() {
		this.universe = new GOALConversionUniverse();
	}

	/**
	 * Returns the universe associated with this converter.
	 *
	 * @return The universe.
	 */
	public GOALConversionUniverse getUniverse() {
		return this.universe;
	}

	/**
	 * @param filter set of predicate signatures. Used to select the relevant
	 *               beliefs from the beliefbase.
	 * @return {@link GOALState} created from given mentalState beliefs and goals.
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	public GOALState translate(final MentalStateWithEvents mentalState, final Set<String> belieffilter,
			final Set<String> goalfilter) throws MSTDatabaseException, MSTQueryException {
		final Set<DatabaseFormula> beliefs = filteredBeliefs(mentalState, belieffilter);
		final Deque<GoalBase> goalBaseStack = filteredGoals(mentalState, goalfilter);
		return translate(beliefs, goalBaseStack);
	}

	/**
	 *
	 * @param mentalState  the {@link MentalStateWithEvents}
	 * @param stateidMap   a map of <state, nr> pairs, where state is a
	 *                     {@link GOALState} turned into string, and nr is a unique
	 *                     Integer ID for this state. This map can be modified
	 * @param statestrMap  a map of <state description, nr> where state description
	 *                     is a textual description of the mentalstate, and nr is a
	 *                     unique Integer ID for this state. This map can be
	 *                     modified
	 * @param belieffilter set of requested belief predicate signatures. Used to
	 *                     select the relevant beliefs from the beliefbase.
	 * @param goalfilter   set of requested goal predicate signatures. Used to
	 *                     select the relevant beliefs from the goalbase.
	 * @return the MentalState translated to a state vector string. The returned
	 *         state is also added to the list of known states with a unique ID, if
	 *         it is not already there.
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	public String getStateString(final MentalStateWithEvents mentalState, final Map<String, Integer> stateidMap,
			final Map<String, String> statestrMap, final Set<String> belieffilter, final Set<String> goalfilter)
			throws MSTDatabaseException, MSTQueryException {
		final String state = translate(mentalState, belieffilter, goalfilter).toString();
		if (!stateidMap.containsKey(state)) {
			stateidMap.put(state, stateidMap.size() + 1);
		}

		if (!statestrMap.containsKey(state)) {
			String s = "";
			final Set<DatabaseFormula> beliefs = filteredBeliefs(mentalState, belieffilter);
			final List<String> strset = new ArrayList<>(beliefs.size());
			for (final DatabaseFormula dbf : beliefs) {
				strset.add(dbf.toString());
			}
			Collections.sort(strset);
			s += strset.toString() + " ";
			for (final GoalBase base : filteredGoals(mentalState, goalfilter)) {
				s += base.getName() + ":\n";
				s += base.showContents() + "\n";
			}
			statestrMap.put(state, s);
		}

		return state;
	}

	/**
	 * Translates the current contents of {@link #mentalState} to a string
	 * representation.
	 *
	 * @param indent The margin (i.e. number of white spaces) that the string to be
	 *               constructed should adhere to.
	 * @return The string representation of {@link #mentalState}.
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	public String toString(final MentalStateWithEvents mentalState, final int indent)
			throws MSTDatabaseException, MSTQueryException {
		/* Process belief base */
		final Set<DatabaseFormula> formulas = new LinkedHashSet<>(mentalState.getBeliefs().size());
		for (final DatabaseFormula formula : mentalState.getBeliefs()) {
			formulas.add(formula);
		}

		/* Process specified indentation to whitespace */
		String tab = "";
		for (int i = 0; i < indent; i++) {
			tab += " ";
		}

		/* Build binary representation and belief base */
		String string = tab + "Bit Repres.: " + translate(mentalState).toString() + "\n" + tab + "Belief base: "
				+ formulas;

		/* Build stack of goal bases */
		int depth = 0;
		for (final GoalBase goalBase : mentalState.getAttentionStack()) {
			String goals = "";
			for (final Update goal : goalBase.getUpdates()) {
				goals += "[" + goal.toString() + "] ";
			}
			string += "\n" + tab + "Goal base " + depth + ": \"" + goalBase.getName() + "\" " + goals + "";
			depth++;
		}

		/* Return */
		return string;
	}

	/********************* support functions **********************/
	/**
	 * Translates the contents of {@link #mentalState} to a binary representation.
	 *
	 * This method is used only by model checker which also uses this class to store
	 * mental state and focus stack...
	 *
	 * @return
	 * @throws MSTQueryException
	 * @throws MSTDatabaseException
	 */
	private GOALState translate(final MentalStateWithEvents mentalState)
			throws MSTDatabaseException, MSTQueryException {
		final Set<DatabaseFormula> beliefs = mentalState.getBeliefs();
		return translate(beliefs, mentalState.getAttentionStack());
	}

	/**
	 * Returns a filtered set of beliefs whose signature matches one of those in
	 * filter.
	 *
	 * @param mentalState
	 * @param filter      A list of predicate signatures.
	 * @return all belief predicates from mentalState that are in the filter list.
	 * @throws MSTQueryException
	 * @throws MSTDatabaseException
	 */
	private static Set<DatabaseFormula> filteredBeliefs(final MentalStateWithEvents mentalState,
			final Set<String> filter) throws MSTDatabaseException, MSTQueryException {
		final Set<DatabaseFormula> beliefs = new HashSet<>(mentalState.getBeliefs());
		final Iterator<DatabaseFormula> iterator = beliefs.iterator();
		while (iterator.hasNext()) {
			if (!filter.contains(iterator.next().getSignature())) {
				iterator.remove();
			}
		}
		return beliefs;
	}

	private static Deque<GoalBase> filteredGoals(final MentalStateWithEvents mentalState, final Set<String> filter)
			throws MSTDatabaseException, MSTQueryException {
		final Deque<GoalBase> original = mentalState.getAttentionStack();
		final Deque<GoalBase> returned = new ArrayDeque<>(original.size());
		for (final GoalBase base : original) {
			final Set<Update> updates = base.getUpdates(); // this is a fresh set
			final Iterator<Update> iterator = updates.iterator();
			while (iterator.hasNext()) {
				if (!filter.contains(iterator.next().getSignature())) {
					iterator.remove();
				}
			}
			final GoalBase clone = mentalState.getOwnModel().createGoalBase(base.getName());
			clone.setGoals(updates);
			returned.add(clone);
		}
		return returned;
	}

	/**
	 * Translates the contents of the mental state to a binary representation.
	 *
	 * @return The binary representation.
	 */
	private GOALState translate(final Set<DatabaseFormula> beliefs, final Deque<GoalBase> goalBaseStack) {
		final GOALState q = new GOALState(this);

		// Convert beliefs.
		for (final DatabaseFormula formula : beliefs) {
			final GOALCE_Belief belief = new GOALCE_Belief(formula);
			final int index = this.universe.addIfNotContains(belief);
			q.set(index);
		}

		// Convert goals.
		int depth = 0;
		for (final GoalBase goalBase : goalBaseStack) {
			// Add goals per goal base on the attention stack.
			for (final Update update : goalBase.getUpdates()) {
				final GOALCE_GoalAtDepth goal = new GOALCE_GoalAtDepth(update, depth);
				final int index = this.universe.addIfNotContains(goal);
				q.set(index);
			}

			// Increment depth.
			depth++;
		}

		// Add foci. TODO Check.
		depth = 0;
		for (final GoalBase module : goalBaseStack) {
			final GOALCE_FocusAtDepth focus = new GOALCE_FocusAtDepth(module, depth);
			final int index = this.universe.addIfNotContains(focus);
			q.set(index);

			// Increment depth.
			depth++;
		}

		// Return GOALState.
		return q;
	}
}
