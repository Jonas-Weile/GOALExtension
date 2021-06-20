package tuPrologMentalState;

import java.util.Set;

import krTools.exceptions.KRDatabaseException;
import krTools.exceptions.KRQueryFailedException;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Update;
import mentalState.BASETYPE;
import mentalState.GoalBase;
import mentalState.SingleGoal;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;
import tuprolog.database.PrologDatabase;

public class TuPrologGoal extends SingleGoal {
	/**
	 * The database used for storing this goal.
	 */
	private final PrologDatabase database;

	TuPrologGoal(GoalBase owner, Update goal) throws MSTDatabaseException {
		super(owner.getOwner(), goal);
		try {
			this.database = (PrologDatabase) owner.getOwner().getOwner().getKRInterface()
					.getDatabase(owner.getOwner().getAgentId() + ":" + owner.getName() + ":" + goal, goal.getAddList());
			owner.getOwner().createdDatabase(this.database, BASETYPE.GOALBASE);
		} catch (KRDatabaseException e) {
			throw new MSTDatabaseException(
					"could not create a database for '" + goal + "' of '" + owner.getOwner() + "'.", e);
		}
	}

	PrologDatabase getDatabase() {
		return this.database;
	}

	@Override
	protected void cleanUp() throws MSTDatabaseException {
		try {
			this.database.destroy();
		} catch (KRDatabaseException e) {
			throw new MSTDatabaseException("could not remove '" + this.goal + "' from the goal database.", e);
		}
	}

	@Override
	protected Set<Substitution> query(Query query) throws MSTQueryException {
		try {
			return this.database.query(query);
		} catch (KRQueryFailedException e) {
			throw new MSTQueryException("failed to evaluate '" + query + "' on '" + this.goal + "'.", e);
		}
	}
}
