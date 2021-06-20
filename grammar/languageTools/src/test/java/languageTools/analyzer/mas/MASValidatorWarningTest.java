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
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import languageTools.analyzer.FileRegistry;
import languageTools.errors.Message;
import languageTools.errors.mas.MASWarning;
import languageTools.program.mas.MASProgram;

public class MASValidatorWarningTest {
	private List<Message> syntaxerrors;
	private List<Message> errors;
	private List<Message> warnings;
	private MASProgram program;

	/**
	 * Creates validator, calls validate, and initializes relevant fields.
	 *
	 * @param resource
	 *            The MAS file used in the test.
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
	public void testMAStemplate() {
		setup("src/test/resources/languageTools/analyzer/mas/template.mas2g");

		// MAS should not produce any syntax errors
		assertTrue(this.syntaxerrors.isEmpty());

		// MAS template should not produce any errors
		assertTrue(this.errors.isEmpty());

		// MAS template should not produce any warnings
		assertTrue(this.warnings.isEmpty());
	}

	@Test
	public void test_AGENT_UNUSED() {
		setup("src/test/resources/languageTools/analyzer/mas/test_AGENT_UNUSED.mas2g");

		// MAS should not produce any syntax errors
		assertTrue(this.syntaxerrors.isEmpty());

		// MAS should not produce any errors
		assertTrue(this.errors.isEmpty());

		// MAS should produce 1 warning
		assertEquals(1, this.warnings.size());

		assertEquals(MASWarning.AGENT_UNUSED, this.warnings.get(0).getType());

		// MAS program should have 2 agent definitions
		assertEquals(2, this.program.getAgentNames().size());
	}

	@Test
	public void test_CONSTRAINT_DUPLICATE() {
		setup("src/test/resources/languageTools/analyzer/mas/test_CONSTRAINT_DUPLICATE.mas2g");

		// MAS should not produce any syntax errors
		assertTrue(this.syntaxerrors.isEmpty());

		// MAS should not produce any errors
		assertTrue(this.errors.isEmpty());

		// MAS should produce 1 warning
		assertEquals(1, this.warnings.size());

		assertEquals(MASWarning.CONSTRAINT_DUPLICATE, this.warnings.get(0).getType());

		assertEquals("connect", this.program.getLaunchRules().get(0).getInstructions().get(0).getGivenName("", 0));
		assertEquals(1, this.program.getLaunchRules().get(0).getInstructions().get(0).getMaxNumberOfAgentsToLaunch());

		assertEquals(1, this.program.getLaunchRules().size());
	}

	@Test
	public void test_INIT_DUPLICATE_KEY() {
		setup("src/test/resources/languageTools/analyzer/mas/test_INIT_DUPLICATE_KEY.mas2g");

		// MAS should not produce any syntax errors
		assertTrue(this.syntaxerrors.isEmpty());

		// MAS should not produce any errors
		assertTrue(this.errors.isEmpty());

		// MAS should produce 1 warning
		assertEquals(1, this.warnings.size());

		assertEquals(MASWarning.INIT_DUPLICATE_KEY, this.warnings.get(0).getType());

		assertEquals(new File("src/test/resources/languageTools/analyzer/mas/dummy_environment.jar"),
				this.program.getEnvironmentfile());
		assertEquals("value1", this.program.getInitParameters().get("key"));
	}

	@Test
	public void test_LAUNCH_NO_CONDITIONAL_RULES() {
		setup("src/test/resources/languageTools/analyzer/mas/test_LAUNCH_NO_CONDITIONAL_RULES.mas2g");

		// MAS should not produce any syntax errors
		assertTrue(this.syntaxerrors.isEmpty());

		// MAS should not produce any errors
		assertTrue(this.errors.isEmpty());

		// MAS should produce 1 warning
		assertEquals(1, this.warnings.size());

		assertEquals(MASWarning.LAUNCH_NO_CONDITIONAL_RULES, this.warnings.get(0).getType());

		assertEquals(1, this.program.getLaunchRules().size());
	}

	@Test
	public void test_USECASE_MISSING() {
		setup("src/test/resources/languageTools/analyzer/mas/test_USECASE_MISSING.mas2g");

		// MAS should not produce any syntax errors
		assertTrue(this.syntaxerrors.isEmpty());

		// MAS should produce 1 error
		assertTrue(this.errors.isEmpty());

		// MAS should not produce any warnings
		assertTrue(this.warnings.isEmpty());

		assertEquals(1, this.program.getLaunchRules().size());
	}

}
