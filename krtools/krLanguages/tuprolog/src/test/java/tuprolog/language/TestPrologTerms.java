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

package tuprolog.language;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import krTools.exceptions.KRInitFailedException;
import tuprolog.TuPrologInterface;

public class TestPrologTerms {

	@Before
	public void init() throws KRInitFailedException {
		new TuPrologInterface();
	}

	@Test
	public void testToString() {
		alice.tuprolog.Term term = new alice.tuprolog.Struct("Aap");
		assertEquals("'Aap'", term.toString());
	}

	/**
	 * JPL lower level test
	 */
	@Test
	public void testEqualVars() {
		alice.tuprolog.Var X = new alice.tuprolog.Var("X");
		alice.tuprolog.Var X1 = new alice.tuprolog.Var("X");

		assertEquals(X, X1);
	}

	/**
	 * PrologVariable test.
	 */
	@Test
	public void testEqualPrologVars() {
		PrologVar X = new PrologVar(new alice.tuprolog.Var("X"), null);
		PrologVar X1 = new PrologVar(new alice.tuprolog.Var("X"), null);

		assertEquals(X, X1);
	}
}
