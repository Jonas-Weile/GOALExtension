package jasonkri;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.parser.ParseException;

import java.util.List;

import org.junit.Test;

public class UtilsTest {
	@Test
	public void testUnzip1() throws ParseException {
		List<LogicalFormula> res = Utils.getConjuncts(ASSyntax
				.parseFormula("p & q"));
		assertEquals(2, res.size());
		assertEquals("p", res.get(0).toString());
		assertEquals("q", res.get(1).toString());

	}

	@Test
	public void testUnzipOrder() throws ParseException {
		List<LogicalFormula> res = Utils.getConjuncts(ASSyntax
				.parseFormula("p & q & r & s"));
		assertEquals(4, res.size());
		assertEquals("p", res.get(0).toString());
		assertEquals("q", res.get(1).toString());
		assertEquals("r", res.get(2).toString());
		assertEquals("s", res.get(3).toString());
	}

	@Test
	public void testNot() throws ParseException {
		List<LogicalFormula> res = Utils.getConjuncts(ASSyntax
				.parseFormula("p & not q & r"));
		assertEquals(3, res.size());
		assertEquals("p", res.get(0).toString());
		assertEquals("not (q)", res.get(1).toString());
		assertEquals("r", res.get(2).toString());
	}

	@Test
	public void testNotWithBracket() throws ParseException {
		List<LogicalFormula> res = Utils.getConjuncts(ASSyntax
				.parseFormula("p & not(q) & r"));
		assertEquals(3, res.size());
		assertEquals("p", res.get(0).toString());
		assertEquals("not (q)", res.get(1).toString());
		assertEquals("r", res.get(2).toString());
	}

	@Test
	public void testOr() throws ParseException {
		List<LogicalFormula> res = Utils.getConjuncts(ASSyntax
				.parseFormula("p & q | r"));
		assertTrue(Utils.containsBinaryStructure(res));
	}

	@Test
	public void testHiddenOr() throws ParseException {
		List<LogicalFormula> res = Utils.getConjuncts(ASSyntax
				.parseFormula("p & not(q | r)"));
		assertTrue(Utils.containsBinaryStructure(res));

	}

	@Test
	public void testHiddenNot() throws ParseException {
		List<LogicalFormula> res = Utils.getConjuncts(ASSyntax
				.parseFormula("p & not( not q)"));
		assertTrue(Utils.containsBinaryStructure(res));

	}

	@Test
	public void testHiddenRelExpr() throws ParseException {
		List<LogicalFormula> res = Utils.getConjuncts(ASSyntax
				.parseFormula("p & not (X>3)"));
		assertTrue(Utils.containsBinaryStructure(res));
	}

	@Test
	public void testThilde() throws ParseException {
		List<LogicalFormula> res = Utils.getConjuncts(ASSyntax
				.parseFormula("p & ~q"));
		assertEquals(2, res.size());
		assertEquals("p", res.get(0).toString());
		assertEquals("~q", res.get(1).toString());

		assertFalse(Utils.containsBinaryStructure(res));
	}

}