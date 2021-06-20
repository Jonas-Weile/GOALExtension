package goal.tools.test.example.helloworld;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;

import org.junit.Test;

import goal.preferences.LoggingPreferences;
import goal.tools.TestResultInspector;
import goal.tools.test.AbstractTest;
import goal.tools.test.example.helloworld.HistoryTest.HistoryTestRun;
import languageTools.program.test.TestProgram;

public class HelloWorldTest extends AbstractTest {
	@Test
	public void testHelloWorldBase() throws Exception {
		assumeTrue(hasUI());
		// Make sure history logging is (still) disabled
		boolean previous = LoggingPreferences.getEnableHistory();
		LoggingPreferences.setEnableHistory(false);

		try {
			// Set-up the helloWorld10x test
			TestProgram testProgram = setup(
					"src/test/resources/goal/tools/test/example/helloworld/HelloWorld10xTest.test2g");
			assertNotNull(testProgram);
			HistoryTestRun testRun = new HistoryTestRun(testProgram);
			testRun.setDebuggerOutput(true);
			TestResultInspector inspector = new TestResultInspector(testProgram);
			testRun.setResultInspector(inspector);

			// Run the test and verify its results
			try {
				final long start = System.nanoTime();
				testRun.run(true);
				final long diff = System.nanoTime() - start;
				System.out.println("run time: " + (diff / 1000000000.0) + "s");
				assertPassedAndPrint(inspector.getResults());
			} finally {
				testRun.getAgent().dispose(true);
			}
		} finally {
			// Reset the preference to the original setting
			LoggingPreferences.setEnableHistory(previous);
		}
	}
}
