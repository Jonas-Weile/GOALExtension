package tuPrologMentalState;

import mentalState.TranslatorTest;
import mentalState.translator.Translator;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import tuPrologMentalState.translator.TuPrologTranslator;
import eis.iilang.Percept;

@RunWith(Parameterized.class)
public class tuPrologTranslatorTest extends TranslatorTest {

	public tuPrologTranslatorTest(Percept percept, String updateStr) {
		super(percept, updateStr);
	}

	@Override
	protected Translator getTranslator() throws Exception {
		return new TuPrologTranslator();
	}

}
