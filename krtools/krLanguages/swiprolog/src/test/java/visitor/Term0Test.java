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

import org.junit.Before;
import org.junit.Test;

import krTools.exceptions.KRInitFailedException;
import krTools.exceptions.ParserException;
import swiprolog.SwiPrologInterface;
import swiprolog.language.PrologTerm;
import swiprolog.parser.Parser4;
import swiprolog.visitor.Visitor4;

/**
 * Tests for Prolog4Parser term0 to see if pipeline parser->visitor works ok.
 * This is already a kind of end-to-end test as we do not stub the parser and
 * also we hook in SWI prolog.
 *
 */
public class Term0Test {

	@Before
	public void init() throws KRInitFailedException {
		new SwiPrologInterface();
	}

	/**
	 * Default version of {@link #checkVisitsAsTerm0(String, String)} where input
	 * and output are exptected identical.
	 *
	 * @param text the text to parse.
	 */
	private void checkVisitsAsTerm0(String text) throws Exception {
		checkVisitsAsTerm0(text, text);
	}

	/**
	 * Test that the parsed(in) {@link PrologTerm}.toString == out
	 *
	 * @param in  the string to parse
	 * @param out the expected result
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
	public void testVariable() throws Exception {
		checkVisitsAsTerm0("X");
	}

	@Test
	public void testVariable2() throws Exception {
		checkVisitsAsTerm0("_123");
	}

	@Test
	public void testString() throws Exception {
		checkVisitsAsTerm0("'Aap'", "Aap");
	}

	@Test
	public void testString1() throws Exception {
		checkVisitsAsTerm0("\"Aap\"", "Aap");
	}

	@Test
	public void testAtom() throws Exception {
		checkVisitsAsTerm0("aap");
	}

	@Test
	public void testString2() throws Exception {
		checkVisitsAsTerm0("`Aap`", "Aap");
	}

	@Test(expected = ParserException.class)
	public void testString3() throws Exception {
		checkVisitsAsTerm0("`Aap'");
	}
}
