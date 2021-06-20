package languageTools.analyzer.module;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import krTools.exceptions.KRInitFailedException;
import languageTools.errors.ParserError.SyntaxError;
import languageTools.errors.module.ModuleError;
import languageTools.errors.module.ModuleWarning;

public class ModuleValidatorErrorTest_PL extends ModuleTestSetup {
	private String path = "/SWI-Prolog/";

	@Test
	public void test_ACTION_CALL_UNBOUND_VARIABLE() {
		setup(this.path + "test_ACTION_CALL_UNBOUND_VARIABLE.mod2g");

		// Module file should have no syntax errors.
		assertTrue(getSyntaxErrors().isEmpty());

		// Module file should produce 3 errors.
		assertEquals(3, getErrors().size());
		assertEquals(ModuleError.ACTION_CALL_UNBOUND_VARIABLE, getErrors().get(0).getType());
		assertEquals(ModuleError.ACTION_CALL_UNBOUND_VARIABLE, getErrors().get(1).getType());
		assertEquals(ModuleError.ACTION_CALL_UNBOUND_VARIABLE, getErrors().get(2).getType());

		// Module file should produce 2 warnings
		assertEquals(2, getWarnings().size());
		assertEquals(ModuleWarning.VARIABLE_UNUSED, getWarnings().get(0).getType());
		assertEquals(ModuleWarning.VARIABLE_UNUSED, getWarnings().get(1).getType());
	}

	@Test
	public void test_ACTION_LABEL_DEFINED_BY_MULTIPLE_REFERENCES() {
		setup(this.path + "test_ACTION_LABEL_DEFINED_BY_MULTIPLE_REFERENCES.mod2g");

		// Module file should not produce any syntax errors.
		assertTrue(getSyntaxErrors().isEmpty());

		// Module file should produce errors.
		assertEquals(1, getErrors().size());
		assertEquals(ModuleError.ACTION_LABEL_DEFINED_BY_MULTIPLE_REFERENCES, getErrors().get(0).getType());

		// Module file should produce no warnings.
		assertTrue(getWarnings().isEmpty());
	}

	@Test
	public void test_ACTION_USED_NEVER_DEFINED() {
		setup(this.path + "test_ACTION_USED_NEVER_DEFINED.mod2g");

		// Module file should have no syntax errors.
		assertTrue(getSyntaxErrors().isEmpty());

		// Module file should produce 2 errors.
		assertEquals(2, getErrors().size());
		assertEquals(ModuleError.ACTION_USED_NEVER_DEFINED, getErrors().get(0).getType());
		assertEquals(ModuleError.ACTION_USED_NEVER_DEFINED, getErrors().get(1).getType());

		// Module file should produce 1 warning
		assertEquals(3, getWarnings().size());
		assertEquals(ModuleWarning.MODULE_NAME_MISMATCH, getWarnings().get(0).getType());
		assertEquals(ModuleWarning.MODULE_NAME_MISMATCH, getWarnings().get(1).getType());
		assertEquals(ModuleWarning.VARIABLE_UNUSED, getWarnings().get(2).getType());
	}

	@Test
	public void test_DUPLICATE_PARAMETER() {
		setup(this.path + "test_DUPLICATE_PARAMETER.mod2g");

		// Module file should have no syntax errors.
		assertTrue(getSyntaxErrors().isEmpty());

		// Module file should produce 1 error.
		assertEquals(1, getErrors().size());
		assertEquals(ModuleError.DUPLICATE_PARAMETER, getErrors().get(0).getType());

		// Module file should produce no warnings.
		assertTrue(getWarnings().isEmpty());
	}

	@Test
	public void test_KR_BELIEF_QUERIED_NEVER_DEFINED() {
		setup(this.path + "test_KR_BELIEF_QUERIED_NEVER_DEFINED.mod2g");

		// Module file should not produce any syntax errors.
		assertTrue(getSyntaxErrors().isEmpty());

		// Module file should produce 2 errors.
		assertEquals(2, getErrors().size());
		assertEquals(ModuleError.KR_BELIEF_QUERIED_NEVER_DEFINED, getErrors().get(0).getType());
		assertEquals(ModuleError.KR_BELIEF_QUERIED_NEVER_DEFINED, getErrors().get(1).getType());

		// Module file should produce no warnings.
		assertTrue(getWarnings().isEmpty());
	}

	@Test
	public void test_KR_COULDNOT_RESOLVE() {
		setup(this.path + "test_KR_COULDNOT_RESOLVE.mod2g");

		// Module file should not produce any syntax errors.
		assertTrue(getSyntaxErrors().isEmpty());

		// Module file should produce 1 error.
		assertEquals(1, getErrors().size());
		assertEquals(ModuleError.KR_COULDNOT_RESOLVE_NO_KR_USECLAUSES, getErrors().get(0).getType());

		// Module file should produce no warnings.
		assertTrue(getWarnings().isEmpty());
	}

	// @Test DISABLED HERE BECAUSE OWL IS GONE FOR NOW
	public void test_KR_COULDNOT_RESOLVE2() {
		setup(this.path + "test_KR_COULDNOT_RESOLVE2.mod2g");

		// Module file should produce no syntax errors.
		assertTrue(getSyntaxErrors().isEmpty());

		// Module file should produce 1 error.
		assertEquals(1, getErrors().size());
		assertEquals(ModuleError.KR_COULDNOT_RESOLVE_DIFFERENT_KRS_USED, getErrors().get(0).getType());

		// Module file should produce no warnings.
		assertTrue(getWarnings().isEmpty());
	}

	@Test
	public void test_KR_GOAL_QUERIED_NEVER_DEFINED() {
		setup(this.path + "test_KR_GOAL_QUERIED_NEVER_DEFINED.mod2g");

		// Module file should not produce any syntax errors.
		assertTrue(getSyntaxErrors().isEmpty());

		// Module file should produce 2 errors.
		assertEquals(2, getErrors().size());
		assertEquals(ModuleError.KR_BELIEF_QUERIED_NEVER_DEFINED, getErrors().get(0).getType());
		assertEquals(ModuleError.KR_GOAL_QUERIED_NEVER_DEFINED, getErrors().get(1).getType());

		// Module file should produce no warnings.
		assertTrue(getWarnings().isEmpty());
	}

	// @Test FIXME this check has been disabled...
	public void test_KR_PROLOG_ADOPT_UPDATE_NEGATIVE() {
		setup(this.path + "test_KR_PROLOG_ADOPT_UPDATE_NEGATIVE.mod2g");

		// Module file should not produce any syntax errors.
		assertTrue(getSyntaxErrors().isEmpty());

		// Module file should produce 1 error.
		assertEquals(1, getErrors().size());
		assertEquals(ModuleError.KR_PROLOG_ADOPT_UPDATE_NEGATIVE, getErrors().get(0).getType());

		// Module file should produce no warnings.
		assertTrue(getWarnings().isEmpty());
	}

	// FIXME: we cannot check for anonymous variable using KR interface.
	// @Test
	public void test_KR_PROLOG_ANONYMOUS_VARIABLE() {
		setup(this.path + "test_KR_PROLOG_ANONYMOUS_VARIABLE.mod2g");

		// Module file should not produce any syntax errors.
		assertTrue(getSyntaxErrors().isEmpty());

		// Module file should produce 2 errors.
		assertEquals(2, getErrors().size());
		assertEquals(ModuleError.KR_PROLOG_ANONYMOUS_VARIABLE, getErrors().get(0).getType());
		assertEquals(ModuleError.KR_PROLOG_ANONYMOUS_VARIABLE, getErrors().get(1).getType());

		// Module file should produce no warnings.
		assertTrue(getWarnings().isEmpty());
	}

	// FIXME: we cannot check for anonymous variable using KR interface.
	// @Test
	public void test_KR_PROLOG_LISTALL_ANONYMOUS_VARIABLE() {
		setup(this.path + "test_KR_PROLOG_LISTALL_ANONYMOUS_VARIABLE.mod2g");

		// Module file should not produce any syntax errors.
		assertTrue(getSyntaxErrors().isEmpty());

		// Module file should produce 1 error.
		assertEquals(1, getErrors().size());
		assertEquals(ModuleError.KR_PROLOG_LISTALL_ANONYMOUS_VARIABLE, getErrors().get(1).getType());

		// Module file should produce no warnings.
		assertTrue(getWarnings().isEmpty());
	}

	// FIXME: we cannot check for anonymous variable using KR interface.
	// @Test
	public void test_KR_PROLOG_MENTAL_LITERAL_ANONYMOUS_VARIABLE() {
		setup(this.path + "test_KR_PROLOG_MENTAL_LITERAL_ANONYMOUS_VARIABLE.mod2g");

		// Module file should not produce any syntax errors.
		assertTrue(getSyntaxErrors().isEmpty());

		// Module file should produce 2 errors.
		assertEquals(2, getErrors().size());
		assertEquals(ModuleError.KR_PROLOG_MENTAL_LITERAL_ANONYMOUS_VARIABLE, getErrors().get(0).getType());
		assertEquals(ModuleError.KR_PROLOG_MENTAL_LITERAL_ANONYMOUS_VARIABLE, getErrors().get(1).getType());

		// Module file should produce no warnings.
		assertTrue(getWarnings().isEmpty());
	}

	@Test
	public void test_KR_UPDATE_EMPTY() {
		setup(this.path + "test_KR_UPDATE_EMPTY.mod2g");

		// Module file should not produce any syntax errors.
		assertTrue(getSyntaxErrors().isEmpty());

		// Module file should produce 1 error.
		assertEquals(1, getErrors().size());
		assertEquals(ModuleError.ACTION_INVALID, getErrors().get(0).getType());

		// Module file should produce no warnings.
		assertTrue(getWarnings().isEmpty());
	}

	// @Test
	public void test_KR_USE_OF_DIFFERENT_KRIS() {
		setup(this.path + "test_KR_USE_OF_DIFFERENT_KRIS.mod2g");

		// Module file should not produce any syntax errors.
		assertTrue(getSyntaxErrors().isEmpty());

		// Module file should produce 1 error.
		assertEquals(1, getErrors().size());
		// assertEquals(ModuleError.KR_USE_OF_DIFFERENT_KRIS, getErrors().get(0)
		// .getType());
		assertEquals(ModuleError.KR_COULDNOT_RESOLVE_DIFFERENT_KRS_USED, getErrors().get(0).getType());

		// Module file should produce no warnings.
		assertTrue(getWarnings().isEmpty());
	}

	@Test
	public void test_MACRO_DUPLICATE_NAME() {
		setup(this.path + "test_MACRO_DUPLICATE_NAME.mod2g");

		// Module file should not produce any syntax errors.
		assertTrue(getSyntaxErrors().isEmpty());

		// Module file should produce 1 error.
		assertEquals(1, getErrors().size());
		assertEquals(ModuleError.MACRO_DUPLICATE_NAME, getErrors().get(0).getType());

		// Module file should produce no warnings.
		assertTrue(getWarnings().isEmpty());
	}

	@Test
	public void test_MACRO_NOT_DEFINED() {
		setup(this.path + "test_MACRO_NOT_DEFINED.mod2g");

		// Module file should not produce any syntax errors.
		assertTrue(getSyntaxErrors().isEmpty());

		// Module file should produce 1 error.
		assertEquals(1, getErrors().size());
		assertEquals(ModuleError.MACRO_NOT_DEFINED, getErrors().get(0).getType());

		// Module file should produce 2 warnings
		assertEquals(2, getWarnings().size());
		assertEquals(ModuleWarning.VARIABLE_UNUSED, getWarnings().get(0).getType());
		assertEquals(ModuleWarning.VARIABLE_UNUSED, getWarnings().get(1).getType());
	}

	@Test
	public void test_PARAMETER_NOT_A_VARIABLE() {
		setup(this.path + "test_PARAMETER_NOT_A_VARIABLE.mod2g");

		// Module file should produce 2 syntax errors.
		assertEquals(2, getSyntaxErrors().size());
		assertEquals(SyntaxError.PARAMETER_NOT_A_VARIABLE, getSyntaxErrors().get(0).getType());
		assertEquals(SyntaxError.PARAMETER_NOT_A_VARIABLE, getSyntaxErrors().get(1).getType());

		// Module file should produce no validation errors.
		assertTrue(getErrors().isEmpty());

		// Module file should produce no warnings.
		assertTrue(getWarnings().isEmpty());
	}

	@Test
	public void test_MACRO_PARAMETERS_NOT_IN_DEFINITION() throws KRInitFailedException {
		setup(this.path + "test_MACRO_PARAMETERS_NOT_IN_DEFINITION.mod2g");

		// Module file should not produce any syntax errors.
		assertTrue(getSyntaxErrors().isEmpty());

		// Module file should produce 1 error.
		assertEquals(1, getErrors().size());
		assertEquals(ModuleError.MACRO_PARAMETERS_NOT_IN_DEFINITION, getErrors().get(0).getType());

		// Module file should produce 1 warning.
		assertEquals(1, getWarnings().size());
		assertEquals(ModuleWarning.VARIABLE_UNUSED, getWarnings().get(0).getType());
	}

	@Test
	public void test_REFERENCE_COULDNOT_FIND() {
		setup(this.path + "test_REFERENCE_COULDNOT_FIND.mod2g");

		// Module file should not produce a syntax error.
		assertTrue(getSyntaxErrors().isEmpty());

		// Module file should produce 2 validation errors.
		assertEquals(2, getErrors().size());
		assertEquals(ModuleError.KR_COULDNOT_RESOLVE_NO_KR_USECLAUSES, getErrors().get(0).getType());
		assertEquals(ModuleError.REFERENCE_COULDNOT_FIND, getErrors().get(1).getType());

		// Module file should produce no warnings.
		assertTrue(getWarnings().isEmpty());

		// Module has 1 use clause but no referenced files.
		assertEquals(1, getProgram().getUseClauses().size());
		assertEquals(0, getProgram().getUseClauses().iterator().next().getResolvedReference().size());
	}

	@Test
	public void test_RULE_MISSING_BODY() {
		setup(this.path + "test_RULE_MISSING_BODY.mod2g");

		// Module file should have 1 syntax error.
		assertEquals(1, getSyntaxErrors().size());
		assertEquals(SyntaxError.INPUTMISMATCH, getSyntaxErrors().get(0).getType());

		// Module file should produce no errors.
		assertTrue(getErrors().isEmpty());

		// Module file should produce no warnings.
		assertTrue(getWarnings().isEmpty());
	}

	@Test
	public void test_RULE_MISSING_CONDITION() {
		setup(this.path + "test_RULE_MISSING_CONDITION.mod2g");

		// Module file should have 1 syntax error.
		assertEquals(1, getSyntaxErrors().size());
		assertEquals(SyntaxError.INPUTMISMATCH, getSyntaxErrors().get(0).getType());

		// Module file should produce 1 error.
		assertEquals(1, getErrors().size());
		assertEquals(ModuleError.CONDITION_INVALID, getErrors().get(0).getType());

		// Module file should produce no warnings.
		assertTrue(getWarnings().isEmpty());
	}

	@Test
	public void test_SEND_INVALID_SELECTOR() {
		setup(this.path + "test_SEND_INVALID_SELECTOR.mod2g");

		// Agent file should have no syntax errors
		assertTrue(getSyntaxErrors().isEmpty());

		// Agent file should produce 2 errors
		assertEquals(2, getErrors().size());
		assertEquals(ModuleError.SEND_INVALID_SELECTOR, getErrors().get(0).getType());
		assertEquals(ModuleError.SEND_INVALID_SELECTOR, getErrors().get(1).getType());

		// Agent file should produce no warnings
		assertTrue(getWarnings().isEmpty());
	}
}
