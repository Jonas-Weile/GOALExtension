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
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;
import swiprolog.language.impl.PrologImplFactory;

public class TestPrologSubstitution {
	@Test
	public void testSubstitution() {
		Substitution solution = new PrologSubstitution();
		Var X = PrologImplFactory.getVar("X", null);
		Var Y = PrologImplFactory.getVar("Y", null);
		Var Z = PrologImplFactory.getVar("Z", null);
		solution.addBinding(X, Y);
		solution.addBinding(Y, Z);

		Term term = PrologImplFactory.getCompound("aap", new Term[] { X, Y }, null);

		Term result = term.applySubst(solution);
		assertTrue(result instanceof PrologCompound);
		PrologCompound compound = (PrologCompound) result;
		assertEquals(Y, compound.getArg(0));
		assertEquals(Z, compound.getArg(1));
	}

	@Test
	public void testToString() {
		Substitution solution = new PrologSubstitution();
		assertTrue(solution.getVariables().isEmpty());

		Var X = PrologImplFactory.getVar("X", null);
		Term a = PrologImplFactory.getAtom("a", null);
		solution.addBinding(X, a);
		assertEquals(1, solution.getVariables().size());
		assertEquals(a, solution.get(X));

		Var Y = PrologImplFactory.getVar("Y", null);
		Term b = PrologImplFactory.getAtom("b", null);
		solution.addBinding(Y, b);
		assertEquals(2, solution.getVariables().size());
		assertEquals(a, solution.get(X));
		assertEquals(b, solution.get(Y));

		Var Z = PrologImplFactory.getVar("Z", null);
		Term V = PrologImplFactory.getVar("V", null);
		solution.addBinding(Z, V);
		assertEquals(3, solution.getVariables().size());
		assertEquals(a, solution.get(X));
		assertEquals(b, solution.get(Y));
		assertEquals(V, solution.get(Z));
	}
}
