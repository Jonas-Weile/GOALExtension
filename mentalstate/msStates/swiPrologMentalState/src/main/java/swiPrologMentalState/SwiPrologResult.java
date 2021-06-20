package swiPrologMentalState;

import mentalState.BASETYPE;
import mentalState.Result;
import mentalState.translator.Translator;
import swiPrologMentalState.translator.SwiPrologTranslator;

public class SwiPrologResult extends Result {
	protected SwiPrologResult(BASETYPE base, String focus) {
		super(base, focus);
	}

	@Override
	protected Translator getTranslator() {
		return new SwiPrologTranslator();
	}
}
