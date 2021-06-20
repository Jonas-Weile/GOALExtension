package goal.core.executors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import goal.core.agent.AgentRegistry;
import goal.core.runtime.service.agent.RunState;
import krTools.language.Substitution;
import krTools.language.Term;
import languageTools.program.agent.AgentId;
import languageTools.program.agent.selector.Selector;
import languageTools.program.agent.selector.Selector.SelectorType;
import mentalState.error.MSTTranslationException;
import mentalState.translator.Translator;

public class SelectExecutorTest {
	private SelectorExecutor executor;
	private final Selector selector = mock(Selector.class);
	private final RunState state = mock(RunState.class);
	private final Term agent1 = mock(Term.class);
	private final Term agent2 = mock(Term.class);
	private final Term agent12 = mock(Term.class);
	private final Translator translator = mock(Translator.class);
	private final List<Term> terms = new LinkedList<>();

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Before
	public void before() throws MSTTranslationException {
		when(this.selector.applySubst(any(Substitution.class))).thenReturn(this.selector);
		when(this.selector.getType()).thenReturn(SelectorType.PARAMETERLIST);

		when(this.agent1.toString()).thenReturn("agent1");
		when(this.agent2.toString()).thenReturn("agent2");
		this.executor = new SelectorExecutor(this.selector) {
			@Override
			public Translator getTranslator(krTools.KRInterface kri) throws msFactory.InstantiationFailedException {
				return SelectExecutorTest.this.translator;
			}
		};

		when(this.translator.unpackTerm(this.agent1)).thenReturn(list(this.agent1));
		when(this.translator.unpackTerm(this.agent2)).thenReturn(list(this.agent2));
		when(this.translator.unpackTerm(this.agent12)).thenReturn(list(this.agent1, this.agent2));

		AgentRegistry registry = mock(AgentRegistry.class);
		when(this.state.getRegistry()).thenReturn(registry);
	}

	@Test
	public void testEmptyTerms() throws MSTTranslationException {
		when(this.selector.getParameters()).thenReturn(this.terms);
		List<AgentId> agents = this.executor.evaluate(this.state);
		assertTrue(agents.isEmpty());
	}

	@Test
	public void testOneTerm() throws MSTTranslationException {
		this.terms.add(this.agent1);
		when(this.selector.getParameters()).thenReturn(this.terms);

		List<AgentId> agents = this.executor.evaluate(this.state);
		assertEquals("[agent1]", agents.toString());
	}

	@Test
	public void testTwoTerms() throws MSTTranslationException {
		this.terms.add(this.agent1);
		this.terms.add(this.agent2);
		when(this.selector.getParameters()).thenReturn(this.terms);

		List<AgentId> agents = this.executor.evaluate(this.state);
		assertEquals("[agent1, agent2]", agents.toString());
	}

	@Test
	public void testListTerm2Elements() throws MSTTranslationException {
		this.terms.add(this.agent12);
		when(this.selector.getParameters()).thenReturn(this.terms);

		List<AgentId> agents = this.executor.evaluate(this.state);
		assertEquals("[agent1, agent2]", agents.toString());
	}

	@Test
	public void testList2Terms2Elements() throws MSTTranslationException {
		this.terms.add(this.agent12);
		this.terms.add(this.agent12);
		when(this.selector.getParameters()).thenReturn(this.terms);

		List<AgentId> agents = this.executor.evaluate(this.state);
		assertEquals("[agent1, agent2, agent1, agent2]", agents.toString());
	}

	private static List<Term> list(Term... terms) {
		List<Term> list = new ArrayList<>(terms.length);
		for (Term t : terms) {
			list.add(t);
		}
		return list;
	}
}
