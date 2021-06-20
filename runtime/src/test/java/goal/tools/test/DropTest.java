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
package goal.tools.test;

import org.junit.Test;

import goal.tools.test.result.TestProgramResult;

/**
 * Testing of the drop action and automated goal removal.
 */
public class DropTest extends AbstractTest {

	/**
	 * Tests whether the drop action removes a goal that is adopted when the
	 * (main) module is entered.
	 */
	@Test
	public void dropTest() throws Exception {
		TestProgramResult results = runTest("src/test/resources/goal/tools/test/actions/dropTest.test2g");
		assertPassedAndPrint(results);
	}

	/**
	 * Tests whether goal is automatically removed when it is believed to have
	 * been achieved.
	 */
	@Test
	public void goalUpdateTest() throws Exception {
		TestProgramResult results = runTest("src/test/resources/goal/tools/test/actions/goalUpdate.test2g");
		assertPassedAndPrint(results);
	}

}