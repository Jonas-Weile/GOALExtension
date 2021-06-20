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
package goal.tools.eclipse;

import java.io.File;

import goal.core.runtime.RuntimeManager;
import goal.preferences.DebugPreferences;
import goal.preferences.LoggingPreferences;
import goal.preferences.Preferences;
import goal.tools.AbstractRun;
import goal.tools.BreakpointManager;
import goal.tools.DebugRun;
import goal.tools.IDEGOALInterpreter;
import goal.tools.Run;
import goal.tools.TestRun;
import goal.tools.debugger.IDEDebugger;
import goal.tools.logging.InfoLog;
import goal.tools.logging.Loggers;
import languageTools.analyzer.FileRegistry;
import languageTools.analyzer.test.TestValidator;
import languageTools.program.test.TestProgram;
import languageTools.utils.Extension;

public class DebugTool {
	@SuppressWarnings("unchecked")
	public static void main(final String[] args) {
		try {
			final File prefs = new File(args[0]);
			DebugPreferences.setDefault(Run.getDefaultPrefs());
			Preferences.changeSettingsFile(prefs);
			Loggers.addConsoleLogger();

			if (LoggingPreferences.getEclipseDebug()) {
				new InfoLog("initializing debug for '" + args[1] + "'.").emit();
			}

			final String filename = args[1];
			final AbstractRun<? extends IDEDebugger, ? extends IDEGOALInterpreter> run;
			if (Extension.getFileExtension(filename) == Extension.MAS2G) {
				run = new DebugRun(new File(filename));
			} else if (Extension.getFileExtension(filename) == Extension.TEST2G) {
				FileRegistry registry = new FileRegistry();
				TestValidator validator = new TestValidator(filename, registry);
				validator.validate();
				if (registry.hasAnyError()) {
					throw new Exception("found errors while parsing: " + registry.getAllErrors() + ".");
				} else {
					final TestProgram test = validator.getProgram();
					run = new TestRun(test, true);
				}
			} else {
				throw new Exception("file extension not supported: '" + filename + "'.");
			}

			// Check for errors
			if (run.getProgram() == null || !run.getErrors().isEmpty()) {
				throw new Exception("found errors while parsing: " + run.getErrors() + ".");
			}

			// Prepare run: set breakpoints.
			final BreakpointManager bpmngr = new BreakpointManager(run.getProgram().getRegistry());
			if (args.length > 2) {
				GoalBreakpointManager.loadAll(args[2]);
				setFileBreaks(bpmngr);
			}

			// Prepare: build a runtime.
			RuntimeManager<? extends IDEDebugger, ? extends IDEGOALInterpreter> runtime = run.buildRuntime();

			// Prepare run: setup Eclipse tools.
			final EclipseEventObserver observer = new EclipseEventObserver(bpmngr);
			final InputReaderWriter readerwriter = new InputReaderWriter(System.in, System.out,
					(RuntimeManager<IDEDebugger, IDEGOALInterpreter>) runtime, bpmngr, observer);
			readerwriter.start();
			runtime.addObserver(observer);

			// Run the system.
			run.run(false);
		} catch (final Exception e) {
			InputReaderWriter.logFatal(e);
			System.exit(-1);
		}
	}

	/**
	 * Sets breakpoints in source files that have been collected by a
	 * {@link GoalBreakpointManager}.
	 *
	 * @param mngr
	 *            A breakpoint manager.
	 * @param registry
	 *            A file registry containing source files.
	 */
	public static void setFileBreaks(BreakpointManager mngr) {
		mngr.clear();
		for (File file : mngr.getRegistry().getSourceFiles()) {
			final GoalBreakpointManager collector = GoalBreakpointManager.getOrCreateGoalBreakpointManager(file);
			mngr.setBreakpoints(file, collector.getBreakPoints());
		}
	}
}