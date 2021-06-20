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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.After;
import org.junit.Before;

import events.Channel;
import events.Channel.ChannelState;
import goal.preferences.CorePreferences;
import goal.preferences.DebugPreferences;
import goal.tools.TestResultInspector;
import goal.tools.TestRun;
import goal.tools.errorhandling.exceptions.GOALRunFailedException;
import goal.tools.test.result.TestProgramResult;
import goal.tools.test.result.TestResultFormatter;
import languageTools.analyzer.FileRegistry;
import languageTools.analyzer.test.TestValidator;
import languageTools.program.test.TestProgram;

public class AbstractTest {
	private static boolean previous;
	protected FileRegistry registry;
	protected TestValidator visitor;

	private final static Object[][] debugPrefs = { { Channel.ACTIONCOMBO_END, ChannelState.HIDDEN },
			{ Channel.ACTIONCOMBO_START, ChannelState.HIDDEN }, { Channel.ACTION_END, ChannelState.HIDDEN },
			{ Channel.ACTION_EXECUTED_BUILTIN, ChannelState.NONE },
			{ Channel.ACTION_EXECUTED_MESSAGING, ChannelState.NONE },
			{ Channel.ACTION_EXECUTED_USERSPEC, ChannelState.VIEW },
			{ Channel.ACTION_POSTCOND_EVALUATION, ChannelState.PAUSE },
			{ Channel.ACTION_PRECOND_EVALUATION, ChannelState.PAUSE }, { Channel.ACTION_START, ChannelState.HIDDEN },
			{ Channel.ADOPT_END, ChannelState.HIDDEN }, { Channel.ADOPT_START, ChannelState.HIDDEN },
			{ Channel.BB_UPDATES, ChannelState.VIEW }, { Channel.BREAKPOINTS, ChannelState.HIDDENPAUSE },
			{ Channel.CALL_ACTION_OR_MODULE, ChannelState.PAUSE }, { Channel.CLEARSTATE, ChannelState.HIDDEN },
			{ Channel.DB_QUERY_END, ChannelState.HIDDEN }, { Channel.DB_QUERY_START, ChannelState.HIDDEN },
			{ Channel.DELETE_END, ChannelState.HIDDEN }, { Channel.DELETE_START, ChannelState.HIDDEN },
			{ Channel.DROP_END, ChannelState.HIDDEN }, { Channel.DROP_START, ChannelState.HIDDEN },
			{ Channel.GB_CHANGES, ChannelState.HIDDEN }, { Channel.GB_UPDATES, ChannelState.VIEW },
			{ Channel.GOAL_ACHIEVED, ChannelState.VIEWPAUSE },
			{ Channel.HIDDEN_RULE_CONDITION_EVALUATION, ChannelState.HIDDEN },
			{ Channel.INSERT_END, ChannelState.HIDDEN }, { Channel.INSERT_START, ChannelState.HIDDEN },
			{ Channel.MAILS, ChannelState.NONE }, { Channel.MAILS_CONDITIONAL_VIEW, ChannelState.CONDITIONALVIEW },
			{ Channel.MODULE_ENTRY, ChannelState.PAUSE }, { Channel.MODULE_EXIT, ChannelState.NONE },
			{ Channel.MSQUERY_END, ChannelState.HIDDEN }, { Channel.MSQUERY_START, ChannelState.HIDDEN },
			{ Channel.NONE, ChannelState.NONE }, { Channel.PERCEPTS, ChannelState.NONE },
			{ Channel.PERCEPTS_CONDITIONAL_VIEW, ChannelState.CONDITIONALVIEW },
			{ Channel.PRINT, ChannelState.HIDDENVIEW }, { Channel.REASONING_CYCLE_SEPARATOR, ChannelState.VIEW },
			{ Channel.RULE_CONDITIONAL_VIEW, ChannelState.CONDITIONALVIEW },
			{ Channel.RULE_CONDITION_EVALUATION, ChannelState.PAUSE },
			{ Channel.RULE_EVAL_CONDITION_DONE, ChannelState.HIDDEN }, { Channel.RULE_EXIT, ChannelState.HIDDEN },
			{ Channel.RULE_START, ChannelState.HIDDEN }, { Channel.RUNMODE, ChannelState.HIDDEN },
			{ Channel.SLEEP, ChannelState.VIEW }, { Channel.TESTFAILURE, ChannelState.VIEWPAUSE },
			{ Channel.WARNING, ChannelState.NONE } };

	@Before
	public void start() {
		DebugPreferences.setDefault(getDefaultPrefs());
		// Loggers.addConsoleLogger();
		previous = CorePreferences.getAbortOnTestFailure();
		CorePreferences.setAbortOnTestFailure(true);
	}

	@After
	public void end() {
		CorePreferences.setAbortOnTestFailure(previous);
		// Loggers.removeConsoleLogger();
	}

	protected static void assertPassedAndPrint(TestProgramResult results) {
		TestResultFormatter formatter = new TestResultFormatter();
		System.out.println(formatter.visit(results));
		assertTrue(results.isPassed());
	}

	protected static void assertFailedAndPrint(TestProgramResult results) {
		TestResultFormatter formatter = new TestResultFormatter();
		System.out.println(formatter.visit(results));
		assertFalse(results.isPassed());
	}

	public TestProgram setup(String path) throws Exception {
		this.registry = new FileRegistry();
		this.visitor = new TestValidator(path, this.registry);
		this.visitor.validate();
		TestProgram program = this.visitor.getProgram();
		if (program == null || this.registry.hasAnyError()) {
			throw new Exception(this.registry.getAllErrors().toString());
		} else {
			return program;
		}
	}

	protected TestProgramResult runTest(String testFileName) throws Exception {
		TestProgram testProgram;
		try {
			testProgram = setup(testFileName);
		} catch (IOException e) {
			throw new GOALRunFailedException("error while reading test file " + testFileName, e);
		}

		assertNotNull(testProgram);

		TestRun testRun = new TestRun(testProgram, false);
		testRun.setDebuggerOutput(true);
		TestResultInspector inspector = new TestResultInspector(testProgram);
		testRun.setResultInspector(inspector);
		testRun.run(true);

		return inspector.getResults();
	}

	protected static boolean hasUI() {
		try {
			return (GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices().length > 0);
		} catch (Exception any) {
			return false;
		}
	}

	private Map<String, Object> getDefaultPrefs() {
		Map<String, Object> map = new HashMap<>();
		for (Object[] keyvalue : debugPrefs) {
			map.put(keyvalue[0].toString(), keyvalue[1].toString());
		}
		return map;
	}
}