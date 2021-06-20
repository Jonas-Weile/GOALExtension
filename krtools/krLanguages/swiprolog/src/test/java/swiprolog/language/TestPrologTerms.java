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

import org.junit.Test;

import krTools.language.Term;
import krTools.language.Var;
import swiprolog.SwiPrologInterface;
import swiprolog.language.impl.PrologImplFactory;

public class TestPrologTerms {
	@Test
	public void testToString() {
		new SwiPrologInterface();
		Term term = PrologImplFactory.getAtom("Aap", null);
		assertEquals("Aap", term.toString());
	}

	@Test
	public void testEqualVars() {
		Var X = PrologImplFactory.getVar("X", null);
		Var X1 = PrologImplFactory.getVar("X", null);

		assertEquals(X, X1);
	}
}
