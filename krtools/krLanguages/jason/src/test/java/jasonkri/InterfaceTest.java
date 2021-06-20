package jasonkri;

import static org.junit.Assert.assertEquals;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.VarTerm;
import jason.asSyntax.parser.ParseException;
import jasonkri.language.JasonTerm;
import jasonkri.language.JasonVar;

import java.util.HashMap;

import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;

import org.junit.Before;
import org.junit.Test;

public class InterfaceTest {
	private JasonInterface jasonInterface;
	private JasonVar x;
	private JasonVar y;
	private JasonTerm pq;
	private HashMap<Var, Term> map;

	@Before
	public void init() throws ParseException {
		jasonInterface = new JasonInterface();
		map = new HashMap<Var, Term>();
		x = new JasonVar(new VarTerm("X"), null);
		y = new JasonVar(new VarTerm("X"), null);
		pq = new JasonTerm(ASSyntax.parseFormula("p & q"), null);
	}

	@Test
	public void testMakeEmptySubsti() {
		Substitution subst = jasonInterface.getSubstitution(map);
		assertEquals(0, subst.getVariables().size());

	}

	@Test
	public void testMakeSubstXToPq() {
		map.put(x, pq);
		Substitution subst = jasonInterface.getSubstitution(map);
		assertEquals(1, subst.getVariables().size());

	}

	@Test
	public void testMakeSubstXToY() {
		map.put(x, y);
		Substitution subst = jasonInterface.getSubstitution(map);
		assertEquals(1, subst.getVariables().size());
	}
}
