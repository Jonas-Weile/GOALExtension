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

import org.junit.Test;

import krTools.language.Term;

public class Terms {

	@Test
	public void atomtostring() {
		// Check that toString works OK.
		// TODO FIXME
		// Term atom = new PrologTerm(new
		// alice.tuprolog.Struct("'/tmp/pl_tmp_1089_0'"));
		// assertEquals("'/tmp/pl_tmp_1089_0'", atom.toString());
	}

	@Test
	public void equality() {
		Term var1 = new PrologVar(new alice.tuprolog.Var("X"), null);
		Term var2 = new PrologVar(new alice.tuprolog.Var("X"), null);
		assertEquals(true, var1.equals(var2));
	}
}