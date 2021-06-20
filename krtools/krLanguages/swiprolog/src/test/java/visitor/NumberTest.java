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

import java.io.StringReader;

import org.junit.Test;

import krTools.exceptions.ParserException;
import swiprolog.language.PrologTerm;
import swiprolog.parser.Parser4;
import swiprolog.visitor.Visitor4;

/**
 * Tests for Prolog4Parser term0 to see if pipeline parser->visitor works ok.
 * This is already a kind of end-to-end test as we do not stub the parser and
 * also we hook in SWI prolog.
 *
 */
public class NumberTest {

	/**
	 * Default version of {@link #checkVisitsAsTerm0(String, String)} where
	 * input and output are exptected identical.
	 *
	 * @param text
	 *            the text to parse.
	 */
	private void checkVisitsAsTerm0(String text) throws Exception {
		checkVisitsAsTerm0(text, text);
	}

	/**
	 * Test that the parsed(in) {@link PrologTerm}.toString == out
	 *
	 * @param in
	 *            the string to parse
	 * @param out
	 *            the expected result
	 */
	private void checkVisitsAsTerm0(String in, String out) throws Exception {
		Visitor4 visitor = new Visitor4(new Parser4(new StringReader(in), null));
		PrologTerm term = visitor.visitTerm0();
		if (visitor.getErrors().isEmpty()) {
			System.out.println(in + " -> " + term);
			assertEquals(out, term.toString());
		} else {
			throw visitor.getErrors().first();
		}
	}

	@Test
	public void testFloat() throws Exception {
		checkVisitsAsTerm0("100.3");
	}

	@Test
	public void testFloat2() throws Exception {
		checkVisitsAsTerm0("100.3e13", "1.003E15");
	}

	@Test
	public void testFloat3() throws Exception {
		checkVisitsAsTerm0("0.3e13", "3.0E12");
	}

	@Test
	public void testInteger() throws Exception {
		checkVisitsAsTerm0("12345");
	}

	@Test
	public void testBigInteger() throws Exception {
		checkVisitsAsTerm0("123456789012345678901234567890123456789012345678901234567890", "1.2345678901234567E59");
	}

	@Test
	public void testAlmostMaxInt() throws Exception {
		checkVisitsAsTerm0("2147483647", "2147483647");
	}

	@Test
	public void testMaxInt() throws Exception {
		checkVisitsAsTerm0("2147483648", "2.147483648E9");
	}

	@Test(expected = ParserException.class)
	public void testHugeNumber() throws Exception {
		checkVisitsAsTerm0("12.1e738273", "1.2345678901234567E59");
	}
}
