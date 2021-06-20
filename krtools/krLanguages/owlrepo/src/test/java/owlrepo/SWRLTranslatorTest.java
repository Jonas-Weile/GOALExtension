package owlrepo;

import java.io.File;

import org.junit.Test;
import org.semanticweb.owlapi.model.SWRLRule;
import org.swrlapi.parser.SWRLParseException;

import owlrepo.database.OWLOntologyDatabase;
import owlrepo.language.SWRLTranslator;


public class SWRLTranslatorTest {
	SWRLTranslator trans;
	OWLOntologyDatabase  db = null;

	
	public SWRLTranslatorTest(){
		try {
		File file = new File("src/test/resources/tradrIndivrdf.owl");
		  db = new OWLOntologyDatabase("myonto", file);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void test_FULL_RULE() throws SWRLParseException {
	 SWRLRule rule = db.getSWRLOntology().createSWRLRule( "rule1",
						"onto:Team_leader(?x) -> onto:Human(?x) ^ onto:isHeadOf(?x, onto:RescueTeam1)");
	 System.out.println("Read: "+rule);
	 
	 trans = new SWRLTranslator(db.getSWRLOntology(), rule);
	 System.out.println("SPARQL:::: \n"+trans.translateToSPARQL());
	}
	
	@Test
	public void test_HEAD() throws SWRLParseException {
	 SWRLRule rule = db.getSWRLOntology().createSWRLRule( "rule2",
			 "onto:Human(?x) ^ onto:isHeadOf(?x, ?t)");
	 System.out.println("Read: "+rule);
	 
	 trans = new SWRLTranslator(db.getSWRLOntology(), rule);
	 System.out.println("SPARQL:::: \n"+trans.translateToSPARQL());
	}
	
	@Test
	public void test_NO_FREE_VARS() throws SWRLParseException {
	 SWRLRule rule = db.getSWRLOntology().createSWRLRule( "rule3",
				"onto:UGV(onto:UGV_1)");
	 System.out.println("Read: "+rule);

	 trans = new SWRLTranslator(db.getSWRLOntology(), rule);
	 System.out.println("SPARQL:::: \n"+trans.translateToSPARQL());
	}
}
