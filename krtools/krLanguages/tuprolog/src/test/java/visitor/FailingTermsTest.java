/**
 * Knowledge Representation Tools. Copyright (C) 2014 Koen Hindriks.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */

package visitor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import java.io.StringReader;

import org.junit.Test;

import krTools.exceptions.ParserException;
import tuprolog.errors.ParserErrorMessages;
import tuprolog.parser.Parser4;
import tuprolog.visitor.Visitor4;

/**
 * Tests for Prolog4Parser term0 to see if pipeline parser->visitor works ok.
 * This is already a kind of end-to-end test as we do not stub the parser and
 * also we hook in SWI prolog.
 */
public class FailingTermsTest {

	/**
	 * Test that the parsing the given term throws a {@link ParserException}
	 * exception and RETURNS the {@link ParserException}. This throws an
	 * exception if somethign else happens.
	 *
	 * @param in
	 *            the string to parse
	 * @param out
	 *            the expected result
	 */
	private ParserException checkFailsAsTerm1000(String in, String expectedErr) throws Exception {
		Visitor4 visitor = new Visitor4(new Parser4(new StringReader(in), null));
		visitor.visitTerm1000();

		// assertEquals(1, visitor.getErrors().size());
		assertFalse(visitor.getErrors().isEmpty());
		assertEquals(expectedErr, visitor.getErrors().first().getMessage());
		return visitor.getErrors().first();
	}

	@Test
	// this fails, I think correctly as {} has 1 parameter: see ISO p. 14. SWI
	// does accept it though?
	public void testEmptyCurlyList() throws Exception {
		ParserException exc = checkFailsAsTerm1000("{}", ParserErrorMessages.FOUND_BUT_NEED.toReadableString("'}'",
				ParserErrorMessages.TERM1200.toReadableString()));
		assertEquals(2, exc.getLineNumber());
		assertEquals(3, exc.getCharacterPosition());
	}

	@Test
	// :- is term1200 and paramlist holds term1000
	public void testTerm1200InList() throws Exception {
		checkFailsAsTerm1000("[asserta(bar(X) :- X), clause(bar(X), B)), [[B , call(X)]]]",
				ParserErrorMessages.FOUND_BUT_NEED.toReadableString("':-'",
						ParserErrorMessages.TERM900.toReadableString()));
	}

	@Test
	// :- is term1200 and paramlist holds term1000
	public void testTerm1200InListB() throws Exception {
		checkFailsAsTerm1000("assert(a:-b,c)", ParserErrorMessages.FOUND_BUT_NEED.toReadableString("':-'",
				ParserErrorMessages.TERM900.toReadableString()));
	}

	@Test
	// -- does not parse and results in 'extraneous input' message.
	public void testUnknownOperator() throws Exception {
		checkFailsAsTerm1000(">>> (1)", ParserErrorMessages.TOKEN_BAD.toReadableString("'>'"));
	}

	@Test
	// . % can not accept , as operator.
	public void testListWithoutFirstArgument() throws Exception {
		checkFailsAsTerm1000("[,(var(X), X=1), [[X ]]]", ParserErrorMessages.TOKEN_MISSING.toReadableString("']'"));
		// CHECK why is parser complaining about ] and not about ,?
	}

	@Test
	// :- is fx operator, so can have only lower-prio ops on the right.
	public void testDoubleImplication() throws Exception {
		ParserException exc = checkFailsAsTerm1000(":- :- a", ParserErrorMessages.FOUND_BUT_NEED
				.toReadableString("':-'", ParserErrorMessages.TERM900.toReadableString()));
		assertEquals(2, exc.getCharacterPosition());
		// CHECK Why isn't this position 3?
	}
}
