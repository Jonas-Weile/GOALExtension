package jasonkri;

import jason.asSyntax.VarTerm;
import jason.asSyntax.parser.ParseException;
import jasonkri.language.JasonTerm;
import jasonkri.language.JasonVar;

import org.junit.Test;

public class TermTest {
	/**
	 * Test that we canNOT put vars in a JasonTerm. #3722
	 * 
	 * @throws ParseException
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testVarInTermFails() throws ParseException {
		new JasonTerm(new VarTerm("X"), null);
	}

	/**
	 * Test that we can put vars in a JasonVar. #3722
	 * 
	 * @throws ParseException
	 */
	@Test
	public void testVarInJasonVar() throws ParseException {
		new JasonVar(new VarTerm("X"), null);
	}
}