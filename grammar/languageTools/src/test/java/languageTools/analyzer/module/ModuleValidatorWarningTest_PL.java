/**
 * The GOAL Grammar Tools. Copyright (C) 2014 Koen Hindriks.
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
package languageTools.analyzer.module;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import languageTools.errors.module.ModuleWarning;

public class ModuleValidatorWarningTest_PL extends ModuleTestSetup {

	private String path = "/SWI-Prolog/";

	@Test
	public void test_DUPLICATE_OPTION() {
		setup(this.path + "test_DUPLICATE_OPTION.mod2g");

		// Module file should not produce any syntax errors.
		assertTrue(getSyntaxErrors().isEmpty());

		// Module file should not produce any errors.
		assertTrue(getErrors().isEmpty());

		// Module file should produce 1 warning.
		assertEquals(1, getWarnings().size());
		assertEquals(ModuleWarning.DUPLICATE_OPTION, getWarnings().get(0).getType());
	}

	@Test
	public void test_EXITMODULE_CANNOT_REACH() {
		setup(this.path + "test_EXITMODULE_CANNOT_REACH.mod2g");

		// Module file should not produce any syntax errors.
		assertTrue(getSyntaxErrors().isEmpty());

		// Module file should produce no errors.
		assertTrue(getErrors().isEmpty());

		// Module file should produce 1 warning.
		assertEquals(1, getWarnings().size());
		assertEquals(ModuleWarning.EXITMODULE_CANNOT_REACH, getWarnings().get(0).getType());
	}

	@Test
	public void test_KR_GOAL_DOES_NOT_MATCH_BELIEF() {
		setup(this.path + "test_KR_GOAL_DOES_NOT_MATCH_BELIEF.mod2g");

		// Module file should not produce any syntax errors.
		assertTrue(getSyntaxErrors().isEmpty());

		// Module file should produce no errors.
		assertTrue(getErrors().isEmpty());

		// Module file should produce 1 warning.
		assertEquals(1, getWarnings().size());
		assertEquals(ModuleWarning.KR_GOAL_DOES_NOT_MATCH_BELIEF, getWarnings().get(0).getType());
	}

	@Test
	public void test_MACRO_NEVER_USED() {
		setup(this.path + "test_MACRO_NEVER_USED.mod2g");

		// Module file should not produce any syntax errors.
		assertTrue(getSyntaxErrors().isEmpty());

		// Module file should not produce any errors.
		assertTrue(getErrors().isEmpty());

		// Module file should produce 1 warning.
		assertEquals(1, getWarnings().size());
		assertEquals(ModuleWarning.MACRO_NEVER_USED, getWarnings().get(0).getType());
	}

	@Test
	public void test_PARAMETER_NEVER_USED() {
		setup(this.path + "test_PARAMETER_NEVER_USED.mod2g");

		// Module file should not produce any syntax errors.
		assertTrue(getSyntaxErrors().isEmpty());

		// Module file should not produce any errors.
		assertTrue(getErrors().isEmpty());

		// Module file should produce 1 warning.
		assertEquals(1, getWarnings().size());
		assertEquals(ModuleWarning.PARAMETER_NEVER_USED, getWarnings().get(0).getType());
		assertEquals("Y", getWarnings().get(0).getArguments()[0]);
	}

}