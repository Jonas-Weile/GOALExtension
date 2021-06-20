package goal.core.executors;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import languageTools.program.mas.LaunchInstruction;

@RunWith(Parameterized.class)
public class LaunchInstructionExecutorTest {
	private int maxNumberOfAgentsToLaunch;
	private int numberOfAgentsToLaunch;
	private int nExecutes;
	private LaunchInstructionExecutor executor;

	public LaunchInstructionExecutorTest(int max, int num, int nexecutes) {
		this.maxNumberOfAgentsToLaunch = max;
		this.numberOfAgentsToLaunch = num;
		this.nExecutes = nexecutes;
	}

	/**
	 * Parameters: M=max agents to launch, N=number of agents to launch,
	 * X=number of calls to execute We compute the expected getNr() ourselves
	 * (should be =N unless we reach M)
	 */
	@Parameters
	public static List<Object[]> testConditions() {
		return Arrays.asList(new Object[][] { { 1, 2, 5 }, { 3, 1, 5 }, { 1, 0, 2 }, { 11, 3, 5 } });
	}

	@Before
	public void setup() {
		LaunchInstruction instruction = mock(LaunchInstruction.class);
		when(instruction.getAgentName()).thenReturn("agent");
		when(instruction.getMaxNumberOfAgentsToLaunch()).thenReturn(this.maxNumberOfAgentsToLaunch);
		when(instruction.getNumberOfAgentsToLaunch()).thenReturn(this.numberOfAgentsToLaunch);
		// getAgentDf will return null.

		this.executor = new LaunchInstructionExecutor(instruction);
	}

	/**
	 * Execute the executor the given nExecutes times. Each time, the getNr()
	 * should return {@link #numberOfAgentsToLaunch}, until we reach
	 * {@link #maxNumberOfAgentsToLaunch}.
	 */
	@Test
	public void testLaunch() {
		int nLaunched = 0;

		// We can't test getGivenName as the doc does not say what it does.
		// from the code it just forwards the call to another class anyway.
		for (int execution = 0; execution < this.nExecutes; execution++) {
			int expectedLaunchNr = Math.min(this.numberOfAgentsToLaunch, this.maxNumberOfAgentsToLaunch - nLaunched);

			this.executor.execute("agent1", "agent1type");
			int n = this.executor.getNr();

			assertEquals(expectedLaunchNr, n);
			nLaunched += n;
		}
	}
}
