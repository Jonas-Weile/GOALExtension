package cognitiveSwiprolog;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.StringReader;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import krTools.exceptions.ParserException;
import krTools.language.DatabaseFormula;
import krTools.parser.Parser;
import swiprolog.SwiPrologInterface;
import swiprolog.language.PrologDBFormula;
import swiprolog.parser.KRInterfaceParser4;
import swiprolog.parser.SourceInfoObject;

@RunWith(Parameterized.class)
public class CognitiveSwiPrologTest {
	/**
	 * parameters for the tests: pairs of <formula, defined signatures, declared
	 * signatures>.
	 */
	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { "p", "p/0", "" }, { "p:-q", "p/0", "" }, { "p(X)", "p/1", "" },
				{ "p(X,Y):- not(q(X)), Z is X+Y, z(Z)", "p/2", "" }, { ":-dynamic p/1, q/3", "", "p/1, q/3" } });
	}

	private Set<String> expectedDefinedSignatures;
	private Set<String> expectedDeclaredSignatures;
	private PrologDBFormula formula;
	private final static CognitiveSwiProlog cognitive = new CognitiveSwiProlog(new SwiPrologInterface());

	/**
	 *
	 * @param formulaString
	 *            the formula to extract defined and declared signatures from
	 * @param definedSignaturesString
	 *            the expected "defined signatures".
	 * @param declaredSignaturesString
	 *            the expected "declared signatures" from the formula
	 */
	public CognitiveSwiPrologTest(String formulaString, String definedSignaturesString, String declaredSignaturesString)
			throws Exception {
		this.expectedDefinedSignatures = getTerms(definedSignaturesString);
		this.expectedDeclaredSignatures = getTerms(declaredSignaturesString);

		StringReader reader = new StringReader(formulaString + ".");
		Parser parser = new KRInterfaceParser4(reader, new SourceInfoObject(null, 0, 0, 0, 0));
		List<DatabaseFormula> formulas = parser.parseDBFs();
		if (!parser.getErrors().isEmpty()) {
			throw (ParserException) (parser.getErrors().get(0));
		}
		if (!parser.getWarnings().isEmpty()) {
			throw new ParserException(parser.getWarnings().get(0).toString(), new File(formulaString));
		}
		assertEquals(1, formulas.size());
		this.formula = (PrologDBFormula) formulas.get(0);
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

	@Test
	public void testDefinedSignatures() throws Exception {
		assertEquals(this.expectedDefinedSignatures, cognitive.getDefinedSignatures(this.formula));
	}

	@Test
	public void testDeclaredSignatures() throws Exception {
		assertEquals(this.expectedDeclaredSignatures, cognitive.getDeclaredSignatures(this.formula));
	}
}
