package jason;

import static org.junit.Assert.assertEquals;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.Rule;
import jason.asSyntax.Structure;
import jason.asSyntax.VarTerm;
import jason.asSyntax.parser.ParseException;
import jasonkri.JasonSourceInfo;
import jasonkri.language.JasonDatabaseFormula;
import jasonkri.language.JasonQuery;
import jasonkri.language.JasonTerm;
import jasonkri.language.JasonVar;

import java.io.File;

import krTools.parser.SourceInfo;

import org.junit.Test;

public class EqualsTest {

	private JasonSourceInfo makeSourceInfo() {
		return new JasonSourceInfo(new SourceInfo() {

			@Override
			public int getStopIndex() {
				return 0;
			}

			@Override
			public int getStartIndex() {
				return 0;
			}

			@Override
			public File getSource() {
				return new File("");
			}

			@Override
			public String getMessage() {
				return "no message";
			}

			@Override
			public int getLineNumber() {
				return 0;
			}

			@Override
			public int getCharacterPosition() {
				return 0;
			}

			@Override
			public int compareTo(SourceInfo o) {
				return 0;
			}
		});
	}

	@Test
	public void testVarEquals() {
		JasonVar var1 = new JasonVar(new VarTerm("X"), makeSourceInfo());
		JasonVar var2 = new JasonVar(new VarTerm("X"), makeSourceInfo());
		var1.equals(var2);
		assertEquals(var1, var2);
	}

	@Test
	public void testAtomEquals() {
		JasonTerm term1 = new JasonTerm(new Atom("p"), makeSourceInfo());
		JasonTerm term2 = new JasonTerm(new Atom("p"), makeSourceInfo());
		assertEquals(term1, term2);
	}

	@Test
	public void testDBFormulaEquals() throws ParseException {
		Rule clause = ASSyntax.parseRule("q(Y) :- p(Z) & Y = Z*3.");
		JasonDatabaseFormula dbformula1 = new JasonDatabaseFormula(clause,
				makeSourceInfo());
		JasonDatabaseFormula dbformula2 = new JasonDatabaseFormula(clause,
				makeSourceInfo());
		assertEquals(dbformula1, dbformula2);
	}

	@Test
	public void testQueryEquals() throws ParseException {
		Literal query = new Structure("p");
		query.addTerm(new VarTerm("X"));

		JasonQuery query1 = new JasonQuery(query, makeSourceInfo());

		query = new Structure("p");
		query.addTerm(new VarTerm("X"));
		JasonQuery query2 = new JasonQuery(query, makeSourceInfo());

		assertEquals(query1, query2);
	}
}
