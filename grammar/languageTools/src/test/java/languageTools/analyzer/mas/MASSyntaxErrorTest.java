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

package languageTools.analyzer.mas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import languageTools.analyzer.FileRegistry;
import languageTools.errors.Message;
import languageTools.errors.ParserError.SyntaxError;
import languageTools.program.mas.MASProgram;

public class MASSyntaxErrorTest {
	private List<Message> syntaxerrors;
	private List<Message> errors;
	private List<Message> warnings;
	private MASProgram program;

	/**
	 * Creates validator, calls validate, and initializes relevant fields.
	 *
	 * @param resource The MAS file used in the test.
	 */
	private void setup(String resource) {
		FileRegistry registry = new FileRegistry();
		MASValidator validator = new MASValidator(resource, registry);
		validator.validate();

		this.syntaxerrors = new ArrayList<>(registry.getSyntaxErrors());
		this.errors = new ArrayList<>(registry.getErrors());
		this.warnings = new ArrayList<>(registry.getWarnings());
		this.program = validator.getProgram();

		List<Message> all = new LinkedList<>();
		all.addAll(this.syntaxerrors);
		all.addAll(this.errors);
		all.addAll(this.warnings);
		System.out.println(this.program.getSourceFile() + ": " + all);
	}

	@Test
	public void test_AGENT_UNKNOWN_USECASE() {
		setup("src/test/resources/languageTools/analyzer/mas/test_AGENT_UNKNOWN_USECASE.mas2g");

		// MAS should produce a syntax error
		assertEquals(1, this.syntaxerrors.size());
		assertEquals(SyntaxError.INPUTMISMATCH, this.syntaxerrors.get(0).getType());

		// MAS should produce an error
		assertEquals(1, this.errors.size());

		// MAS should not produce any warnings
		assertTrue(this.warnings.isEmpty());

		// MAS program should have at least 1 agent definition
		assertTrue(this.program.getAgentNames().size() > 0);
	}

	@Test
	public void test_CONSTRAINT_UNKNOWN_KEY() {
		setup("src/test/resources/languageTools/analyzer/mas/test_CONSTRAINT_UNKNOWN_KEY.mas2g");

		// MAS should produce 1 syntax error
		assertEquals(1, this.syntaxerrors.size());

		assertEquals(SyntaxError.UNWANTEDTOKEN, this.syntaxerrors.get(0).getType());

		// MAS should produce 1 error...
		assertEquals(1, this.errors.size());

		// MAS should produce no warnings
		assertEquals(0, this.warnings.size());

		assertEquals(1, this.program.getLaunchRules().size());
	}

	@Test
	public void test_INIT_UNRECOGNIZED_KEY() {
		setup("src/test/resources/languageTools/analyzer/mas/test_INIT_UNRECOGNIZED_KEY.mas2g");

		// MAS should produce 1 syntax error
		assertEquals(1, this.syntaxerrors.size());

		assertEquals(SyntaxError.INPUTMISMATCH, this.syntaxerrors.get(0).getType());

		// MAS should produce no errors
		assertTrue(this.errors.isEmpty());

		// MAS should produce no warnings
		assertTrue(this.warnings.isEmpty());

		assertEquals(new File("src/test/resources/languageTools/analyzer/mas/dummy_environment.jar"),
				this.program.getEnvironmentfile());
		assertEquals(new HashMap<String, Object>(0), this.program.getInitParameters());
	}

	@Test
	public void test_LAUNCH_NO_RULES() {
		setup("src/test/resources/languageTools/analyzer/mas/test_LAUNCH_NO_RULES.mas2g");

		// MAS should not produce any syntax errors
		assertTrue(this.syntaxerrors.isEmpty());

		// MAS should not produce any errors
		assertTrue(this.errors.isEmpty());

		// MAS should produce 2 warnings
		assertEquals(2, this.warnings.size());

		assertEquals(0, this.program.getLaunchRules().size());
	}
}
