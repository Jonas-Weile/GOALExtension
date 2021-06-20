package jason;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.LiteralImpl;
import jason.asSyntax.StringTermImpl;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jason.asSyntax.parser.ParseException;
import jason.bb.BeliefBase;
import jason.bb.DefaultBeliefBase;

import java.util.List;

import org.junit.Test;

public class ParserTest {

	@Test
	public void parseP() throws ParseException {
		BeliefBase bb = new DefaultBeliefBase();
		Literal lit = ASSyntax.parseLiteral("p");
		bb.add(lit);
	}

	/**
	 * check basic multiple literals parsing
	 * 
	 * @throws ParseException
	 */
	@Test
	public void parseBeliefsTest() throws ParseException {
		List<LiteralImpl> literals = ASSyntax.parseBeliefs("p. q.");
		assertEquals(2, literals.size());
		assertEquals("p", literals.get(0).toString());
		assertEquals("q", literals.get(1).toString());
	}

	/**
	 * a rule and an atom
	 * 
	 * @throws ParseException
	 */
	@Test
	public void parseRuleAndAtomTest() throws ParseException {
		List<LiteralImpl> literals = ASSyntax.parseBeliefs("p :- q. q.");
		assertEquals(2, literals.size());
		assertEquals("p :- q", literals.get(0).toString());
		assertEquals("q", literals.get(1).toString());
	}

	/**
	 * ~p
	 * 
	 * @throws ParseException
	 */
	@Test
	public void parseThilde() throws ParseException {
		LiteralImpl formula = (LiteralImpl) ASSyntax.parseFormula("~p");
		assertEquals("p", formula.getFunctor());
		assertFalse(formula.isAtom()); // ~(p) is not an atom.
		assertTrue(formula.negated());
	}

	@Test
	public void parseNoThilde() throws ParseException {
		LiteralImpl formula = (LiteralImpl) ASSyntax.parseFormula("p");
		assertEquals("p", formula.getFunctor());
		assertTrue(formula.isAtom());
		assertFalse(formula.negated());

	}

	/**
	 * newlines
	 * 
	 * @throws ParseException
	 */
	@Test
	public void parseNewlines() throws ParseException {
		List<LiteralImpl> literals = ASSyntax
				.parseBeliefs("\n\n\t\tp.\n\n\t q.\t\t");
		assertEquals(2, literals.size());
		assertEquals("p", literals.get(0).toString());
		assertEquals("q", literals.get(1).toString());
	}

	/**
	 * Testing this in parser because from the outset it was not clear how lists
	 * are represented in JASON.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void parseList() throws ParseException {
		ListTermImpl list = (ListTermImpl) ASSyntax.parseList("[1,2,3,4]");
		assertTrue(list.isList());
		assertFalse(list.isLiteral());
		assertEquals(".", list.getFunctor());
		assertEquals(2, list.getArity());
	}

	@Test
	public void parseEmptyList() throws ParseException {
		ListTermImpl list = (ListTermImpl) ASSyntax.parseList("[]");
		assertTrue(list.isList());
		assertFalse(list.isLiteral());
		assertEquals(".", list.getFunctor());
		assertEquals(0, list.getArity());
	}

	@Test
	public void parseTermWithList() throws ParseException {
		Structure term = (Structure) ASSyntax.parseFormula("p([1,2,3,4])");
		assertTrue(term.getTerm(0).isList());
	}

	@Test
	public void parseString() throws ParseException {
		StringTermImpl list = (StringTermImpl) ASSyntax.parseTerm("\"p\"");
		assertTrue(list.isString());
	}

	@Test
	public void parseNotp() throws ParseException {

		Literal query = ASSyntax.parseStructure("not p");
	}

	@Test
	public void parseNotP() throws ParseException {
		BeliefBase bb = new DefaultBeliefBase();
		Literal lit = ASSyntax.parseLiteral("~p");
		bb.add(lit);
	}

	@Test
	public void parseRule() throws ParseException {
		ASSyntax.parseRule("clear(X) :- block(X) & not( on(_, X) ).");
	}

	@Test
	public void parseBeliefs() throws ParseException {
		ASSyntax.parseBeliefs("		block(X) :- on(X, _).\n      	clear(X) :- block(X) & not( on(_, X) ).		clear(table).\n		tower([X]) :- on(X, table).\n		tower([X, Y| T]) :- on(X, Y) & tower([Y| T]).");
	}

	/** Smoke test, we want only ParseExceptions, not RuntimeExceptions */
	@Test(expected = ParseException.class)
	public void parsePercent() throws ParseException {
		Term formula = ASSyntax.parseTerm("% this is comment");
	}

	@Test
	public void ParseUpdate() throws ParseException {
		ASSyntax.parseFormula("not (on(X,Z)) & on(X,Y)");
	}

	/**
	 * Check that we get an error referring to the single quote.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void ParseSingleQuoteError() throws ParseException {
		try {
			ASSyntax.parseFormula("X='table'");
		} catch (ParseException e) {
			assertTrue(e.getCause().getMessage()
					.contains("Encountered: \"\\\'\""));
		}
	}

}