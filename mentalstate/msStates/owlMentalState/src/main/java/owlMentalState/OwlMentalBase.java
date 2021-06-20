package owlMentalState;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashSet;
import java.util.Set;

import krTools.exceptions.KRDatabaseException;
import krTools.exceptions.KRInitFailedException;
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

import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.SWRLRule;
import org.swrlapi.parser.SWRLParseException;

import owlMentalState.translator.OwlEISTranslator;
import owlrepo.OWLRepoKRInterface;
import owlrepo.database.OWLOntologyDatabase;
import owlrepo.language.SWRLDatabaseFormula;
import owlrepo.language.SWRLQuery;
import owlrepo.parser.SWRLParser;

public class OwlMentalBase extends MentalBase {

	/**
	 * The database used for storing the contents of this base.
	 */
	protected final OWLOntologyDatabase database;
	protected final String id;

	protected OwlMentalBase(MentalState owner, AgentId agentMM, BASETYPE type) throws MSTDatabaseException {
		super(owner);
		try {
			this.id = owner.getAgentId().getName() + "_" + agentMM.getName() + "_" + type.name();
		//	System.out.println("MentalBase: " + id);
			OWLRepoKRInterface kri = (OWLRepoKRInterface) owner.getOwner().getKRInterface();
		
			if (type.equals(BASETYPE.MESSAGEBASE)) {
				File ontology = File.createTempFile("agentOntology", ".owl");
				ontology.deleteOnExit();
				Files.copy(Thread.currentThread().getContextClassLoader().getResourceAsStream("agentOntology.owl"),
						ontology.toPath(), StandardCopyOption.REPLACE_EXISTING);
				this.database = kri.getDatabase(type.name(), ontology);
			} 
			else {
				this.database = (OWLOntologyDatabase) kri.getDatabase(type.name(), new HashSet<DatabaseFormula>(0));
			}
			this.owner.createdDatabase(this.database, type);
		} catch (KRDatabaseException | KRInitFailedException | IOException e) {
			throw new MSTDatabaseException(
					"Could not create a database of type " + type.name() + " for " + owner.getOwner(), e);
		}
	}

	public String getMentalBaseId() {
		return this.id;
	}

	Set<DatabaseFormula> getAllDBFormulas() throws MSTDatabaseException {
		return this.database.getAllDBFormulas(this.id);
	}

	public OwlEISTranslator getTranslator() {
		return new OwlEISTranslator(new SWRLParser(database.getSWRLOntology()));
	}

	@Override
	public void destroy() throws MSTDatabaseException {
		try {
			// System.out.println("Destroying database: "+id);
			if (this.database != null) {
				this.database.destroy(this.id);
			}
		} catch (KRDatabaseException e) {
			throw new MSTDatabaseException("Could not destroy database " + this.database.getName(), e);
		}
	}

	public void remove(OWLAxiom axiom) {
		this.database.delete(axiom);
	}

	@Override
	public Set<Substitution> query(Query formula) throws MSTQueryException {
		try {
			Query form = ((SWRLQuery) formula).addNamedGraph(this.id);
			return this.database.query(form);
		} catch (KRQueryFailedException e) {
			e.printStackTrace();
			throw new MSTQueryException(
					String.format("Failed to query %s on %s", formula.toString(), this.database.getName()), e);
		}
	}

	public Set<Substitution> query(String qString) throws MSTQueryException {
		try {
			return this.database.query(qString);
		} catch (KRQueryFailedException e) {
			e.printStackTrace();
			throw new MSTQueryException(String.format("Failed to query %s on %s", qString, this.database.getName()), e);
		}
	}

	//not used, since it is broken down to delete + insert operations
	public void queryUpdate(String qString) throws MSTQueryException {
		try {
			this.database.queryUpdate(qString);
		} catch (KRQueryFailedException e) {
			e.printStackTrace();
			throw new MSTQueryException(String.format("Failed to query %s on %s", qString, this.database.getName()), e);
		}
	}

	@Override
	public void insert(DatabaseFormula formula) throws MSTQueryException {
		try {
			DatabaseFormula form = ((SWRLDatabaseFormula) formula).addNamedGraph(this.id);
			this.database.insert(form);
		} catch (KRDatabaseException e) {
			throw new MSTQueryException(
					String.format("Failed to add formula %s to %s", formula.toString(), this.database.getName()), e);
		}
	}

	@Override
	public void delete(DatabaseFormula formula) throws MSTQueryException {
		try {
			DatabaseFormula form = ((SWRLDatabaseFormula) formula).addNamedGraph(this.id);
			this.database.delete(form);
		} catch (KRDatabaseException e) {
			throw new MSTQueryException(
					String.format("Failed to delete formula %s from %s", formula.toString(), this.database.getName()),
					e);
		}
	}

	/**
	 * Creates a swrl query from a content string Needed when trying to unify
	 * the existing contents of the message base with the actual query posed to
	 * the msg base
	 *
	 * @param content
	 * @return
	 */
	public SWRLQuery getContentTermQuery(String content) {
		SWRLQuery query = null;

		SWRLRule rule;
		try {

			rule = this.database.getSWRLOntology().createSWRLRule("queryContentRule", content);
			query = new SWRLQuery(rule);

		} catch (SWRLParseException e) {
			e.printStackTrace();
		}
		return query;
	}

}
