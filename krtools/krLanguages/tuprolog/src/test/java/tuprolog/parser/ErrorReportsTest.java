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

import java.io.StringReader;

import org.junit.Test;

import tuprolog.errors.ParserErrorMessages;

/**
 * Tests the error reports coming from the {@link Parser4}.
 */
public class ErrorReportsTest {
	/**
	 * Creates parser to parse given text.
	 *
	 * @return The {@link Parser4} for the text.
	 */
	private Parser4 getParser(String text) throws Exception {
		return new Parser4(new StringReader(text), null);
	}

	/**
	 * Check that we have implemented all prettyprint rules in
	 * prettyPrintRuleContext.
	 */
	@Test
	public void checkAllTokensTranslated() {
		ErrorStrategy4 strat = new ErrorStrategy4();
		for (int n = 0; n < Prolog4Parser.ruleNames.length; n++) {
			strat.prettyPrintRuleContext(n);
		}
	}

	@Test
	public void testSpuriousText() throws Exception {
		// term0 will not eat the second number.
		Parser4 parser = getParser("100.3 200 ");
		parser.term0();

		assertEquals(1, parser.getErrors().size());
		assertEquals(2, parser.getErrors().first().getLineNumber());
		assertEquals(11, parser.getErrors().first().getCharacterPosition());
	}

	@Test
	public void testSpuriousText2() throws Exception {
		Parser4 parser = getParser("\n\n\n100.3 200 ");
		parser.term0();

		assertEquals(1, parser.getErrors().size());
		assertEquals(5, parser.getErrors().first().getLineNumber());
	}

	@Test
	public void testBadText() throws Exception {
		Parser4 parser = getParser("ç");
		parser.term0();

		assertEquals(2, parser.getErrors().size());
		assertEquals(ParserErrorMessages.CANNOT_BE_USED.toReadableString("ç"), parser.getErrors().first().getMessage());
		// TODO: other error?!
	}
}
