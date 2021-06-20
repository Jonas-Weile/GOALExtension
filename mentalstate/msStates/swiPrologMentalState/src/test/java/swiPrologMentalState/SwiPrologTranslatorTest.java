package swiPrologMentalState;

import static org.junit.Assert.assertArrayEquals;

import org.jpl7.JPL;
import org.junit.Test;

import krTools.language.Term;
import mentalState.error.MSTTranslationException;
import swiPrologMentalState.translator.SwiPrologTranslator;
import swiprolog.language.PrologCompound;
import swiprolog.language.PrologTerm;
import swiprolog.language.impl.PrologImplFactory;

public class SwiPrologTranslatorTest {
	private final SwiPrologTranslator translator = new SwiPrologTranslator();
	private final PrologCompound a = PrologImplFactory.getAtom("a", null);
	private final PrologCompound b = PrologImplFactory.getAtom("b", null);
	private final PrologCompound c = PrologImplFactory.getAtom("c", null);
	private final PrologTerm one = PrologImplFactory.getNumber(1.0, null);
	private final PrologCompound nil = PrologImplFactory.getAtom(JPL.LIST_NIL.name(), null);

	@Test
	public void testUnpackWeirdSimpleList() throws MSTTranslationException {
		PrologCompound term = PrologImplFactory.getCompound(JPL.LIST_PAIR, new Term[] { a, b }, null);
		assertArrayEquals(new Term[] { a, b }, translator.unpackTerm(term).toArray(new Term[0]));
	}

	@Test
	public void testUnpackSimpleList() throws MSTTranslationException {
		PrologCompound term = PrologImplFactory.getCompound(JPL.LIST_PAIR, new Term[] { a, nil }, null);
		assertArrayEquals(new Term[] { a }, translator.unpackTerm(term).toArray(new Term[0]));
	}

	@Test
	public void testUnpackbadList() throws MSTTranslationException {
		PrologCompound term = PrologImplFactory.getCompound(JPL.LIST_PAIR, new Term[] { a, b, c }, null);
		assertArrayEquals(new Term[] { term }, translator.unpackTerm(term).toArray(new Term[0]));
	}

	@Test
	public void testUnpackNonList() throws MSTTranslationException {
		PrologCompound term = PrologImplFactory.getCompound("ds", new Term[] { a, b }, null);
		assertArrayEquals(new Term[] { term }, translator.unpackTerm(term).toArray(new Term[0]));
	}

	@Test
	public void testUnpackNumber() throws MSTTranslationException {
		assertArrayEquals(new Term[] { one }, translator.unpackTerm(one).toArray(new Term[0]));
	}

	@Test
	public void testUnpackNumbers() throws MSTTranslationException {
		PrologCompound list1 = PrologImplFactory.getCompound(JPL.LIST_PAIR, new Term[] { one, nil }, null);
		PrologCompound list2 = PrologImplFactory.getCompound(JPL.LIST_PAIR, new Term[] { one, list1 }, null);
		assertArrayEquals(new Term[] { one, one }, translator.unpackTerm(list2).toArray(new Term[0]));
	}
}
