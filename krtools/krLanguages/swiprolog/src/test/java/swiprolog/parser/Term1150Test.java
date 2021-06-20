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

import swiprolog.parser.Prolog4Parser.Term1150Context;

/**
 * Tests for Prolog4Parser term900. predicates are term0 but the argument list
 * inside the predicate are term900 elements.
 */
public class Term1150Test {
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
	 * Checks term parses as term1150.
	 */
	private void checkParsesAsTerm1150(String in, String out) throws Exception {
		Parser4 parser = getParser(in);
		Term1150Context tree = parser.term1150();
		if (parser.getErrors().isEmpty()) {
			System.out.println(in + " -> " + parser.toStringTree(tree));
			assertEquals(out, parser.toStringTree(tree));
		} else {
			throw parser.getErrors().first();
		}
	}

	/**
	 * Bit complex case. '1;2' does not parse as term900. The parser seems to
	 * try the deepest possibilities first
	 */
	@Test
	public void testDynamicPredicate1() throws Exception {
		checkParsesAsTerm1150("dynamic on/2, clear/0",
				"(term1150 dynamic (term1000 (term900 (term700 (term500 (term400 (term200 (term100 (term50 (term0 on)))) (term400b / (term200 (term100 (term50 (term0 2))))))))) , (term1000 (term900 (term700 (term500 (term400 (term200 (term100 (term50 (term0 clear)))) (term400b / (term200 (term100 (term50 (term0 0))))))))))))");
	}
}
