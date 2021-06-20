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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import goal.tools.TestRun;
import goal.tools.errorhandling.exceptions.GOALRunFailedException;
import goal.tools.test.result.TestProgramResult;
import languageTools.program.test.TestProgram;

public class TestTest extends AbstractTest {
	@Test
	public void testCorrectMinimal() throws Exception {
		TestProgramResult results = runTest("src/test/resources/goal/tools/test/correctMinimal.test2g");

		assertPassedAndPrint(results);
	}

	@Test
	public void testCorrectExhaustive() throws Exception {
		TestProgramResult results = runTest("src/test/resources/goal/tools/test/correctExhaustive.test2g");

		assertPassedAndPrint(results);
	}

	@Test
	public void testCorrectExhaustiveModuleActions() throws Exception {
		TestProgramResult results = runTest("src/test/resources/goal/tools/test/correctExhaustiveModuleActions.test2g");

		assertPassedAndPrint(results);
	}

	@Test
	public void testCorrectExhaustiveLTL() throws Exception {
		TestProgramResult results = runTest("src/test/resources/goal/tools/test/correctExhaustiveLTL.test2g");

		assertPassedAndPrint(results);
	}

	@Test
	public void testCountsTo100() throws Exception {
		TestProgramResult results = runTest("src/test/resources/goal/tools/test/counter/CountsTo100.test2g");

		assertPassedAndPrint(results);
	}

	@Test
	public void testCorrectFailingLTL() throws Exception {
		TestProgramResult results = runTest("src/test/resources/goal/tools/test/correctFailingLTL.test2g");

		assertFailedAndPrint(results);
	}

	@Test
	public void testCorrectMinimalLTL() throws Exception {
		TestProgramResult results = runTest("src/test/resources/goal/tools/test/correctMinimalLTL.test2g");

		assertPassedAndPrint(results);
	}

	@Test
	public void testNewCorrectBoundary() throws Exception {
		TestProgramResult results = runTest("src/test/resources/goal/tools/test/counter/newCorrectBoundary.test2g");

		assertPassedAndPrint(results);
	}

	@Test
	public void testModuleArgs() throws Exception {
		TestProgramResult results = runTest(
				"src/test/resources/goal/tools/test/twoarguments/moduleTwoArguments.test2g");

		assertPassedAndPrint(results);
	}

	@Test
	public void testCorrectModuleAction() throws Exception {
		TestProgramResult results = runTest("src/test/resources/goal/tools/test/correctModuleAction.test2g");

		assertPassedAndPrint(results);
	}

	// @Test FIXME
	public void testTimers() throws Exception {
		TestProgramResult results = runTest("src/test/resources/goal/tools/test/timers/timersTest.test2g");

		assertPassedAndPrint(results);
	}

	@Test
	public void testPercentage100() {
		TestProgram testProgram = null;
		try {
			testProgram = setup("src/test/resources/goal/tools/test/counter/CountsTo100.test2g");
		} catch (Exception e) {
			System.out.println("Unable to setup testPercentage100 test program");
			e.printStackTrace();
		}

		assertNotNull(testProgram);
		TestRun testRun = null;
		try {
			testRun = new TestRun(testProgram, false);
			testRun.run(false);
		} catch (GOALRunFailedException e) {
			e.printStackTrace();
		}
		assertEquals(100, (int) testRun.calculatePercentage());
	}

	@Test
	public void testPercentage0() {
		TestProgram testProgram = null;
		try {
			testProgram = setup("src/test/resources/goal/tools/test/correctFailingLTL.test2g");
		} catch (Exception e) {
			System.out.println("Unable to setup testPercentage0 test program");
			e.printStackTrace();
		}

		assertNotNull(testProgram);
		TestRun testRun = null;
		try {
			testRun = new TestRun(testProgram, false);
			testRun.run(false);
		} catch (GOALRunFailedException e) {
			e.printStackTrace();
		}
		assertEquals(0, (int) testRun.calculatePercentage());
	}

	@Test
	public void testPercentage50() {
		TestProgram testProgram = null;
		try {
			testProgram = setup(
					"src/test/resources/goal/tools/test/example/blocksworld/simple/newOperatorsFailed.test2g");
		} catch (Exception e) {
			System.out.println("Unable to setup testPercentage50 test program");
			e.printStackTrace();
		}

		assertNotNull(testProgram);
		TestRun testRun = null;
		try {
			testRun = new TestRun(testProgram, false);
			testRun.run(false);
		} catch (GOALRunFailedException e) {
			e.printStackTrace();
		}
		assertEquals(0, (int) testRun.calculatePercentage());
	}
}
