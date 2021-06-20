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

import goal.preferences.DebugPreferences;
import goal.preferences.LoggingPreferences;
import goal.preferences.Preferences;
import goal.tools.AbstractRun;
import goal.tools.Run;
import goal.tools.SingleRun;
import goal.tools.TestRun;
import goal.tools.logging.InfoLog;
import goal.tools.logging.Loggers;
import languageTools.analyzer.FileRegistry;
import languageTools.analyzer.test.TestValidator;
import languageTools.program.test.TestProgram;
import languageTools.utils.Extension;

public class RunTool {
	public static void main(final String[] args) {
		try {
			final File prefs = new File(args[0]);
			DebugPreferences.setDefault(Run.getDefaultPrefs());
			Preferences.changeSettingsFile(prefs);
			Loggers.addConsoleLogger();

			if (LoggingPreferences.getEclipseDebug()) {
				new InfoLog("initializing run for '" + args[1] + "'.").emit();
			}

			final String filename = args[1];
			final AbstractRun<?, ?> run;
			if (Extension.getFileExtension(filename) == Extension.MAS2G) {
				run = new SingleRun(new File(filename));
			} else if (Extension.getFileExtension(filename) == Extension.TEST2G) {
				FileRegistry registry = new FileRegistry();
				TestValidator validator = new TestValidator(filename, registry);
				validator.validate();
				if (registry.hasAnyError()) {
					throw new Exception("found errors while parsing: " + registry.getAllErrors() + ".");
				} else {
					final TestProgram test = validator.getProgram();
					run = new TestRun(test, false);
				}
			} else {
				throw new Exception("file extension not supported: '" + filename + "'.");
			}

			// Run the system.
			run.setDebuggerOutput(true);
			run.run(true);

			// Clean-up when finished.
			System.exit(0);
		} catch (final Exception e) { // Run tool outer exception reporting
			InputReaderWriter.logFatal(e);
			System.exit(-1);
		}
	}
}