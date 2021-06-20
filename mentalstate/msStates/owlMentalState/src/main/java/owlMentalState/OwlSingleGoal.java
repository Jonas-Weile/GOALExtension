package owlMentalState;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import krTools.exceptions.KRDatabaseException;
import krTools.exceptions.KRQueryFailedException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Update;
import languageTools.program.mas.AgentDefinition;
import mentalState.BASETYPE;
import mentalState.SingleGoal;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;
import owlrepo.database.OWLOntologyDatabase;
import owlrepo.language.SWRLDatabaseFormula;
import owlrepo.language.SWRLQuery;

public class OwlSingleGoal extends SingleGoal {

	/**
	 * The database used for storing this goal.
	 */
	private final OWLOntologyDatabase database;
	private final String id;

	protected OwlSingleGoal(OwlGoalBase owner, Update goal) throws MSTDatabaseException {
		super(owner.getOwner(), goal);
		AgentDefinition agent = owner.getOwner().getOwner();
		String type = BASETYPE.GOALBASE.name();
		this.id = owner.getOwner().getAgentId() + "_" + owner.getName() + "_" + type;
		//System.out.println("Creating single goal base: " + id);
		try {
			List<DatabaseFormula> content = new LinkedList<DatabaseFormula>();
			for (DatabaseFormula dbf : goal.getAddList()) {
				//System.out.println("GOAL DBF:" + dbf);
				DatabaseFormula form = ((SWRLDatabaseFormula) dbf).addNamedGraph(this.id);
				content.add(form);
			}
			this.database = (OWLOntologyDatabase) agent.getKRInterface().getDatabase(type, content);
			owner.getOwner().createdDatabase(this.database, BASETYPE.GOALBASE);
		} catch (KRDatabaseException e) {
			throw new MSTDatabaseException(
					"Could not create a database for single goal " + goal + " of " + agent.getName(), e);
		}
	}

	public String getGoalBaseId() {
		return this.id;
	}

	OWLOntologyDatabase getDatabase() {
		return this.database;
	}

	@Override
	protected void cleanUp() throws MSTDatabaseException {
		try {
			// System.out.println("Destroying database:
			// "+id+"_"+this.database.getName());
			this.database.destroy(this.id);
		} catch (KRDatabaseException e) {
			throw new MSTDatabaseException("Could not destroy database " + this.database.getName(), e);
		}
	}

	@Override
	protected Set<Substitution> query(Query query) throws MSTQueryException {
		try {
		//	System.out.println("Querying goalbase: " + query + " of agent: "		+ id);
			Query q = ((SWRLQuery) query).addNamedGraph(this.id);
			return this.database.query(q);
		} catch (KRQueryFailedException e) {
			throw new MSTQueryException(
					String.format("Failed to query %s on %s", query.toString(), this.database.getName()), e);
		}
	}

}
