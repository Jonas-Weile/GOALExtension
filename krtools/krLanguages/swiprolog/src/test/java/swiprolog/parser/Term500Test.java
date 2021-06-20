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
import swiprolog.parser.Prolog4Parser.Term1000Context;

/**
 * Tests for Prolog4Parser term500. predicates are term0 but the argument list
 * inside the predicate are term500 elements. *
 */
public class Term500Test {
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
	private void checkParsesAsTerm1000(String text1, String text2) throws Exception {
		String text = text1 + ":" + text2;
		Parser4 parser = getParser(text1);
		Term1000Context tree = parser.term1000();
		if (parser.getErrors().isEmpty()) {
			System.out.println(text + " -> " + parser.toStringTree(tree));
			assertEquals(text2, parser.toStringTree(tree));
		} else {
			throw parser.getErrors().first();
		}
	}

	@Test(expected = ParserException.class)
	public void testNotTerm500() throws Exception {
		checkParsesAsTerm1000("X=Y=Z", "");
	}

	@Test
	public void testGoodTerm500() throws Exception {
		// the term500 is after the '=' sign.
		checkParsesAsTerm1000("kat = aap + beer",
				"(term1000 (term900 (term700 (term500 (term400 (term200 (term100 (term50 (term0 kat)))))) = (term500 (term400 (term200 (term100 (term50 (term0 aap))))) (term500b + (term400 (term200 (term100 (term50 (term0 beer))))))))))");
	}
}
