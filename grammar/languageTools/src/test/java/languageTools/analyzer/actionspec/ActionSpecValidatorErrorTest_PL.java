package languageTools.analyzer.actionspec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import languageTools.errors.ParserError.SyntaxError;
import languageTools.errors.actionspec.ActionSpecError;
import languageTools.errors.module.ModuleError;

public class ActionSpecValidatorErrorTest_PL extends ActionSpecTestSetup {

	private String path = "/SWI-Prolog/";

	@Test
	public void test_ACTION_LABEL_ALREADY_DEFINED() {
		setup(this.path + "test_ACTION_LABEL_ALREADY_DEFINED.act2g");

		// Action specification file should produce no syntax errors.
		assertTrue(getSyntaxerrors().isEmpty());

		// Action specification file should produce 1 validation error.
		assertEquals(1, getErrors().size());
		assertEquals(ActionSpecError.ACTION_LABEL_ALREADY_DEFINED, getErrors().get(0).getType());

		// Action specification file should produce no warnings.
		assertTrue(getWarnings().isEmpty());
	}

	@Test
	public void test_DUPLICATE_PARAMETER() {
		setup(this.path + "test_DUPLICATE_PARAMETER.act2g");

		// Action specification file should produce no syntax errors.
		assertTrue(getSyntaxerrors().isEmpty());

		// Action specification file should produce 1 validation error.
		assertEquals(1, getErrors().size());
		assertEquals(ActionSpecError.DUPLICATE_PARAMETER, getErrors().get(0).getType());

		// Action specification file should produce no warnings.
		assertTrue(getWarnings().isEmpty());
	}

	// @Test
	public void test_KR_COULDNOT_RESOLVE_DIFFERENT_KRS_USED() {
		setup(this.path + "test_KR_COULDNOT_RESOLVE_DIFFERENT_KRS_USED.act2g");

		// Action specification file should produce no syntax errors.
		assertTrue(getSyntaxerrors().isEmpty());

		// Action specification file should produce 1 validation error.
		assertEquals(1, getErrors().size());
		assertEquals(ActionSpecError.KR_COULDNOT_RESOLVE_DIFFERENT_KRS_USED, getErrors().get(0).getType());

		// Action specification file should produce no warnings.
		assertTrue(getWarnings().isEmpty());
	}

	@Test
	public void test_KR_COULDNOT_RESOLVE_NO_KR_USECLAUSES() {
		setup(this.path + "test_KR_COULDNOT_RESOLVE_NO_KR_USECLAUSES.act2g");

		// Action specification file should produce no syntax errors.
		assertTrue(getSyntaxerrors().isEmpty());

		// Action specification file should produce 1 validation error.
		assertEquals(1, getErrors().size());
		assertEquals(ActionSpecError.KR_COULDNOT_RESOLVE_NO_KR_USECLAUSES, getErrors().get(0).getType());

		// Action specification file should produce no warnings.
		assertTrue(getWarnings().isEmpty());
	}

	@Test
	public void test_KR_PRECONDITION_NEVER_DEFINED() {
		setup(this.path + "test_KR_PRECONDITION_NEVER_DEFINED.act2g");

		// Action specification file should produce no syntax errors.
		assertTrue(getSyntaxerrors().isEmpty());

		// Action specification file should produce 2 validation errors.
		assertEquals(2, getErrors().size());
		assertEquals(ModuleError.KR_BELIEF_QUERIED_NEVER_DEFINED, getErrors().get(0).getType());
		assertEquals(ModuleError.KR_BELIEF_QUERIED_NEVER_DEFINED, getErrors().get(1).getType());

		// Action specification file should produce no warnings.
		assertTrue(getWarnings().isEmpty());
	}

	// @Test
	public void test_KR_PROLOG_ANONYMOUS_VARIABLE() {
		setup(this.path + "test_KR_PROLOG_ANONYMOUS_VARIABLE.act2g");

		// Action specification file should produce no syntax errors.
		assertTrue(getSyntaxerrors().isEmpty());

		// Action specification file should produce 1 validation error.
		assertEquals(1, getErrors().size());
		assertEquals(ActionSpecError.KR_PROLOG_ANONYMOUS_VARIABLE, getErrors().get(0).getType());

		// Action specification file should produce no warnings.
		assertTrue(getWarnings().isEmpty());
	}

	@Test
	public void test_PARAMETER_NOT_A_VARIABLE() {
		setup(this.path + "test_PARAMETER_NOT_A_VARIABLE.act2g");

		// Action specification file should produce 1 syntax error.
		assertEquals(1, getSyntaxerrors().size());
		assertEquals(SyntaxError.PARAMETER_NOT_A_VARIABLE, getSyntaxerrors().get(0).getType());

		// Action specification file should produce no validation errors.
		assertTrue(getErrors().isEmpty());

		// Action specification file should produce no warnings.
		assertTrue(getWarnings().isEmpty());
	}

	@Test
	public void test_POSTCONDITION_UNBOUND_VARIABLE() {
		setup(this.path + "test_POSTCONDITION_UNBOUND_VARIABLE.act2g");

		// Action specification file should produce no syntax errors.
		assertTrue(getSyntaxerrors().isEmpty());

		// Action specification file should produce 1 error.
		assertEquals(1, getErrors().size());
		assertEquals(ActionSpecError.POSTCONDITION_UNBOUND_VARIABLE, getErrors().get(0).getType());

		// Action specification file should produce no warnings.
		assertTrue(getWarnings().isEmpty());
	}

	@Test
	public void test_REFERENCE_COULDNOT_FIND() {
		setup(this.path + "test_REFERENCE_COULDNOT_FIND.act2g");

		// Action specification file should produce no syntax errors.
		assertTrue(getSyntaxerrors().isEmpty());

		// Action specification file should produce 2 validation errors.
		assertEquals(2, getErrors().size());
		assertEquals(ActionSpecError.KR_COULDNOT_RESOLVE_NO_KR_USECLAUSES, getErrors().get(0).getType());
		assertEquals(ActionSpecError.REFERENCE_COULDNOT_FIND, getErrors().get(1).getType());

		// Action specification file should produce no warnings.
		assertTrue(getWarnings().isEmpty());
	}

	// @Test
	public void test_REFERENCE_COULDNOT_RESOLVE() {
		setup(this.path + "test_REFERENCE_COULDNOT_RESOLVE.act2g");

		// Action specification file should produce no syntax errors.
		assertTrue(getSyntaxerrors().isEmpty());

		// Action specification file should produce 2 validation errors.
		assertEquals(1, getErrors().size());
		// assertEquals(ActionSpecError.REFERENCE_COULDNOT_RESOLVE,
		// getErrors().get(0).getType());
		assertEquals(ActionSpecError.KR_COULDNOT_RESOLVE_NO_KR_USECLAUSES, getErrors().get(1).getType());

		// Action specification file should produce no warnings.
		assertTrue(getWarnings().isEmpty());
	}

}
