package goal.core.executors.actions;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Deque;

import events.ExecutionEventGeneratorInterface;
import goal.core.runtime.service.agent.Result;
import goal.core.runtime.service.agent.Result.RunStatus;
import goal.core.runtime.service.agent.RunState;
import goal.tools.errorhandling.exceptions.GOALActionFailedException;
import krTools.language.Substitution;
import krTools.language.Update;
import languageTools.program.agent.actions.AdoptAction;
import languageTools.program.agent.selector.Selector;
import languageTools.program.agent.selector.Selector.SelectorType;
import mentalState.GoalBase;
import mentalState.MentalStateWithEvents;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;

public class AdoptActionExecutorTest {
	// @Test
	public void testSelf() throws GOALActionFailedException, MSTDatabaseException, MSTQueryException {
		test(SelectorType.SELF);
	}

	// @Test
	public void testThis() throws GOALActionFailedException, MSTDatabaseException, MSTQueryException {
		test(SelectorType.THIS);
	}

	// @Test(expected = GOALActionFailedException.class)
	public void testOther() throws GOALActionFailedException, MSTDatabaseException, MSTQueryException {
		test(SelectorType.SOME);
	}

	@SuppressWarnings("unchecked")
	public void test(SelectorType type) throws GOALActionFailedException, MSTDatabaseException, MSTQueryException {
		Selector selector = mock(Selector.class);
		when(selector.getType()).thenReturn(type);

		Update update = mock(Update.class);
		when(update.toString()).thenReturn("somegoal(X)");
		Update update1 = mock(Update.class);
		when(update1.toString()).thenReturn("somegoal(1)");
		when(update.applySubst(any(Substitution.class))).thenReturn(update1);

		GoalBase goalbase = mock(GoalBase.class);

		Deque<GoalBase> goalstack = mock(Deque.class);
		when(goalstack.getFirst()).thenReturn(goalbase);
		when(goalstack.getLast()).thenReturn(goalbase);

		MentalStateWithEvents mentalstate = mock(MentalStateWithEvents.class);
		when(mentalstate.getAttentionStack()).thenReturn(goalstack);

		ExecutionEventGeneratorInterface eventGenerator = mock(ExecutionEventGeneratorInterface.class);

		AdoptAction adopt = mock(AdoptAction.class);
		when(adopt.getSelector()).thenReturn(selector);
		when(adopt.getUpdate()).thenReturn(update);
		Substitution substitution = mock(Substitution.class);
		RunState state = mock(RunState.class);
		when(state.getMentalState()).thenReturn(mentalstate);
		when(state.getEventGenerator()).thenReturn(eventGenerator);
		AdoptActionExecutor executor = new AdoptActionExecutor(adopt, substitution);

		Result res = executor.execute(state);

		assertTrue(res instanceof Result);
		assertTrue(res.justPerformedAction());
		assertTrue(res.justPerformedRealAction());
		assertFalse(res.getStatus() != RunStatus.RUNNING);
		// self adopts to the current level
		verify(mentalstate, times(1)).adopt(update1, type != SelectorType.SELF, eventGenerator);
	}
}
