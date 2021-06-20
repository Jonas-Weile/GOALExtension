package jason;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import jason.asSemantics.Unifier;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Structure;
import jason.asSyntax.VarTerm;
import jason.asSyntax.parser.ParseException;
import jason.bb.BeliefBase;
import jason.bb.DefaultBeliefBase;

import java.util.Iterator;

import org.junit.Test;

/**
 * Stress test that does "complex" computation that may take lots of memory or
 * CPU if not done smartly. This test takes about 35 seconds on a 3GHz 8core i7.
 * 
 * @author W.Pasman 4jun15
 *
 */
public class JasonFibonacciTest {
	@Test
	public void fiboTest() throws ParseException {
		// set up database
		/********* initialize beliefs **************/
		BeliefBase bb = new DefaultBeliefBase();
		bb.add(ASSyntax.parseLiteral("fib(1,1)"));
		bb.add(ASSyntax.parseLiteral("fib(2,2)"));
		bb.add(ASSyntax
				.parseRule("fib(X,FX):- X>0 & X1 = X-1 & X2 = X-2 & fib(X1, F1X) & fib(X2, F2X) & FX = F1X + F2X."));

		/******* do query ************/
		System.out.println("agent beliefs=" + bb);

		Literal query = new Structure("fib");
		query.addTerm(new NumberTermImpl(10));
		query.addTerm(new VarTerm("X"));
		Iterator<Unifier> result = query.logicalConsequence(bb, new Unifier());
		System.out.println("waiting result(s) of query " + query
				+ ". This may take 30s or more.");

		assertTrue(result.hasNext());
		assertEquals("{X=89}", result.next().toString());

	}
}