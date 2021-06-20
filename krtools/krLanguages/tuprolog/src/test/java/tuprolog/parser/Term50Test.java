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

package tuprolog.parser;

import static org.junit.Assert.assertEquals;

import java.io.InputStream;
import java.io.StringBufferInputStream;

import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.antlr.v4.runtime.tree.ParseTree;
import org.junit.Test;

/**
 * Tests for Prolog4Parser term50
 */
@SuppressWarnings("deprecation")
public class Term50Test {
	/**
	 * Parses the textStream.
	 *
	 * @return The ANTLR parser for the file.
	 */
	private Prolog4Parser getParser(InputStream textStream) throws Exception {
		ANTLRInputStream input = new ANTLRInputStream(textStream);
		Prolog4Lexer lexer = new Prolog4Lexer(input);
		CommonTokenStream tokens = new CommonTokenStream(lexer);
		Prolog4Parser parser = new Prolog4Parser(tokens);
		parser.getInterpreter().setPredictionMode(PredictionMode.LL_EXACT_AMBIG_DETECTION);
		return parser;
	}

	private Prolog4Parser getParser(String text) throws Exception {
		return getParser(new StringBufferInputStream(text));
	}

	/**
	 * Checks that two ':' separated texts (which should be term0 parse-able
	 * texts) are parsed properly.
	 */
	private void checkParsesAsTerm50(String text1, String text2) throws Exception {
		String text = text1 + ":" + text2;
		Prolog4Parser parser = getParser(text);
		ParseTree tree = parser.term50();
		System.out.println(text + " -> " + tree.toStringTree(parser));
		assertEquals("(term50 " + "(term0 " + text1 + ") : (term0 " + text2 + "))", tree.toStringTree(parser));
	}

	@Test
	public void testFloats() throws Exception {
		checkParsesAsTerm50("100.3", "100.3e13");
	}

	@Test
	public void testVariables() throws Exception {
		checkParsesAsTerm50("X", "Y");
	}

	@Test
	public void testVariable2() throws Exception {
		checkParsesAsTerm50("X", "_123");
	}

	@Test
	public void testStrings() throws Exception {
		checkParsesAsTerm50("'Aap'", "\"Aap\"");
	}

	@Test
	public void testMix1() throws Exception {
		checkParsesAsTerm50("12", "\"Aap\"");
	}

	@Test
	public void testMix2() throws Exception {
		checkParsesAsTerm50("aap", "\"Aap\"");
	}
}
