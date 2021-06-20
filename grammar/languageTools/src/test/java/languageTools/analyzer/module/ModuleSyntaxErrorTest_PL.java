package languageTools.analyzer.module;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import languageTools.errors.ParserError.SyntaxError;
import languageTools.errors.module.ModuleError;

public class ModuleSyntaxErrorTest_PL extends ModuleTestSetup {
	private final String path = "/SWI-Prolog/";

	@Test
	public void test_CORRECT() {
		setup(this.path + "testcorrect.mod2g");

		// Module file should not produce any errors or warnings.
		assertTrue(getSyntaxErrors().isEmpty());
		assertTrue(getErrors().isEmpty());
		assertTrue(getWarnings().isEmpty());
	}

	@Test
	public void test_KR_INVALID_PARAMETER() {
		setup(this.path + "test_KR_INVALID_PARAMETER.mod2g");

		// Action specification file should produce 4 syntax errors.
		assertEquals(4, getSyntaxErrors().size());
		assertEquals(SyntaxError.EMBEDDED_LANGUAGE_ERROR, getSyntaxErrors().get(0).getType());
		assertEquals(SyntaxError.EMBEDDED_LANGUAGE_ERROR, getSyntaxErrors().get(1).getType());
		assertEquals(SyntaxError.EMBEDDED_LANGUAGE_ERROR, getSyntaxErrors().get(2).getType());
		assertEquals(SyntaxError.EMBEDDED_LANGUAGE_ERROR, getSyntaxErrors().get(3).getType());

		// Action specification file should produce 1 validation error
		assertEquals(1, getErrors().size());
		assertEquals(ModuleError.ACTION_INVALID, getErrors().get(0).getType());

		// Action specification file should produce no warnings.
		assertTrue(getWarnings().isEmpty());
	}

	@Test
	public void test_MODULE_EMPTY_PROGRAMSECTION() {
		setup(this.path + "test_MODULE_EMPTY_PROGRAMSECTION.mod2g");

		// Module file should produce no syntax errors.
		assertTrue(getSyntaxErrors().isEmpty());

		// Module file should produce no errors.
		assertTrue(getErrors().isEmpty());

		// Module file should produce no warnings.
		assertTrue(getWarnings().isEmpty());
	}

	@Test
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

		// Module file should produce 1 warnings.
		assertEquals(1, getWarnings().size());
	}

	@Test
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

}
