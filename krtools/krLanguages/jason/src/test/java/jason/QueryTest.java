package jason;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.LiteralImpl;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.parser.ParseException;
import jason.bb.BeliefBase;
import jason.bb.DefaultBeliefBase;

import java.util.Iterator;

import org.junit.Test;

/**
 * Test jason inference system
 * 
 * @author W.Pasman 10jun15
 *
 */
public class QueryTest {

	@Test
	public void queryNotP() throws ParseException {
		BeliefBase bb = new DefaultBeliefBase();
		Literal lit = ASSyntax.parseLiteral("~p");
		bb.add(lit);

		LogicalFormula form = ASSyntax.parseFormula("not p");
		Iterator<Unifier> res = form.logicalConsequence(bb, new Unifier());
		assertTrue(res.hasNext());
		assertEquals("{}", res.next().toString());
	}

	/**
	 * believe p and query p. Have 1 result..
	 * 
	 * @throws ParseException
	 */
	@Test
	public void queryP() throws ParseException {
		BeliefBase bb = new DefaultBeliefBase();
		Literal lit = ASSyntax.parseLiteral("p");
		bb.add(lit);

		LogicalFormula form = ASSyntax.parseFormula("p");
		Iterator<Unifier> res = form.logicalConsequence(bb, new Unifier());
		assertTrue(res.hasNext());
	}

	/**
	 * believe not(p) and query p. No results.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void queryPFalse() throws ParseException {
		BeliefBase bb = new DefaultBeliefBase();
		Literal lit = ASSyntax.parseLiteral("~p");
		bb.add(lit);

		LogicalFormula form = ASSyntax.parseFormula("p");
		Iterator<Unifier> res = form.logicalConsequence(bb, new Unifier());
		assertFalse(res.hasNext());
	}

	/**
	 * Belief p and query atom p, instead of LiteralImpl p that comes out of
	 * parseLiteral.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void queryPwithAtom() throws ParseException {
		BeliefBase bb = new DefaultBeliefBase();
		Literal lit = ASSyntax.parseLiteral("p");
		bb.add(lit);

		Atom form = new Atom("p");
		Iterator<Unifier> res = form.logicalConsequence(bb, new Unifier());
		assertTrue(res.hasNext());
	}

	/**
	 * In Jason, you don't need to define predicates dynamic.
	 */
	@Test
	public void queryUndefinedP() throws ParseException {
		BeliefBase bb = new DefaultBeliefBase();

		LogicalFormula form = ASSyntax.parseFormula("p");
		Iterator<Unifier> res = form.logicalConsequence(bb, new Unifier());
		assertFalse(res.hasNext());
	}

	/**
	 * Undefined predicates are not true.
	 */
	@Test
	public void queryNotUndefinedP() throws ParseException {
		BeliefBase bb = new DefaultBeliefBase();

		LogicalFormula form = ASSyntax.parseFormula("not p");
		Iterator<Unifier> res = form.logicalConsequence(bb, new Unifier());
		assertTrue(res.hasNext());
		assertEquals("{}", res.next().toString());
	}

	/**
	 * Belief p(q) where p is proper LiteralImpl but q an atom and then query
	 * p(X). This still works fine.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void queryMix() throws ParseException {
		BeliefBase bb = new DefaultBeliefBase();
		Literal lit = new LiteralImpl("p");
		lit.addTerm(new Atom("q"));
		bb.add(lit);

		LogicalFormula form = ASSyntax.parseFormula("p(X)");
		Iterator<Unifier> res = form.logicalConsequence(bb, new Unifier());
		assertTrue(res.hasNext());
		Unifier result = res.next();
		assertFalse(res.hasNext());

		assertEquals("q", result.get("X").toString());
	}

}