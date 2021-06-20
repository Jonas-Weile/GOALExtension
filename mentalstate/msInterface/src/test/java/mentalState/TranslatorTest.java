package mentalState;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import eis.iilang.Function;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Percept;
import krTools.language.DatabaseFormula;
import mentalState.error.MSTTranslationException;
import mentalState.translator.Translator;

@RunWith(Parameterized.class)
public abstract class TranslatorTest {
	private Translator translator;
	private Percept percept;
	private String updateStr;

	abstract protected Translator getTranslator() throws Exception;

	@Before
	public void init() throws Exception {
		this.translator = getTranslator();
	}

	@Parameters
	public static Collection<Object[]> data() {
		return Arrays
				.asList(new Object[][] { { new Percept("p"), "p" }, { new Percept("q", new Identifier("c1")), "q(c1)" },
						{ new Percept("q", new Numeral(1), new Numeral(2)), "q(1,2)" },
						{ new Percept("q", new Function("r", new Numeral(2))), "q(r(2))" } });
	}

	public TranslatorTest(Percept percept, String updateStr) {
		this.percept = percept;
		this.updateStr = updateStr;
	}

	@Test
	public void testEisTranslator() throws MSTTranslationException {
		DatabaseFormula krFormula = this.translator.convertPercept(this.percept);
		assertEquals(this.updateStr, krFormula.toString());
	}
}
