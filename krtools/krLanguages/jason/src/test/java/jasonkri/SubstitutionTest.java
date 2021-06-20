package jasonkri;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Structure;
import jason.asSyntax.VarTerm;
import jason.asSyntax.parser.ParseException;
import jasonkri.language.JasonExpression;
import jasonkri.language.JasonSubstitution;
import jasonkri.language.JasonVar;

import java.util.HashSet;
import java.util.Set;

import krTools.language.Expression;
import krTools.language.Substitution;
import krTools.language.Var;

import org.junit.Before;
import org.junit.Test;

public class SubstitutionTest {

	private VarTerm X;
	private Structure pX;
	private VarTerm Y;
	private VarTerm Z;
	private Structure p1;

	@Before
	public void init() throws ParseException {
		X = ASSyntax.parseVar("X");
		Y = ASSyntax.parseVar("Y");
		Z = ASSyntax.parseVar("Z");
		pX = ASSyntax.parseStructure("p(X)");
		p1 = ASSyntax.parseStructure("p(1)");
	}

	@Test
	public void testEmptySubsti() throws ParseException {
		Unifier unifier = new Unifier();
		assertEquals(0, unifier.size());
	}

	@Test
	public void testSubsti() throws ParseException {
		Unifier unifier = bindX2p();
		assertEquals(1, unifier.size());
	}

	private Unifier bindX2pX() throws ParseException {
		Unifier unifier = new Unifier();
		unifier.bind(X, pX);
		return unifier;
	}

	private Unifier bindXY() throws ParseException {
		Unifier unifier = new Unifier();
		unifier.bind(X, Y);
		return unifier;
	}

	/**
	 * Bind X=Y, Y=Z
	 * 
	 * @return
	 * @throws ParseException
	 */
	private Unifier bindXYZ() throws ParseException {
		Unifier unifier = new Unifier();
		unifier.bind(X, Y);
		unifier.bind(Y, Z);
		return unifier;
	}

	/**
	 * bind X=Y, Y=Z, Z=p1
	 * 
	 * @return
	 * @throws ParseException
	 */
	private Unifier bindXYZfull() throws ParseException {
		Unifier unifier = new Unifier();
		unifier.bind(X, Y);
		unifier.bind(Y, Z);
		unifier.bind(Z, p1);
		return unifier;
	}

	private Unifier bindX2p() throws ParseException {
		Unifier unifier = new Unifier();
		unifier.bind(X, p1);
		return unifier;
	}

	/**
	 * test substi with infinite recursion. Surprisingly works fine
	 * 
	 * @throws ParseException
	 */
	@Test
	public void testSubstiInfinite() throws ParseException {
		Unifier unifier = bindX2pX();
		assertEquals(1, unifier.size());
	}

	@Test
	public void testApplySubst() throws ParseException {
		JasonExpression expr = new JasonExpression(X, null);
		Substitution subst = new JasonSubstitution(bindX2p());
		Expression result = expr.applySubst(subst);
		System.out.println(expr + " with substitution " + subst + " -> "
				+ result);
	}

	/**
	 * Smoke test. It does not even bulge.
	 * 
	 * @throws ParseException
	 */
	@Test
	public void testApplySubstInfinity() throws ParseException {
		JasonExpression expr = new JasonExpression(X, null);
		Substitution subst = new JasonSubstitution(bindX2pX());
		Expression result = expr.applySubst(subst);
		System.out.println(expr + " with substitution " + subst + " -> "
				+ result);
	}

	/**
	 * chain substitution renaming vars. This is NOT working as expected.
	 */
	@Test
	public void testApplySubstXY() throws ParseException {
		JasonExpression expr = new JasonExpression(pX, null);
		Substitution subst = new JasonSubstitution(bindXY());
		Expression result = expr.applySubst(subst);
		System.out.println(expr + " with substitution " + subst + " -> "
				+ result);
		assertEquals("p(Y)", result.toString());
	}

	/**
	 * chain substitution renaming vars. This is NOT working as expected.
	 */
	@Test
	public void testApplySubstXYZ() throws ParseException {
		JasonExpression expr = new JasonExpression(pX, null);
		Substitution subst = new JasonSubstitution(bindXYZ());
		Expression result = expr.applySubst(subst);
		System.out.println(expr + " with substitution " + subst + " -> "
				+ result);
		assertEquals("p(Z)", result.toString());
	}

	/**
	 * Check retain X gives us 1 var.
	 */
	@Test
	public void testRetainAllX() throws ParseException {
		Substitution subst = new JasonSubstitution(bindXYZfull());
		Set<Var> vars = new HashSet<Var>();
		vars.add(new JasonVar(X, null));
		boolean varsWereRemoved = subst.retainAll(vars);
		assertTrue(varsWereRemoved);
		assertEquals(1, subst.getVariables().size());
	}

	/**
	 * Check retain XYZ gives us 3 var.
	 */
	@Test
	public void testRetainAllXYZ() throws ParseException {
		Substitution subst = new JasonSubstitution(bindXYZfull());
		Set<Var> vars = new HashSet<Var>();
		vars.add(new JasonVar(X, null));
		vars.add(new JasonVar(Y, null));
		vars.add(new JasonVar(Z, null));
		boolean varsWereRemoved = subst.retainAll(vars);
		assertFalse(varsWereRemoved);
		assertEquals(3, subst.getVariables().size());
	}

	/**
	 * Check retain XY gives us 1 var.
	 */
	@Test
	public void testRetainAllXY() throws ParseException {
		Substitution subst = new JasonSubstitution(bindXYZfull());
		Set<Var> vars = new HashSet<Var>();
		vars.add(new JasonVar(X, null));
		vars.add(new JasonVar(Y, null));
		boolean varsWereRemoved = subst.retainAll(vars);
		assertTrue(varsWereRemoved);
		assertEquals(2, subst.getVariables().size());
	}

	/**
	 * Check retain gives us 0 var.
	 */
	@Test
	public void testRetainAll() throws ParseException {
		Substitution subst = new JasonSubstitution(bindXYZfull());
		Set<Var> vars = new HashSet<Var>();
		boolean varsWereRemoved = subst.retainAll(vars);
		assertTrue(varsWereRemoved);
		assertEquals(0, subst.getVariables().size());
	}

}