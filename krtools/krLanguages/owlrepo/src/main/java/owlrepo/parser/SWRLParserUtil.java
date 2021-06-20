package owlrepo.parser;

import org.openrdf.model.Statement;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.model.SWRLRule;
import org.swrlapi.parser.SWRLParseException;

public class SWRLParserUtil {

	org.swrlapi.parser.SWRLParser parser;
	PrefixManager mng;

	public SWRLParserUtil(org.swrlapi.parser.SWRLParser parser,
			PrefixManager mng) {
		this.parser = parser;
		this.mng = mng;
	}

	public SWRLRule getSWRLRule(Statement st) throws SWRLParseException {
		System.out.println("Statement: " + st);
		SWRLRule rule = null;
		String subj = st.getSubject().stringValue();
		String[] triple = subj.split("#");
		if (triple.length < 2)
			return null;
		String subjPref = mng.getPrefixIRI(IRI.create(triple[0] + "#"));
		subj = subjPref + triple[1];
		String pred = st.getPredicate().stringValue();
		triple = pred.split("#");
		String predPref = mng.getPrefixIRI(IRI.create(triple[0] + "#"));
		pred = predPref + triple[1];
		String obj = st.getObject().stringValue();
		triple = obj.split("#");
		if (triple.length > 1) { // IRI
			String objPref = mng.getPrefixIRI(IRI.create(triple[0] + "#"));
			obj = objPref + triple[1];
		} else { // literal
			obj = "\"" + obj + "\"";
		}
		// System.out.println(subj + ", " + pred + ", " + obj);

		String term = null;
		if (pred.equals("rdf:type")) {
			term = obj + "(" + subj + ")";
		} else
			term = pred + "(" + subj + ", " + obj + ")";
		System.out.println(term);
		try {
			rule = parser.parseSWRLRule(term, false, "term", "triple");
		} catch (SWRLParseException e) {
			e.printStackTrace();
			throw e;
		}
		System.out.println("R: " + rule);
		return rule;
	}

}
