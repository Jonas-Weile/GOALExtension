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
 * Various test cases for passing on variable bindings via a module parameter.
 */
public class VarSubstitution extends AbstractTest {
	/**
	 * Tests whether only module parameters are passed into rules of module itself.
	 * Module parameters should act as a filter that prevents any variable bindings
	 * that do not bind a formal parameter of the module to be applied to the rules
	 * of the module.
	 */
	@Test
	public void moduleParameterFilterTest() throws Exception {
		TestProgramResult results = runTest(
				"src/test/resources/goal/core/program/moduleParameterFilter/moduleParameterFilter.test2g");
		assertPassedAndPrint(results);
	}

	/**
	 * Tests whether module parameter is correctly passed on to action in listall
	 * rule within the module.
	 */
	@Test
	public void passParameterToListallTest() throws Exception {
		TestProgramResult results = runTest(
				"src/test/resources/goal/core/program/passParameterToListallRule/passParameterToListall.test2g");
		assertPassedAndPrint(results);
	}

	/**
	 * Tests whether a parameter is passed correctly into a nested rule section.
	 */
	@Test
	public void passParameterToNestedRulesTest() throws Exception {
		TestProgramResult results = runTest(
				"src/test/resources/goal/core/program/passParameterToNestedRules/passParameterToNestedRules.test2g");
		assertPassedAndPrint(results);
	}

	/**
	 * Tests whether variable in post-condition is processed correctly.
	 */
	@Test
	public void postConditionVariableTest() throws Exception {
		TestProgramResult results = runTest(
				"src/test/resources/goal/core/program/postConditionVariable/postConditionVariable.test2g");
		assertPassedAndPrint(results);
	}

	/**
	 * Tests whether a module parameter is correctly passed on to rule condition.
	 */
	@Test
	public void passParameterToRuleConditionTest() throws Exception {
		TestProgramResult results = runTest(
				"src/test/resources/goal/core/program/passParameterToRuleCondition/passParameterToRuleCondition.test2g");
		assertPassedAndPrint(results);
	}

	/**
	 * Tests that a parameter that is not part of the action is not passed.
	 */
	@Test
	public void passParameterToAction() throws Exception {
		TestProgramResult results = runTest(
				"src/test/resources/goal/core/program/passParameterToAction/passparam.test2g");
		assertPassedAndPrint(results);
	}
}