package owlrepo;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;

import krTools.language.Var;

import org.junit.Test;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.swrlapi.core.SWRLAPIFactory;
import org.swrlapi.core.SWRLAPIOWLOntology;
import org.swrlapi.parser.SWRLParseException;

import owlrepo.language.SWRLExpression;
import owlrepo.language.SWRLTranslator;
import owlrepo.parser.SWRLParser;


public class ParserTest {
	
	SWRLParser parser = null;
	 BufferedReader reader;
	 SWRLRule r = null;
	 SWRLAPIOWLOntology swrlapiOnto;
	
	public ParserTest(){
		try{
		    File file = new File("src/test/resources/tradrIndivrdf.owl");
if (file.exists() && file.canRead()){
		OWLOntologyManager mng = OWLManager.createOWLOntologyManager();
		
	    OWLOntology  ontology = mng.loadOntologyFromOntologyDocument(file);
	    System.out.println("NR of owl axioms in onto: "+ontology.getAxiomCount());

//	    ontology.setOWLOntologyManager(mng);
//		mng.setOntologyFormat(ontology, mng.getOntologyFormat(ontology));
		
	     DefaultPrefixManager pmg = new DefaultPrefixManager();
	     swrlapiOnto = SWRLAPIFactory.createOntology(ontology, pmg);
	    
	    System.out.println("NR of swrl rules in onto: "+swrlapiOnto.getNumberOfSWRLRules());
	    
	    pmg.setPrefix("tradr", "http://www.semanticweb.org/ontologies/tradr#");
	   
	    reader = new BufferedReader(new InputStreamReader(System.in));
				parser = new SWRLParser(swrlapiOnto, reader, null);
}else
	System.out.println("File not found or not accessible.");
		}catch(Exception e){
			e.printStackTrace();
		}
	}

	@Test
	public void test() {
		String text= "tradr:Human(?x) ^ tradr:isHeadOf(?x, ?t) -> tradr:Team_leader(?x)";
		//text = "tradr:Human(P1)";
		try {
			System.out.println("SWRL Rule:::: "+ text);
			 r = parser.parseRule(text, "rule1");
		} catch (SWRLParseException e) {
			e.printStackTrace();
		}
		System.out.println("Read:::: "+r.toString());
		System.out.println("Axiom type:::: "+r.getAxiomType());
		//System.out.println("NNF: "+r.getNNF());
		System.out.println("Signature:::: "+r.getSignature());
		System.out.println("Variables:::: "+r.getVariables());

		SWRLTranslator transl = new SWRLTranslator(swrlapiOnto,r);
		System.out.println("SPARQL translation::::: \n"+transl.translateToSPARQL());
		
		SWRLExpression exp = new SWRLExpression(r);
		System.out.println("SWRL expression: "+exp.toString());
		System.out.println("Free vars:");
		for (Var v: exp.getFreeVar()){
			System.out.print(v+", ");
		}
	}

}
