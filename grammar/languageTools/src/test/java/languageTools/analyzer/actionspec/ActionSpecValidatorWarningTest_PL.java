package languageTools.analyzer.actionspec;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ActionSpecValidatorWarningTest_PL extends ActionSpecTestSetup {

	private String path = "/SWI-Prolog/";

	@Test
	public void test_ACTIONSPEC_MISSING_POST() {
		setup(this.path + "test_ACTIONSPEC_MISSING_POST.act2g");

		// Action specification file should produce no syntax errors.
		assertTrue(getSyntaxerrors().isEmpty());

		// Action specification file should produce no validation errors.
		assertTrue(getErrors().isEmpty());

		// Action specification file should produce no warnings..
		assertTrue(getWarnings().isEmpty());
	}

	@Test
	public void test_ACTIONSPEC_MISSING_PRE() {
		setup(this.path + "test_ACTIONSPEC_MISSING_PRE.act2g");

		// Action specification file should produce no syntax errors.
		assertTrue(getSyntaxerrors().isEmpty());

		// Action specification file should produce no validation errors.
		assertTrue(getErrors().isEmpty());

		// Action specification file should produce no warnings.
		assertTrue(getWarnings().isEmpty());
	}

	@Test
	public void test_ACTIONSPEC_PARAMETER_NOT_USED() {
		setup(this.path + "test_ACTIONSPEC_PARAMETER_NOT_USED.act2g");

		// Action specification file should produce no syntax errors.
		assertTrue(getSyntaxerrors().isEmpty());

		// Action specification file should produce no validation errors.
		assertTrue(getErrors().isEmpty());

		// Action specification file should produce no warnings.
		assertTrue(getWarnings().isEmpty());
	}

}
