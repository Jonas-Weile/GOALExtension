package jason;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.ArithExpr;
import jason.asSyntax.ArithExpr.ArithmeticOp;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.LiteralImpl;
import jason.asSyntax.LogExpr;
import jason.asSyntax.LogExpr.LogicalOp;
import jason.asSyntax.NumberTerm;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.RelExpr;
import jason.asSyntax.RelExpr.RelationalOp;
import jason.asSyntax.Rule;
import jason.asSyntax.Structure;
import jason.asSyntax.VarTerm;
import jason.asSyntax.parser.ParseException;
import jason.bb.BeliefBase;
import jason.bb.DefaultBeliefBase;

import java.util.Iterator;

import org.junit.Test;

/**
 * Test some basic functionality of JSON that we need.
 * 
 * @author W.Pasman 4jun15
 *
 */
public class BasicJasonTest {

	@Test
	public void testTermp() {
		LiteralImpl f1 = pZ();
		assertEquals("p", f1.getFunctor());
		assertEquals(1, f1.getArity());

	}

	@Test
	public void testTermHead() {
		LiteralImpl head = makeBasicTermq();
		assertEquals("q", head.getFunctor());
		assertEquals(1, head.getArity());

	}

	@Test
	public void testArithTerm() {
		RelExpr f2 = makeArithTerm();
		assertEquals(" = ", f2.getFunctor());
		assertEquals(2, f2.getArity());
	}

	@Test
	public void testRuleParser() throws ParseException {
		Rule clause = ASSyntax.parseRule("q(Y) :- p(Z) & Y = Z*3.");
		assertEquals("q", clause.getFunctor());
		assertTrue(clause.isRule());
		assertTrue(clause.getBody().isStructure());
		assertEquals(" & ", ((Structure) clause.getBody()).getFunctor());
	}

	@Test
	public void testClause() {
		Rule clause = makeClause();
		assertTrue(clause.isRule());
		assertTrue(!clause.isGround());
	}

	@Test
	public void testClause2() {
		Rule clause = makeClause2();
		assertTrue(clause.isRule());
		// jason only checks heads of rules.
		assertEquals("q", clause.getFunctor());
		assertTrue(clause.isGround());
	}

	public LiteralImpl pZ() {
		// f1: p(Z)
		LiteralImpl f1 = new LiteralImpl("p");
		f1.addTerm(new VarTerm("Z"));

		return f1;
	}

	/**
	 * @return q(Y)
	 */
	public LiteralImpl makeBasicTermq() {
		LiteralImpl head = new LiteralImpl("q");
		head.addTerm(new VarTerm("Y"));

		return head;
	}

	/**
	 * @return q(Y) :- p(Z) & Y = Z * 2.
	 */
	public Rule makeClause() {
		LiteralImpl head = makeBasicTermq();
		LiteralImpl f1 = pZ();
		RelExpr f2 = makeArithTerm();

		LogExpr body = new LogExpr(f1, LogicalOp.and, f2);
		Rule initbel = new Rule(head, body);
		return initbel;
	}

	/**
	 * @return q :- p(Z) & Y = Z * 2. Notice, no var in head.
	 */
	public Rule makeClause2() {
		LiteralImpl head = new LiteralImpl("q");
		LiteralImpl f1 = pZ();
		RelExpr f2 = makeArithTerm();

		LogExpr body = new LogExpr(f1, LogicalOp.and, f2);
		Rule initbel = new Rule(head, body);
		return initbel;
	}

	public RelExpr makeArithTerm() {
		// f2: Y=Z*2
		NumberTerm t1 = new VarTerm("Z");
		NumberTerm t2 = new NumberTermImpl(2);
		ArithExpr ztimes2 = new ArithExpr(t1, ArithmeticOp.times, t2);
		RelExpr f2 = new RelExpr(new VarTerm("Y"), RelationalOp.unify, ztimes2);
		return f2;
	}

	public LiteralImpl makeAtomp() {
		LiteralImpl belief = new LiteralImpl("p");
		belief.addTerm(new NumberTermImpl(3.2));
		return belief;
	}

	/**
	 * returns p(p(p(.......()))..)
	 * 
	 * @return
	 */
	private Literal cyclicTermp() {
		Literal query = new RelExpr(new VarTerm("Z"), RelationalOp.unify,
				pZ());
		BeliefBase bb = new DefaultBeliefBase();
		Iterator<Unifier> result = query.logicalConsequence(bb, new Unifier());
		return (Literal) result.next().get("Z");
	}

	/**
	 * Tests basic query, with an Atom in the beliefbase.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void basicQueryTest() throws ParseException {
		// set up database

		// add q clause q():-p()... and p() fact.
		BeliefBase bb = new DefaultBeliefBase();
		bb.add(new LiteralImpl("p"));

		/******* do query ************/
		System.out.println("agent beliefs=" + bb);

		Literal query = new Structure("p");
		Iterator<Unifier> result = query.logicalConsequence(bb, new Unifier());
		System.out.println("result(s) of query " + query + ":");

		assertTrue(result.hasNext());
		assertEquals("{}", result.next().toString());
	}

	/**
	 * We can not insert {@link Atom}s into the BB.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void noAtomInBbTest() throws ParseException {
		// set up database

		// add q clause q():-p()... and p() fact.
		BeliefBase bb = new DefaultBeliefBase();
		bb.add(new Atom("p"));
		// this will only PRINT to stdout
		// SEVERE: Error: 'p' can not be added in the belief base.

		assertEquals(0, bb.size());
	}

	/**
	 * Tests complex query, and calculation with multiplication of floats.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void complexQueryTest() throws ParseException {
		// set up database

		// add q clause q():-p()... and p() fact.
		BeliefBase bb = new DefaultBeliefBase();
		bb.add(makeClause());
		bb.add(makeAtomp());

		/******* do query ************/
		System.out.println("agent beliefs=" + bb);

		Literal query = new Structure("q");
		query.addTerm(new VarTerm("X"));
		Iterator<Unifier> result = query.logicalConsequence(bb, new Unifier());

		assertTrue(result.hasNext());
		assertEquals("{X=6.4}", result.next().toString());
	}

	/**
	 * Tests complex query, and calculation with multiplication of floats.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void testQueryOfRule() throws ParseException {
		// set up database
		// add q clause q():-p()... and p() fact.
		BeliefBase bb = new DefaultBeliefBase();
		bb.add(makeClause());

		/******* do query ************/
		System.out.println("agent beliefs=" + bb);

		Rule query = makeClause();

		Iterator<Unifier> result = query.logicalConsequence(bb, new Unifier());
		System.out.println("result(s) of query " + query + ":");

		assertFalse(result.hasNext()); // apparently, clause query returns NO.
	}

	/**
	 * Tests complex query, and calculation with multiplication of floats.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void testQueryOfCyclicTerm() throws ParseException {
		// set up database
		BeliefBase bb = new DefaultBeliefBase();
		bb.add(cyclicTermp());
		/******* do query ************/
		System.out.println("agent beliefs=" + bb);

		Literal query = cyclicTermp();
		Iterator<Unifier> result = query.logicalConsequence(bb, new Unifier());
		System.out.println("result(s) of query " + query + ":");
		assertTrue(result.hasNext()); // the query with cyclic term succeeds
	}
}