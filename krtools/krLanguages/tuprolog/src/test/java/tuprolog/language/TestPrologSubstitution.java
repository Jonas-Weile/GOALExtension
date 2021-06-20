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
import static org.junit.Assert.assertTrue;

import java.util.Hashtable;

import org.junit.Test;

import alice.tuprolog.Term;

public class TestPrologSubstitution {

	/**
	 * Check that variables are substituted only once.
	 */
	@Test
	public void testSubstitution() {
		Hashtable<String, Term> solution = new Hashtable<>(2);

		alice.tuprolog.Var Y = new alice.tuprolog.Var("Y");
		alice.tuprolog.Var Z = new alice.tuprolog.Var("Z");
		solution.put("X", Y);
		solution.put("Y", Z);

		Term[] args = new Term[2];
		args[0] = new alice.tuprolog.Var("X");
		args[1] = new alice.tuprolog.Var("Y");
		Term term = new alice.tuprolog.Struct("aap", args);

		Term result = JPLUtils.applySubst(solution, term);
		assertTrue(result instanceof alice.tuprolog.Struct);
		alice.tuprolog.Struct compound = (alice.tuprolog.Struct) result;
		assertEquals(Y, compound.getArg(0));
		assertEquals(Z, compound.getArg(1));
	}

	@Test
	public void testToString() {
		Hashtable<String, Term> solution = new Hashtable<>(3);
		PrologSubstitution substitution1 = PrologSubstitution.getSubstitutionOrNull(solution);
		assertTrue(substitution1.getJPLSolution().isEmpty());

		alice.tuprolog.Var var = new alice.tuprolog.Var("X");
		alice.tuprolog.Term term = new alice.tuprolog.Struct("a");
		solution.put(var.getName(), term);
		PrologSubstitution substitution2 = PrologSubstitution.getSubstitutionOrNull(solution);
		assertEquals(1, substitution2.getJPLSolution().size());
		assertEquals(term, substitution2.getJPLSolution().get(var.getName()));

		alice.tuprolog.Var var1 = new alice.tuprolog.Var("Y");
		alice.tuprolog.Term term1 = new alice.tuprolog.Struct("b");
		solution.put(var1.getName(), term1);
		PrologSubstitution substitution3 = PrologSubstitution.getSubstitutionOrNull(solution);
		assertEquals(2, substitution3.getJPLSolution().size());
		assertEquals(term, substitution3.getJPLSolution().get(var.getName()));
		assertEquals(term1, substitution3.getJPLSolution().get(var1.getName()));

		alice.tuprolog.Var var2 = new alice.tuprolog.Var("Z");
		alice.tuprolog.Var var3 = new alice.tuprolog.Var("V");
		solution.put(var2.getName(), var3);
		PrologSubstitution substitution4 = PrologSubstitution.getSubstitutionOrNull(solution);
		assertEquals(3, substitution4.getJPLSolution().size());
		assertEquals(term, substitution4.getJPLSolution().get(var.getName()));
		assertEquals(term1, substitution4.getJPLSolution().get(var1.getName()));
		assertEquals(var3, substitution4.getJPLSolution().get(var2.getName()));
	}

}
