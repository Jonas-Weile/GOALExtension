package owlMentalState.translator;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import krTools.language.DatabaseFormula;
import krTools.language.Term;
import krTools.language.Update;
import languageTools.program.actionspec.UserSpecAction;
import languageTools.program.agent.AgentId;
import languageTools.program.agent.msg.Message;
import languageTools.program.agent.msg.SentenceMood;
import mentalState.error.MSTTranslationException;
import mentalState.translator.Translator;

import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLDArgument;
import org.semanticweb.owlapi.model.SWRLIArgument;
import org.semanticweb.owlapi.model.SWRLIndividualArgument;
import org.semanticweb.owlapi.model.SWRLLiteralArgument;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.swrlapi.parser.SWRLParseException;

import owlrepo.language.SWRLDatabaseFormula;
import owlrepo.language.SWRLTerm;
import owlrepo.language.SWRLUpdate;
import owlrepo.parser.SWRLParser;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;
import eis.iilang.Action;
import eis.iilang.Function;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Parameter;
import eis.iilang.ParameterList;
import eis.iilang.Percept;
import eis.iilang.TruthValue;

public class OwlEISTranslator implements Translator {

	private OWLDataFactory df = new OWLDataFactoryImpl();
	private SWRLParser parser;

	public OwlEISTranslator(SWRLParser parser) {
		this.parser = parser;
	}

	public OwlEISTranslator() {
	}

	@Override
	public Term convert(Parameter parameter) throws MSTTranslationException {
		SWRLTerm term = null;
		if (parameter instanceof Identifier) { // TODO: WHAT DOES IDENTIFIER
			// STAND FOR?
			Identifier id = (Identifier) parameter;
			String idf = id.getValue();
			// try to parser as argument
			SWRLArgument arg = parser.parseArgument(idf);
			if (arg != null) {
				term = new SWRLTerm(arg);
			}
			// else if (idf.contains("#")) {
			// // return individual
			// term = new SWRLTerm(
			// this.df.getSWRLIndividualArgument(this.df.getOWLNamedIndividual(IRI.create(trim(idf)))));
			// } else {
			// // return string literal
			// term = new
			// SWRLTerm(this.df.getSWRLLiteralArgument(this.df.getOWLLiteral(trim(idf))));
			// }
		} else if (parameter instanceof Numeral) {
			Number number = ((Numeral) parameter).getValue();
			SWRLArgument arg = parser.parseArgument(number.toString());
			if (arg != null) {
				term = new SWRLTerm(arg);
			}
			// OWLLiteral literal = null;
			// // check subtype
			// // owlliteral - boolean, double, float, int
			// if (number instanceof Double) {
			// literal = this.df.getOWLLiteral(number.doubleValue());
			// } else if (number instanceof Float) {
			// literal = this.df.getOWLLiteral(number.floatValue());
			// } else if (number instanceof Integer) {
			// literal = this.df.getOWLLiteral(number.intValue());
			// } else if (number instanceof Byte) {
			// literal = this.df.getOWLLiteral(number.byteValue());
			// } else if (number instanceof Long
			// && (number.longValue() < Integer.MIN_VALUE || number.longValue()
			// > Integer.MAX_VALUE)) {
			// throw new ArithmeticException("EIS long value " + number +
			// " does not fit into a JPL integer");
			// }
			// return literal
			// term = new SWRLTerm(this.df.getSWRLLiteralArgument(literal));

		} else if (parameter instanceof Function) {
			Function f = (Function) parameter;
			// get predicate name and arguments
			String name = f.getName();
			List<Parameter> params = f.getParameters();

			for (Parameter param : params) {
				if (param instanceof ParameterList) {// TODO: deal with lists!

				}
			}

			// take first argument and convert it
			Parameter arg1 = params.get(0);
			SWRLTerm term1 = (SWRLTerm) convert(arg1);
			IRI predicate = IRI.create(trim(name));

			if (params.size() == 1) {
				// create class atom
				OWLClass classname = this.df.getOWLClass(predicate);
				term = new SWRLTerm(this.df.getSWRLClassAtom(classname, (SWRLIArgument) term1.getArgument()));
			} else if (params.size() == 2) { // not class atom
				// take second argument and convert it
				Parameter arg2 = params.get(1);
				SWRLTerm term2 = (SWRLTerm) convert(arg2);

				// if second argument data value
				if (arg2 instanceof Numeral || arg2 instanceof TruthValue
						|| (arg2 instanceof Identifier && !arg2.toString().contains("#"))) {
					// create data property atom

					term = new SWRLTerm(this.df.getSWRLDataPropertyAtom(this.df.getOWLDataProperty(predicate),
							(SWRLIArgument) (term1.getArgument()), (SWRLDArgument) (term2.getArgument())));

				} else { //
					// create object property atom
					term = new SWRLTerm(this.df.getSWRLObjectPropertyAtom(this.df.getOWLObjectProperty(predicate),
							(SWRLIArgument) (term1.getArgument()), (SWRLIArgument) (term2.getArgument())));

				}
			} // else >2 ??

			// return class/object/data property atom
		} else if (parameter instanceof ParameterList) {
			ParameterList pl = (ParameterList) parameter;
			List<SWRLTerm> terms = new LinkedList<SWRLTerm>();
			for (Parameter p : pl) {
				SWRLTerm t = (SWRLTerm) convert(p);
				terms.add(t);
			}

			// create a conjunction of atoms and a rule out of it, and a term
			// out of it
			// for now just return the first one
			if (terms.size() > 0) {
				term = terms.get(0);
				// parameter list without predicate TODO: WHAT DOES THIS STAND
				// FOR?
				// term = new ?
			}
		} else if (parameter instanceof TruthValue) {
			TruthValue tv = (TruthValue) parameter;
			// return boolean literal
			term = new SWRLTerm(this.df.getSWRLLiteralArgument(this.df.getOWLLiteral(tv.getBooleanValue())));

		} else {
			throw new IllegalArgumentException("Failed to convert EIS parameter " + parameter + " to SWRL.");
		}

		return term;
	}

	@Override
	public Parameter convert(Term term) throws MSTTranslationException {
		Parameter parameter = null;
		SWRLTerm swrlterm = (SWRLTerm) term;

		// check if s argument
		if (swrlterm.isArgument()) {
			SWRLArgument arg = swrlterm.getArgument();
			// variable - x,y,z
			if (swrlterm.isVariable()) {
				SWRLVariable var = (SWRLVariable) arg;

				// create new Identifier
				parameter = new Identifier(shortForm(var.toString()));

				// data value - numbers, strings
			} else if (swrlterm.isLiteral()) {
				SWRLLiteralArgument literal = (SWRLLiteralArgument) arg;
				OWLLiteral lit = literal.getLiteral();

				// create new Numeral
				if (lit.isDouble()) {
					parameter = new Numeral(lit.parseDouble());
				} else if (lit.isFloat()) {
					parameter = new Numeral(lit.parseFloat());
				} else if (lit.isInteger()) {
					parameter = new Numeral(lit.parseInteger());
				} else if (lit.isBoolean()) {
					parameter = new TruthValue(lit.parseBoolean());
				} else {
					// create Identifier
					parameter = new Identifier(lit.getLiteral());
				}

				// individual - IRI <http://.../John>
			} else if (swrlterm.isIndividual()) {
				SWRLIndividualArgument indiv = (SWRLIndividualArgument) arg;
				// create new Identifier
				parameter = new Identifier(shortForm(indiv.getIndividual().toString()));
			}

			// if it's atom - unary or binary
		} else if (swrlterm.isAtom()) {
			LinkedList<Parameter> parameters = new LinkedList<Parameter>();
			String predicate = swrlterm.getAtom().getPredicate().toString();
			for (SWRLArgument arg : swrlterm.getAtom().getAllArguments()) {
				parameters.add(convert(new SWRLTerm(arg)));
			}
			// create new Function
			parameter = new Function(predicate, parameters);
		}

		// TODO create new ParameterList
		return parameter;
	}

	@Override
	public Action convert(UserSpecAction action) throws MSTTranslationException {
		LinkedList<Parameter> parameters = new LinkedList<Parameter>();
		for (Term term : action.getParameters()) {
			parameters.add(convert(term));
		}
		return new Action(action.getName(), parameters);
	}

	@Override
	public Update convert(Percept percept) throws MSTTranslationException {

		// Get main operator name and parameters of the percept.
		List<Parameter> parameters = percept.getParameters();
		String perceptAtom = shortForm(percept.getName());
		if (parameters.size()>1){
				perceptAtom += "(";
				int size = parameters.size() - 1;
				for (int i = 0; i < size; i++) {
					Parameter param = parameters.get(i);
					perceptAtom += shortForm(convertString(param)) + ", ";
					}
				perceptAtom += shortForm(convertString(parameters.get(size))) + ")";
		}else
			throw new MSTTranslationException("cannot handle percept without any parameter!"+percept);
		
		// parse percept atom into rule
		SWRLRule r;
		try {
			r = parser.parseRule(perceptAtom, "r1");
			return new SWRLUpdate(r);

		} catch (SWRLParseException e) {
			e.printStackTrace();
			throw new MSTTranslationException(
					"Failed to convert percept to update:" + percept, e.getCause());
		}
	}

	
	private String convertString(Parameter p) throws MSTTranslationException{
		if (p instanceof Identifier) {
			Identifier id = (Identifier) p;
			return id.getValue();
		} else
			throw new MSTTranslationException("cannot handle not identifier percept parameter!"+p);
	}
	
	@Override
	public Term convert(AgentId id) throws MSTTranslationException {
		// TODO Auto-generated method stub
		System.out.println("Converting agentId to Term: " + id);

		return null;
	}

	@Override
	public Term makeList(List<Term> termList) throws MSTTranslationException {
		// TODO Auto-generated method stub
		System.out.println("Making term from termlist: " + termList);
		return null;
	}

	private String trim(String s) {
		if (s.startsWith("<") && s.endsWith(">")) {
			return s.substring(1, s.length() - 1);
		}
		return s;
	}

	@Override
	public Percept convert(Update update) throws MSTTranslationException {
		// TODO Auto-generated method stub
		System.out.println("Converting update to percept: " + update);
		return null;
	}

	public Percept convert(DatabaseFormula dbf)
			throws MSTTranslationException {
		String name = "";
		LinkedList<Parameter> parameters = new LinkedList<Parameter>();
		System.out.println("Converting dbf to percept: " + dbf);


		return new Percept(name, parameters);
	}

	public Message convertToMessage(DatabaseFormula dbf, AgentId id) {
		// System.out.println("Converting dbf to message: " + dbf);
		// a dbf here contains a rules with all atoms for one message in
		// conjunction
		SWRLDatabaseFormula sdb = (SWRLDatabaseFormula) dbf;
		Update content = null;
		SentenceMood mood = null;
		AgentId sender = null;
		String goalBaseURI = "<http://ii.tudelft.nl/goal#";


		for (SWRLAtom atom : sdb.getRule().getBody()) {
			String pred = atom.getPredicate().toString();
			Collection<SWRLArgument> args = atom.getAllArguments();
			int size = args.toArray().length;
			if (size > 1) {
				String obj = args.toArray()[1].toString();
				obj = shortForm(obj);
				System.out.println("OBJ: " + obj);
				System.out.println("PRED: " + pred);
				if (pred.equals(goalBaseURI + "mood>")) {
					// got mood
					mood = getMessageMood(unescapeString(obj));
				}
				if (pred.equals(goalBaseURI + "content>")) {
					// got content
					content = new SWRLUpdate(unescapeString(obj));
				}
				if (pred.equals(goalBaseURI + "sentBy>")) {
					// got sender
					sender = new AgentId(obj);
				}
			}
		}

		System.out.println("NEW MSG: "+content + " : "+mood + " : "+sender);
		Message m = new Message(content, mood);
		m.setSender(sender);
		Set<AgentId> receivers = new HashSet<AgentId>();
		receivers.add(id);
		m.setReceivers(receivers);
		return m;
	}

	private SentenceMood getMessageMood(String mood) {
		switch (mood) {
		case "IMP":
			return SentenceMood.IMPERATIVE;
		case "INT":
			return SentenceMood.INTERROGATIVE;
		case "IND":
			return SentenceMood.INDICATIVE;
		default:
			return SentenceMood.INDICATIVE;
		}
	}

	private String unescapeString(String s) {
		if (s.contains("^^")) {
			s = s.split("^^")[0];
			System.out.println("After split: " + s);
		}
		if (s.startsWith("\"") && s.endsWith("\""))
			s = s.substring(1, s.length() - 1);
		System.out.println("S:" + s);
		String[] contents = s.split("\\\"");
		String content = "";
		for (int i = 0; i < contents.length - 1; i++) {
			content += contents[i] + "\"";
		}
		content += contents[contents.length - 1];
		return content;
	}

	private String shortForm(String s) {
		if (s.contains("#")) {
			return s.substring(s.indexOf("#") + 1, s.indexOf(">"));
		}
		if (s.contains("^^")){
			return s.substring(0, s.indexOf("^^"));
		}
		return s;
	}

}
