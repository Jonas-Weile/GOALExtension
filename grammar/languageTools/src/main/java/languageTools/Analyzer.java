/**
 * The GOAL Grammar Tools. Copyright (C) 2014 Koen Hindriks.
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

package languageTools;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import cognitiveKrFactory.InstantiationFailedException;
import languageTools.analyzer.FileRegistry;
import languageTools.analyzer.Validator;
import languageTools.analyzer.actionspec.ActionSpecValidator;
import languageTools.analyzer.mas.MASValidator;
import languageTools.analyzer.module.ModuleValidator;
import languageTools.analyzer.planner.PlannerValidator;
import languageTools.analyzer.test.TestValidator;
import languageTools.codeanalysis.MasAnalysis;
import languageTools.utils.Extension;

/**
 * <pre>
 * {@code
 * Usage: languageTools.Analyzer [options] [[file|directory]]
 *  -mas				Analyze MAS files
 *  -module				Analyze module files
 *  -r,--recursive		Recursively search directories
 * }
 * </pre>
 *
 * FIXME: add act2g and test2g here too
 */
public class Analyzer {
	private static final String OPTION_MAS = "mas";
	private static final String OPTION_MOD2G = "module";

	private static final String OPTION_RECURSIVE = "recursive";
	private static final String OPTION_RECURSIVE_SHORT = "r";

	private static final String OPTION_HELP = "help";
	private static final String OPTION_HELP_SHORT = "h";

	private static final String OPTION_LICENSE = "license";

	private static final Options options = createOptions();

	// Analyze MAS files?
	private static boolean masFile;
	// Analyze module files?
	private static boolean moduleFile;
	// Recursively search directories?
	private static boolean recursive;

	/**
	 *
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) {
		// Get start time.
		long startTime = System.nanoTime();

		// Parse command line options
		File file;
		try {
			file = parseOptions(args);
		} catch (ParseException e) {
			System.out.println(e.getMessage());
			showHelp();
			return;
		}

		// Get all files that should be analyzed
		List<File> files = new LinkedList<>();
		if (file.isDirectory()) {
			// Search directory for indicated file types
			files = searchDirectory(file);
			System.out.println("Found " + files.size() + " file(s).\n");
		} else {
			files.add(file);
		}

		// Process files found.
		for (File masFile : files) {
			try {
				System.out.println(new MasAnalysis(masFile).toString());
			} catch (InstantiationFailedException e) {
				e.printStackTrace();
			}
		}

		// Get elapsed time.
		long elapsedTime = (System.nanoTime() - startTime) / 1000000;
		System.out.println("Took " + elapsedTime + " milliseconds to analyze " + files.size() + " file(s).");
	}

	/**
	 * Validates a source file with known extension.
	 *
	 * @param source A file to be validated.
	 * @return Validator that validated the given source file.
	 */
	public static Validator<?, ?, ?, ?> processFile(File source, FileRegistry registry) throws IOException {
		Validator<?, ?, ?, ?> validator = null;
		switch (Extension.getFileExtension(source)) {
		case MOD2G:
			validator = new ModuleValidator(source.getCanonicalPath(), registry);
			break;
		case MAS2G:
			validator = new MASValidator(source.getCanonicalPath(), registry);
			break;
		case ACT2G:
			validator = new ActionSpecValidator(source.getCanonicalPath(), registry);
			break;
		case PLAN2G:
			validator = new PlannerValidator(source.getCanonicalPath(), registry);
			break;
		case TEST2G:
			validator = new TestValidator(source.getCanonicalPath(), registry);
			break;
		default:
			throw new IOException("Expected file with extension 'act2g', 'mas2g', 'mod2g', or 'test2g'");
		}

		validator.validate();
		return validator;
	}

	/**
	 * Collects relevant files in a directory.
	 *
	 * @param directory The directory to be searched.
	 * @param recursive Indicates whether directory should be searched recursively,
	 *                  i.e., whether directories inside directories should also be
	 *                  searched.
	 * @return List of retrieved files.
	 */
	private static List<File> searchDirectory(File directory) {
		List<File> files = new LinkedList<>();
		for (File file : directory.listFiles()) {
			if (file.isFile()) {
				if (isMASFile(file) && masFile) {
					files.add(file);
				} else if (isModuleFile(file) && moduleFile) {
					files.add(file);
				}
			} else if (file.isDirectory() && recursive) {
				files.addAll(searchDirectory(file));
			}
		}
		return files;
	}

	/**
	 * Checks whether file is a MAS file.
	 *
	 * @param file The file to check.
	 * @return {@code true} if extension of the file is {@link Extension#MAS2G}.
	 */
	public static boolean isMASFile(File file) {
		return Extension.getFileExtension(file) == Extension.MAS2G;
	}

	/**
	 * Checks whether file is a module file.
	 *
	 * @param file The file to check.
	 * @return {@code true} if extension of the file is {@link Extension#MOD2G}.
	 */
	public static boolean isModuleFile(File file) {
		return Extension.getFileExtension(file) == Extension.MOD2G;
	}

	/**
	 * Checks whether file is a test file.
	 *
	 * @param file The file to check.
	 * @return {@code true} if extension of the file is {@link Extension#TEST2G} .
	 */
	public static boolean isTestFile(File file) {
		return Extension.getFileExtension(file) == Extension.TEST2G;
	}

	// -------------------------------------------------------------
	// Command line options
	// -------------------------------------------------------------

	/**
	 * Creates the command line options.
	 *
	 * @return The command line options.
	 */
	private static Options createOptions() {
		Options options = new Options();

		options.addOption(new Option(OPTION_MAS, "Analyze MAS files"));

		options.addOption(new Option(OPTION_MOD2G, "Analyze module files"));

		options.addOption(new Option(OPTION_RECURSIVE_SHORT, OPTION_RECURSIVE, false,
				"Recursively search directories for files"));

		options.addOption(new Option(OPTION_HELP_SHORT, OPTION_HELP, false, "Displays this help"));

		options.addOption(new Option(OPTION_LICENSE, "Shows the license"));

		return options;
	}

	private static File parseOptions(String[] args) throws ParseException {
		CommandLineParser parser = new DefaultParser();
		CommandLine cmd = parser.parse(options, args);

		masFile = cmd.hasOption(OPTION_MAS);
		moduleFile = cmd.hasOption(OPTION_MOD2G);
		recursive = cmd.hasOption(OPTION_RECURSIVE);

		/*
		 * Handle general options.
		 */
		if (cmd.hasOption(OPTION_HELP)) {
			throw new ParseException("The GOAL Grammar Tools. Copyright (C) 2018 GPLv3");
		}

		if (cmd.hasOption(OPTION_LICENSE)) {
			showLicense();
			throw new ParseException("");
		}

		// Process remaining arguments
		if (cmd.getArgs().length == 0) {
			throw new ParseException("Missing file or directory");
		}
		if (cmd.getArgs().length > 1) {
			throw new ParseException("Expected single file or directory name but got: " + Arrays.asList(cmd.getArgs()));
		}

		// Check existence of file
		File file = new File(cmd.getArgs()[0]);
		if (!file.exists()) {
			throw new ParseException("Could not find " + file);
		}
		return file;
	}

	/**
	 * Prints help message with command line options.
	 */
	private static void showHelp() {
		HelpFormatter formatter = new HelpFormatter();
		formatter.printHelp(Analyzer.class.getCanonicalName() + " [options] [[file|directory]]", options);
	}

	/**
	 * Print the license; required by GPL v3.
	 */
	private static void showLicense() {
		System.out.println("The GOAL Grammar Tools. Copyright (C) 2018 Koen Hindriks.\n\n"
				+ "This program is free software: you can redistribute it and/or modify\n"
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
}
