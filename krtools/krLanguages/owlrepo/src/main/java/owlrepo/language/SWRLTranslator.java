package owlrepo.language;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataPropertyExpression;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLBinaryAtom;
import org.semanticweb.owlapi.model.SWRLBuiltInAtom;
import org.semanticweb.owlapi.model.SWRLClassAtom;
import org.semanticweb.owlapi.model.SWRLDArgument;
import org.semanticweb.owlapi.model.SWRLDataPropertyAtom;
import org.semanticweb.owlapi.model.SWRLDifferentIndividualsAtom;
import org.semanticweb.owlapi.model.SWRLIArgument;
import org.semanticweb.owlapi.model.SWRLIndividualArgument;
import org.semanticweb.owlapi.model.SWRLLiteralArgument;
import org.semanticweb.owlapi.model.SWRLObjectPropertyAtom;
import org.semanticweb.owlapi.model.SWRLPredicate;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLSameIndividualAtom;
import org.semanticweb.owlapi.model.SWRLUnaryAtom;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.swrlapi.builtins.arguments.SWRLBuiltInArgument;
import org.swrlapi.core.SWRLAPIFactory;
import org.swrlapi.core.SWRLAPIOWLOntology;
import org.swrlapi.core.SWRLAPIRenderer;

public class SWRLTranslator {
	
	SWRLRule rule;
	DefaultPrefixManager prefixManager;
	Set<IRI> builtins;
	SWRLAPIRenderer renderer;
	
	public SWRLTranslator(){
		SWRLAPIOWLOntology onto = SWRLAPIFactory.createOntology();
		prefixManager = onto.getPrefixManager();
		builtins = onto.getSWRLBuiltInIRIs();
		renderer = SWRLAPIFactory.createSWRLAPIRenderer(onto);  
		
	//	printSWRLVocab();
		
	}
	
	public SWRLTranslator(SWRLAPIOWLOntology swrlonto, SWRLRule rule){
		prefixManager = swrlonto.getPrefixManager();
		builtins = swrlonto.getSWRLBuiltInIRIs();
		this.rule = rule;
	}
	public String translateToSPARQL(){
		return translateToSPARQL(null, null);
	}
	public String translateToSPARQL(String namedGraph, String baseURI){
		String SPARQLquery = "";
		String SPARQLfilter = "";
		
		//Start by adding prefixes
		Set<String> prefixNames = prefixManager.getPrefixNames();
		Iterator<String> prefixIt = prefixNames.iterator();
		while (prefixIt.hasNext()){
			String prefixName = prefixIt.next();
			String prefix = prefixManager.getPrefix(prefixName);
			SPARQLquery += "PREFIX "+prefixName+" <"+prefix+">\n";
		}
		if (rule.getVariables().isEmpty()) {
			// ASK query
			SPARQLquery += "ASK ";
			
			if (namedGraph!=null && !namedGraph.isEmpty())
				SPARQLquery += "\nFROM  <"+namedGraph+">";
			if (baseURI != null && !baseURI.isEmpty())
				SPARQLquery += "\nFROM  <" + baseURI + ">";

		} else {
			// SELECT query

			SPARQLquery += "SELECT * ";
			
			if (namedGraph!=null && !namedGraph.isEmpty())
				SPARQLquery += "\nFROM  <"+namedGraph+">";
			if (baseURI!=null && !baseURI.isEmpty())
				SPARQLquery += "\nFROM  <"+baseURI+">";

			SPARQLquery +=	"\n WHERE ";
		}
		
		SPARQLquery += " \n{ ";

		Iterator<SWRLAtom> it = rule.getBody().iterator();
		while (it.hasNext())
		{
			SWRLAtom atom = it.next();
			if (atom instanceof SWRLUnaryAtom){
				if (atom instanceof SWRLClassAtom){
					SWRLClassAtom classatom = (SWRLClassAtom)atom;
					OWLClassExpression classexp = classatom.getPredicate();
					SWRLIArgument argument = classatom.getArgument();
					SPARQLquery += translate(argument) + " rdf:type "
							+ getShortForm(classexp.asOWLClass()) + ".\n";
					// System.out.println(SPARQLquery);

				}//else if SWRLDataRangeAtom
			}
			else if (atom instanceof SWRLBinaryAtom){
				SWRLBinaryAtom<?,?> batom = (SWRLBinaryAtom<?,?>) atom;
				SWRLArgument arg1 = batom.getFirstArgument();
				SWRLArgument arg2 = batom.getSecondArgument();

				if (atom instanceof SWRLDataPropertyAtom || atom instanceof SWRLObjectPropertyAtom){
					SWRLPredicate predicate = batom.getPredicate();
					SPARQLquery += translate(arg1) + " " + translate(predicate)
							+ " " + translate(arg2) + ".\n";
					// System.out.println(SPARQLquery);

				} else if (atom instanceof SWRLDifferentIndividualsAtom) {

					SPARQLfilter += "\nFILTER (" + translate(arg1) + " != "
							+ translate(arg2) + ")\n";

				} else if (atom instanceof SWRLSameIndividualAtom) {

					SPARQLfilter += "\nFILTER (" + translate(arg1) + " == "
							+ translate(arg2) + ")\n";

				}
			}
			else if (atom instanceof SWRLBuiltInAtom){
				
				SWRLBuiltInAtom builtin = (SWRLBuiltInAtom) atom;
				IRI pred = builtin.getPredicate();
				String operator = "";
				// SWRLDataFactory df = new OWLDataFactoryImpl();
				List<SWRLDArgument> args= builtin.getArguments();

				String prefix = prefixManager.getShortForm(pred);//getPrefixIRI(pred).;
				String op = prefix.substring(prefix.indexOf(":")+1,prefix.length());
				if (op == "swrlb"){
					//swrl built-ins
					switch(op){
					//for comparison
					case ("equal"): operator = "="; break;
					case ("notequal"): operator = "!="; break;
					case ("lessThan"): operator = "<"; break;
					case ("lessThanOrEqual"): operator = "<="; break;
					case ("greaterThan"): operator = ">"; break;
					case ("greaterThanOrEqual"): operator = ">="; break;	
					//mathematical operations
					case ("add"): operator = "+"; break;
					case ("subtract"): operator = "-"; break;
					case ("multiply"): operator = "*"; break;
					case ("divide"): operator = "/"; break;
					//more math: mod pow floor ceiling abs sin cos tan
					//booleanNot
					//string ops: 
					//date, time, duration 
					//URIs
					//lists - not supported by Pellet

					}
					if (args.size() == 2)
						SPARQLfilter += "\nFILTER ("+ translate(args.get(0)) + operator + translate(args.get(1)) +")\n";
					else if (args.size() == 3)
						SPARQLfilter += "\nBIND ("+ translate(args.get(1)) + operator + translate(args.get(2)) +" AS " + translate(args.get(0)) +")\n";
					// System.out.println(SPARQLquery);

				}
				else if (op.equalsIgnoreCase("owl")){
					if (op.equalsIgnoreCase("sameAs")){
						SPARQLfilter += "\nFILTER ( sameTerm("+ translate(args.get(0))+", "+ translate(args.get(1)) +")\n";
					}else if (op.equalsIgnoreCase("differentFrom")){
						SPARQLfilter += "\nFILTER ( !sameTerm("+ translate(args.get(0)) + ", " + translate(args.get(1)) +")\n";
						
					}
					// System.out.println(SPARQLquery);

				}
				/*else if (op == "sqwrl"){
					//sqwrl operators
					switch(op){
					case ("limit"): break;
					case ("min"): break;
					case ("max"): break;
					case ("sum"): break;
					case ("avg"): break;
					case ("count"): break;
					case ("orderBy"): break;
					case ("orderByDescending"): break;
					case ("grouBy"): break;
					case ("size"): break;
					case ("isEmpty"): break;
					case ("contains"): break;
					
					}
				}*/
			}
			
		}
		SPARQLquery = SPARQLquery + SPARQLfilter +  "}";

		// System.out.println(SPARQLquery);
		return SPARQLquery;
	}
	
	public String getRuleText(SWRLRule rule){
		//return renderer.renderSWRLRule(rule);

		char impl = '\u2192';
		if (rule.getHead().isEmpty())
		return atomlistToString(rule.getBody());
		else 
			return  atomlistToString(rule.getBody()) + " -> " + atomlistToString(rule.getHead());
	}
	
	private String atomlistToString(Set<SWRLAtom> atoms){
		String ruletext="";
		Iterator<SWRLAtom> atomsit = atoms.iterator();
		int last = atoms.size()-1;
		
		for (int i=0;i<last+1;i++){
			
			if (atomsit.hasNext() ){
				SWRLAtom atom = atomsit.next();
			
				//translate predicate
				SWRLPredicate predicate = atom.getPredicate();
				ruletext+= translate(predicate)+ "(";
				
				//translate arguments
				Collection<SWRLArgument> args = atom.getAllArguments();
				int llast = args.size()-1;
				Iterator<SWRLArgument> argsit = args.iterator();
				for (int j=0;j<llast+1;j++){
					if (argsit.hasNext()){
						//translate each argument
						ruletext+= translate(argsit.next());
						if (j!=llast)
							ruletext+= ", ";
					}
				}
				ruletext+=")";
				if (i!=last)
					ruletext+=" ^ ";
				
			}
		}
		return ruletext;
	}
	
	public String translate(SWRLArgument arg){
		if (arg instanceof SWRLVariable){
			SWRLVariable var = (SWRLVariable) arg;
			return "?"+var.getIRI().getShortForm();
		}
		else if (arg instanceof SWRLLiteralArgument){
			// get literal argument, backslash existing double quotes
			String literalArg = ((SWRLLiteralArgument)arg).getLiteral().getLiteral();
			String[] lit = literalArg.split("\"");
			literalArg = "";
			for (int i = 0; i < lit.length - 1; i++) {
				literalArg += lit[i] + "\\\"";
			}
			literalArg += lit[lit.length - 1];

			// process type
			String type = "";
			if (!arg.getDatatypesInSignature().isEmpty()) {
				type = arg.getDatatypesInSignature().iterator().next()
						.toString();
				if (type.contains("#"))
					type = "^^xsd:" + type.split("#")[1];
			}
			// return literalArg;
			return "\"" + literalArg + "\"";// + type;

		}else if (arg instanceof SWRLIndividualArgument){
			return getShortForm((OWLEntity) ((SWRLIndividualArgument)arg).getIndividual());
		}
		else if (arg instanceof SWRLBuiltInArgument){
			return ((SWRLBuiltInArgument)arg).getBoundVariableName();
		}
		
		return "";
	}
	
	public String getShortForm(OWLEntity entity){
		if (entity!=null){
			String pred = entity.toString();
			if (pred.contains("#")){
			String shortpred = pred.substring(1, pred.indexOf('#')+1);
			if (!prefixManager.getPrefixName2PrefixMap().containsValue(shortpred))
				prefixManager.setPrefix("", shortpred);
			}
			return prefixManager.getShortForm(entity);
			
		}
		return "";
//		else{
//			//swrl builtin
//			return prefixManager.getShortForm(IRI.create(pred));
//		}
	}
	
	public String translate(SWRLPredicate predicate){
		OWLEntity entity=null;
		if (predicate instanceof OWLClassExpression){
			entity = (OWLEntity)(OWLClassExpression)predicate;
			
		}else if (predicate instanceof OWLDataPropertyExpression){
			entity = (OWLEntity)(OWLDataPropertyExpression)predicate;
			
		}else if (predicate instanceof OWLObjectPropertyExpression){
			entity = (OWLEntity)(OWLObjectPropertyExpression)predicate;

		}
		return getShortForm(entity);
		
	}
	
	
	private void printSWRLVocab(){
		for (String prefix: prefixManager.getPrefixNames()){
			System.out.println(prefix);
			
		Iterator<IRI> it = builtins.iterator();
		while (it.hasNext()){
			String namesp = it.next().toString();
			if (namesp.contains(prefixManager.getPrefix(prefix)))
				System.out.println(namesp.substring(namesp.indexOf("#")+1, namesp.length()));
		}
		}
	}
}
