package tuprolog.visitor;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import krTools.exceptions.ParserException;
import tuprolog.language.PrologTerm;
import tuprolog.parser.Parser4;
import tuprolog.parser.Prolog4Parser.PossiblyEmptyConjunctContext;
import tuprolog.parser.Prolog4Parser.PossiblyEmptyDisjunctContext;
import tuprolog.parser.Prolog4Parser.PrologtextContext;
import tuprolog.parser.Prolog4Parser.Term0Context;
import tuprolog.parser.Prolog4Parser.Term1000Context;

/**
 * Visitor that converts a parse tree coming from an {@link Parser4} into a
 * {@link PrologTerm}.<br>
 * <h1>example</h1> example parsing a term0<br>
 * * <code>
		visitor = new Visitor4(
				new ErrorStoringProlog4Parser(new StringReader("term"), null));<br>
		PrologTerm term = visitor.visitTerm0();<br>
 * </code>
 */
public class Visitor4 {
	private final Parser4 parser;
	private final Visitor4Internal visitor;

	/**
	 * @param parser
	 *            a {@link Parser4}
	 */
	public Visitor4(Parser4 p) {
		this.parser = p;
		this.visitor = new Visitor4Internal(p.getSourceInfo());
	}

	public PrologTerm visitPossiblyEmptyConjunct() {
		PossiblyEmptyConjunctContext parsed = this.parser.possiblyEmptyConjunct();
		return this.parser.isSuccess() ? this.visitor.visitPossiblyEmptyConjunct(parsed) : null;
	}

	public List<PrologTerm> visitPrologtext() {
		PrologtextContext parsed = this.parser.prologtext();
		return this.parser.isSuccess() ? this.visitor.visitPrologtext(parsed) : new ArrayList<PrologTerm>(0);
	}

	public PrologTerm visitPossiblyEmptyDisjunct() {
		PossiblyEmptyDisjunctContext parsed = this.parser.possiblyEmptyDisjunct();
		return this.parser.isSuccess() ? this.visitor.visitPossiblyEmptyDisjunct(parsed) : null;
	}

	public PrologTerm visitTerm0() {
		Term0Context parsed = this.parser.term0();
		return this.parser.isSuccess() ? this.visitor.visitTerm0(parsed) : null;
	}

	public PrologTerm visitTerm1000() {
		Term1000Context parsed = this.parser.term1000();
		return this.parser.isSuccess() ? this.visitor.visitTerm1000(parsed) : null;
	}

	/**
	 * Get all errors from both visitor and parser
	 *
	 * @return all errors from both visitor and parser
	 */
	public SortedSet<ParserException> getErrors() {
		SortedSet<ParserException> allErrors = new TreeSet<ParserException>();
		allErrors.addAll(this.visitor.getVisitorErrors());
		allErrors.addAll(this.parser.getErrors());
		return allErrors;
	}
}