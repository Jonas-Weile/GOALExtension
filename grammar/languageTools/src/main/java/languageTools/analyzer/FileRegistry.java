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

package languageTools.analyzer;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;

import languageTools.errors.Message;
import languageTools.program.Program;

/**
 * Keeps a record of all files processed by a validator, to avoid duplicate
 * processing. Stores a time stamp and the program that is the result of
 * validating the file with the file.
 */
public class FileRegistry {
	protected final Map<File, Program> filePrograms;
	protected final Map<File, Long> fileTimeStamps;
	private final SortedSet<Message> syntaxErrors;
	private final SortedSet<Message> errors;
	private final SortedSet<Message> warnings;

	/**
	 * copies existing registry for temporary additions (eg when user does a ad-hoc
	 * query from introspector. We need to copy this registry then to avoid
	 * additional errors from the ad-hoc query polluting the agent's registry
	 *
	 * @param registry
	 */
	public FileRegistry(FileRegistry registry) {
		this.filePrograms = new ConcurrentHashMap<>(registry.filePrograms);
		this.fileTimeStamps = new ConcurrentHashMap<>(registry.fileTimeStamps);
		this.syntaxErrors = new TreeSet<>();
		this.errors = new TreeSet<>();
		this.warnings = new TreeSet<>();
	}

	public FileRegistry() {
		this.filePrograms = new ConcurrentHashMap<>();
		this.fileTimeStamps = new ConcurrentHashMap<>();
		this.syntaxErrors = new TreeSet<>();
		this.errors = new TreeSet<>();
		this.warnings = new TreeSet<>();

	}

	/**
	 * Registers a file with associated program.
	 *
	 * @param source  The source file for a program.
	 * @param program A program.
	 */
	public void register(File source, Program program) {
		if (source != null && program != null) {
			this.filePrograms.put(source, program);
			this.fileTimeStamps.put(source, source.lastModified());
		}
	}

	/**
	 * Removes the file from the registry
	 */
	public void unregister(File source) {
		this.filePrograms.remove(source);
		this.fileTimeStamps.remove(source);
	}

	/**
	 * @return All source files stored in this registry.
	 */
	public List<File> getSourceFiles() {
		return new ArrayList<>(this.filePrograms.keySet());
	}

	/**
	 * @param source A source file.
	 * @return A program, if the file is registered, {@code null} otherwise.
	 */
	public Program getProgram(File source) {
		return this.filePrograms.get(source);
	}

	/**
	 * @param source A file.
	 * @return {@code true} if file is new or modified since it was last registered.
	 */
	public boolean needsProcessing(File source) {
		boolean newfile = !this.fileTimeStamps.containsKey(source);
		boolean modified = false;
		if (!newfile) {
			modified = this.fileTimeStamps.get(source) != source.lastModified();
		}
		return newfile || modified;
	}

	/**
	 * @return The list of syntax errors found by the parser.
	 */
	public SortedSet<Message> getSyntaxErrors() {
		return Collections.unmodifiableSortedSet(this.syntaxErrors);
	}

	public boolean addSyntaxError(Message syntaxError) {
		return this.syntaxErrors.add(syntaxError);
	}

	/**
	 * @return The list of semantic (validation) errors found during validation.
	 */
	public SortedSet<Message> getErrors() {
		return Collections.unmodifiableSortedSet(this.errors);
	}

	public boolean addError(Message error) {
		return this.errors.add(error);
	}

	/**
	 * @return The list of warnings found during validation.
	 */
	public SortedSet<Message> getWarnings() {
		return Collections.unmodifiableSortedSet(this.warnings);
	}

	public boolean addWarning(Message warning) {
		return this.warnings.add(warning);
	}

	public boolean hasSyntaxError() {
		return !this.syntaxErrors.isEmpty();
	}

	public boolean hasError() {
		return !this.errors.isEmpty();
	}

	public boolean hasAnyError() {
		return hasSyntaxError() || hasError();
	}

	public boolean hasWarning() {
		return !this.warnings.isEmpty();
	}

	public SortedSet<Message> getAllErrors() {
		SortedSet<Message> allErrors = new TreeSet<>(this.syntaxErrors);
		allErrors.addAll(this.errors);
		return allErrors;
	}

	/**
	 * Reports the results of the validation, listing all syntax and validation
	 * errors and warnings.
	 */
	public String report() {
		StringBuilder report = new StringBuilder();
		// Report parsing errors
		report.append("\n");
		report.append("-----------------------------------------------------------------\n");
		report.append(" PARSING REPORT: ");
		if (getSyntaxErrors().size() == 0) {
			report.append("Parsing of file was successful.\n");
		} else {
			report.append("Found " + getSyntaxErrors().size() + " parsing error(s).\n");
		}
		report.append(" Files parsed: " + getSourceFiles() + "\n");
		report.append("-----------------------------------------------------------------\n");
		for (Message error : getSyntaxErrors()) {
			report.append(error + "\n");
		}
		report.append("\n");
		// Report validation errors
		report.append("-----------------------------------------------------------------\n");
		report.append(" VALIDATOR REPORT: ");
		report.append("Found " + getErrors().size() + " error(s) and " + getWarnings().size() + " warning(s).\n");
		report.append("-----------------------------------------------------------------\n");
		for (Message error : getErrors()) {
			report.append(error + "\n");
		}
		report.append("\n");
		for (Message warning : getWarnings()) {
			report.append(warning + "\n");
		}
		report.append("-----------------------------------------------------------------\n");

		return report.toString();
	}
}