package languageTools.analyzer.mas;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.mockito.Matchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import cognitiveKr.CognitiveKR;
import krTools.exceptions.ParserException;
import krTools.language.DatabaseFormula;
import krTools.language.Expression;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Update;
import krTools.language.Var;
import krTools.parser.SourceInfo;
import languageTools.analyzer.FileRegistry;

@RunWith(Parameterized.class)
public class MasValidatorTest {
	private static Formula p = new Formula("p/0", "");
	private static Formula p1 = new Formula("p/1", "");
	private static Formula q = new Formula("q/0", "");
	private static Formula r = new Formula("r/1", "");
	private static Formula dynamicp = new Formula("", "p/0");
	private static Formula dynamicpqr = new Formula("", "p/0,q/0,r/0");

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] {
				// only knowledge
				{ list(p), list(), null },

				// only beliefs
				{ list(), list(p), null },

				// overlap knowledge and beliefs
				{ list(p), list(p), "'p/0' is treated as a belief" },

				// p/0 versus p/1 should be fine
				{ list(p), list(p1), null },

				// p/0 occurs in beliefs also
				{ list(p), list(p1, q, p, r), "'p/0' is treated as a belief" },

				// p/0 occurs in kr also
				{ list(p1, q, p, r), list(p), "'p/0' is treated as a belief" },

				// overlap knowledge and beliefs
				{ list(p), list(q, r), null },

				// p is just declared dynamic in knowledge
				{ list(dynamicp), list(p), null },

				// p is both declared dynamic and defined in knowledge
				{ list(p, dynamicp), list(), "'p/0' is treated as a belief" },

				// switched the order of p and dynamic decl
				{ list(dynamicp, p), list(), "'p/0' is treated as a belief" },

				// dynamic decl in beliefs is fine
				{ list(), list(dynamicp, p), null },

				// declaring multiple dynamics at once and also defining it in
				// knowledge.
				{ list(dynamicpqr, q), list(), "'q/0' is treated as a belief" }, });
	}

	private static List<Formula> list(Formula... a) {
		return Arrays.asList(a);
	}

	private final FileRegistry registry;
	private final MASValidator validator;
	private final Set<DatabaseFormula> knowledge = new LinkedHashSet<>();
	private final Set<DatabaseFormula> beliefs = new LinkedHashSet<>();
	private final String expectedError;

	public MasValidatorTest(List<Formula> kformulas, List<Formula> bformulas, String expectedErr)
			throws ParserException {
		this.registry = new FileRegistry();
		this.validator = new MasValidatorWithMockKR("test", this.registry);
		this.knowledge.addAll(kformulas);
		this.beliefs.addAll(bformulas);
		this.expectedError = expectedErr;
	}

	/**
	 * Runs the actual call to
	 * {@link MASValidator#checkNoPredicatesBeliefAndKnowledge}. If an error occurs,
	 * it checks that the message contains the given errormessage.
	 *
	 * @param knowledge
	 * @param beliefs
	 * @param errormessage
	 *            if this is not null, it is checked that there is an error and that
	 *            it contains this errormessage as substring.
	 * @throws ParserException
	 */
	@Test
	public void runCheck() throws ParserException {
		Map<String, SourceInfo> knowledgeDefined = this.validator.getDefinedSignatures(this.knowledge);
		Map<String, SourceInfo> knowledgeDeclared = this.validator.getDeclaredSignatures(this.knowledge);
		this.validator.checkNoPredicatesBeliefAndKnowledge(knowledgeDefined, knowledgeDeclared, this.beliefs);

		assertTrue(this.registry.getWarnings().isEmpty());
		if (this.expectedError == null) {
			assertTrue("Expected no error bot got " + this.registry.getErrors(), this.registry.getErrors().isEmpty());
		} else {
			assertFalse("Expected error like " + this.expectedError + " but the test ran fine",
					this.registry.getErrors().isEmpty());
			assertTrue(
					"Expected error message like " + this.expectedError + " but got "
							+ this.registry.getErrors().first().toString(),
					this.registry.getErrors().first().toString().contains(this.expectedError));
		}
	}

	/**************** support classes ******************/

	/**
	 * Mocks the cognitiveKR of the MASValidator but otherwise uses it as it is.
	 *
	 */
	private static class MasValidatorWithMockKR extends MASValidator {
		private CognitiveKR cognitiveKR;

		public MasValidatorWithMockKR(String filename, FileRegistry registry) throws ParserException {
			super(filename, registry);
			this.cognitiveKR = mock(CognitiveKR.class);

			when(this.cognitiveKR.getDefinedSignatures(Matchers.any(DatabaseFormula.class)))
					.thenAnswer(new Answer<Set<String>>() {
						@Override
						public Set<String> answer(InvocationOnMock invocation) {
							Formula formula = invocation.getArgumentAt(0, Formula.class);
							return formula.getDefinedSignatures();
						}
					});

			when(this.cognitiveKR.getDeclaredSignatures(Matchers.any(DatabaseFormula.class)))
					.thenAnswer(new Answer<Set<String>>() {
						@Override
						public Set<String> answer(InvocationOnMock invocation) {
							Formula formula = invocation.getArgumentAt(0, Formula.class);
							return formula.getDeclaredSignatures();
						}
					});
		}

		@Override
		public CognitiveKR getCognitiveKR() throws ParserException {
			return this.cognitiveKR;
		}
	}

	/**
	 * Mock DatabaseFormula so that we can test without using a real KR
	 * implementation
	 *
	 */
	private static class Formula implements DatabaseFormula {
		private Set<String> definedSignatures;
		private Set<String> declaredSignatures;

		public Formula(String definedSignatures, String declaredSignatures) {
			this.definedSignatures = getTerms(definedSignatures);
			this.declaredSignatures = getTerms(declaredSignatures);
		}

		public Set<String> getDeclaredSignatures() {
			return this.declaredSignatures;
		}

		public Set<String> getDefinedSignatures() {
			return this.definedSignatures;
		}

		/**
		 * @param termsString
		 *            comma-separated list of terms, eg "p/1, q/2".
		 * @return terms from termString
		 */
		private Set<String> getTerms(String termsString) {
			Set<String> terms = new LinkedHashSet<>();
			if (!termsString.isEmpty()) {
				for (String s : termsString.split(",")) {
					terms.add(s.replaceAll("\\s+", ""));
				}
			}
			return terms;
		}

		@Override
		public String getSignature() {
			return null;
		}

		@Override
		public boolean isVar() {
			return false;
		}

		@Override
		public boolean isClosed() {
			return true;
		}

		@Override
		public Set<Var> getFreeVar() {
			return null;
		}

		@Override
		public Substitution mgu(Expression expression) {
			return null;
		}

		@Override
		public SourceInfo getSourceInfo() {
			return mock(SourceInfo.class);
		}

		@Override
		public DatabaseFormula applySubst(Substitution substitution) {
			return null;
		}

		@Override
		public Query toQuery() {
			return null;
		}
		
		@Override
		public Update toUpdate() {
			return null;
		}
	}
}
