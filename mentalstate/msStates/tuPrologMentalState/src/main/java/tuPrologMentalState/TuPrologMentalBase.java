package tuPrologMentalState;

import java.util.Set;

import krTools.exceptions.KRDatabaseException;
import krTools.exceptions.KRQueryFailedException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import languageTools.program.agent.AgentId;
import mentalState.BASETYPE;
import mentalState.MentalBase;
import mentalState.MentalState;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;
import tuprolog.database.PrologDatabase;

public class TuPrologMentalBase extends MentalBase {
	/**
	 * The database used for storing the contents of this base.
	 */
	private final PrologDatabase database;

	protected TuPrologMentalBase(MentalState owner, AgentId forAgent, BASETYPE type) throws MSTDatabaseException {
		super(owner);
		try {
			this.database = (PrologDatabase) owner.getOwner().getKRInterface()
					.getDatabase(owner.getAgentId() + ":" + forAgent + ":" + type, null);
			this.owner.createdDatabase(this.database, type);
		} catch (KRDatabaseException e) {
			throw new MSTDatabaseException("could not create a database for '" + owner.getOwner() + "'.", e);
		}
	}

	PrologDatabase getDatabase() {
		return this.database;
	}

	@Override
	public void destroy() throws MSTDatabaseException {
		try {
			this.database.destroy();
		} catch (KRDatabaseException e) {
			throw new MSTDatabaseException("could not destroy '" + this.database.getName() + "'.", e);
		}
	}

	@Override
	public Set<Substitution> query(Query formula) throws MSTQueryException {
		try {
			return this.database.query(formula);
		} catch (KRQueryFailedException e) {
			throw new MSTQueryException(
					String.format("failed to query '%s' on '%s'.", formula.toString(), this.database.getName()), e);
		}
	}

	@Override
	public void insert(DatabaseFormula formula) throws MSTQueryException {
		try {
			this.database.insert(formula);
		} catch (KRDatabaseException e) {
			throw new MSTQueryException(
					String.format("failed to add '%s' to '%s'.", formula.toString(), this.database.getName()), e);
		}
	}

	@Override
	public void delete(DatabaseFormula formula) throws MSTQueryException {
		try {
			this.database.delete(formula);
		} catch (KRDatabaseException e) {
			throw new MSTQueryException(
					String.format("failed to delete '%s' from '%s'.", formula.toString(), this.database.getName()), e);
		}
	}
}
