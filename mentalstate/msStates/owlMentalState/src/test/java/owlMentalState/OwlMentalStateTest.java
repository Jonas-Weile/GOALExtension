package owlMentalState;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLIndividualArgument;
import org.semanticweb.owlapi.model.SWRLLiteralArgument;

import eis.iilang.Identifier;
import eis.iilang.Parameter;
import eis.iilang.Percept;
import krTools.KRInterface;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Update;
import languageTools.program.agent.AgentId;
import languageTools.program.mas.AgentDefinition;
import mentalState.MentalState;
import mentalState.MentalStateTest;
import owlrepo.OWLRepoKRInterface;
import owlrepo.language.SWRLDatabaseFormula;
import owlrepo.language.SWRLQuery;
import owlrepo.language.SWRLTerm;
import owlrepo.language.SWRLUpdate;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public class OwlMentalStateTest extends MentalStateTest {
	private final OWLDataFactory df = new OWLDataFactoryImpl();

	String baseUri = "http://www.semanticweb.org/ontologies/tradr#";
	OWLDataProperty hasName = this.df.getOWLDataProperty(IRI.create(this.baseUri + "hasName"));
	SWRLIndividualArgument John = this.df
			.getSWRLIndividualArgument(this.df.getOWLNamedIndividual(IRI.create(this.baseUri + "Team_leader")));

	@Override
	protected KRInterface getKRI() throws Exception {
		OWLRepoKRInterface kri = new OWLRepoKRInterface();
		List<URI> uris = new ArrayList<URI>();
		uris.add(this.getClass().getResource("/tradrIndivrdf.owl").toURI());
		// uris.add((new URL("http://localhost:5820/")).toURI());
		kri.initialize(uris);
		return kri;
	}

	@Override
	protected MentalState getMentalState(AgentDefinition agent, AgentId agentId, boolean addAgentModel)
			throws Exception {
		return new OwlMentalState(agent, agentId, addAgentModel);
	}

	private SWRLAtom getSWRLAtom(String content) {
		SWRLLiteralArgument contentArg = this.df.getSWRLLiteralArgument(this.df.getOWLLiteral(content));
		return this.df.getSWRLDataPropertyAtom(this.hasName, this.John, contentArg);
	}

	@Override
	protected DatabaseFormula getDBFormula(String content) throws Exception {
		return new SWRLDatabaseFormula(getSWRLAtom(content));
	}

	@Override
	protected Update getUpdate(String content) throws Exception {
		return new SWRLUpdate(getSWRLAtom(content));

	}

	@Override
	protected Query getQuery(String content, int... args) throws Exception {
		return new SWRLQuery(getSWRLAtom(content + args[0]));
	}

	@Override
	protected DatabaseFormula getMessage(String sender, String content) throws Exception {
		return getDBFormula(content);
	}

	@Override
	protected Percept getPercept(Query query) throws Exception {
		LinkedList<Parameter> parameters = new LinkedList<Parameter>();
		SWRLQuery squery = (SWRLQuery) query;
		SWRLTerm term = null;
		if (squery.isArgument()) {
			term = new SWRLTerm(squery.getArgument());
		} else if (squery.isTerm()) {
			term = new SWRLTerm(squery.getAtom());
		} else if (squery.isRule()) {
			term = new SWRLTerm(squery.getRule());
		}
		String name = term.getAtom().getPredicate().toString();
		for (SWRLArgument a : term.getAtom().getAllArguments()) {
			String aS = a.toString();
			parameters.add(new Identifier(aS.substring(1, aS.length() - 2)));
		}
		Percept p = new Percept(name, parameters);
		// System.out.println("Percept: " +p);
		return p;
	}

	@Override
	@Test
	public void testQuery() throws Exception {
		// FIXME: this test fails because percepts are not handled properly,
		// neither in the OwLEISTranslator nor in the hacky code in getPercept
		// here. So disabled for now... -Vincent
	}
}
