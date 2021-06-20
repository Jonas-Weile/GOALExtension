package languageTools.analyzer.module;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import languageTools.errors.ParserError.SyntaxError;
import languageTools.errors.module.ModuleError;

public class ModuleSyntaxErrorTest_OWL extends ModuleTestSetup {
	private String path = "/OWL/";
	private String krInterface = "owlrepo.OWLRepoKRInterface";

	// @Test
	public void test_OWLFILE_AS_KNOWLEDGE() {
		setup(this.path + "test_OWLFILE_AS_KNOWLEDGE.mod2g");

		// Module file should not produce any errors or warnings.
		assertTrue(getSyntaxErrors().isEmpty());
		assertTrue(getErrors().isEmpty());
		assertTrue(getWarnings().isEmpty());
		assertTrue(getKRInterface().equals(this.krInterface));
	}

	// @Test
	public void test_NEW_FACTS() {
		setup(this.path + "test_NEW_FACTS.mod2g");

		// Module file should not produce any errors or warnings.
		assertTrue(getSyntaxErrors().isEmpty());
		assertTrue(getErrors().isEmpty());
		assertTrue(getWarnings().isEmpty());
		assertTrue(getKRInterface().equals(this.krInterface));

	}

	// @Test
	public void test_NEW_RULES() {
		setup(this.path + "test_NEW_RULES.mod2g");

		// Module file should not produce any errors or warnings.
		assertTrue(getSyntaxErrors().isEmpty());
		assertTrue(getErrors().isEmpty());
		assertTrue(getWarnings().isEmpty());
		assertTrue(getKRInterface().equals(this.krInterface));

	}

	// @Test
	public void test_KR_INVALID_PARAMETER() {
		setup(this.path + "test_KR_INVALID_PARAMETER.mod2g");

		// Action specification file should produce 4 syntax errors.
		assertEquals(1, getSyntaxErrors().size());
		assertEquals(SyntaxError.PARAMETER_NOT_A_VARIABLE, getSyntaxErrors().get(0).getType());

		// Action specification file should produce no validation errors.
		assertTrue(getErrors().isEmpty());

		// Action specification file should produce no warnings.
		assertTrue(getWarnings().isEmpty());

		// Check the correct kr interface
		assertTrue(getKRInterface().equals(this.krInterface));

	}

	// @Test
	public void test_MODULE_EMPTY_PROGRAMSECTION() {
		setup(this.path + "test_MODULE_EMPTY_PROGRAMSECTION.mod2g");

		// Module file should produce 1 syntax error.
		assertEquals(1, getSyntaxErrors().size());
		assertEquals(SyntaxError.INPUTMISMATCH, getSyntaxErrors().get(0).getType());

		// Module file should produce no errors.
		assertTrue(getErrors().isEmpty());

		// Module file should produce no warnings.
		assertTrue(getWarnings().isEmpty());

		assertTrue(getKRInterface().equals(this.krInterface));

	}

	// @Test
	public void test_MODULE_MISSING_NAME() {
		setup(this.path + "test_MODULE_MISSING_NAME.mod2g");

		// Module file should produce 1 syntax error.
		assertEquals(1, getSyntaxErrors().size());
		assertEquals(SyntaxError.MISSINGTOKEN, getSyntaxErrors().get(0).getType());

		// Module file should produce no validation errors.
		// TODO: this module does not have any dependencies on a KRT(!) Even
		// though
		// a bit of an extreme case, we may want to delay KR resolution until it
		// is
		// really required during validation to avoid the error below.
		// assertEquals(1, getErrors().size());
		// assertEquals(ModuleError.KR_COULDNOT_RESOLVE_NO_KR_USECLAUSES,
		// getErrors().get(0).getType());

		// Module file should produce no warnings.
		assertTrue(getWarnings().isEmpty());
		assertTrue(getKRInterface().equals(this.krInterface));

	}

	// @Test
	public void test_USE_INSTEAD_OF_USES() {
		setup(this.path + "test_USES_INSTEAD_OF_USE.mod2g");

		// Module file should produce 1 syntax error.
		assertEquals(1, getSyntaxErrors().size());
		assertEquals(SyntaxError.INPUTMISMATCH, getSyntaxErrors().get(0).getType());

		// Module file should produce 1 validation error.
		assertEquals(1, getErrors().size());
		assertEquals(ModuleError.KR_COULDNOT_RESOLVE_NO_KR_USECLAUSES, getErrors().get(0).getType());

		// Module file should produce no warnings.
		assertTrue(getWarnings().isEmpty());

	}

	// @Test
	public void test_KR_BELIEF_QUERIED_NEVER_DEFINED() {
		setup(this.path + "test_KR_BELIEF_QUERIED_NEVER_DEFINED.mod2g");

		// Module file should produce 1 syntax error.
		assertEquals(1, getSyntaxErrors().size());
		assertEquals(SyntaxError.EMBEDDED_LANGUAGE_ERROR, getSyntaxErrors().get(0).getType());

		// Module file should produce no errors.
		assertEquals(1, getErrors().size());

		// Module file should produce no warnings.
		assertTrue(getWarnings().isEmpty());
	}

	// @Test
	public void test_KR_GOAL_QUERIED_NEVER_DEFINED() {
		setup(this.path + "test_KR_GOAL_QUERIED_NEVER_DEFINED.mod2g");

		// Module file should produce 1 syntax error.
		assertEquals(1, getSyntaxErrors().size());
		assertEquals(SyntaxError.EMBEDDED_LANGUAGE_ERROR, getSyntaxErrors().get(0).getType());

		// Module file should produce no errors.
		assertEquals(1, getErrors().size());

		// Module file should produce no warnings.
		assertTrue(getWarnings().isEmpty());
	}
}
