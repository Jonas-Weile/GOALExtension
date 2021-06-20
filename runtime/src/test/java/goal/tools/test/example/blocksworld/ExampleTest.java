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
package goal.tools.test.example.blocksworld;

import static org.junit.Assume.assumeTrue;

import org.junit.Test;

import goal.tools.test.AbstractTest;
import goal.tools.test.result.TestProgramResult;

public class ExampleTest extends AbstractTest {
	@Test
	public void testSimpleBlocksWorldSWI() throws Exception {
		assumeTrue(hasUI());
		TestProgramResult results = runTest(
				"src/test/resources/goal/tools/test/example/blocksworld/simple/blocksworld.test2g");
		assertPassedAndPrint(results);
	}

	@Test
	public void testNewOperators() throws Exception {
		assumeTrue(hasUI());
		TestProgramResult results = runTest(
				"src/test/resources/goal/tools/test/example/blocksworld/simple/newOperators.test2g");
		assertPassedAndPrint(results);
	}

	@Test
	public void testImprovedBlocksWorld() throws Exception {
		assumeTrue(hasUI());
		TestProgramResult results = runTest(
				"src/test/resources/goal/tools/test/example/blocksworld/improved/blocksworld.test2g");
		assertPassedAndPrint(results);
	}
}
