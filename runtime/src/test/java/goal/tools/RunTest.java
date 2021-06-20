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
package goal.tools;

import java.io.FileNotFoundException;

import org.apache.commons.cli.UnrecognizedOptionException;
import org.junit.Test;

public class RunTest {
	@Test
	public void testSinglefile() throws Exception {
		Run.run("src/test/resources/goal/agents/fibonaci.mas2g");
	}

	// @Test FIXME
	public void testMultipleFiles() throws Exception {
		Run.run("src/test/resources/goal/tools/test/correctMasUnderTest.mas2g",
				"src/test/resources/goal/agents/fibonaci.mas2g");
	}

	@Test
	public void testVerboseReverse() throws Exception {
		Run.run("src/test/resources/goal/agents/fibonaci.mas2g", "--verbose");
	}

	@Test
	public void testVerbose() throws Exception {
		Run.run("--verbose", "src/test/resources/goal/agents/fibonaci.mas2g");
	}

	@Test
	public void testVerboseArgumentsNoSeperator() throws Exception {
		Run.run("--verbose", "-iw", "src/test/resources/goal/agents/fibonaci.mas2g");
	}

	@Test
	public void testVerboseNoArguments() throws Exception {
		Run.run("--verbose", "src/test/resources/goal/agents/fibonaci.mas2g");
	}

	@Test(expected = FileNotFoundException.class)
	public void testVerboseArguments() throws Exception {
		Run.run("--verbose", "iwep", "src/test/resources/goal/agents/fibonaci.mas2g");
	}

	@Test(expected = UnrecognizedOptionException.class)
	public void testVerboseArgumentsException() throws Exception {
		Run.run("--verbose=all", "src/test/resources/goal/agents/fibonaci.mas2g");
	}

	@Test
	public void testRepeats() throws Exception {
		Run.run("--repeats=5", "src/test/resources/goal/agents/fibonaci.mas2g");
	}

	// @Test FIXME
	public void testUnitTest() throws Exception {
		Run.run("src/test/resources/goal/tools/test/correctMinimal.test2g",
				"src/test/resources/goal/tools/test/correctExhaustive.test2g");
	}

	// @Test FIXME
	public void testRMI() throws Exception {
		Run.run("--rmi", "localhost", "src/test/resources/goal/tools/test/correctMinimal.test2g");
	}
}
