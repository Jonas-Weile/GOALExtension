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

import java.util.Set;

import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Update;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;

/**
 * Stores a pair of some database and an {@link Update} pair that represent a
 * single goal. The database is maintained by the KR technology while the update
 * is used for purposes internal to GOAL.
 */
public abstract class SingleGoal {
	protected final MentalState owner;
	/**
	 * The single goal represented as an {@link Update}.
	 */
	protected final Update goal;

	/**
	 * @param owner
	 *            The base that the goal resides in
	 * @param goal
	 *            goal to be added. ASSUMES a goal consists of a list of
	 *            database formulas to be added only.
	 * @throws MSTDatabaseException
	 */
	protected SingleGoal(MentalState owner, Update goal) throws MSTDatabaseException {
		this.owner = owner;
		this.goal = goal;
	}

	/**
	 * @return The KR version of the goal that is represented.
	 */
	Update getGoal() {
		return this.goal;
	}

	/**
	 * Forces clean up of the database. Should be used only as a forced final
	 * attempt to clean up, never during normal run.
	 *
	 * @throws MSTDatabaseException
	 */
	abstract public void cleanUp() throws MSTDatabaseException;

	/**
	 * Evaluates a query on the goal's database.
	 *
	 * @param query
	 *            The query to evaluate
	 * @return A set that is empty when the evaluation failed, and contains the
	 *         appropriate substitutions otherwise.
	 * @throws MSTQueryException
	 */
	abstract public Set<Substitution> query(Query query) throws MSTQueryException;

	@Override
	public String toString() {
		return this.goal.toString();
	}

	@Override
	public int hashCode() {
		return ((this.goal == null) ? 0 : this.goal.hashCode());
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || !(obj instanceof SingleGoal)) {
			return false;
		}
		SingleGoal other = (SingleGoal) obj;
		if (this.goal == null) {
			if (other.goal != null) {
				return false;
			}
		} else if (!this.goal.equals(other.goal)) {
			return false;
		}
		return true;
	}
}