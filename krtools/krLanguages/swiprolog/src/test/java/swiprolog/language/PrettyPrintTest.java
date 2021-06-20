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

package swiprolog.language;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import swiprolog.SwiPrologInterface;
import swiprolog.errors.PrologError;

/**
 * Test the pretty printing of terms.
 */
@RunWith(Parameterized.class)
public class PrettyPrintTest {
	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
				// terms covering toString itself
				{ "a, b" }, { "a, b , c" }, { "head :- body" }, { "head :- a , b , c" }, { "[p]" }, { "[a,b,c]" },
				{ "between(-1,1,X)" }, { "[[a,b,c],[d,e,f]]" }, { "[1,2|4]" }, { "[1,2|3]" }, { "3.1415" }, { "- p" },
				{ ":- p" }, { "'.'(1)" },
				// terms covering maybeBracketed
				{ "(a:-b),1" }, { " (a:-b):-c" }, { " a+(b - c)" }, { "(a,b), c" }, { "a + b - c" },
				{ " - - - - 2" } });
	}

	private final String input;

	public PrettyPrintTest(final String input) {
		this.input = input;
		new SwiPrologInterface();
	}

	@Test
	@SuppressWarnings("deprecation") // FIXME
	public void test() {
		final org.jpl7.Term list = org.jpl7.Util.textToTerm(this.input);
		/*
		 * With some terms like "between(-1,1,X), JPLUtils is playing some weird tricks:
		 * it inserts whitespaces where they are not in the original term, and removes
		 * them where they are in the original term.
		 */
		assertEquals(this.input.replaceAll(" ", ""), PrologError.fromJpl(list).toString().replaceAll(" ", ""));
	}
}