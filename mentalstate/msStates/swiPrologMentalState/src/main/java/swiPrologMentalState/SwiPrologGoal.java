package swiPrologMentalState;

import java.util.Set;

import krTools.exceptions.KRDatabaseException;
import krTools.exceptions.KRQueryFailedException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Update;
import mentalState.BASETYPE;
import mentalState.GoalBase;
import mentalState.SingleGoal;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;
import swiprolog.database.PrologDatabase;

public class SwiPrologGoal extends SingleGoal {
	/**
	 * The database used for storing this goal.
	 */
	private final PrologDatabase database;

	SwiPrologGoal(final GoalBase base, final int count, final Update goal) throws MSTDatabaseException {
		super(base.getOwner(), goal);
		try { // we need a separate module each time because we cannot insert conjunctions
			this.database = (PrologDatabase) this.owner.getOwner().getKRInterface()
					.getDatabase(this.owner.getAgentId().toString(), base.getName() + count);
			this.owner.createdDatabase(this.database, BASETYPE.GOALBASE); // adds the required knowledge
			for (final DatabaseFormula dbf : goal.getAddList()) { // cannot use toDBF here
				this.database.insert(dbf);
			}
		} catch (final KRDatabaseException e) {
			throw new MSTDatabaseException(
					"could not create a database for '" + goal + "' of '" + this.owner.getOwner() + "'.", e);
		}
	}

	PrologDatabase getDatabase() {
		return this.database;
	}

	@Override
	public void cleanUp() throws MSTDatabaseException {
		try {
			this.database.destroy();
		} catch (final KRDatabaseException e) {
			throw new MSTDatabaseException("could not remove '" + this.goal + "' from the goal database.", e);
		}
	}

	@Override
	public Set<Substitution> query(final Query query) throws MSTQueryException {
		try {
			return this.database.query(query);
		} catch (final KRQueryFailedException e) {
			e.printStackTrace();
			throw new MSTQueryException("failed to evaluate '" + query + "' on '" + this.goal + "'.", e);
		}
	}
}
