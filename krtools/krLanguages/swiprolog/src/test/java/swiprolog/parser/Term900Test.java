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
import swiprolog.parser.Prolog4Parser.Term1000Context;

/**
 * Tests for Prolog4Parser term900. predicates are term0 but the argument list
 * inside the predicate are term900 elements.
 */
public class Term900Test {
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

	/**
	 * Checks that two ':' separated texts (which should be term0 parse-able
	 * texts) are parsed properly.
	 */
	private void checkParsesAsTerm0(String text1, String text2) throws Exception {
		String text = text1 + ":" + text2;
		Parser4 parser = getParser(text1);
		Term0Context tree = parser.term0();
		if (parser.getErrors().isEmpty()) {
			System.out.println(text + " -> " + parser.toStringTree(tree));
			assertEquals(text2, parser.toStringTree(tree));
		} else {
			throw parser.getErrors().first();
		}
	}

	/**
	 * Checks term parses as term1000.
	 */
	private void checkParsesAsTerm1000(String text1, String text2) throws Exception {
		Parser4 parser = getParser(text1);
		Term1000Context tree = parser.term1000();
		if (parser.getErrors().isEmpty()) {
			System.out.println(text1 + " -> " + parser.toStringTree(tree));
			assertEquals(text2, parser.toStringTree(tree));
		} else {
			throw parser.getErrors().first();
		}
	}

	@Test
	public void testTerm1() throws Exception {
		checkParsesAsTerm0("aap(1)",
				"(term0 aap ( (arglist (expression (term900 (term700 (term500 (term400 (term200 (term100 (term50 (term0 1)))))))))) ))");
	}

	@Test
	public void testList1() throws Exception {
		checkParsesAsTerm0("[1,2]",
				"(term0 (listterm [ (items (expression (term900 (term700 (term500 (term400 (term200 (term100 (term50 (term0 1))))))))) , (items (expression (term900 (term700 (term500 (term400 (term200 (term100 (term50 (term0 2))))))))))) ]))");
	}

	@Test
	public void testList2() throws Exception {
		checkParsesAsTerm0("[1|X]",
				"(term0 (listterm [ (items (expression (term900 (term700 (term500 (term400 (term200 (term100 (term50 (term0 1))))))))) | X) ]))");
	}

	@Test
	public void testList3() throws Exception {
		checkParsesAsTerm0("[1,2|X]",
				"(term0 (listterm [ (items (expression (term900 (term700 (term500 (term400 (term200 (term100 (term50 (term0 1))))))))) , (items (expression (term900 (term700 (term500 (term400 (term200 (term100 (term50 (term0 2))))))))) | X)) ]))");
	}

	// disabled, #3555, not clear why this is not allowed.
	public void testList4() throws Exception {
		checkParsesAsTerm0("[1|2]",
				"(term0 (listterm [ (items (expression (term900 (term700 (term500 (term400 (term200 (term100 (term50 (term0 1))))))))) , (items (expression (term900 (term700 (term500 (term400 (term200 (term100 (term50 (term0 2))))))))) | X)) ]))");
	}

	/**
	 * Bit complex case. '1;2' does not parse as term900. The parser seems to
	 * try the deepest possibilities first
	 */
	@Test(expected = ParserException.class)
	public void testList5() throws Exception {
		checkParsesAsTerm1000("1;2", "");
	}
}
