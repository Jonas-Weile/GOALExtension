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

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import org.junit.Test;

import krTools.exceptions.ParserException;
import swiprolog.errors.ParserErrorMessages;

/**
 * Tests for token errors.
 */
public class TokenErrorsTest {
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
	 * Check that given parse as prolog text throws the given error message.
	 *
	 * @param text
	 * @param errormess
	 * @throws IOException
	 * @throws ParserException
	 */
	private void failsParseAsProlog(String text, String errormess) throws Exception {
		Parser4 parser = getParser(text);
		parser.prologtext();
		if (parser.getErrors().isEmpty()) {
			throw new IllegalStateException("Parse of " + text + " should have failed");
		} else {
			assertEquals(errormess, parser.getErrors().first().getMessage());
		}
	}

	@Test
	public void testNoEnd() throws Exception {
		failsParseAsProlog("kata", ParserErrorMessages.TOKEN_MISSING.toReadableString("'.'"));
	}

	@Test
	public void testTwoNames() throws Exception {
		failsParseAsProlog("kata kata.", ParserErrorMessages.TOKEN_BAD.toReadableString("an atom 'kata'",
				ParserErrorMessages.TERM200.toReadableString()));

		// [ParserException: found an atom 'kata' but we need an (other)
		// operator here line 1, position 7]
	}

	@Test
	public void testExtraNumber() throws Exception {
		failsParseAsProlog("kata 1.", ParserErrorMessages.TOKEN_BAD.toReadableString("a number '1'",
				ParserErrorMessages.TERM200.toReadableString()));
	}

	@Test
	public void testExtraVariable() throws Exception {
		failsParseAsProlog("kata X.", ParserErrorMessages.TOKEN_BAD.toReadableString("a variable 'X'",
				ParserErrorMessages.TERM200.toReadableString()));

	}

	@Test
	public void testExtraString() throws Exception {
		failsParseAsProlog("kata \"X\".", ParserErrorMessages.TOKEN_BAD.toReadableString("a string \"X\"",
				ParserErrorMessages.TERM200.toReadableString()));
	}
}
