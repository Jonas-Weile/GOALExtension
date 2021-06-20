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

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Update;
import mentalState.converter.GOALMentalStateConverter;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;

/**
 * <p>
 * A goal base consists of a set of individual or single goals. Each individual
 * goal is represented as a single goal that consists of an update It should
 * correspond with the database, except possibly for background knowledge
 * present in the database which is not reflected in the theory. In order to
 * ensure the theory corresponds correctly in this sense with the database, any
 * changes to the belief base need to be made by using the methods provided by
 * this class. By directly modifying the underlying database, the correspondence
 * may be lost. Background knowledge added to the database is assumed to be
 * static. Additionally, the predicates declared/defined in the goal base's
 * theory and in background theories should not overlap (this will raise
 * exceptions when inserting the background knowledge into a Prolog database).
 *
 * </p>
 * <p>
 * Note that each goal of the agent is implemented as a separate database.
 *
 * </p>
 * <p>
 * The notification of changes works as follows. GoalBases representing modules
 * notify their parent module. This notification chain is created in
 * {@link MentalModel#focus}. Therefore, listeners for changes in goalbases can
 * observe the topmost goalbase and then also will be notified of submodule
 * changes.
 * </p>
 * FIXME: some methods are public because the converter package uses them, but
 * they should'nt be.
 */
public abstract class GoalBase implements Iterable<SingleGoal> {
	/**
	 * The contents of this goalbase.
	 */
	private final Set<SingleGoal> goals = new LinkedHashSet<>();
	/**
	 * Needed for the creation of {@link SingleGoal}s.
	 */
	protected final MentalState owner;
	/**
	 * The name of this goal base; used to link goal base to attention set
	 * associated with a particular module. The top level goal base is linked to the
	 * "main" module.
	 */
	protected final String name;

	/**
	 * Creates a new goalbase.
	 *
	 * <p>
	 * The knowledge, beliefs, messages, and percepts of an agent are stored in
	 * {@link MentalBase}s. Goals are handled by {@link GoalBase}s.
	 * </p>
	 *
	 * @param owner The mental state that is creating this base.
	 * @param name  The name of the base.
	 */
	protected GoalBase(final MentalState owner, final String name) {
		this.owner = owner;
		this.name = name;
	}

	/**
	 * @param goal The update representing the goal.
	 * @return A single goal for the given update.
	 * @throws MSTDatabaseException
	 */
	abstract protected SingleGoal createGoal(Update goal) throws MSTDatabaseException;

	/**
	 * The name of this goal base, linking it to a module that has a particular
	 * attention set. Used by the converter package...
	 *
	 * @return The name of this goal base
	 */
	public String getName() {
		return this.name;
	}

	public MentalState getOwner() {
		return this.owner;
	}

	/**
	 * @return the set of goals present in this {@link GoalBase}, represented as a
	 *         set of {@link SingleGoal}s.
	 */
	Set<SingleGoal> getGoals() {
		return this.goals;
	}

	/**
	 * @return All goals in the current attention stack. Used by the converter
	 *         package...
	 */
	public Set<Update> getUpdates() {
		final Set<Update> goals = new LinkedHashSet<>(this.goals.size());
		for (final SingleGoal singleGoal : this.goals) {
			goals.add(singleGoal.getGoal());
		}
		return goals;
	}

	/**
	 * Adds the content provided as a list of {@Update}s as goals to this
	 * {@link GoalBase}.
	 *
	 * @param content The content to be added to this goal base.
	 * @throws MSTDatabaseException
	 */
	public void setGoals(final Set<Update> content) throws MSTDatabaseException {
		for (final Update goal : content) {
			this.goals.add(createGoal(goal));
		}
	}

	/**
	 * @return <code>true</code> iff no goals are present in this goal base.
	 */
	boolean isEmpty() {
		return this.goals.isEmpty();
	}

	@Override
	public Iterator<SingleGoal> iterator() {
		return this.goals.iterator();
	}

	public int getCount() {
		return this.goals.size();
	}

	// *********** query methods ****************/

	/**
	 * Performs a query on the goal base by checking whether the query follows from
	 * a single goal stored in one of the databases associated with the goal base.
	 * The substitutions computed are such that when applied to the query the query
	 * follows from one of the goals. Note that knowledge is part of these
	 * databases, thus a query follows from a goal in combination with the knowledge
	 * the agent has.
	 *
	 * @param query The query.
	 *
	 * @return a (possibly empty) set of substitutions each of which make the query
	 *         succeed.
	 * @throws MSTQueryException if the query fails to be executed.
	 */
	public Set<Substitution> query(final Query query) throws MSTQueryException {
		if (getCount() == 1) {
			return this.goals.iterator().next().query(query);
		} else {
			final Set<Substitution> substitutions = new LinkedHashSet<>();
			for (final SingleGoal goal : this.goals) {
				substitutions.addAll(goal.query(query));
			}
			return substitutions;
		}
	}

	// *********** insertion methods ****************/

	/**
	 * Inserts a new goal into the goal base. Checks whether the formula (update)
	 * already occurs in the goal base.
	 *
	 * Public as used by the {@link GOALMentalStateConverter}...
	 *
	 * @param update The update to be inserted.
	 * @return true if anything changed.
	 * @throws MSTDatabaseException
	 */
	public Result insert(final Update update) throws MSTDatabaseException {
		final Result result = this.owner.createResult(BASETYPE.GOALBASE, getName());
		final SingleGoal goal = createGoal(update);
		if (this.goals.add(goal)) {
			result.added(update.toDBF());
		}
		return result;
	}

	// *********** deletion methods ****************/

	/**
	 * Drops all goals that entail the goal to be dropped.
	 *
	 * @param dropgoal goal to be dropped.
	 * @return A (possibly empty) list of goals that have been dropped.
	 * @throws MSTDatabaseException
	 */
	Result drop(final Update dropgoal) throws MSTQueryException, MSTDatabaseException {
		// Gather the actual goals matching the update
		final List<SingleGoal> goalsToBeDropped = new LinkedList<>();
		for (final SingleGoal goal : this.goals) {
			final Set<Substitution> dropCheck = goal.query(dropgoal.toQuery());
			if (!dropCheck.isEmpty()) {
				goalsToBeDropped.add(goal);
			}
		}
		// Actually remove the goals (from the collection & clean-up themselves)
		final Result result = this.owner.createResult(BASETYPE.GOALBASE, getName());
		for (final SingleGoal goal : goalsToBeDropped) {
			result.merge(remove(goal));
		}
		return result;
	}

	/**
	 * @param goal The goal to remove and clean up.
	 * @return True when the remove was successful.
	 * @throws MSTQueryException
	 * @throws MSTDatabaseException
	 */
	Result remove(final SingleGoal goal) throws MSTQueryException, MSTDatabaseException {
		final Result result = this.owner.createResult(BASETYPE.GOALBASE, getName());
		if (this.goals.remove(goal)) {
			result.removed(goal.getGoal().toDBF());
			goal.cleanUp();
		}
		return result;
	}

	/**
	 * Public as used by the {@link GOALMentalStateConverter}...
	 *
	 * @param goal The goal to remove
	 * @return True when the remove was successful.
	 * @throws MSTQueryException
	 * @throws MSTDatabaseException
	 */
	public Result remove(final Update goal) throws MSTQueryException, MSTDatabaseException {
		for (final SingleGoal g : this.goals.toArray(new SingleGoal[this.goals.size()])) {
			if (g.getGoal().equals(goal)) {
				return remove(g);
			}
		}
		return this.owner.createResult(BASETYPE.GOALBASE, getName());
	}

	/**
	 * cleanup all databases used to store goals by calling the corresponding
	 * methods of the kr technology. Should be called when goal base is deleted.
	 * <br>
	 *
	 * @throws MSTQueryException
	 * @throws MSTDatabaseException
	 */
	Result cleanUp() throws MSTQueryException, MSTDatabaseException {
		final Result result = this.owner.createResult(BASETYPE.GOALBASE, getName());
		for (final SingleGoal goal : this.goals.toArray(new SingleGoal[this.goals.size()])) {
			result.merge(remove(goal));
		}
		return result;
	}

	// *********** helper methods ****************/

	/**
	 * Converts goal base into a string.
	 */
	@Override
	public String toString() {
		final StringBuffer text = new StringBuffer("GoalBase[");
		boolean first = true;
		for (final SingleGoal goal : this.goals) {
			if (first) {
				text.append(goal);
				first = false;
			} else {
				text.append("| ").append(goal);
			}
		}
		text.append("]");
		return text.toString();
	}

	/**
	 * Gets the goals in this {@link GoalBase} as a string representation, one goal
	 * per line. Used by export functionality and Database viewer.
	 *
	 * FIXME change name. This does not show anything, it just returns string
	 * representation.
	 *
	 * @return The goals inside this goal base, one per newline-terminated line.
	 *
	 */
	public String showContents() {
		final StringBuilder sbuild = new StringBuilder();
		for (final SingleGoal goal : this.goals) {
			sbuild.append(goal).append(".\n");
		}
		return sbuild.toString();
	}
}
