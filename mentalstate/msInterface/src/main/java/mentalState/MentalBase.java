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

import java.util.List;
import java.util.Set;

import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Update;
import languageTools.program.agent.AgentId;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;

/**
 * A mental base is some database with generic handling and querying
 * capabilities on {@link DatabaseFormula}s.
 */
public abstract class MentalBase {
	protected final MentalState owner;
	protected final AgentId forAgent;
	protected final BASETYPE type;

	/**
	 * Creates the corresponding database maintained by the KR layer.
	 *
	 * <p>
	 * The knowledge, beliefs, messages, and percepts of an agent are stored in
	 * {@link MentalBase}s. Goals are handled by {@link GoalBase}s.
	 * </p>
	 *
	 * @param owner The mental state that is creating this base.
	 * @throws MSTDatabaseException
	 */
	protected MentalBase(final MentalState owner, final AgentId forAgent, final BASETYPE type)
			throws MSTDatabaseException {
		this.owner = owner;
		this.forAgent = forAgent;
		this.type = type;
	}

	/**
	 * Dispose all resources.
	 *
	 * @throws MSTDatabaseException
	 */
	abstract public void destroy() throws MSTDatabaseException;

	/**
	 * Performs a query on the base and returns a non-empty set of substitutions if
	 * the query succeeds.
	 *
	 * @param formula The query.
	 * @return a set of substitutions each of which make the query true, or an empty
	 *         set otherwise.
	 * @throws MSTQueryException if query fails.
	 */
	abstract public Set<Substitution> query(Query formula) throws MSTQueryException;

	/*********** updating (insertion, deletion) methods ****************/

	/**
	 * Adds a {@link DatbaseFormula} to the base.
	 *
	 * @param formula The formula to be inserted (added).
	 * @throws MSTQueryException
	 */
	abstract public Result insert(DatabaseFormula formula) throws MSTQueryException;

	/**
	 * See {@link #update(List, List)}.
	 *
	 * @param update The update to be processed.
	 * @throws MSTQueryException
	 */
	public Result insert(final Update update) throws MSTQueryException {
		return update(update.getAddList(), update.getDeleteList());
	}

	/**
	 * Removes a {@link DatbaseFormula} from the base.
	 *
	 * @param formula The formula to be deleted (removed). The debugger monitoring
	 *                the removal.
	 * @throws MSTQueryException
	 */
	abstract public Result delete(DatabaseFormula formula) throws MSTQueryException;

	/**
	 * See {@link #update(List, List)}. Deleting an {@link Update} is calling update
	 * with the add and delete lists reversed.
	 *
	 * @param update The update to be processed.
	 * @throws MSTQueryException
	 */
	public Result delete(final Update update) throws MSTQueryException {
		return update(update.getDeleteList(), update.getAddList());
	}

	/**
	 * Deletes the formulas in the delete list and adds the formulas in the add list
	 * to this base. First removes and then adds, so any formulas that appear in
	 * both lists will result in adding the formula if it is not already present.
	 *
	 * @param addList    The 'add' list of formulas that are to be inserted.
	 * @param deleteList The 'delete' list of formulas that are to be removed.
	 * @throws MSTQueryException
	 */
	private Result update(final List<DatabaseFormula> addList, final List<DatabaseFormula> deleteList)
			throws MSTQueryException {
		final Result result = this.owner.createResult(this.type, this.forAgent.toString());
		// First handle the delete list and remove formulas.
		for (final DatabaseFormula formula : deleteList) {
			result.merge(delete(formula));
		}
		// And then handle the add list and add formulas.
		for (final DatabaseFormula formula : addList) {
			result.merge(insert(formula));
		}
		return result;
	}
}
