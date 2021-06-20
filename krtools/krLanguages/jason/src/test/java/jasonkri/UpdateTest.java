package jasonkri;

import static org.junit.Assert.assertEquals;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.parser.ParseException;
import jasonkri.language.JasonUpdate;

import org.junit.Test;

/**
 * Test {@link JasonUpdate}
 * 
 * @author W.Pasman 10jun15
 *
 */
public class UpdateTest {

	@Test
	public void ConstructSmokeTest1() throws ParseException {
		new JasonUpdate(ASSyntax.parseFormula("p"), null);
	}

	@Test
	public void ConstructSmokeTest2() throws ParseException {
		new JasonUpdate(ASSyntax.parseFormula("~p"), null);
	}

	@Test
	public void ConstructSmokeTest3() throws ParseException {
		new JasonUpdate(ASSyntax.parseFormula("not p"), null);
	}

	@Test
	public void ConstructSmokeTest4() throws ParseException {
		// updates take ~, not "not"
		new JasonUpdate(ASSyntax.parseFormula("p & ~q & r"), null);
	}

	@Test
	public void ConstructSmokeTest5() throws ParseException {
		// updates take ~, not "not"
		new JasonUpdate(ASSyntax.parseFormula("p & ~q & not ~r"), null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void ConstructSmokeTest6() throws ParseException {
		// 'not r' is not a term that can be deleted.
		new JasonUpdate(ASSyntax.parseFormula("not not r"), null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void ConstructSmokeTest7() throws ParseException {
		// disjunct not allowed.
		new JasonUpdate(ASSyntax.parseFormula("r | s"), null);
	}

	@Test
	public void checkAddDeleteList1() throws ParseException {
		JasonUpdate update = new JasonUpdate(ASSyntax.parseFormula("p & q"),
				null);
		assertEquals(update.getAddList().size(), 2);
		assertEquals(update.getDeleteList().size(), 0);
		assertEquals("p", update.getAddList().get(0).toString());
		assertEquals("q", update.getAddList().get(1).toString());
	}

	@Test
	public void checkAddDeleteList2() throws ParseException {
		JasonUpdate update = new JasonUpdate(
				ASSyntax.parseFormula("not p & not ~q"), null);
		assertEquals(update.getAddList().size(), 0);
		assertEquals(update.getDeleteList().size(), 2);
		assertEquals("p", update.getDeleteList().get(0).toString());
		assertEquals("~q", update.getDeleteList().get(1).toString());
	}

	@Test
	public void checkTrue() throws ParseException {
		JasonUpdate update = new JasonUpdate(ASSyntax.parseFormula("true"),
				null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void checkFalse() throws ParseException {
		JasonUpdate update = new JasonUpdate(ASSyntax.parseFormula("false"),
				null);
	}

	/**
	 * The following test points to a bug in the Jason parser. We can't fix it.
	 * 
	 * @throws ParseException
	 */
	// @Test(expected = IllegalArgumentException.class)
	public void checkOne() throws ParseException {
		JasonUpdate update = new JasonUpdate(ASSyntax.parseFormula("1"), null);
	}

}
