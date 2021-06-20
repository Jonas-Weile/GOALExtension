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
package goal.tools.test.bugs;

import org.junit.Test;

import goal.tools.test.AbstractTest;
import goal.tools.test.result.TestProgramResult;

public class GreaterThanCharacterTest extends AbstractTest {

	/**
	 * Test to check whether unit test evaluators immediately pick up post
	 * condition changes (and not only by atend operator). Program performs two
	 * rules (linearall) where first rule performs user-defined action to insert
	 * fact {@code test1} and remove {@code test2} by means of postcondition and
	 * second rule performs delete action to remove fact {@code test1} and
	 * insert action to add {@code test2} again.
	 */
	@Test
	public void test() throws Exception {
		TestProgramResult results = runTest("src/test/resources/goal/tools/test/bugs/gtlttoken.test2g");
		assertPassedAndPrint(results);
	}

}
