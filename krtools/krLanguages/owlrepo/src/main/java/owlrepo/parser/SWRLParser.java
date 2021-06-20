package owlrepo.parser;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Term;
import krTools.language.Update;
import krTools.language.Var;
import krTools.parser.Parser;
import krTools.parser.SourceInfo;

import org.openrdf.model.Statement;
import org.openrdf.rio.RDFFormat;
import org.openrdf.rio.RDFHandlerException;
import org.openrdf.rio.RDFParseException;
import org.openrdf.rio.RDFParser;
import org.openrdf.rio.Rio;
import org.openrdf.rio.helpers.StatementCollector;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.PrefixManager;
import org.semanticweb.owlapi.model.SWRLArgument;
import org.semanticweb.owlapi.model.SWRLDArgument;
import org.semanticweb.owlapi.model.SWRLIArgument;
import org.semanticweb.owlapi.model.SWRLRule;
import org.semanticweb.owlapi.model.SWRLVariable;
import org.swrlapi.core.SWRLAPIOWLOntology;
import org.swrlapi.parser.SWRLParseException;
import org.swrlapi.parser.SWRLParserSupport;
import org.swrlapi.parser.SWRLTokenizer;

import owlrepo.language.SWRLDatabaseFormula;
import owlrepo.language.SWRLQuery;
import owlrepo.language.SWRLTerm;
import owlrepo.language.SWRLUpdate;
import owlrepo.language.SWRLVar;

public class SWRLParser implements Parser {
	org.swrlapi.parser.SWRLParser parser;
	SourceInfo info;
	String currentLine = "";
	int lineNr = 0;
	List<SourceInfo> errors;
	SWRLAPIOWLOntology onto;
	SWRLParserSupport swrlParserSupport;
	BufferedReader reader = null;
	SWRLParserUtil parserUtil;
	PrefixManager mng;
	Set<String> undefined = new HashSet<String>();

	public SWRLParser(SWRLAPIOWLOntology swrlapiOWLOntology) {
		this.parser = new org.swrlapi.parser.SWRLParser(swrlapiOWLOntology);
		this.errors = new ArrayList<SourceInfo>();
		this.onto = swrlapiOWLOntology;
		this.swrlParserSupport = new SWRLParserSupport(swrlapiOWLOntology);
		this.mng = this.onto.getPrefixManager();
	}

	public SWRLParser(SWRLAPIOWLOntology swrlapiOWLOntology, Reader reader, SourceInfo info) {
		this(swrlapiOWLOntology);
		this.reader = new BufferedReader(reader);

		this.info = info;
	}

	public void parse(Reader reader, SourceInfo info) {
		this.reader = new BufferedReader(reader);
		this.info = info;
		this.errors.clear();
		this.undefined.clear();
	}

	private boolean parseCurrentLine() {
		// takes the next from the List of parsed lines as Strings
		try {
			this.currentLine = this.reader.readLine();
			this.lineNr++;
		} catch (IOException e) {
			this.errors.add(new SWRLParserSourceInfo(this.info.getSource(), this.lineNr, -1, e.getMessage()));
			return false;
		}

		// lines.get(lineNr); lineNr++;
		if (this.currentLine != null) {
			if (this.currentLine.isEmpty()) {
				return parseCurrentLine();
			}
			// process current line
			this.currentLine = this.currentLine.trim();
			if (this.currentLine.startsWith("(") && this.currentLine.endsWith(")")) {
				this.currentLine = this.currentLine.substring(1, this.currentLine.length() - 1);
			}
			return true;
		}
		return false;
	}

	private SWRLRule parseRule(String string) {
		SWRLRule rule = null;
		try {

			if (!string.isEmpty()) {
				// TODO: to avoid second pass parse KR file -- find better
				// alternative/add all possibilities
				if (string.startsWith("<?") || string.startsWith("<!")
						|| string.startsWith("@prefix")) {
					return rule;
				}
				// call SWRL parser
				if (string.contains("(")) {// we know it's a term, not an
					// argument
					// if (parser.isSWRLRuleCorrectAndComplete(string))
					rule = parseRule(string, "rule" + this.lineNr);
				}
			}
		} catch (SWRLParseException e) {
			this.undefined.add(string);
			this.errors.add(new SWRLParserSourceInfo(this.info.getSource(), this.lineNr, -1, e.getMessage()));
		}
		return rule;
	}

	public SourceInfo getInfo() {
		return this.info;
	}

	public Set<String> getUndefined() {
		return this.undefined;
	}

	public SWRLArgument parseArgument(String string) {
		SWRLArgument arg = null;
		// it's not a swrl rule or contains undefined iri-s

		if (!string.isEmpty()) {
			boolean isInHead = false;
			// get private method of swrlparser we need - swrldargument
			try {
				SWRLTokenizer tokenizer = new SWRLTokenizer(string, false);
				Method parseArgumentList = org.swrlapi.parser.SWRLParser.class.getDeclaredMethod("parseSWRLDArgument",
						SWRLTokenizer.class, boolean.class, boolean.class);
				parseArgumentList.setAccessible(true);// Abracadabra
				SWRLDArgument parsedArg = (SWRLDArgument) parseArgumentList.invoke(this.parser, tokenizer, isInHead,
						isInHead);
				arg = parsedArg;
			} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
					| NoSuchMethodException | SecurityException | SWRLParseException ex) {
				try {
					// try parsing swrliargument
					SWRLTokenizer tokenizer = new SWRLTokenizer(string, false);
					Method parseArgumentList = org.swrlapi.parser.SWRLParser.class
							.getDeclaredMethod("parseSWRLIArgument", SWRLTokenizer.class, boolean.class);
					parseArgumentList.setAccessible(true);// Abracadabra
					SWRLIArgument parsedArg = (SWRLIArgument) parseArgumentList.invoke(this.parser, tokenizer,
							isInHead);// now its OK
					arg = parsedArg;

				} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException
						| NoSuchMethodException | SecurityException | SWRLParseException e) {
					return this.onto.getOWLDataFactory()
							.getSWRLLiteralArgument(this.onto.getOWLDataFactory().getOWLLiteral(string));
				}
			}
		}
		return arg;
	}

	public SWRLRule parseRule(String line, String name) throws SWRLParseException {
		// public SWRLRule parseSWRLRule(String ruleText, boolean
		// interactiveParseOnly, String ruleName, String comment)
		// throws SWRLParseException
		return this.parser.parseSWRLRule(line, false, name, "nocomment");
	}

	@Override
	public List<DatabaseFormula> parseDBFs() {
		// rule to list of dbformula
		List<DatabaseFormula> dbfs = new LinkedList<DatabaseFormula>();
		List<SWRLTerm> parsed = parseRDF();
		if (parsed != null) {
			for (SWRLTerm triple : parsed) {
				if (triple.isRule())
					dbfs.add(new SWRLDatabaseFormula(triple.getRule()));
				else if (triple.isAtom())
					dbfs.add(new SWRLDatabaseFormula(triple.getAtom()));
				else
					dbfs.add(new SWRLDatabaseFormula(triple.getAxiom()));
			}
		} else {
			System.out.println("PARSE DBFS NO FORMAT!");
		}
		return dbfs;
	}

	private List<SWRLTerm> parseRDF() {
		List<SWRLTerm> triples = new LinkedList<SWRLTerm>();
		File file = this.info.getSource();
		RDFFormat format = RDFFormat.forFileName(file.getPath());

		if (format == null)
			return null;
		RDFParser rdfParser = Rio.createParser(format);
		List<Statement> statements = new ArrayList<Statement>();
		StatementCollector collector = new StatementCollector(statements);
		rdfParser.setRDFHandler(collector);
		try {
			// if (reader.ready()) {
			// throw new IOException("Reader not ready");
			// }
			// reader.mark(1);
			rdfParser.parse(reader, this.onto.getPrefixManager()
					.getDefaultPrefix());
			for (Statement stm : statements) {
				// convert it into a SWRLRule or DBFormula somehow
				SWRLTerm t = getSWRLTerm(stm);
				if (t != null)
					triples.add(t);
			}
			// reader.reset();
		} catch (RDFParseException | RDFHandlerException | IOException | SWRLParseException e) {
			e.printStackTrace();
			this.errors.add(new SWRLParserSourceInfo(this.info.getSource(), this.lineNr, -1, e.getMessage()));
		}
	//	System.out.println("SIZE: " + triples.size());
		return triples;
	}

	public SWRLTerm getSWRLTerm(Statement st) throws SWRLParseException {
		//basically filters out statements of only ABOX assertions
		//the ontology is already loaded, so we use the swrl parser to create terms
	//	System.out.println("Statement: " + st);
		SWRLTerm atom = null;
		String subj = st.getSubject().stringValue();
		String pred = st.getPredicate().stringValue();
		String obj = st.getObject().stringValue();
		
		String[] triple = subj.split("#");

		if (triple.length < 2)
			return null;
		String subjPref = mng.getPrefixIRI(IRI.create(triple[0] + "#"));
		subj = subjPref + triple[1];
		triple = pred.split("#");
		String predPref = mng.getPrefixIRI(IRI.create(triple[0] + "#"));
		pred = predPref + triple[1];
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
		// System.out.println(term);
		atom = parseTerm(term);
		// System.out.println("R: " + atom);
		return atom;
	}

	@Override
	public Update parseUpdate() {
		// rule to update
		SWRLRule rule = null;

		if (parseCurrentLine()) {
			// parse rule
			rule = parseRule(this.currentLine);
			// rule to query
			if (rule != null) {
				return new SWRLUpdate(rule);
			} else if (this.errors.isEmpty()) {
				// parse argument
				SWRLArgument arg = parseArgument(this.currentLine);
				if (arg != null) {
					// check if it was an argument or a rule
					return new SWRLUpdate(arg);
				} else {
					return new SWRLUpdate(this.currentLine);
				}
			}
		}
		return new SWRLUpdate(this.onto.getOWLDataFactory()
				.getSWRLIndividualArgument(this.onto.getOWLDataFactory().getOWLAnonymousIndividual()));
		// throw new ParserException("Cannot create update from: " +
		// currentLine);
	}

	@Override
	public List<Query> parseQueries() {
		List<Query> queries = new LinkedList<Query>();

		List<SWRLTerm> parsed = parseRDF();
		if (parsed != null) {
			for (SWRLTerm triple : parsed) {
				if (triple.isRule())
					queries.add(new SWRLQuery(triple.getRule()));
				else if (triple.isAtom())
					queries.add(new SWRLQuery(triple.getAtom()));
			}
		}else {
			Query q = parseQuery();
			while (q!=null){
				//System.out.println("READ Q: " + q);
				queries.add(q);
				q = parseQuery();
			}
		}

		return queries;
	}

	@Override
	public Query parseQuery() {
		SWRLRule rule = null;
		try{
			if (parseCurrentLine()) {
				// parse rule
				rule = parseRule(this.currentLine);
				// rule to query
				if (rule != null) {
					return new SWRLQuery(rule);
				} else if (this.errors.isEmpty()) {
					// parse argument
					SWRLArgument arg = parseArgument(this.currentLine);
					if (arg != null && arg instanceof SWRLVariable) {
						// check if it was an argument or a rule
						return new SWRLQuery(arg);
					} else { //not a rule, not a variable argument, cannot be a query
						 this.errors.add(new SWRLParserSourceInfo(this.info.getSource(), this.lineNr,
						 -1, "Could not create query from: " + this.currentLine));
					}
					// return new SWRLQuery(arg);
				}
			}
		}catch(Exception e){
			this.errors.add(new SWRLParserSourceInfo(this.info.getSource(), this.lineNr, -1,
					"Could not create query: parsing is finished (returns null)"));
		}
		return null;
	}

	/**
	 * parses the reader given to the parser on creation, tries to create a list
	 * of terms
	 *
	 * @return list of terms parsed, or empty list
	 */
	@Override
	public List<Term> parseTerms() {
		List<Term> terms = new LinkedList<Term>();
		// does not use currentLine, but instead splits the line by commas
		while (parseCurrentLine()) {
			String[] termstrings = this.currentLine.split(",");
			for (String term : termstrings) {
				Term pterm = parseTerm(term.trim());
				if (pterm != null) {
					terms.add(pterm);
				}
			}
		}
		return terms;
	}

	private SWRLTerm parseTerm(String termstring) {
		SWRLTerm term = null;
		if (!termstring.isEmpty() && !termstring.startsWith("owl:")
				&& !termstring.startsWith("rdfs:") && !termstring.startsWith("swrl")) {
			// parse rule
			SWRLRule rule = parseRule(termstring);
			// rule to term
			if (rule != null) {
//				System.out.println("Parsed " + termstring + " as: "
//						+ rule.toString());
				term = new SWRLTerm(rule);
			} else if (this.errors.isEmpty()) {
				// variable
				SWRLArgument arg = parseArgument(termstring);
				if (arg != null) {
					if (arg instanceof SWRLVariable) {
						term = new SWRLVar((SWRLVariable) arg);
					} else {
						term = new SWRLTerm(arg);
					}
				} else {
					term = new SWRLTerm(termstring);
				}
			}
		}
		return term;
	}

	// private SWRLVar parseVariable(String string) throws SWRLParseException{
	// return new
	// SWRLVar(swrlParserSupport.getSWRLVariable(string.substring(1,string.length())));
	// }

	/**
	 * parses the reader given to the parser on creation, tries to create a term
	 *
	 * @return a Term parsed, or null
	 */
	@Override
	public Term parseTerm() {
		// rule to term
		if (parseCurrentLine()) {
			return parseTerm(this.currentLine);
		}
		return null;
	}

	@Override
	public Var parseVar() {
		// rule to var
		if (parseCurrentLine()) {
			SWRLArgument arg = parseArgument(this.currentLine);
			if (arg != null) {
				if (arg instanceof SWRLVariable) {
					return new SWRLVar((SWRLVariable) arg);
				}
			} else {
				return new SWRLVar(this.currentLine);
			}
		}
		return null;
	}

	@Override
	public List<SourceInfo> getErrors() {
		return this.errors;
	}

	@Override
	public List<SourceInfo> getWarnings() {
		return new ArrayList<>(0); // TODO
	}

}
