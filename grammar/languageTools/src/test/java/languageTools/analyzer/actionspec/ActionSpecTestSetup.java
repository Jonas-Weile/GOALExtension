package languageTools.analyzer.actionspec;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import languageTools.analyzer.FileRegistry;
import languageTools.errors.Message;
import languageTools.program.actionspec.ActionSpecProgram;

public abstract class ActionSpecTestSetup {

	private String path = "src/test/resources/languageTools/analyzer/actionspec";

	private List<Message> syntaxerrors;
	private List<Message> errors;
	private List<Message> warnings;

	private ActionSpecProgram program;

	/**
	 * Creates validator, calls validate, and initializes relevant fields.
	 *
	 * @param resource
	 *            The module file used in the test.
	 */
	void setup(String resource) {
		FileRegistry registry = new FileRegistry();
		ActionSpecValidator validator = new ActionSpecValidator(this.path + resource, registry);
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

	List<Message> getSyntaxerrors() {
		return this.syntaxerrors;
	}

	List<Message> getErrors() {
		return this.errors;
	}

	List<Message> getWarnings() {
		return this.warnings;
	}

	ActionSpecProgram getProgram() {
		return this.program;
	}

}
