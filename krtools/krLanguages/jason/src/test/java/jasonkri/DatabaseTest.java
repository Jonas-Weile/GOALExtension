package jasonkri;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.LiteralImpl;
import jason.asSyntax.parser.ParseException;
import jasonkri.language.JasonDatabaseFormula;
import jasonkri.language.JasonQuery;
import jasonkri.language.JasonUpdate;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import krTools.exceptions.KRDatabaseException;
import krTools.exceptions.KRQueryFailedException;
import krTools.exceptions.ParserException;
import krTools.language.DatabaseFormula;
import krTools.language.Substitution;
import krTools.parser.Parser;

import org.junit.Test;

/**
 * Test {@link JasonUpdate}
 * 
 * @author W.Pasman 10jun15
 *
 */
public class DatabaseTest {

	@Test
	public void constructorInsertTest() throws ParseException,
			KRDatabaseException {
		Collection<DatabaseFormula> formulas = new HashSet<DatabaseFormula>();
		LiteralImpl p = (LiteralImpl) ASSyntax.parseLiteral("p");
		formulas.add(new JasonDatabaseFormula(p, null));
		JasonDatabase db = new JasonDatabase(formulas);
		assertEquals(1, db.getContents().size());
		assertTrue(db.getContents().contains(p));
	}

	@Test
	public void constructorInsertTest2() throws KRDatabaseException,
			ParseException, ParserException {
		Parser parser = new JasonParser("p:-q. q.", new mySourceInfo());
		JasonDatabase db = new JasonDatabase(parser.parseDBFs());

		assertEquals(2, db.getContents().size());
	}

	@Test
	public void queryTest1() throws KRDatabaseException, ParseException,
			ParserException, KRQueryFailedException {
		Parser parser = new JasonParser("p:-q. q.", new mySourceInfo());
		JasonDatabase db = new JasonDatabase(parser.parseDBFs());

		JasonQuery query = new JasonQuery(ASSyntax.parseFormula("q"),
				new JasonSourceInfo(new mySourceInfo()));
		Set<Substitution> results = db.query(query);
		System.out.println("result=" + results);
		assertEquals("[{}]", results.toString());// YES

	}

	@Test
	public void queryTest2() throws KRDatabaseException, ParseException,
			ParserException, KRQueryFailedException {
		Parser parser = new JasonParser("p :- q. q.", new mySourceInfo());
		JasonDatabase db = new JasonDatabase(parser.parseDBFs());

		JasonQuery query = new JasonQuery(ASSyntax.parseFormula("p"),
				new JasonSourceInfo(new mySourceInfo()));
		Set<Substitution> results = db.query(query);
		System.out.println("result=" + results);
		assertEquals("[{}]", results.toString());// YES
	}
}
