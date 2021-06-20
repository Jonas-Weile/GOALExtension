package jasonkri;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Atom;
import jason.asSyntax.Plan;
import jason.asSyntax.VarTerm;
import jason.asSyntax.parser.ParseException;
import jasonkri.language.JasonExpression;
import jasonkri.language.JasonVar;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

public class ExpressionTest {

	private Set<JasonVar> set(String... names) {
		Set<JasonVar> set = new HashSet<JasonVar>();

		for (String name : names) {
			set.add(new JasonVar(new VarTerm(name), null));
		}
		return set;
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNoPlans() throws ParseException {
		Plan plan = ASSyntax
				.parsePlan("+!safe(X,Y) : safe(X,Y) <- .print(safe(X,Y)).");
		// this should fail, we do not handle plans.
		new JasonExpression(plan, null);
	}

	/**
	 * Test that we canNOT put numbers in a JasonExpression. #3580
	 * 
	 * @throws ParseException
	 */
	@Test
	public void sigTestIntDoesNotFitInExpression() throws ParseException {
		JasonExpression expr = new JasonExpression(ASSyntax.parseNumber("1"),
				null);
		assertEquals(null, expr.getSignature());
	}

	@Test
	public void sigTestAtom() throws ParseException {
		JasonExpression expr = new JasonExpression(
				ASSyntax.parseStructure("aap"), null);
		assertEquals("aap/0", expr.getSignature());
		assertTrue(expr.isAtom());
		assertFalse(expr.isArithExpr());
		assertTrue(expr.isClosed());
		assertEquals(expr.getFreeVar(), set());
	}

	/**
	 * Test with atom object.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void sigTestAtom1() throws ParseException {
		JasonExpression expr = new JasonExpression(new Atom("aap"), null);
		assertEquals("aap/0", expr.getSignature());
		assertEquals(expr.getFreeVar(), set());
	}

	@Test
	public void sigTestTerm2() throws ParseException {
		JasonExpression expr = new JasonExpression(
				ASSyntax.parseStructure("aap(1,X)"), null);
		assertEquals("aap/2", expr.getSignature());
		assertEquals("aap", expr.getName());
		assertFalse(expr.isAtom());
		assertFalse(expr.isArithExpr());
		assertFalse(expr.isClosed());
		assertEquals(expr.getFreeVar(), set("X"));

	}

	@Test
	public void testVar() throws ParseException {
		JasonExpression expr = new JasonExpression(
				ASSyntax.parseStructure("X"), null);
		assertEquals("X", expr.getName());
		assertEquals("X/0", expr.getSignature());
		assertFalse(expr.isVar()); // it's extending JasonVar (but could be
									// parsed as var)
		assertFalse(expr.isArithExpr());
		assertFalse(expr.isClosed());
		assertEquals(expr.getFreeVar(), set("X"));

	}

	@Test
	public void testVarEquals() throws ParseException {
		VarTerm x1 = ASSyntax.parseVar("X");
		VarTerm x2 = ASSyntax.parseVar("X");
		assertTrue(x1.equals(x2));
	}

	@Test
	public void testVarHashCode() throws ParseException {
		VarTerm x1 = ASSyntax.parseVar("X");
		VarTerm x2 = ASSyntax.parseVar("X");
		assertTrue(x1.hashCode() == x2.hashCode());
	}

	@Test
	public void sigTestEquation() throws ParseException {
		JasonExpression expr = new JasonExpression(
				ASSyntax.parseStructure("X = 1 + 2"), null);
		assertEquals(" = /2", expr.getSignature());
		assertEquals(" = ", expr.getName());
		assertFalse(expr.isArithExpr());
		assertFalse(expr.isAtom());
		assertFalse(expr.isClosed());

	}

	@Test
	public void sigTestEquation1() throws ParseException {
		JasonExpression expr = new JasonExpression(
				ASSyntax.parseStructure("3 = 1 + 2"), null);
		assertEquals(" = /2", expr.getSignature());
		assertEquals(" = ", expr.getName());
		assertFalse(expr.isArithExpr());
		assertFalse(expr.isAtom());
		assertTrue(expr.isClosed());

	}

	@Test
	public void sigTestArithmetic() throws ParseException {
		JasonExpression expr = new JasonExpression(
				ASSyntax.parseStructure("1 + 2"), null);
		assertEquals("+/2", expr.getSignature());
		assertEquals("+", expr.getName());
		assertTrue(expr.isArithExpr());
		assertFalse(expr.isAtom());
		assertTrue(expr.isClosed());
	}

	@Test
	public void sigTestConjunct() throws ParseException {
		JasonExpression expr = new JasonExpression(
				ASSyntax.parseStructure("aap & beer"), null);
		assertEquals(" & /2", expr.getSignature());
		assertEquals(" & ", expr.getName());
		assertFalse(expr.isArithExpr());
		assertFalse(expr.isAtom());
		assertTrue(expr.isClosed());
		assertEquals(expr.getFreeVar(), set());

	}

	/**
	 * Check if we can handle clause in Term. Notice, unlike SWI Prolog the
	 * clause signature comes entirely from the head of the clause!
	 * 
	 * @throws ParseException
	 */
	@Test
	public void sigTestClause() throws ParseException {
		JasonExpression expr = new JasonExpression(
				ASSyntax.parseRule("aap :- beer."), null);
		assertEquals("aap/0", expr.getSignature());
		assertEquals("aap", expr.getName());
		assertFalse(expr.isArithExpr());
		assertTrue(expr.isAtom());
		assertTrue(expr.isClosed());
	}

	/**
	 * Check if we can handle clause in Term. Notice, unlike SWI Prolog the
	 * clause signature, isClosed etc depends solely on the head of the clause!
	 * 
	 * @throws ParseException
	 */
	@Test
	public void sigTestClauseVarInBody() throws ParseException {
		JasonExpression expr = new JasonExpression(
				ASSyntax.parseRule("aap :- beer(X)."), null);
		assertEquals("aap/0", expr.getSignature());
		assertEquals("aap", expr.getName());
		assertFalse(expr.isArithExpr());
		assertTrue(expr.isAtom());
		assertFalse(expr.isClosed());
		assertEquals(expr.getFreeVar(), set("X"));

	}

	/**
	 * Check if we can handle clause in Term. Notice, unlike SWI Prolog the
	 * clause signature, isClosed etc depends solely on the head of the clause!
	 * 
	 * @throws ParseException
	 */
	@Test
	public void testRelExprInBody() throws ParseException {
		JasonExpression expr = new JasonExpression(
				ASSyntax.parseRule("aap :- X>3."), null);
		assertEquals("aap/0", expr.getSignature());
		assertEquals("aap", expr.getName());
		assertFalse(expr.isArithExpr());
		assertTrue(expr.isAtom());
		assertFalse(expr.isClosed());
	}

	/**
	 * Check if we can handle clause in Term. Notice, unlike SWI Prolog the
	 * clause signature comes entirely from the head of the clause!
	 * 
	 * @throws ParseException
	 */
	@Test
	public void sigTestClauseVarInHead() throws ParseException {
		JasonExpression expr = new JasonExpression(
				ASSyntax.parseRule("aap(X) :- beer."), null);
		assertEquals("aap/1", expr.getSignature());
		assertEquals("aap", expr.getName());
		assertFalse(expr.isArithExpr());
		assertFalse(expr.isAtom());
		assertFalse(expr.isClosed());
		assertEquals(expr.getFreeVar(), set("X"));

	}

	/**
	 * Check if we can handle conjuncts
	 * 
	 * @throws ParseException
	 */
	@Test
	public void sigTestConjunct1() throws ParseException {
		JasonExpression expr = new JasonExpression(
				ASSyntax.parseFormula("clear(X) & clear(Y)"), null);
		assertEquals(" & /2", expr.getSignature());
		assertFalse(expr.isArithExpr());
		assertFalse(expr.isAtom());
		assertFalse(expr.isClosed());
		assertEquals(set("X", "Y"), expr.getFreeVar());
	}

	/**
	 * Check if we can handle conjuncts
	 * 
	 * @throws ParseException
	 */
	@Test
	public void sigTestConjunct2() throws ParseException {
		JasonExpression expr = new JasonExpression(
				ASSyntax.parseFormula("percept( on(X, Y) ) & not( on(X, Y) )"),
				null);
		assertEquals(" & /2", expr.getSignature());
		assertFalse(expr.isArithExpr());
		assertFalse(expr.isAtom());
		assertFalse(expr.isClosed());
		assertEquals(set("X", "Y"), expr.getFreeVar());
	}

	/**
	 * Check if we can handle conjuncts
	 * 
	 * @throws ParseException
	 */
	@Test
	public void sigTestConjunct3() throws ParseException {
		JasonExpression expr = new JasonExpression(
				ASSyntax.parseFormula("clear(X) & clear(Y) & on(X, Z) & not( on(X, Y) ) & not( X=Y )"),
				null);
		assertEquals(" & /2", expr.getSignature());
		assertFalse(expr.isArithExpr());
		assertFalse(expr.isAtom());
		assertFalse(expr.isClosed());
		assertEquals(set("X", "Y", "Z"), expr.getFreeVar());
	}

}