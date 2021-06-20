/**
 * The GOAL Runtime Environment. Copyright (C) 2015 Koen Hindriks.
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
package goal.agents;

import org.junit.Test;

import goal.tools.test.AbstractTest;
import goal.tools.test.result.TestProgramResult;

/**
 * Test macro definitions.
 */
public class Macro extends AbstractTest {
	/**
	 * Tests whether only module parameters are passed into rules of module itself.
	 * Module parameters should act as a filter that prevents any variable bindings
	 * that do not bind a formal parameter of the module to be applied to the rules
	 * of the module.
	 */
	@Test
	public void macroVariablesTest() throws Exception {
		TestProgramResult results = runTest("src/test/resources/goal/core/program/macroTest/macro.test2g");
		assertPassedAndPrint(results);
	}

	@Test
	public void macroVarFromModuleTest() throws Exception {
		TestProgramResult results = runTest("src/test/resources/goal/core/program/macroVarFromModule/macro.test2g");
		assertPassedAndPrint(results);
	}

	/**
	 * Using macro from inside macro currently should result in a parser error.
	 *
	 * @throws Exception
	 */
	@Test(expected = Exception.class)
	public void recursiveMacroTest() throws Exception {
		runTest("src/test/resources/goal/core/program/recursiveMacroTest/macro.test2g");
	}
}