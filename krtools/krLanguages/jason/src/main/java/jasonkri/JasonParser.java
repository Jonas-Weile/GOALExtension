package jasonkri;

import jason.asSyntax.ASSyntax;
import jason.asSyntax.LiteralImpl;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.VarTerm;
import jason.asSyntax.parser.ParseException;
import jasonkri.language.JasonDatabaseFormula;
import jasonkri.language.JasonQuery;
import jasonkri.language.JasonTerm;
import jasonkri.language.JasonUpdate;
import jasonkri.language.JasonVar;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import krTools.exceptions.ParserException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Term;
import krTools.language.Update;
import krTools.language.Var;
import krTools.parser.Parser;
import krTools.parser.SourceInfo;

public class JasonParser implements Parser {
	private final String text; // the text to parse
	private final SourceInfo sourceInfo;

	/**
	 * The "list" of errors that occured while parsing. Notice that the JASON
	 * parser only can throw and we thus get never more than 1 syntax error in
	 * here. There may be multiple semantic issues. Actually we store
	 * {@link ParserException}
	 */
	private List<SourceInfo> exceptions = new ArrayList<SourceInfo>();

	/**
	 * Constructor that takes GOAL's {@link SourceInfo}. We need to offset our
	 * own parser with the info from this.
	 * 
	 * @param source
	 *            the source {@link Reader}
	 * @param info
	 *            GOAL's {@link SourceInfo}.
	 */
	public JasonParser(Reader source, SourceInfo info) {
		sourceInfo = info;
		Scanner scanner = new Scanner(source);
		text = scanner.useDelimiter("\\A").next();
		scanner.close();
	}

	/**
	 * Convenient for testing and quick parses.
	 * 
	 * @param text
	 * @param info
	 */
	public JasonParser(String txt, SourceInfo info) {
		sourceInfo = info;
		text = txt;
	}

	/**
	 * Stores the error that occured. converts a
	 * jason.asSyntax.parser.ParseException to
	 * krTools.errors.exceptions.ParserException and throws it. This also
	 * adjusts the line info. This is a bit hacky, as we can't set the parser
	 * info properly to start with.
	 * 
	 * @param e
	 *            the jason {@link ParseException}
	 * @return ParserException
	 */
	// private ParserException parserException(ParseException e) {
	// JasonSourceInfo info = new JasonSourceInfo(sourceInfo, e.currentToken);
	// return new ParserException(e.getMessage(), info, e);
	// }

	/**
	 * Create {@link ParserException} from given message,
	 * jason.asSyntax.SourceInfo and exception that caused the problem.
	 * 
	 * @param msg
	 * @param info
	 *            jason.asSyntax.SourceInfo. If null, we only use the
	 *            {@link #sourceInfo}.
	 * @param e
	 *            the exception that occured. Ignored if null.
	 */
	private void addParserException(String msg, jason.asSyntax.SourceInfo info,
			ParseException e) {
		if (info == null && e != null && e.currentToken != null) {
			// maybe we still can get some info
			info = new jason.asSyntax.SourceInfo(sourceInfo.getSource()
					.getAbsolutePath(), e.currentToken.beginLine,
					e.currentToken.endLine);
		}
		JasonSourceInfo jsinfo = new JasonSourceInfo(sourceInfo, info);
		ParserException exc;
		if (e != null) {
			msg = msg + getFullMessage(e);
			exc = new ParserException(msg, jsinfo, e);
		} else {
			exc = new ParserException(msg, jsinfo);
		}
		exceptions.add(exc);
	}

	/**
	 * Extract all sub-messages from the exception for full report to user.
	 * 
	 * @param e
	 *            throwable that may contain subcauses.
	 * @return full error message.
	 */
	private String getFullMessage(Throwable e) {
		String msg = e.getMessage();
		if (msg == null) {
			// generic message
			msg = e.toString();
		}
		if (e.getCause() != null) {
			msg = msg + ": " + getFullMessage(e.getCause());
		}
		return msg;
	}

	@Override
	public List<DatabaseFormula> parseDBFs() {
		List<DatabaseFormula> dbFormulas = new ArrayList<DatabaseFormula>();

		try {
			List<LiteralImpl> rules = ASSyntax.parseBeliefs(text);

			for (LiteralImpl rule : rules) {
				if (!Utils.isDatabaseFormula(rule)) {
					addParserException("Rule " + rule
							+ " is not a good database formula",
							rule.getSrcInfo(), null);
				} else {
					dbFormulas
							.add(new JasonDatabaseFormula(rule,
									new JasonSourceInfo(sourceInfo, rule
											.getSrcInfo())));
				}
			}
		} catch (ParseException e) {
			addParserException("could not parse beliefs", null, e);
		}

		return dbFormulas;
	}

	@Override
	public List<Query> parseQueries() {
		List<Query> queries = new ArrayList<Query>();
		try {
			List<LogicalFormula> terms = ASSyntax.parseFormulas(text);
			for (LogicalFormula term : terms) {
				queries.add(new JasonQuery(term, sourceInfo));
			}
		} catch (ParseException e) {
			addParserException("could not parse queries", null, e);
		}
		return queries;
	}

	@Override
	public Query parseQuery() {
		try {
			LogicalFormula term = ASSyntax.parseFormula(text);

			if (!Utils.isQuery(term)) {
				addParserException("Term " + term + " is not a good query",
						term.getSrcInfo(), null);
			}
			return new JasonQuery(term, new JasonSourceInfo(sourceInfo));

		} catch (ParseException e) {
			addParserException("could not parse query", null, e);
		}
		return null;

	}

	@Override
	public Update parseUpdate() {
		try {
			LogicalFormula term = ASSyntax.parseFormula(text);
			if (!Utils.isUpdate(term)) {
				addParserException("Term " + term + " is not a good update",
						term.getSrcInfo(), null);
			}
			return new JasonUpdate(term, new JasonSourceInfo(sourceInfo));
		} catch (ParseException e) {
			addParserException("could not parse update", null, e);
		} catch (IllegalArgumentException e) {
			addParserException("could not parse update", null,
					new ParseException("illegal argument for update", e));
		}
		return null;
	}

	@Override
	public Var parseVar() {

		try {
			VarTerm term = ASSyntax.parseVar(text);
			if (!term.isVar()) {
				addParserException("Term " + term + " is not a var",
						term.getSrcInfo(), null);
			}
			return new JasonVar(term, new JasonSourceInfo(sourceInfo));
		} catch (ParseException e) {
			addParserException("could not parse var", null, e);
		}
		return null;
	}

	@Override
	public Term parseTerm() {

		try {
			jason.asSyntax.Term term = ASSyntax.parseTerm(text);
			JasonSourceInfo inf = new JasonSourceInfo(sourceInfo);
			if (term.isVar()) {
				return new JasonVar((VarTerm) term, inf);
			}
			return new JasonTerm(term, inf);
		} catch (ParseException e) {
			addParserException("could not parse term", null, e);
		}
		return null;
	}

	@Override
	public List<Term> parseTerms() {
		List<Term> goalterms = new ArrayList<Term>();
		try {
			List<jason.asSyntax.Term> terms = ASSyntax.parseTerms(text);

			for (jason.asSyntax.Term t : terms) {
				goalterms.add(JasonTerm.makeTerm(t, new JasonSourceInfo(
						sourceInfo, t.getSrcInfo())));
			}
		} catch (ParseException e) {
			addParserException("could not parse terms '" + text + "'", null, e);
		}
		return goalterms;
	}

	@Override
	public List<SourceInfo> getErrors() {
		return exceptions;
	}

	@Override
	public List<SourceInfo> getWarnings() {
		// FIXME handle warnings.
		return new ArrayList<SourceInfo>();
	}

}
