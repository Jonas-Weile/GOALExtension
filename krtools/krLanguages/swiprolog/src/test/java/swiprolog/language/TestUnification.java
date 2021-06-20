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

import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;
import swiprolog.language.impl.PrologImplFactory;

public class TestUnification {
	/**
	 * Test case: unification of basic constants.
	 */
	@Test
	public void test1unify() throws Exception {
		Term a = PrologImplFactory.getAtom("a", null);
		Term b = PrologImplFactory.getAtom("b", null);

		assertEquals(null, a.mgu(b));
	}

	/**
	 * Test case: unification of single variable X with constant a.
	 */
	@Test
	public void test2unify() {
		Var x = PrologImplFactory.getVar("X", null);
		Term a = PrologImplFactory.getAtom("a", null);

		Substitution unifier = new PrologSubstitution(x, a);
		assertEquals(unifier, x.mgu(a));
	}

	/**
	 * Test case: unification of variable X with term f(...) where ... does not
	 * contain X.
	 */
	@Test
	public void test3unify() {
		// f(a)
		Var X = PrologImplFactory.getVar("X", null);
		Term a = PrologImplFactory.getAtom("a", null);
		Term fa = PrologImplFactory.getCompound("f", new Term[] { a }, null);

		Substitution unifier1 = new PrologSubstitution(X, fa);
		assertEquals(unifier1, X.mgu(fa));
		assertEquals(unifier1, fa.mgu(X));

		// f(a, b)
		Term b = PrologImplFactory.getAtom("b", null);
		Term fab = PrologImplFactory.getCompound("f", new Term[] { a, b }, null);

		Substitution unifier2 = new PrologSubstitution(X, fab);
		assertEquals(unifier2, X.mgu(fab));
		assertEquals(unifier2, fab.mgu(X));

		// f(Y, b)
		Var Y = PrologImplFactory.getVar("Y", null);
		Term fYb = PrologImplFactory.getCompound("f", new Term[] { Y, b }, null);

		Substitution unifier3 = new PrologSubstitution(X, fYb);
		assertEquals(unifier3, X.mgu(fYb));
		assertEquals(unifier3, fYb.mgu(X));
	}

	/**
	 * Test case: unification of f(X,Y) and f(a,b) and f(a,b,c).
	 */
	@Test
	public void test4unify() {
		// Construct f(X, Y)
		Var X = PrologImplFactory.getVar("X", null);
		Var Y = PrologImplFactory.getVar("Y", null);
		Term fXY = PrologImplFactory.getCompound("f", new Term[] { X, Y }, null);
		// Construct f(a, b)
		Term a = PrologImplFactory.getAtom("a", null);
		Term b = PrologImplFactory.getAtom("b", null);
		Term fab = PrologImplFactory.getCompound("f", new Term[] { a, b }, null);
		// Construct f(a, b, c)
		Term c = PrologImplFactory.getAtom("c", null);
		Term fabc = PrologImplFactory.getCompound("f", new Term[] { a, b, c }, null);

		Substitution unifier = new PrologSubstitution(X, a);
		unifier.addBinding(Y, b);

		assertEquals(unifier, fXY.mgu(fab));
		assertEquals(unifier, fab.mgu(fXY));
		assertEquals(null, fXY.mgu(fabc));
		assertEquals(null, fabc.mgu(fXY));
	}

	/**
	 * Test case: unification of f(a, X) and f(Y, Z).
	 */
	@Test
	public void test5unify() {
		// Construct f(a, X)
		Var X = PrologImplFactory.getVar("X", null);
		Term a = PrologImplFactory.getAtom("a", null);
		Term faX = PrologImplFactory.getCompound("f", new Term[] { a, X }, null);
		// Construct f(Y, Z)
		Var Y = PrologImplFactory.getVar("Y", null);
		Var Z = PrologImplFactory.getVar("Z", null);
		Term fYZ = PrologImplFactory.getCompound("f", new Term[] { Y, Z }, null);

		Substitution unifier1 = new PrologSubstitution(Y, a);
		unifier1.addBinding(X, Z);
		assertEquals(unifier1, faX.mgu(fYZ));

		Substitution unifier2 = new PrologSubstitution(Y, a);
		unifier2.addBinding(Z, X);
		assertEquals(unifier2, fYZ.mgu(faX));
	}

	/**
	 * Test case: unification of f(a, X) and f(X, a).
	 */
	@Test
	public void test6unify() {
		// Construct f(a, X)
		Var X = PrologImplFactory.getVar("X", null);
		Term a = PrologImplFactory.getAtom("a", null);
		Term faX = PrologImplFactory.getCompound("f", new Term[] { a, X }, null);
		// Construct f(X, a)
		Term fXa = PrologImplFactory.getCompound("f", new Term[] { X, a }, null);

		Substitution unifier = new PrologSubstitution(X, a);
		assertEquals(unifier, faX.mgu(fXa));
		assertEquals(unifier, fXa.mgu(faX));
	}

	/**
	 * Test case: unification of f(X, X) and f(a, b).
	 */
	@Test
	public void test7unify() {
		// Construct f(X, X)
		Var X = PrologImplFactory.getVar("X", null);
		Term fXX = PrologImplFactory.getCompound("f", new Term[] { X, X }, null);
		// Construct f(a, b)
		Term a = PrologImplFactory.getAtom("a", null);
		Term b = PrologImplFactory.getAtom("b", null);
		Term fab = PrologImplFactory.getCompound("f", new Term[] { a, b }, null);

		assertEquals(null, fXX.mgu(fab));
		assertEquals(null, fab.mgu(fXX));
	}

	/**
	 * Test case: unification of f(X, X) and f(a, Y).
	 */
	@Test
	public void test8unify() {
		// Construct f(X, X)
		Var X = PrologImplFactory.getVar("X", null);
		Term fXX = PrologImplFactory.getCompound("f", new Term[] { X, X }, null);
		// Construct f(a, b)
		Term a = PrologImplFactory.getAtom("a", null);
		Var Y = PrologImplFactory.getVar("Y", null);
		Term faY = PrologImplFactory.getCompound("f", new Term[] { a, Y }, null);

		Substitution unifier = new PrologSubstitution(X, a);
		unifier.addBinding(Y, a);

		assertEquals(unifier, fXX.mgu(faY));
		assertEquals(unifier, faY.mgu(fXX));
	}

	/**
	 * Test case: f(g(Y), X, Y) = f(X, g(a), a).
	 */
	@Test
	public void test9unify() {
		// Construct f(g(Y), X, Y)
		Var X = PrologImplFactory.getVar("X", null);
		Var Y = PrologImplFactory.getVar("Y", null);
		Term gY = PrologImplFactory.getCompound("g", new Term[] { Y }, null);
		Term fgYXY = PrologImplFactory.getCompound("f", new Term[] { gY, X, Y }, null);
		// Construct f(X, g(a), a)
		Term a = PrologImplFactory.getAtom("a", null);
		Term gA = PrologImplFactory.getCompound("g", new Term[] { a }, null);
		Term fXgaa = PrologImplFactory.getCompound("f", new Term[] { X, gA, a }, null);
		// Construct f(g(Y), X)
		Term fgYX = PrologImplFactory.getCompound("f", new Term[] { gY, X }, null);
		// Construct f(X, g(a))
		Term fXga = PrologImplFactory.getCompound("f", new Term[] { X, gA }, null);

		Substitution unifier1 = new PrologSubstitution(X, gY);
		Substitution unifier2 = new PrologSubstitution(X, gY);
		unifier2.addBinding(Y, a);

		assertEquals(unifier1, gY.mgu(X));
		assertEquals(unifier2, fgYX.mgu(fXga));
		// f(g(Y), X, Y) = f(X, g(a), a)
		assertEquals(unifier2, fgYXY.mgu(fXgaa));
	}

	/**
	 * Test case: unification of f(X, Y) and f(Y, X).
	 */
	@Test
	public void test10unify() {
		// Construct f(X, Y)
		Var X = PrologImplFactory.getVar("X", null);
		Var Y = PrologImplFactory.getVar("Y", null);
		Term fXY = PrologImplFactory.getCompound("f", new Term[] { X, Y }, null);
		// Construct f(Y, X)
		Term fYX = PrologImplFactory.getCompound("f", new Term[] { Y, X }, null);

		Substitution unifier1 = new PrologSubstitution(X, Y);
		Substitution unifier2 = new PrologSubstitution(Y, X);

		assertEquals(unifier1, fXY.mgu(fYX));
		assertEquals(unifier2, fYX.mgu(fXY));
	}

	/**
	 * Test case: unification of f(X) and f(g(X)) (occurs check should kick in).
	 * #3470
	 */
	@Test
	public void test11unify() {
		// Construct f()
		Var X = PrologImplFactory.getVar("X", null);
		Term fX = PrologImplFactory.getCompound("f", new Term[] { X }, null);
		// Construct f(g(X))
		Term gX = PrologImplFactory.getCompound("g", new Term[] { X }, null);
		Term fgX = PrologImplFactory.getCompound("f", new Term[] { gX }, null);

		assertEquals(null, fX.mgu(fgX));
		assertEquals(null, fgX.mgu(fX));
	}

	/**
	 * Test case: X should match with X. #3469
	 */
	@Test
	public void test12unify() {
		Var X = PrologImplFactory.getVar("X", null);

		assertEquals(new PrologSubstitution(), X.mgu(X));
	}

	/**
	 * Test case: unification of f(X,X) and f(X,X)
	 */
	@Test
	public void test13unify() {
		// Construct f(X, X)
		Var X = PrologImplFactory.getVar("X", null);
		Term fXX = PrologImplFactory.getCompound("f", new Term[] { X, X }, null);

		assertEquals(new PrologSubstitution(), fXX.mgu(fXX));
	}

	@Test
	public void testOccursCheck() {
		// Construct aap(X)
		Var X = PrologImplFactory.getVar("X", null);
		Term aapX = PrologImplFactory.getCompound("aap", new Term[] { X }, null);

		assertEquals(null, aapX.mgu(X));
		assertEquals(null, X.mgu(aapX));
	}

	/**
	 * Test case: aap(1,X) versus aap(2,X)
	 */
	@Test
	public void test14unify() {
		// Construct aap(1,X)
		Var X = PrologImplFactory.getVar("X", null);
		Term one = PrologImplFactory.getAtom("1", null);
		Term aap1X = PrologImplFactory.getCompound("aap", new Term[] { one, X }, null);
		// Construct aap(2,X)
		Term two = PrologImplFactory.getAtom("2", null);
		Term aap2X = PrologImplFactory.getCompound("aap", new Term[] { two, X }, null);

		assertEquals(null, aap1X.mgu(aap2X));
		assertEquals(null, aap2X.mgu(aap1X));
	}

	/**
	 * Test case: aap(1,beer(3)) versus aap(2,beer(3))
	 */
	@Test
	public void test15unify() {
		// Construct beer(3)
		Term three = PrologImplFactory.getAtom("3", null);
		Term beer3 = PrologImplFactory.getCompound("beer", new Term[] { three }, null);
		// Construct aap(1,beer(3))
		Term one = PrologImplFactory.getAtom("1", null);
		Term aap1beer3 = PrologImplFactory.getCompound("aap", new Term[] { one, beer3 }, null);
		// Construct aap(2,beer(3))
		Term two = PrologImplFactory.getAtom("2", null);
		Term aap2beer3 = PrologImplFactory.getCompound("aap", new Term[] { two, beer3 }, null);

		assertEquals(null, aap1beer3.mgu(aap2beer3));
		assertEquals(null, aap2beer3.mgu(aap1beer3));
	}

	/**
	 * Test case: '1' versus 1
	 */
	@Test
	public void testAtom1Int1() {
		Term oneI = PrologImplFactory.getNumber(1, null);
		Term oneA = PrologImplFactory.getAtom("1", null);

		assertEquals(null, oneI.mgu(oneA));
		assertEquals(null, oneA.mgu(oneI));
	}
}
