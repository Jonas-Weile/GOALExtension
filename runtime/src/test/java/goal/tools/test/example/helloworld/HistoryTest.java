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
package goal.tools.test.example.helloworld;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assume.assumeTrue;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

import goal.core.agent.Agent;
import goal.core.agent.AgentFactory;
import goal.core.agent.AgentRegistry;
import goal.core.runtime.service.agent.RunState;
import goal.preferences.LoggingPreferences;
import goal.tools.TestResultInspector;
import goal.tools.TestRun;
import goal.tools.debugger.IDEDebugger;
import goal.tools.debugger.NOPObserver;
import goal.tools.eclipse.QueryTool;
import goal.tools.errorhandling.exceptions.GOALLaunchFailureException;
import goal.tools.errorhandling.exceptions.GOALRunFailedException;
import goal.tools.history.EventStorage;
import goal.tools.history.StorageEventObserver;
import goal.tools.history.events.AbstractEvent;
import goal.tools.history.explanation.DebuggingIsExplaining;
import goal.tools.history.explanation.reasons.Reason;
import goal.tools.test.AbstractTest;
import goal.tools.test.TestInterpreter;
import krTools.language.DatabaseFormula;
import languageTools.program.agent.actions.Action;
import languageTools.program.test.TestProgram;

public class HistoryTest extends AbstractTest {
	@Test
	public void testHelloWorldHistory() throws Exception {
		assumeTrue(hasUI());
		// First enable the history logging
		boolean previous = LoggingPreferences.getEnableHistory();
		LoggingPreferences.setEnableHistory(true);

		try {
			// Set-up the helloWorld10x test
			TestProgram testProgram = setup(
					"src/test/resources/goal/tools/test/example/helloworld/HelloWorld10xTest.test2g");
			assertNotNull(testProgram);
			HistoryTestRun testRun = new HistoryTestRun(testProgram);
			TestResultInspector inspector = new TestResultInspector(testProgram);
			testRun.setResultInspector(inspector);

			// Run the test, verify its results,
			// and inspect the generated trace.
			try {
				final long start1 = System.nanoTime();
				testRun.run(true);
				final long diff1 = System.nanoTime() - start1;
				System.out.println("run time: " + (diff1 / 1000000000.0) + "s");
				assertPassedAndPrint(inspector.getResults());

				Agent<TestInterpreter> agent = testRun.getAgent();
				RunState runstate = agent.getController().getRunState();
				runstate.getEventGenerator().clearListeners();

				// Get the EventStorage and test it...
				EventStorage history = StorageEventObserver.getHistory(agent.getId());
				assertNotNull(history);
				history.finish(false);
				Map<Integer, String> statesBackward = new HashMap<>(history.getMax());
				Map<Integer, String> statesForward = new HashMap<>(history.getMax());
				List<AbstractEvent> readonly = new EventStorage(history.getDataFile()).getAll();
				assertEquals(history.getMax(), readonly.size());
				// Do all steps back
				final long start2 = System.nanoTime();
				for (int i = history.getMax(); i > 0; --i) {
					statesBackward.put(i, runstate.toString());
					AbstractEvent event = history.oneStepBack(runstate);
					assertNotNull(event);
					assertNotNull(event.getSource(runstate.getMap()));
					assertNotNull(event.getDescription(runstate));
					// System.out.println("BACK " + i + ": " + event + " @" +
					// event.getSource(runstate.getMap()));
					assertEquals(event, readonly.get(i - 1));
				}
				final long diff2 = System.nanoTime() - start2;
				System.out.println("back took " + (diff2 / 1000000000.0) + "s");
				// Do all steps forward
				final long start3 = System.nanoTime();
				for (int i = 1; i <= statesBackward.size(); ++i) {
					AbstractEvent event = history.oneStepForward(runstate);
					statesForward.put(i, runstate.toString());
					assertNotNull(event);
					assertNotNull(event.getSource(runstate.getMap()));
					assertNotNull(event.getDescription(runstate));
					// System.out.println("FORWARD " + i + ": " + event + " @" +
					// event.getSource(runstate.getMap()));
					assertEquals(event, readonly.get(i - 1));
				}
				final long diff3 = System.nanoTime() - start3;
				System.out.println("forward took " + (diff3 / 1000000000.0) + "s");
				// Check the results
				assertEquals(statesBackward.size(), statesForward.size());
				for (int i = 0; i < statesForward.size(); ++i) {
					assertEquals(statesBackward.get(i), statesForward.get(i));
				}

				// NEW: test DebuggingIsExplaining
				final DebuggingIsExplaining explain = new DebuggingIsExplaining(history, runstate.getMap());
				explain.process();
				System.out.println();

				final Set<Action<?>> actions = explain.getAllActions();
				assertEquals(2, actions.size());
				final Set<DatabaseFormula> beliefs = explain.getAllBeliefs();
				assertEquals(1002, beliefs.size());
				final Set<DatabaseFormula> goals = explain.getAllGoals();
				assertEquals(2, goals.size());

				final QueryTool tool = new QueryTool(agent);
				final List<Reason> whyAction1 = explain.whyAction(actions.iterator().next(), runstate.getKRI());
				assertEquals(1000, whyAction1.size());
				System.out.println(whyAction1.iterator().next());
				final Action<?> action2 = tool.parseAction("printText(X)").getActions().iterator().next();
				final List<Reason> whyAction2 = explain.whyAction(action2, runstate.getKRI());
				assertEquals(1000, whyAction2.size());
				System.out.println(whyAction2.iterator().next());

				final Action<?> notAction1 = tool.parseAction("printText('Bye, world!')").getActions().iterator()
						.next();
				final List<Reason> whyNotAction1 = explain.whyNotAction(notAction1, runstate.getKRI());
				assertEquals(1, whyNotAction1.size());
				System.out.println(whyNotAction1.iterator().next());

				final Action<?> notAction2 = tool.parseAction("printText(_)").getActions().iterator().next();
				final List<Reason> whyNotAction2 = explain.whyNotAction(notAction2, runstate.getKRI());
				assertEquals(whyAction2.size(), whyNotAction2.size());
			} finally {
				testRun.getAgent().dispose(true);
			}
		} finally {
			LoggingPreferences.setEnableHistory(previous);
		}
	}

	protected static class HistoryTestRun extends TestRun {
		private AgentRegistry<TestInterpreter> registry;

		private class HistoryTestRunAgentFactory extends TestRunAgentFactory {
			public HistoryTestRunAgentFactory() throws GOALLaunchFailureException {
				super();
			}

			@Override
			protected IDEDebugger provideDebugger() {
				IDEDebugger debugger = new IDEDebugger(getAgentId(), getManager(), getEnvironmentPort());
				debugger.setKeepRunning(true);
				if (!LoggingPreferences.getEnableHistory()) {
					new NOPObserver(debugger).subscribe();
				}
				return debugger;
			}
		}

		public HistoryTestRun(TestProgram program) throws GOALRunFailedException {
			super(program, true);
		}

		public Agent<TestInterpreter> getAgent() {
			return this.registry.getAgent(this.registry.getRegisteredAgents().iterator().next());
		}

		@Override
		protected AgentFactory<IDEDebugger, TestInterpreter> buildAgentFactory() throws GOALLaunchFailureException {
			HistoryTestRunAgentFactory factory = new HistoryTestRunAgentFactory();
			this.registry = factory.getRegistry();
			return factory;
		}
	}
}
