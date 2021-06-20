package languageTools.analyzer.module;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import languageTools.analyzer.FileRegistry;
import languageTools.errors.Message;
import languageTools.program.agent.Module;

public abstract class ModuleTestSetup {
	private String path = "src/test/resources/languageTools/analyzer/module";

	private List<Message> syntaxerrors;
	private List<Message> errors;
	private List<Message> warnings;

	private Module program;

	/**
	 * Creates validator, calls validate, and initializes relevant fields.
	 *
	 * @param resource The module file used in the test.
	 */
	void setup(String resource) {
		FileRegistry registry = new FileRegistry();
		ModuleValidator validator = new ModuleValidator(this.path + resource, registry);
		validator.validate();

		this.syntaxerrors = new ArrayList<>(registry.getSyntaxErrors());
		this.errors = new ArrayList<>(registry.getErrors());
		this.warnings = new ArrayList<>(registry.getWarnings());
		this.program = validator.getProgram();

		List<Message> all = new LinkedList<>();
		all.addAll(this.syntaxerrors);
		all.addAll(this.errors);
		all.addAll(this.warnings);
		String source = "???";
		if (this.program != null && this.program.getSourceFile() != null) {
			source = this.program.getSourceFile().toString();
		}
		System.out.println(source + ": " + all);
	}

	List<Message> getSyntaxErrors() {
		return this.syntaxerrors;
	}

	List<Message> getErrors() {
		return this.errors;
	}

	List<Message> getWarnings() {
		return this.warnings;
	}

	String getKRInterface() {
		return this.program.getKRInterface().getClass().getName();
	}

	Module getProgram() {
		return this.program;
	}
}
