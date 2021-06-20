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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.junit.Test;

import languageTools.analyzer.FileRegistry;
import languageTools.errors.Message;
import languageTools.errors.ParserError.SyntaxError;
import languageTools.errors.mas.MASError;
import languageTools.errors.mas.MASWarning;
import languageTools.program.mas.MASProgram;

public class MASValidatorErrorTest {
	private MASProgram program;
	private FileRegistry registry;
	private MASValidator validator;

	/**
	 * Creates validator, calls validate, and initializes relevant fields.
	 *
	 * @param resource
	 *            The MAS file used in the test.
	 */
	private void setup(String resource) {
		this.registry = new FileRegistry();
		this.validator = new MASValidator(resource, this.registry);
		this.validator.validate();
		collectErrors(this.validator);
	}

	private List<Message> errors() {
		return new ArrayList<>(this.registry.getErrors());
	}

	private List<Message> warnings() {
		return new ArrayList<>(this.registry.getWarnings());
	}

	private List<Message> syntaxerrors() {
		return new ArrayList<>(this.registry.getSyntaxErrors());
	}

	/**
	 * side effect: sets the private fields.
	 *
	 * @param validator
	 * @return collected errors.
	 */
	private List<Message> collectErrors(MASValidator validator) {
		this.program = validator.getProgram();

		List<Message> all = new LinkedList<>();
		all.addAll(syntaxerrors());
		all.addAll(errors());
		all.addAll(warnings());
		System.out.println(this.program.getSourceFile() + ": " + all);
		return all;
	}

	private void setupAndProcess(String resource) {
		this.registry = new FileRegistry();
		this.validator = new MASValidator(resource, this.registry);
		this.validator.validate();
		this.validator.process();
		collectErrors(this.validator);
	}

	@Test
	public void test_AGENT_DUPLICATE_GIVENNAME() {
		setup("src/test/resources/languageTools/analyzer/mas/test_AGENT_DUPLICATE_GIVENNAME.mas2g");

		// MAS should not produce any syntax errors
		assertTrue(syntaxerrors().isEmpty());

		// MAS should produce 1 error
		assertEquals(1, errors().size());
		assertEquals(MASError.AGENT_DUPLICATE_GIVENNAME, errors().get(0).getType());

		// MAS should produce no warnings
		assertTrue(warnings().isEmpty());
	}

	@Test
	public void test_AGENT_DUPLICATE_NAME() {
		setup("src/test/resources/languageTools/analyzer/mas/test_AGENT_DUPLICATE_NAME.mas2g");

		// MAS should not produce any syntax errors
		assertTrue(syntaxerrors().isEmpty());

		// MAS should produce 1 error
		assertEquals(1, errors().size());
		assertEquals(MASError.AGENT_DUPLICATE_NAME, errors().get(0).getType());
		assertEquals(1, this.program.getAgentNames().size());

		// MAS should produce no warnings
		assertTrue(warnings().isEmpty());
	}

	@Test
	public void test_ENVIRONMENT_COULDNOT_FIND() {
		setup("src/test/resources/languageTools/analyzer/mas/test_ENVIRONMENT_COULDNOT_FIND.mas2g");

		// MAS should not produce any syntax errors
		assertTrue(syntaxerrors().isEmpty());

		// MAS should produce 1 error
		assertEquals(1, errors().size());
		assertEquals(MASError.ENVIRONMENT_COULDNOT_FIND, errors().get(0).getType());
		assertEquals(null, this.program.getEnvironmentfile());

		// MAS should produce 1 warning
		assertEquals(1, warnings().size());
		assertEquals(MASWarning.LAUNCH_CONDITIONAL_RULE, warnings().get(0).getType());
	}

	@Test
	public void test_ENVIRONMENT_NO_REFERENCE() {
		setup("src/test/resources/languageTools/analyzer/mas/test_ENVIRONMENT_NO_REFERENCE.mas2g");

		// MAS should produce a syntax error
		assertEquals(1, syntaxerrors().size());
		assertEquals(SyntaxError.INPUTMISMATCH, syntaxerrors().get(0).getType());

		// MAS should produce 1 error
		assertEquals(1, errors().size());
		assertEquals(MASError.ENVIRONMENT_COULDNOT_FIND, errors().get(0).getType());
		assertEquals(null, this.program.getEnvironmentfile());

		// MAS should produce 1 warning
		assertEquals(1, warnings().size());
		assertEquals(MASWarning.LAUNCH_CONDITIONAL_RULE, warnings().get(0).getType());
	}

	@Test
	public void test_INIT_UNRECOGNIZED_PARAMETER() {
		setup("src/test/resources/languageTools/analyzer/mas/test_INIT_UNRECOGNIZED_PARAMETER.mas2g");

		// MAS should not produce any syntax errors
		assertEquals(1, syntaxerrors().size());
		assertEquals(SyntaxError.INPUTMISMATCH, syntaxerrors().get(0).getType());

		// MAS should produce 1 error
		assertEquals(1, errors().size());
		assertEquals(MASError.INIT_UNRECOGNIZED_PARAMETER, errors().get(0).getType());
		assertEquals(new HashMap<String, Object>(0), this.program.getInitParameters());

		// MAS should produce no warnings
		assertTrue(warnings().isEmpty());
	}

	@Test
	public void test_LAUNCH_INVALID_WILDCARD() {
		setup("src/test/resources/languageTools/analyzer/mas/test_LAUNCH_INVALID_WILDCARD.mas2g");

		// MAS should not produce any syntax errors
		assertTrue(syntaxerrors().isEmpty());

		// MAS should produce 1 error
		assertEquals(1, errors().size());
		assertEquals(MASError.LAUNCH_INVALID_WILDCARD, errors().get(0).getType());
		assertEquals(1, this.program.getLaunchRules().size());

		// MAS should produce not produce any warnings
		assertTrue(warnings().isEmpty());
	}

	@Test
	public void test_LAUNCH_MISSING_AGENTDF() {
		setup("src/test/resources/languageTools/analyzer/mas/test_LAUNCH_MISSING_AGENTDF.mas2g");

		// MAS should not produce any syntax errors
		assertTrue(syntaxerrors().isEmpty());

		// MAS should produce 1 error
		assertEquals(1, errors().size());
		assertEquals(MASError.LAUNCH_MISSING_AGENTDF, errors().get(0).getType());
		assertEquals(2, this.program.getLaunchRules().size());

		// MAS should produce not produce any warnings
		assertTrue(warnings().isEmpty());
	}

	@Test
	public void test_REFERENCE_COULDNOT_RESOLVE() {
		setup("src/test/resources/languageTools/analyzer/mas/test_REFERENCE_COULDNOT_RESOLVE.mas2g");

		// MAS should not produce any syntax errors
		assertTrue(syntaxerrors().isEmpty());

		// MAS should produce 1 error
		assertEquals(1, errors().size());
		assertEquals(MASError.REFERENCE_COULDNOT_FIND, errors().get(0).getType());
		assertEquals(1, this.program.getLaunchRules().size());

		// MAS should not produce any warnings
		assertTrue(warnings().isEmpty());
	}

	@Test
	public void test_USECASE_DUPLICATE() {
		setup("src/test/resources/languageTools/analyzer/mas/test_USECASE_DUPLICATE.mas2g");

		// MAS should not produce any syntax errors
		assertTrue(syntaxerrors().isEmpty());

		// MAS should produce 1 error
		assertEquals(1, errors().size());
		assertEquals(MASError.USECASE_DUPLICATE, errors().get(0).getType());
		assertEquals(1, this.program.getLaunchRules().size());

		// MAS should produce not produce any warnings
		assertTrue(warnings().isEmpty());
	}

	/**
	 * Check if predicate defined both as knowledge and belief is rejected. This
	 * test is different, it also tests the process() part of the validator.
	 */
	@Test
	public void test_p_both_knowledge_and_belief() {
		setupAndProcess("src/test/resources/languageTools/analyzer/mas/p_both_knowledge_and_belief/test1.mas2g");

		// MAS should not produce any syntax errors
		assertTrue(syntaxerrors().isEmpty());

		// MAS should produce 1 error
		assertEquals(1, errors().size());
		assertEquals(MASError.PREDICATE_ALREADY_KNOWLEDGE, errors().get(0).getType());

		// MAS should produce 1 warning
		assertEquals(1, warnings().size());
		assertEquals(MASWarning.PREDICATE_UNUSED, warnings().get(0).getType());
	}

	/**
	 * Similar as above, but now we insert the p dynamically.
	 */
	@Test
	public void test_p_both_knowledge_and_belief2() {
		setupAndProcess("src/test/resources/languageTools/analyzer/mas/p_both_knowledge_and_belief2/test1.mas2g");

		// MAS should not produce any syntax errors
		assertTrue(syntaxerrors().isEmpty());

		// MAS should produce 1 error
		assertEquals(1, errors().size());
		assertEquals(MASError.PREDICATE_ALREADY_KNOWLEDGE, errors().get(0).getType());

		// MAS should produce 2 warnings
		assertEquals(2, warnings().size());
		assertEquals(MASWarning.PREDICATE_UNUSED, warnings().get(0).getType());
		assertEquals(MASWarning.VARIABLE_UNUSED, warnings().get(1).getType());
	}

	/**
	 * Similar as above, but now we insert the p by executing an action.
	 */
	@Test
	public void test_p_both_knowledge_and_belief3() {
		setupAndProcess("src/test/resources/languageTools/analyzer/mas/p_both_knowledge_and_belief3/test1.mas2g");

		// MAS should not produce any syntax errors
		assertTrue(syntaxerrors().isEmpty());

		// MAS should produce 1 error
		assertEquals(1, errors().size());
		assertEquals(MASError.PREDICATE_ALREADY_KNOWLEDGE, errors().get(0).getType());

		// MAS should not produce any warnings
		assertTrue(warnings().isEmpty());
	}
}
