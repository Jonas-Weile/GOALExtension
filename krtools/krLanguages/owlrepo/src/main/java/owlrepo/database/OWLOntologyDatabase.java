package owlrepo.database;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import krTools.database.Database;
import krTools.exceptions.KRDatabaseException;
import krTools.exceptions.KRQueryFailedException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Update;

import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.impl.ValueFactoryImpl;
import org.openrdf.query.BindingSet;
import org.openrdf.query.QueryResult;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.openrdf.repository.event.RepositoryConnectionListener;
import org.openrdf.rio.helpers.StatementCollector;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyID;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLIndividualArgument;
import org.semanticweb.owlapi.model.SWRLLiteralArgument;
import org.semanticweb.owlapi.model.SWRLPredicate;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.semanticweb.owlapi.reasoner.OWLReasoner;
import org.semanticweb.owlapi.rio.RioRenderer;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.swrlapi.builtins.arguments.SWRLBuiltInArgument;
import org.swrlapi.core.SWRLAPIFactory;
import org.swrlapi.core.SWRLAPIOWLOntology;
import org.swrlapi.sqwrl.exceptions.SQWRLException;

import owlrepo.language.SWRLDatabaseFormula;
import owlrepo.language.SWRLQuery;
import owlrepo.language.SWRLSubstitution;
import owlrepo.language.SWRLTerm;
import owlrepo.language.SWRLTranslator;

import com.complexible.common.openrdf.query.BooleanQueryResult;
import com.complexible.common.rdf.model.StardogValueFactory.RDF;
import com.google.common.base.Optional;

public class OWLOntologyDatabase implements Database {

	private boolean SHARED_MODE = false;
	private String name, baseURI;

	private OWLOntologyManager manager;
	// private OWLOntologyFactory ontofactory;
	private OWLOntology owlontology;
	private OWLDataFactory owlfactory;

	// private OWLReasonerFactory reasonerFactory;
	private OWLReasoner reasoner;

	private SWRLAPIOWLOntology swrlontology;
	// private SWRLRuleEngine ruleEngine;
	// private SQWRLQueryEngine queryEngine;
	// private SWRLAPIFactory swrlfactory;
	// private SWRLDataFactory swrldfactory;

	private RDFRepositoryDatabase localdb = null;
	private RDFRepositoryDatabase shareddb = null;
	private RepositoryConnectionListener local_listener;
	private RepositoryConnectionListener shared_listener;

	// private boolean firstInsert = true;
	private Collection<Statement> baseStm;
	// private SWRLAPIRenderer renderer;
	private DefaultPrefixManager prefixManager;

	private ValueFactory vfactory;
	
	public OWLOntologyDatabase(String name, File file)
			throws KRDatabaseException {
		this.name = name;

		// KB is shared
//		if (this.name.equals("KNOWLEDGEBASE"))
//			this.SHARED_MODE = true;

		// create owl ontology and its manager
		manager = OWLManager.createOWLOntologyManager();

		try{
			// insert ontology from file into in-memory owlontology object
			if (file != null)
				owlontology = manager.loadOntologyFromOntologyDocument(file);
			else
				owlontology = manager.createOntology(IRI.create(name));

//			System.out.println("Created owlontodb with name:" + name
//					+ " from: " + file);
		}catch (OWLOntologyCreationException ex) {
			throw new KRDatabaseException("Could not create OWL DB: "+ex.getMessage(), ex.getCause());
		}
		// reasoner = reasonerFactory.createReasoner(owlontology);
		owlfactory = manager.getOWLDataFactory();
		vfactory = new ValueFactoryImpl();

		// create prefix manager
		OWLOntologyID ontoId = owlontology.getOntologyID();
		Optional<IRI> ontoIri = ontoId.getOntologyIRI();
		IRI ontologyIri = ontoIri.get();
		this.prefixManager = new DefaultPrefixManager();
		baseURI = ontologyIri.toString() + "#";
		prefixManager.setDefaultPrefix(baseURI);

		// create swrl ontology
		swrlontology = SWRLAPIFactory
				.createOntology(owlontology, prefixManager);
		try {
			swrlontology.processOntology();
		} catch (SQWRLException e1) {
			throw new KRDatabaseException("Could not create OWL DB: "+e1.getMessage(), e1.getCause());
		}
		prefixManager.setPrefix("", baseURI);
		SWRLAPIFactory.updatePrefixManager(owlontology, prefixManager);
		swrlontology.getPrefixManager().setDefaultPrefix(baseURI);
		swrlontology.getPrefixManager().setPrefix("", baseURI);
		// renderer = SWRLAPIFactory.createSWRLAPIRenderer(swrlontology);

		StatementCollector stc = new StatementCollector();
		RioRenderer render = new RioRenderer(owlontology, stc,
				manager.getOntologyFormat(owlontology), (Resource) vfactory.createURI(baseURI));
		try {
			render.render();
			baseStm = stc.getStatements();
		} catch (IOException e) {
			e.printStackTrace();
			throw new KRDatabaseException(e.getMessage(), e.getCause());
		}



	}

	public Set<DatabaseFormula> getAllDBFormulas(String id) {	
	//	System.out.println("\nQuerying all dbfs from "+ id);
		Set<DatabaseFormula> dbfs = new HashSet<DatabaseFormula>();
		for (Statement st: getAllStatements(id))
			dbfs.add(statementToFormula(st));
		return dbfs;
	}
	

	public Set<Statement> getAllStatements(String id){
		Set<Statement> sts = new HashSet<Statement>();
		try {
			RepositoryResult<Statement> statements = this.getCurrentDb().getTriples(getResource(id));
			while (statements.hasNext()){
				Statement st = statements.next();
				sts.add(st);
			}
		} catch (RepositoryException e) {
			e.printStackTrace();
		}
		return sts;
	}
	
	
	public void setupRepo(URL repoUrl) throws KRDatabaseException {
		// set up RDF repository = triple store local + shared

		if (repoUrl != null && name.equals("BELIEFBASE")) {
			//System.out.println("Setting up remote repo "+name+" at: "+repoUrl);
			SHARED_MODE = true;
			shared_listener = new RDFRepositoryConnectionListener(this,
					"shared");

			this.shareddb = new RDFRepositoryDatabase(name, owlontology,
					baseURI, repoUrl, shared_listener);
			this.shareddb.insert(baseStm, null);
		} else {
			if (this.localdb == null) {
			//	System.out.println("Setting up local repo: "+name);
				local_listener = new RDFRepositoryConnectionListener(this,
						"local");
				this.localdb = new RDFRepositoryDatabase(name, owlontology,
						baseURI, null, local_listener);
			}
		}
	}

	public RDFRepositoryDatabase getRepo(){
		return getCurrentDb();
	}
	
	public String getbaseURI() {
		return this.baseURI;
	}

	public OWLOntologyManager getOntologyManager() {
		return this.manager;
	}

	public OWLOntology geOWLOntology() {
		return this.owlontology;
	}

	public SWRLAPIOWLOntology getSWRLOntology() {
		return this.swrlontology;
	}

	@Override
	public String getName() {
		return this.name;
	}
	
	
	@Override
	public Set<Substitution> query(Query query) throws KRQueryFailedException {
		SWRLQuery qr = (SWRLQuery) (query);
		//get and remove named graph
		String id = qr.getNamedGraph();
		qr = qr.removeNamedGraph();

		//System.out.println("\nQuerying::: " + query + " from "+ id);
		//translate
		SWRLTranslator transl = new SWRLTranslator(this.swrlontology,
				qr.getRule());
		String querySPARQL = "";
		
		if (SHARED_MODE)
			 querySPARQL = transl.translateToSPARQL();
		else
			 querySPARQL = transl.translateToSPARQL(id, baseURI);

		return query(querySPARQL);
	}
	
	
	private RDFRepositoryDatabase getCurrentDb(){
		if (SHARED_MODE && this.shareddb != null && this.shareddb.isOpen()) {
			//System.out.println("Querying shared");
			return this.shareddb;
		} else if (this.localdb != null && this.localdb.isOpen()) {
			//System.out.println("Querying local");
			return this.localdb;
		}
		return this.localdb;
	}
	
	public Set<Substitution> query(String queryString) throws KRQueryFailedException {
		Set<Substitution> qresult = new HashSet<Substitution>();
//		System.out.println("\nQUERYING:::: \n"
//				+ queryString.substring(queryString.lastIndexOf("PREFIX")));

		try {
			QueryResult rdfresult = null;
			rdfresult = getCurrentDb().query(queryString);

			if (rdfresult instanceof TupleQueryResult) {
				TupleQueryResult tupleResult = (TupleQueryResult) rdfresult;

				while (tupleResult.hasNext()) {
					BindingSet bindingSet = tupleResult.next();
					Set<String> bindings = bindingSet.getBindingNames();
					Iterator<String> it = bindings.iterator();
					SWRLSubstitution subst = new SWRLSubstitution();
					while (it.hasNext()) {
						String name = it.next();
						Value val = bindingSet.getValue(name);
						SWRLArgument valueArgument = null;

						if (val instanceof Literal) {
							valueArgument = // SWRL.constant(val.stringValue());
									owlfactory.getSWRLLiteralArgument(owlfactory
											.getOWLLiteral(val.stringValue()));
						} else if (val instanceof Resource) {
							valueArgument = // SWRL.individual(val.stringValue());
									owlfactory.getSWRLIndividualArgument(owlfactory
											.getOWLNamedIndividual(IRI.create(val
													.stringValue())));
						}
						//System.out.println(name + " : " + val);
						subst.addSWRLSubstitution(owlfactory.getSWRLVariable(IRI.create(this.baseURI+name)),
								valueArgument);
					}
					if (subst != null) {
						qresult.add(subst);
					}
				}
			} else if (rdfresult instanceof BooleanQueryResult) {
				BooleanQueryResult booleanResult = (BooleanQueryResult) rdfresult;
				boolean res;
				if (booleanResult.hasNext()) {
					res = booleanResult.next();
				//	System.out.println("RESULT::: " + res);
					if (res) //only add empty substitution if result is true
						qresult.add(new SWRLSubstitution());
				}
			}
			rdfresult.close();

		} catch (Exception e) {
			throw new KRQueryFailedException(e.getMessage());
		}

		return qresult;
	}
	
	public void queryUpdate(String queryString) throws KRQueryFailedException {
		//System.out.println("\nQUERYING:::: \n" + queryString.toString());
		try {
			getCurrentDb().update(queryString);
		} catch (Exception e) {
			throw new KRQueryFailedException(e.getMessage());
		}
		
	}

	public boolean isEntailed(Query query) {
		OWLAxiom axiom = ((SWRLQuery) query).getAxiom();
		return reasoner.isEntailed(axiom);
	}

	public void insertAll(Collection<DatabaseFormula> formulas)
			throws KRDatabaseException {
		for (DatabaseFormula formula : formulas)
			insert(formula);
	}


	@Override
	public void insert(DatabaseFormula formula) throws KRDatabaseException {
		SWRLDatabaseFormula form = (SWRLDatabaseFormula) (formula);
		//get and remove named graph
		String id = form.getNamedGraph();		
		//System.out.println("Got id "+id);
		form = form.removeNamedGraph();

		//System.out.println("\nINSERTING::: " + formula + " into "+ id);

		OWLAxiom axiom = form.getAxiom();
		manager.addAxiom(owlontology, axiom);
		Resource res = getResource(id);
		Collection<Statement> statements = formulaToStatements(formula, res);
		
//		for (Statement st : statements)
//			System.out.println("ST: "+st);
		
		insert(statements, res);
	}
	
	private Resource getResource(String id){
		if (id.startsWith("http"))
			return vfactory.createURI(id);
		if (id == null)
			return (Resource)null;
		return vfactory.createURI("http://ii.tudelft.nl/goal#"+id);
	}
	
	public DatabaseFormula statementsToRuleFormula(List<Statement> st) {
		Set<SWRLAtom> body = new HashSet<SWRLAtom>();
		for (Statement s : st) {
			body.add(statementToAtom(s));
		}
		SWRLRule r = this.owlfactory.getSWRLRule(body, new HashSet<SWRLAtom>());
		return new SWRLDatabaseFormula(r);
	}

	private DatabaseFormula statementToFormula(Statement st){
		SWRLDatabaseFormula dbf = null;
		SWRLAtom atom = statementToAtom(st);
		// System.out.println("ST to DBF: "+st + " = "+atom);
		dbf = new SWRLDatabaseFormula(atom);
		// System.out.println("Got formula: "+dbf);
		return dbf;
	}

	private SWRLAtom statementToAtom(Statement st) {
		SWRLAtom atom = null;
		Resource subj = st.getSubject();
		IRI subjIri = IRI.create(subj.stringValue());
		URI pred = st.getPredicate();
		IRI predIri = IRI.create(pred.stringValue());
		Value obj = st.getObject();
		
		if (pred.equals(RDF.TYPE)){
			//class atom
			atom = this.owlfactory.getSWRLClassAtom(owlfactory.getOWLClass(IRI.create(obj.stringValue())), 
					owlfactory.getSWRLIndividualArgument(owlfactory.getOWLNamedIndividual(subjIri)));
		}else if (obj instanceof URI){
			//object prop
			atom = this.owlfactory.getSWRLObjectPropertyAtom(owlfactory.getOWLObjectProperty(predIri),
					owlfactory.getSWRLIndividualArgument(owlfactory.getOWLNamedIndividual(subjIri)), 
					owlfactory.getSWRLIndividualArgument(owlfactory.getOWLNamedIndividual(IRI.create(obj.stringValue()))));
		}else if (obj instanceof Literal){
			//data prop
			atom = this.owlfactory.getSWRLDataPropertyAtom(owlfactory.getOWLDataProperty(predIri),
					owlfactory.getSWRLIndividualArgument(owlfactory.getOWLNamedIndividual(subjIri)), 
					owlfactory.getSWRLLiteralArgument(owlfactory.getOWLLiteral(obj.stringValue())));
		}
		return atom;
	}

	private Collection<Statement> formulaToStatements(DatabaseFormula formula, Resource resource) throws KRDatabaseException{
		Collection<Statement> statements = new HashSet<Statement>();
		SWRLDatabaseFormula form = (SWRLDatabaseFormula) (formula);
		if( form.isArgument()){
			return statements; //cannot insert argument without predicate
		} else if (form.isTerm()) {
			statements.add(createStatement(form.getAtom(), resource));
		} else if (form.isRule()) {
			SWRLRule rule = form.getRule();
			if (rule.getHead().isEmpty()) {
				// we only have body, which is a conjunction of atoms
				for (SWRLAtom atom : rule.getBody()) {
					statements.add(createStatement(atom, resource));
				}
			} else {
				try {

					String ruletext = form.toString();
					// System.out.println("Inserting to db: " + ruletext);

					manager.addAxiom(owlontology, rule);
					swrlontology.processOntology();
					// unfortunately the only way to insert a rule is by
					// creating it
					// letting swrl parse it from text representation
					rule = swrlontology.createSWRLRule("rulename", ruletext);

					// we render the full ontology
					StatementCollector stc = new StatementCollector();
					RioRenderer render = new RioRenderer(owlontology, stc,
							manager.getOntologyFormat(owlontology),
							(Resource) null);
					render.render();
					statements.addAll(stc.getStatements());
					// and remove the already existing statements
					statements.removeAll(baseStm);
				} catch (Exception e) {
					e.printStackTrace();
					throw new KRDatabaseException(e.getMessage());
				}
			}
		}
		return statements;
	}

	private Statement createStatement(SWRLAtom atom, Resource resource) throws KRDatabaseException {
		Resource subj = null;
		URI pred = null;
		Value obj = null;
		ValueFactory vf = getCurrentDb().getValueFactory();

		//if it has no variables // cannot insert smth with variables
		Collection<SWRLArgument> args = atom.getAllArguments();
		SWRLPredicate predt = atom.getPredicate();

		// unary
		if (args.size() == 1) {
			String subjstring = args.iterator().next().toString();
			subj = vf
					.createURI(subjstring.substring(1, subjstring.length() - 1));
			pred = vf
					.createURI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type");
			String predstring = predt.toString();
			if (predstring.startsWith("<"))
				predstring = predstring.substring(1, predstring.length() - 1);
			else if (predstring.startsWith("owl:"))
				predstring = "http://www.w3.org/2002/07/owl#"+predstring.substring(4);
			obj = vf.createURI(predstring);
		}
		// binary
		else if (args.size() == 2) {
			Iterator<SWRLArgument> it = args.iterator();
			// subject
			String subjstring = it.next().toString();
			subj = vf
					.createURI(subjstring.substring(1, subjstring.length() - 1));
			// predicate
			String predstring = predt.toString();
			pred = vf
					.createURI(predstring.substring(1, predstring.length() - 1));
			// object
			SWRLArgument objArg = it.next();
			if (objArg instanceof SWRLVariable){
				SWRLVariable objv = (SWRLVariable)objArg;
				obj = vf.createURI(objv.getIRI().toString());
			}else if (objArg instanceof SWRLIndividualArgument){
				SWRLIndividualArgument objI = (SWRLIndividualArgument)objArg;
				obj = vf.createURI(objI.getIndividual().toStringID());
			}else if (objArg instanceof SWRLLiteralArgument){
				SWRLLiteralArgument objL = (SWRLLiteralArgument) objArg;
				obj = vf.createLiteral(objL.getLiteral().getLiteral());
			}else if (objArg instanceof SWRLBuiltInArgument){
				SWRLBuiltInArgument objB = (SWRLBuiltInArgument)objArg;
				//TODO:test
				obj = vf.createURI(objB.getBoundVariableName());
			}else
				obj = vf.createBNode();
		}

		//System.out.println("TRIPLE: "+subj + ", "+pred + ", "+obj);
		return  vf.createStatement(subj, pred, obj, resource);
	}

	public DatabaseFormula getDBFormula(Term term) throws KRDatabaseException {
		SWRLTerm swt = (SWRLTerm) term;
		Set<SWRLAtom> body = new HashSet<SWRLAtom>();
		Set<SWRLAtom> head = new HashSet<SWRLAtom>();
		body.add(swt.getAtom());
		SWRLRule rule = owlfactory.getSWRLRule(body, head);
		return new SWRLDatabaseFormula(rule);
	}

	public void insert(Collection<Statement> statements, Resource resources) {
		if (SHARED_MODE){
			insertShared(statements, null);
		}else
			insertLocal(statements, resources);
	}

	private void insertLocal(Collection<Statement> statements, Resource resources) {
		// System.out.println("Inserting into local");
		if (localdb != null && localdb.isOpen())
			localdb.insert(statements, resources);
	}
	

	private void insertShared(Collection<Statement> statements, Resource resources) {
		// System.out.println("Inserting into shared");
		if (shareddb != null && shareddb.isOpen())
			shareddb.insert(statements, resources);
	}
	

	public void delete(Collection<Statement> statements, Resource resources) {
		if (SHARED_MODE){
			deleteShared(statements, resources);
		}else
			deleteLocal(statements, resources);
	}
	
	private void deleteLocal(Collection<Statement> statements, Resource resources) {
		// System.out.println("Inserting into local");
		if (localdb != null && localdb.isOpen())
			localdb.delete(statements, resources);
	}

	private void deleteShared(Collection<Statement> statements, Resource resources) {
		//System.out.println("Deleting from shared");
//		for (Statement s : statements)
//			System.out.println("S: " + s);
		if (shareddb != null && shareddb.isOpen())
			shareddb.delete(statements, (Resource) null);
	}

	@Override
	public void insert(Update update) throws KRDatabaseException {
		for (DatabaseFormula formula : update.getDeleteList()) {
			delete(formula);
		}
		for (DatabaseFormula formula : update.getAddList()) {
			insert(formula);
		}
	}

//	public void updateDB() {
//		if (SHARED_MODE) {
//			try {
//				RepositoryResult<Statement> res = shareddb.getTriples();
//				StatementCollector stc = new StatementCollector(res.asList());
//				localdb.insert(stc.getStatements());
//			} catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//	}

	@Override
	public void delete(DatabaseFormula formula) throws KRDatabaseException {
		SWRLDatabaseFormula form = (SWRLDatabaseFormula) (formula);
		OWLAxiom axiom = form.getAxiom();
		manager.removeAxiom(owlontology, axiom);
		
		//get and remove named graph
		String id = form.getNamedGraph();
		//System.out.println("Got id "+id);
		form = form.removeNamedGraph();
		Resource res = getResource(id);
		Collection<Statement> statements = formulaToStatements(formula, res);
		// for (Statement st : statements)
	//	System.out.println("\nDELETING::: " + formula + " from " + id);
		delete(statements, res);
	}

	@Override
	public void delete(Update update) throws KRDatabaseException {
		for (DatabaseFormula formula : update.getAddList()) {
			delete(formula);
		}
		for (DatabaseFormula formula : update.getDeleteList()) {
			insert(formula);
		}
	}

	public void delete(OWLAxiom axiom) {
		manager.removeAxiom(owlontology, axiom);
	}

	public boolean isOpen(){
		if (localdb != null)
		return localdb.isOpen();
		return false;
	}

	public void destroy(String id) throws KRDatabaseException {
	//delete everything that belongs to named graph id
		delete(getAllStatements(id), getResource(id));
	}
	
	@Override
	public void destroy() throws KRDatabaseException {
		manager.removeOntology(owlontology);
		if (localdb != null)
			localdb.shutdown();
		//		if (server!=null)
		//			server.stop();
	}

}
