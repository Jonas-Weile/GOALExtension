package owlrepo;

import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import krTools.exceptions.KRDatabaseException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;

import org.junit.Test;
import org.openrdf.model.Statement;
import org.openrdf.model.ValueFactory;
import org.openrdf.model.vocabulary.RDF;
import org.semanticweb.owlapi.model.SWRLRule;
import org.swrlapi.parser.SWRLParseException;
import org.swrlapi.sqwrl.SQWRLQuery;
import org.swrlapi.sqwrl.exceptions.SQWRLException;

import owlrepo.database.OWLOntologyDatabase;
import owlrepo.database.RDFRepositoryDatabase;
import owlrepo.language.SWRLDatabaseFormula;
import owlrepo.language.SWRLQuery;
import owlrepo.parser.SWRLParser;


public class OWLOntologyDatabaseTest {

	OWLOntologyDatabase db;
	File file;
	char ring ;
	String repoURL;
	
	public OWLOntologyDatabaseTest() {
		file = new File("src/test/resources/tradrIndivrdf.owl");
		repoURL = "http://localhost:5820/tradr";
	}
	
	@Test
	public void test1(){
		try {
			System.out.println("\n\n*********************Test 1************************");

			 db = new OWLOntologyDatabase("tradr", file);
			db.setupRepo(null);
			 db.insert(getRule(
					 //"onto:Human(?d) ^ onto:hasAge(?d, ?age) ^ swrlb:add(?newage, ?age, 1) -> onto:hasNewAge(?d, ?newage)"));
					 "onto:Human(?x) ^ onto:hasRole(?x,?r) ^ onto:Firefighter(?r) ^ onto:isHeadOf(?x,?t) ^ onto:Team(?t) -> onto:FirefighterTeam(?t)"));
			query(//"onto:hasNewAge(?d,?x)-> sqwrl:select(?d, ?x)");
					 "onto:FirefighterTeam(?x)  -> sqwrl:select(?x)");
					// "onto:Agent(?x) ^ onto:hasLocation(?x,?y) ^ onto:Agent(?z) ^ onto:hasLocation(?z, ?w) ^ differentFrom(?x,?z) ->  sqwrl:select(?x, ?y, ?z, ?w)");
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				if (db!=null)
				db.destroy();
			} catch (KRDatabaseException e) {
				e.printStackTrace();
			}

		}
	}
	
	
	
	@Test
	public void test2() throws SQWRLException, SWRLParseException, KRDatabaseException{
		try {
			System.out.println("\n\n*********************Test 2************************");

			 db = new OWLOntologyDatabase("tradr", file);
			 db.setupRepo(null);
			 db.insert(getRule("onto:Robot(?x) ^ onto:belongsToTeam(?x,?t) -> onto:RobotTeam(?t)"));
			 
			 //add a new fact to the triple store
			 RDFRepositoryDatabase rdb = db.getRepo();
			 ValueFactory vf = rdb.getValueFactory();
			 String baseURI = db.getbaseURI();
			 Statement stm = vf.createStatement(vf.createURI(baseURI+"Team1"), RDF.TYPE, vf.createURI(baseURI+"RobotTeam"));
			 Collection<Statement> stms = new HashSet<Statement>();
			 stms.add(stm);
			 rdb.insert(stms, null);
			 
			 //query to check if we get back data 
			 query("onto:RobotTeam(?x) -> sqwrl:select(?x)");
			 
			 //delete fact from database
			 rdb.delete(stms, null);
			 query("onto:RobotTeam(?x) -> sqwrl:select(?x)");
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			db.destroy();
		}
	}
	
	//@Test
	public void test3() throws SQWRLException, SWRLParseException, KRDatabaseException{
		try {
			System.out.println("\n\n*********************Test 3************************");
			 db = new OWLOntologyDatabase("tradr", file);
			 db.setupRepo(new URL("http://localhost:5820/"));
			 db.insert(getRule("onto:Image(?x) ^ onto:reference(?x,?t) -> onto:showOnMap(?x, \"true\")"));

			query("onto:Image(?x) ^ onto:reference(?x,?t) -> sqwrl:select(?x)");
			
			db.destroy();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	private DatabaseFormula getRule(String ruletext) throws SWRLParseException{
		SWRLParser parser = new SWRLParser(db.getSWRLOntology());

		SWRLRule rule = parser.parseRule(ruletext, "r1");

		DatabaseFormula formula = new SWRLDatabaseFormula(rule);
		return formula;
	}
	
	private void query(String queryString){
		try{
			long first = System.currentTimeMillis();
			 SQWRLQuery q =  db.getSWRLOntology().createSQWRLQuery("q1",queryString);
			 Query query = new SWRLQuery(q);
													
			//Drools QE	needed to pose sqwrl queryies "onto:Agent(?x) ^ onto:belongsToTeam(?x, ?t) "+ring+" sqwrl:makeSet(?s, ?t) ^ sqwrl:size(?n, ?s) -> sqwrl:select(?x,?n)");
			 
			 Set<Substitution> subs = db.query(query);
			 Iterator<Substitution> it = subs.iterator();
			 while (it.hasNext()){
				 Substitution sub = it.next();
				 Set<Var> vars = sub.getVariables();
				 Iterator<Var> itv = vars.iterator();
				 while(itv.hasNext()){
					 Var v = itv.next();
					 Term t = sub.get(v);
					 System.out.println("Subst: "+ v.toString() + " / "+ t.toString());
				 }
			 }
			long second = System.currentTimeMillis();
			long time = second-first;
			System.out.println("Querying took: "+time+" ms");
		

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
