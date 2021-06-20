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

package swiprolog.parser;

import static org.junit.Assert.assertEquals;

import java.io.Reader;
import java.io.StringReader;

import org.junit.Test;

import krTools.exceptions.ParserException;
import swiprolog.parser.Prolog4Parser.Term0Context;

/**
 * Tests for Prolog4Parser term0
 */
public class Term0Test {
	/**
	 * Parses the textStream.
	 *
	 * @return The ANTLR parser for the file.
	 */
	private Parser4 getParser(Reader textStream) throws Exception {
		Parser4 parser = new Parser4(textStream, null);
		parser.switchToFullLL();
		return parser;
	}

	private Parser4 getParser(String text) throws Exception {
		return getParser(new StringReader(text));
	}

	private void checkParsesAsTerm0(String text) throws Exception {
		Parser4 parser = getParser(text);
		Term0Context tree = parser.term0();
		if (parser.getErrors().isEmpty()) {
			System.out.println(text + " -> " + parser.toStringTree(tree));
			assertEquals("(term0 " + text + ")", parser.toStringTree(tree));
		} else {
			throw parser.getErrors().first();
		}
	}

	@Test(expected = ParserException.class)
	public void testLexerError() throws Exception {
		checkParsesAsTerm0("");
	}

	@Test
	public void testFloat() throws Exception {
		checkParsesAsTerm0("100.3");
	}

	@Test
	public void testFloat2() throws Exception {
		checkParsesAsTerm0("100.3e13");
	}

	@Test
	public void testFloat3() throws Exception {
		checkParsesAsTerm0("0.3e13");
	}

	@Test
	public void testInteger() throws Exception {
		checkParsesAsTerm0("12345");
	}

	@Test
	public void testBigInteger() throws Exception {
		checkParsesAsTerm0("123456789012345678901234567890123456789012345678901234567890");
	}

	@Test
	public void testVariable() throws Exception {
		checkParsesAsTerm0("X");
	}

	@Test
	public void testVariable2() throws Exception {
		checkParsesAsTerm0("_123");
	}

	@Test
	public void testString() throws Exception {
		checkParsesAsTerm0("'Aap'");
	}

	@Test
	public void testString1() throws Exception {
		checkParsesAsTerm0("\"Aap\"");
	}

	@Test
	public void testAtom() throws Exception {
		checkParsesAsTerm0("aap");
	}

	@Test
	public void testString2() throws Exception {
		checkParsesAsTerm0("`Aap`");
	}

	@Test(expected = ParserException.class)
	public void testString3() throws Exception {
		checkParsesAsTerm0("`Aap'");
	}
}
