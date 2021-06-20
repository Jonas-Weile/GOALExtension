package owlrepo;

import java.io.File;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import krTools.KRInterface;
import krTools.database.Database;
import krTools.exceptions.KRDatabaseException;
import krTools.exceptions.KRInitFailedException;
import krTools.exceptions.ParserException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;
import krTools.parser.Parser;
import krTools.parser.SourceInfo;

import org.semanticweb.owlapi.model.SWRLRule;
import org.swrlapi.parser.SWRLParseException;

import owlrepo.database.OWLOntologyDatabase;
import owlrepo.language.SWRLQuery;
import owlrepo.language.SWRLSubstitution;
import owlrepo.parser.SWRLParser;

public class OWLRepoKRInterface implements KRInterface {
	OWLOntologyDatabase database = null;
	Map<String, OWLOntologyDatabase> databases = new HashMap<String, OWLOntologyDatabase>();
	File owlfile = null;
	URL repoUrl = null;
	SWRLParser parser = null;
	List<String> undefined = new LinkedList<String>();

	public void initialize(List<URI> uris) throws KRInitFailedException {
		for (URI uri : uris) {
		//	System.out.println("Resource: " + uri);
			if (uri.toString().startsWith("http")) {
				try {
					this.repoUrl = uri.toURL();
					System.out.println("REPO URL: "+repoUrl);
				} catch (MalformedURLException e) {
					throw new KRInitFailedException(e.getMessage());
				}
			} else if (uri.isAbsolute() && uri.getPath().endsWith("owl")) {
				// get the ontology file from the module
				this.owlfile = new File(uri);
			}
		}

		database = getDatabase("parser", this.owlfile);

	}

	public OWLOntologyDatabase getDatabase(String name, File file)
			throws KRInitFailedException {
		// create a database with the initializing ontology
		// if (database == null) {
		if (databases.containsKey(name))
			return databases.get(name);
		else {
			try {
				if (file == null) {
					// database = (OWLOntologyDatabase) getDatabase(new HashSet<DatabaseFormula>());
					throw new KRDatabaseException(
							"Creation of OWL parser needs an OWL file set to the Interface");
				} else {
					database = new OWLOntologyDatabase(name, file);
					if (!name.equals("parser")) {
						//in case it is needed for parser, do not set up real triplestores
						database.setupRepo(repoUrl);
						//put it in our database map
						databases.put(name, database);
					}
					return database;
				}
			} catch (KRDatabaseException e) {
				throw new KRInitFailedException(e.getMessage(), e.getCause());
			}
		 }
	}

	public void release() throws KRDatabaseException {
		for (String dbname : databases.keySet()){
			System.out.println("Destroying database: "+dbname);
			databases.get(dbname).destroy();
		}
		databases.clear();
	}

	public Database getDatabase(String name, Collection<DatabaseFormula> content)
			throws KRDatabaseException {
		try {
			database = getDatabase(name, this.owlfile);
			database.insertAll(content);
		} catch (KRInitFailedException e) {
			throw new KRDatabaseException(
					"Failed to create OWL Ontology Database", e);
		}
		// Return new database.
		return database;
	}

	@Override
	public Parser getParser(Reader source, SourceInfo info)
			throws ParserException {
		// BufferedReader reader = new BufferedReader(source);
		if (database == null)
			throw new ParserException(
					"OWLREPO KR Interface was not correctly initialized with an owl file!");

		// create parser for this database onto and reader source
		if (this.parser == null) {
			this.parser = new SWRLParser(database.getSWRLOntology(), source, info);
		} else
			this.parser.parse(source, info);
		return parser;
	}
	
	public SWRLRule getRule(String s) throws SWRLParseException{
		return this.database.getSWRLOntology().createSWRLRule("parse", s);
	}

	public File getOwlFile() {
		return this.owlfile;
	}

	public URL getRepoUrl() {
		return this.repoUrl;
	}

	public Substitution getSubstitution(Map<Var, Term> map) {
		return new SWRLSubstitution(map);
	}

	public Set<Query> getUndefined(Set<DatabaseFormula> dbfs, Set<Query> queries) {
		Set<Query> undef = new HashSet<Query>();
		if (this.parser != null)
			for (String s : parser.getUndefined()) {
				undef.add(new SWRLQuery(s));
				// parser.getErrors().add(
				// new SWRLParserSourceInfo(parser.getInfo().getSource(),
				// 1, -1, "Invalid SWRL atom predicate in" + s));
			}
		// System.out.println(parser.getErrors().size());
		// keep parser errors only until getundefined is called
		parser.getErrors().clear();
		parser.getUndefined().clear();
		for (Query query : queries) {
			// System.out.println("query " + query.toString());
			if (((SWRLQuery) query).isUndefined()) {
				undef.add(query);
			}
		}
		return undef;
	}

	public Set<DatabaseFormula> getUnused(Set<DatabaseFormula> dbfs,
			Set<Query> queries) {
		// TODO:check terms in db
		return new HashSet<DatabaseFormula>();
	}

	@Override
	public boolean supportsSerialization() {
		// TODO Auto-generated method stub
		return false;
	}
}
