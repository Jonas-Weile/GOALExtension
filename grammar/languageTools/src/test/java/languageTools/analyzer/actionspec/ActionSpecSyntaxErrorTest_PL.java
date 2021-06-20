package languageTools.analyzer.actionspec;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import krTools.exceptions.KRInitFailedException;
import languageTools.errors.ParserError.SyntaxError;

public class ActionSpecSyntaxErrorTest_PL extends ActionSpecTestSetup {

	private String path = "/SWI-Prolog/";

	@Test
	public void test_KR_INVALID_PARAMETER() throws KRInitFailedException {
		setup(this.path + "test_KR_INVALID_PARAMETER.act2g");

		// Action specification file should produce 2 syntax errors.
		assertEquals(3, getSyntaxerrors().size());
		assertEquals(SyntaxError.EMBEDDED_LANGUAGE_ERROR, getSyntaxerrors().get(0).getType());
		assertEquals(SyntaxError.EMBEDDED_LANGUAGE_ERROR, getSyntaxerrors().get(1).getType());
		assertEquals(SyntaxError.INPUTMISMATCH, getSyntaxerrors().get(2).getType());

		// Action specification file should produce no validation errors.
		assertTrue(getErrors().isEmpty());

		// Action specification file should produce no warnings.
		assertTrue(getWarnings().isEmpty());
	}

}
