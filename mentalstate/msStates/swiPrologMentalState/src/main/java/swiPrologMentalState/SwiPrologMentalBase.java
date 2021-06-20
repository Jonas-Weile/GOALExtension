package swiPrologMentalState;

import java.util.List;
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
import mentalState.Result;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;
import swiprolog.database.PrologDatabase;

public class SwiPrologMentalBase extends MentalBase {
	/**
	 * The database used for storing the contents of this base.
	 */
	private final PrologDatabase database;

	/**
	 *
	 * @param owner    the owner (MentalState).
	 * @param forAgent the agent owning this database
	 * @param type     the type of this database. If type is BASETYPE.KNOWLEDGEBASE,
	 *                 this database will be assumed static
	 * @param dbfs     the initial database formulas (only for
	 *                 BASETYPE.KNOWLEDGEBASE
	 * @throws MSTDatabaseException
	 */
	protected SwiPrologMentalBase(final MentalState owner, final AgentId forAgent, final BASETYPE type,
			final List<DatabaseFormula> dbfs) throws MSTDatabaseException {
		super(owner, forAgent, type);
		try {
			final String ownerName = owner.getAgentId().equals(forAgent) ? forAgent.toString()
					: (owner.getAgentId() + ":" + forAgent);
			if (type == BASETYPE.KNOWLEDGEBASE) {
				this.database = (PrologDatabase) owner.getOwner().getKRInterface().getDatabase(ownerName,
						type.toString(), dbfs);
			} else {
				this.database = (PrologDatabase) owner.getOwner().getKRInterface().getDatabase(ownerName,
						type.toString());
			}
			this.owner.createdDatabase(this.database, type);
		} catch (final KRDatabaseException e) {
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
		} catch (final KRDatabaseException e) {
			throw new MSTDatabaseException("could not destroy '" + this.database.getName() + "'.", e);
		}
	}

	@Override
	public Set<Substitution> query(final Query formula) throws MSTQueryException {
		try {
			return this.database.query(formula);
		} catch (final KRQueryFailedException e) {
			throw new MSTQueryException(
					String.format("failed to query '%s' on '%s'.", formula.toString(), this.database.getName()), e);
		}
	}

	@Override
	public Result insert(final DatabaseFormula formula) throws MSTQueryException {
		try {
			final Result result = this.owner.createResult(this.type, this.forAgent.toString());
			if (this.database.insert(formula)) {
				result.added(formula);
			}
			return result;
		} catch (final KRDatabaseException e) {
			throw new MSTQueryException(
					String.format("failed to add '%s' to '%s'.", formula.toString(), this.database.getName()), e);
		}
	}

	@Override
	public Result delete(final DatabaseFormula formula) throws MSTQueryException {
		try {
			final Result result = this.owner.createResult(this.type, this.forAgent.toString());
			if (this.database.delete(formula)) {
				result.removed(formula);
			}
			return result;
		} catch (final KRDatabaseException e) {
			throw new MSTQueryException(
					String.format("failed to delete '%s' from '%s'.", formula.toString(), this.database.getName()), e);
		}
	}
}
