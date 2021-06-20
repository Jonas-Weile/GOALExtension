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
 * Various test cases for testing if goals are working properly.
 */
public class GoalsTest extends AbstractTest {
	/**
	 * Tests whether goals are properly declared dynamic.
	 */
	@Test
	public void goalTest() throws Exception {
		TestProgramResult results = runTest("src/test/resources/goal/core/program/goals/testgoal.test2g");
		assertPassedAndPrint(results);
	}

	/**
	 * Tests whether goals are properly declared dynamic.
	 */
	@Test
	public void goalTest2() throws Exception {
		TestProgramResult results = runTest("src/test/resources/goal/core/program/goals2/testgoal.test2g");
		assertPassedAndPrint(results);
	}
}