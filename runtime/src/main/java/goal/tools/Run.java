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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import events.Channel;
import events.Channel.ChannelState;
import goal.preferences.CorePreferences;
import goal.preferences.DebugPreferences;
import goal.preferences.LoggingPreferences;
import goal.tools.errorhandling.Warning;
import goal.tools.errorhandling.exceptions.GOALRunFailedException;
import goal.tools.logging.Loggers;
import krTools.exceptions.ParserException;
import languageTools.analyzer.FileRegistry;
import languageTools.analyzer.test.TestValidator;
import languageTools.program.test.TestProgram;
import languageTools.utils.Extension;

/**
 * Command line utility to run multi-agent systems and tests. Will accept one or
 * more .mas2g and .test2g files and run them. Options can be used to repeat
 * runs and enable learning between runs.
 *
 * Each program will be ran until all the agents have terminated or the
 * environment is terminated or the program times out, which ever happens first.
 *
 * <pre>
 * {@code
 * usage: goal.tools.Run [options] [[file|directory]]
 *  -d,--debug               Display output from debugger while running agent
 *  -h,--help                Displays this help
 *  -i                       Print messages from info
 *  -l <dir>				 Write agent logs to files (in the given directory if any)
 *     --license             Shows the license
 *  -r,--repeats <number>    Number of times to repeat running all episodes
 *     --recursive           Recursively search for mas files
 *  -t,--timeout <number>	 Maximum time to run a mas (in seconds)
 *  -v,--verbose             Print all messages
 *  -w                       Print messages from warning
 *  --keep-killed    		 Keep agents available on the platform when killed
 *  --sleep                  Sleep agents when they receive the same percepts/messages
 *  						  and do the same actions all the time
 *  --agent-copies-env-state New agents use the environment run state (or run if no environment)
 *  --sequential-runmode     Run multiple agents in sequence (instead of in parallel as is the default)
 * }
 * </pre>
 */
public class Run {
	private static final String OPTION_HELP = "help";
	private static final String OPTION_HELP_SHORT = "h";

	private static final String OPTION_LICENSE = "license";

	private static final String OPTION_RECURSIVE = "recursive";

	private static final String OPTION_DEBUG = "debug";
	private static final String OPTION_DEBUG_SHORT = "d";

	private static final String OPTION_VERBOSE = "verbose";
	private static final String OPTION_VERBOSE_SHORT = "v";
	private static final String OPTION_VERBOSE_WARNING = "w";
	private static final String OPTION_VERBOSE_INFO = "i";

	private static final String OPTION_LOGTOFILE = "l";

	private static final String OPTION_REPEATS = "repeats";
	private static final String OPTION_REPEATS_SHORT = "r";
	private static final String OPTION_TIMEOUT = "timeout";
	private static final String OPTION_TIMEOUT_SHORT = "t";

	private static final String OPTION_KEEP_KILLED = "keep-killed";
	private static final String OPTION_SLEEP_REPETITIVE = "sleep";
	private static final String OPTION_TAKE_ENV_STATE = "agent-copies-env-state";
	private static final String OPTION_SEQUENTIAL_RUNMODE = "sequential-runmode";

	private static Options options;

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
			{ Channel.WARNING, ChannelState.HIDDENVIEW } };

	public static void main(String[] args) {
		try {
			run(args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			showHelp();
		} catch (Exception e) { // run throws generic Exceptions...
			e.printStackTrace();
		}
	}

	/**
	 * @param args
	 * @throws ParseException
	 * @throws ParserException
	 * @throws FileNotFoundException
	 * @throws GOALRunFailedException
	 * @throws Exception
	 */
	public static void run(String... args)
			throws GOALRunFailedException, ParseException, FileNotFoundException, ParserException {
		CommandLineParser parser = new DefaultParser();
		options = createOptions();
		CommandLine cmd = parser.parse(options, args);

		/*
		 * Handle general options.
		 */
		if (cmd.hasOption(OPTION_HELP)) {
			showHelp();
			return;
		}

		if (cmd.hasOption(OPTION_LICENSE)) {
			showLicense();
			return;
		}

		// Verbose makes other verbose options irrelevant.
		DebugPreferences.setDefault(getDefaultPrefs());
		if (cmd.hasOption(OPTION_LOGTOFILE)) {
			LoggingPreferences.setLogToFile(true);
			String logdir = cmd.getOptionValue(OPTION_LOGTOFILE);
			if (logdir != null && !logdir.isEmpty()) {
				LoggingPreferences.setLogDirectory(logdir);
			}
		}
		if (cmd.hasOption(OPTION_VERBOSE_SHORT)) {
			Loggers.addConsoleLogger();
		} else {
			if (cmd.hasOption(OPTION_VERBOSE_INFO)) {
				Loggers.getInfoLogger().addConsoleLogger();
			}

			if (cmd.hasOption(OPTION_VERBOSE_WARNING)) {
				Loggers.getWarningLogger().addConsoleLogger();
			}
		}

		/*
		 * Run .mas2g files.
		 */
		List<File> masFiles = parseFileArguments(cmd.getArgs(), new MASProgramFilter(cmd.hasOption(OPTION_RECURSIVE)));

		BatchRun repeatedBatchRun = new BatchRun(masFiles);

		final boolean debuggerOutput = cmd.hasOption(OPTION_DEBUG);
		repeatedBatchRun.setDebuggerOutput(debuggerOutput);

		if (cmd.hasOption(OPTION_REPEATS)) {
			Number repeats = (Number) cmd.getParsedOptionValue(OPTION_REPEATS);
			repeatedBatchRun.setRepeats(repeats.longValue());
		}
		if (cmd.hasOption(OPTION_TIMEOUT)) {
			Number timeout = (Number) cmd.getParsedOptionValue(OPTION_TIMEOUT);
			repeatedBatchRun.setTimeOut(timeout.longValue());
		}

		CorePreferences.setRemoveKilledAgent(!cmd.hasOption(OPTION_KEEP_KILLED));
		CorePreferences.setSleepRepeatingAgent(cmd.hasOption(OPTION_SLEEP_REPETITIVE));
		CorePreferences.setAgentCopyEnvRunState(cmd.hasOption(OPTION_TAKE_ENV_STATE));
		CorePreferences.setSequentialExecution(cmd.hasOption(OPTION_SEQUENTIAL_RUNMODE));

		repeatedBatchRun.run(true);

		/*
		 * Run .test2g files.
		 */
		List<File> testFiles = parseFileArguments(cmd.getArgs(), new UnitTestFilter(cmd.hasOption(OPTION_RECURSIVE)));

		for (File unitTestFile : testFiles) {
			try {
				FileRegistry registry = new FileRegistry();
				TestValidator validator = new TestValidator(unitTestFile.getCanonicalPath(), registry);
				validator.validate();
				TestProgram testProgram = validator.getProgram();
				TestRun testRun = new TestRun(testProgram, false);
				testRun.setDebuggerOutput(debuggerOutput);
				testRun.run(true);
			} catch (IOException e) {
				new Warning("running '" + unitTestFile + "' failed.", e).emit();
			}
		}

		Loggers.removeConsoleLogger();
	}

	/**
	 * Parses any left over arguments as files.
	 *
	 * @param arguments holding the unparsed arguments.
	 * @param function  that transforms a file into a list of T
	 *
	 * @return a list of T
	 * @throws ParseException        when no left over arguments were present
	 * @throws ParserException       when the file could not be parsed
	 * @throws FileNotFoundException when the argument was not a file or directory
	 */
	private static List<File> parseFileArguments(String[] arguments, FileFilter filter) throws FileNotFoundException {
		if (arguments.length == 0) {
			throw new FileNotFoundException("missing file or directory.");
		}

		List<File> files = new LinkedList<>();
		for (String fileOrFolder : arguments) {
			File f = new File(fileOrFolder);
			if (f.isDirectory() || f.isFile()) {
				files.addAll(filter.proccess(f));
			} else {
				throw new FileNotFoundException("'" + fileOrFolder + "' is neither a file nor a directory.");
			}
		}
		return files;
	}

	private static interface FileFilter {
		public abstract SortedSet<File> proccess(File f);
	}

	private static class MASProgramFilter implements FileFilter {
		private final boolean recursive;

		public MASProgramFilter(boolean recursive) {
			this.recursive = recursive;
		}

		@Override
		public SortedSet<File> proccess(File f) {
			return getMASFiles(f, this.recursive);
		}
	}

	private static class UnitTestFilter implements FileFilter {
		private final boolean recursive;

		public UnitTestFilter(boolean recursive) {
			this.recursive = recursive;
		}

		@Override
		public SortedSet<File> proccess(File f) {
			return getUnitTestFiles(f, this.recursive);
		}
	}

	/**
	 * Creates the command line options.
	 *
	 * @return the command line options.
	 */
	private static Options createOptions() {
		Options options = new Options();
		Option.Builder option;

		option = Option.builder(OPTION_VERBOSE_SHORT).desc("Print all messages").longOpt(OPTION_VERBOSE);
		options.addOption(option.build());

		option = Option.builder(OPTION_VERBOSE_INFO).desc("Print messages from info");
		options.addOption(option.build());

		option = Option.builder(OPTION_VERBOSE_WARNING).desc("Print messages from warning");
		options.addOption(option.build());

		option = Option.builder(OPTION_LOGTOFILE).desc("Write agent logs to files (in the given directory if any)")
				.hasArg().type(String.class).optionalArg(true);
		options.addOption(option.build());

		options.addOption(new Option(OPTION_HELP_SHORT, OPTION_HELP, false, "Displays this help"));

		option = Option.builder(OPTION_LICENSE).desc("Shows the license");
		options.addOption(option.build());

		option = Option.builder(OPTION_REPEATS_SHORT).longOpt(OPTION_REPEATS).argName("number")
				.desc("Number of times to repeat running all episodes").hasArg().type(Number.class);
		options.addOption(option.build());

		option = Option.builder(OPTION_TIMEOUT_SHORT).longOpt(OPTION_TIMEOUT).argName("number")
				.desc("Maximum time to run a system (in seconds)").hasArg().type(Number.class);
		options.addOption(option.build());

		option = Option.builder().longOpt(OPTION_RECURSIVE).desc("Recursively search for mas files");
		options.addOption(option.build());

		option = Option.builder(OPTION_DEBUG_SHORT).longOpt(OPTION_DEBUG)
				.desc("Display output from debugger while running agent");
		options.addOption(option.build());

		option = Option.builder().longOpt(OPTION_KEEP_KILLED).desc("Keep killed agents available in the runtime");
		options.addOption(option.build());

		option = Option.builder().longOpt(OPTION_SLEEP_REPETITIVE).desc(
				"Sleep agents when they receive the same percepts/messages and do the same actions in consecutive cycles");
		options.addOption(option.build());

		option = Option.builder().longOpt(OPTION_TAKE_ENV_STATE)
				.desc("Let new agents copy the environment's run state (or run if no environment)");
		options.addOption(option.build());

		option = Option.builder().longOpt(OPTION_SEQUENTIAL_RUNMODE)
				.desc("Run multiple agents in sequence (instead of in parallel as is the default)");
		options.addOption(option.build());

		return options;
	}

	/**
	 * If argument is a mas2g file it will be added. If the argument is a folder all
	 * mas2g files in it will be added. If <code>recursive</code> is {@code true},
	 * mas2g files from all sub-folders (and sub-sub-folders, etc.) will also be
	 * added.
	 *
	 * @param fileOrFolder File or folder to load mas2g file(s) from.
	 * @param recursive    If {@code true} all mas2g files in subfolders will also
	 *                     be loaded.
	 * @return List of the MAS files that were loaded, in alphabetical order.
	 */
	public static SortedSet<File> getMASFiles(File fileOrFolder, boolean recursive) {
		SortedSet<File> masFiles = new TreeSet<>();

		if (fileOrFolder.isFile() && Extension.getFileExtension(fileOrFolder) == Extension.MAS2G) {
			masFiles.add(fileOrFolder);
			return masFiles;
		}

		if (fileOrFolder.isDirectory()) {
			for (File file : fileOrFolder.listFiles()) {
				if (file.isFile()) {
					masFiles.addAll(getMASFiles(file, recursive));
				}

				if (file.isDirectory() && recursive) {
					masFiles.addAll(getMASFiles(file, recursive));
				}
			}
		}

		return masFiles;
	}

	/**
	 * If argument is a test2g file it will be added. If the argument is a folder
	 * all test2g files in it will be added. If <code>recursive</code> is
	 * {@code true}, test2g files from all sub-folders (and sub-sub-folders, etc.)
	 * will also be added.
	 *
	 * @param fileOrFolder File or folder to load test2g file(s) from.
	 * @param recursive    If {@code true} all test2g files in subfolders will also
	 *                     be loaded.
	 * @return Set of the test2g files that were loaded, in alphabetical order.
	 */
	public static SortedSet<File> getUnitTestFiles(File fileOrFolder, boolean recursive) {
		SortedSet<File> files = new TreeSet<>();

		if (fileOrFolder.isFile() && Extension.getFileExtension(fileOrFolder) == Extension.TEST2G) {
			files.add(fileOrFolder);
			return files;
		}

		if (fileOrFolder.isDirectory()) {
			for (File file : fileOrFolder.listFiles()) {
				if (file.isFile()) {
					files.addAll(getUnitTestFiles(file, recursive));
				}

				if (file.isDirectory() && recursive) {
					files.addAll(getUnitTestFiles(file, recursive));
				}
			}
		}

		return files;
	}

	/**
	 * Prints the help for the command line options.
	 */
	private static void showHelp() {
		System.out.println("GOAL Copyright (C) 2020 GPLv3");
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(Run.class.getCanonicalName() + " [options] [[file|directory]]", options);
	}

	/**
	 * Print the license; required by GPL v3.
	 */
	private static void showLicense() {
		System.out.println("GOAL interpreter that facilitates developing and executing GOAL multi-agent programs.\n"
				+ "Copyright (C) 2020\n\n" + "This program is free software: you can redistribute it and/or modify\n"
				+ "it under the terms of the GNU General Public License as published by\n"
				+ "the Free Software Foundation, either version 3 of the License, or\n"
				+ "(at your option) any later version.\n\n"
				+ "This program is distributed in the hope that it will be useful,\n"
				+ "but WITHOUT ANY WARRANTY; without even the implied warranty of\n"
				+ "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n"
				+ "GNU General Public License for more details.\n\n"
				+ "You should have received a copy of the GNU General Public License\n"
				+ "along with this program.  If not, see <http://www.gnu.org/licenses/>.\n");
	}

	public static Map<String, Object> getDefaultPrefs() {
		Map<String, Object> map = new HashMap<>();
		for (Object[] keyvalue : debugPrefs) {
			map.put(keyvalue[0].toString(), keyvalue[1].toString());
		}
		return map;
	}
}
