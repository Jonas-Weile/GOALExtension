package goal.core.executors.stack;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mockito;

import events.Channel;
import events.ExecutionEventGeneratorInterface;
import goal.core.runtime.service.agent.Result;
import goal.core.runtime.service.agent.Result.RunStatus;
import goal.core.runtime.service.agent.RunState;
import goal.tools.errorhandling.exceptions.GOALActionFailedException;
import goal.tools.errorhandling.exceptions.GOALLaunchFailureException;
import krTools.KRInterface;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;
import krTools.parser.SourceInfo;
import languageTools.program.actionspec.UserSpecAction;
import languageTools.program.agent.Module;
import languageTools.program.agent.Module.FocusMethod;
import languageTools.program.agent.Module.RuleEvaluationOrder;
import languageTools.program.agent.actions.ActionCombo;
import languageTools.program.agent.msc.MentalStateCondition;
import languageTools.program.agent.rules.IfThenRule;
import languageTools.program.agent.rules.ListallDoRule;
import mentalState.MSCResult;
import mentalState.MentalStateWithEvents;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;
import mentalState.error.MSTTranslationException;
import mentalState.executors.MentalStateConditionExecutor;
import mentalState.translator.Translator;
import msFactory.translator.TranslatorFactory;

/**
 * Unit tests for the {@link RuleStackExecutor}. We mock/stub a lot of classes
 * because executors are triggering a lot of machinery. We also try to mock
 * terms a bit deeper so that we can actually see what is going on if something
 * fails (both for better error messages and to support debugging).
 */
@SuppressWarnings("unchecked")
public class RuleStackExecutorTest {
	private RunState runstate;
	private Module context;
	private ExecutionEventGeneratorInterface debugger;
	private MentalStateConditionExecutor mockedMentalStateConditionExecutor;
	private Substitution emptySubst = new SubstitutionStub();
	private Translator translator;
	private KRInterface krInterface = mock(KRInterface.class);

	@Before
	public void setup() throws MSTDatabaseException, MSTQueryException {

		this.debugger = mock(ExecutionEventGeneratorInterface.class);

		this.context = mock(Module.class);
		when(this.context.getKRInterface()).thenReturn(this.krInterface);

		when(this.krInterface.getSubstitution(org.mockito.Matchers.isNull(Map.class))).thenReturn(this.emptySubst);

		this.translator = mock(Translator.class);
		TranslatorFactory.register(this.krInterface.getClass(), this.translator);

		MentalStateWithEvents mentalstate = mock(MentalStateWithEvents.class);
		// when(mentalstate.getTranslator()).thenReturn(this.translator);

		this.mockedMentalStateConditionExecutor = mock(MentalStateConditionExecutor.class);
		when(mentalstate.getConditionExecutor(any(MentalStateCondition.class), any(Substitution.class)))
				.thenReturn(this.mockedMentalStateConditionExecutor);

		this.runstate = mock(RunState.class);
		when(this.runstate.getEventGenerator()).thenReturn(this.debugger);
		when(this.runstate.getActiveModule()).thenReturn(this.context);
		when(this.runstate.getMentalState()).thenReturn(mentalstate);
		when(this.runstate.getKRI()).thenReturn(this.krInterface);
	}

	/**
	 * we are testing this call: <code>
	 * if goal( objective(OBJ) ) then objectiveHandler.
	 * </code>
	 *
	 * module objectiveHandler has focus=filter. exit=nogoals. order=random.
	 * <p>
	 * After the call, objective(a) must be adopted
	 *
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 * @throws GOALActionFailedException
	 * @throws GOALLaunchFailureException
	 */
	@Test
	public void focusFilter()
			throws MSTDatabaseException, MSTQueryException, GOALActionFailedException, GOALLaunchFailureException {
		CallStack parent_callstack = mock(CallStack.class);

		when(this.context.getRuleEvaluationOrder()).thenReturn(RuleEvaluationOrder.LINEAR);

		Var OBJ = mockVar("OBJ");
		MentalStateCondition goal_objective_OBJ = mockMentalStateCondition("goal(objective(OBJ)", OBJ);
		MentalStateCondition goal_objective_a = mockMentalStateCondition("goal(objective(a))");
		MentalStateCondition goal_objective_b = mockMentalStateCondition("goal(objective(b))");

		// mock substitution of the MentalStateCondition
		Substitution subst_OBJ_a = new SubstitutionStub(OBJ, mockTerm("a"));
		Substitution subst_OBJ_b = new SubstitutionStub(OBJ, mockTerm("b"));

		when(goal_objective_OBJ.applySubst(subst_OBJ_a)).thenReturn(goal_objective_a);
		when(goal_objective_OBJ.applySubst(subst_OBJ_b)).thenReturn(goal_objective_b);

		ActionCombo callObjectiveHandler = mockActionCombo("objectiveHandler1");
		IfThenRule rule = mockIfThenRule(goal_objective_OBJ, callObjectiveHandler, FocusMethod.FILTER);

		// [OBJ/a] substitution.
		Set<Substitution> answers = new HashSet<>();
		answers.add(subst_OBJ_a);
		answers.add(subst_OBJ_b);

		Set<MentalStateCondition> focusedGoals = new HashSet<>();
		focusedGoals.add(goal_objective_OBJ);

		// mock the MSCResult which will be the result of a mentalstate call.
		MSCResult substResult = mockMSCResult(answers, focusedGoals);

		when(this.mockedMentalStateConditionExecutor.evaluate(any(MentalStateWithEvents.class), eq(FocusMethod.FILTER),
				any(ExecutionEventGeneratorInterface.class))).thenReturn(substResult);

		final ActionComboStackExecutor mockedActionComboExecutor = mock(ActionComboStackExecutor.class);

		/**
		 * Finally, create the object to be tested. Stub the factories
		 */
		RuleStackExecutor executorReal = new RuleStackExecutor(parent_callstack, this.runstate, rule, this.emptySubst) {
			@Override
			protected StackExecutor getExecutor(Object object, Substitution substitution) {
				return mockedActionComboExecutor;
			}

		};
		// we need to spy calls to the StackExecutor factory
		RuleStackExecutor executor = spy(executorReal);

		executor.setContext(this.context);

		/*
		 * First real test. pushed() mostly affects the internal state only. Not
		 * much we can check we might want to check that executor.actions
		 * contains two actions one for goal_objective_a and one for
		 * goal_objective_b. however actions can not be seen outside so that's
		 * not public functionality. We can only check if a number of expected
		 * calls have been done.
		 */
		// executor.pushed();
		// It's not clear from docs if we can call popped without pushed()
		executor.popped();

		verifyRunstateUnchanged();

		InOrder inOrder = Mockito.inOrder(parent_callstack);
		// verify that the callstack has been modified properly.
		// top should be our executor
		inOrder.verify(parent_callstack).push(executor);

		// 2nd/last should be our mockedActionComboExecutor
		inOrder.verify(parent_callstack).push(mockedActionComboExecutor);

		// Check that mockedActionComboExecutor has received the correct substi
		// and focus - objective(a) .
		verify(executor).getExecutor(callObjectiveHandler, subst_OBJ_b);
		verify(executor).getExecutor(callObjectiveHandler, subst_OBJ_a);

		// check the call to setFocus was correct
		verify(mockedActionComboExecutor).setFocus(goal_objective_a);

		// we can not test getResult, as this is not for sure the
		// "last time the executor calls itself"
	}

	/**
	 * test the call of listall X <- bel(p(_)) do insert(q(X)). with beliefs
	 * p(1), p(3) and p(7). X should become the empty list [] in this case
	 *
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 * @throws GOALActionFailedException
	 * @throws GOALLaunchFailureException
	 * @throws MSTTranslationException
	 */
	@Test
	public void listall_Anon_var() throws MSTDatabaseException, MSTQueryException, GOALActionFailedException,
			GOALLaunchFailureException, MSTTranslationException {
		CallStack parent_callstack = mock(CallStack.class);

		when(this.context.getRuleEvaluationOrder()).thenReturn(RuleEvaluationOrder.LINEAR);

		Var U = mockVar("_");
		Var X = mockVar("X");

		MentalStateCondition bel_p_U = mockMentalStateCondition("bel(p(U))", U);
		ActionCombo insert_q_X = mockActionCombo("insert(q(X))");
		ListallDoRule rule = mockListallDoRule(X, bel_p_U, insert_q_X, FocusMethod.NONE);

		// query results : yes (emptySubst).
		Set<Substitution> answers = new LinkedHashSet<>();
		answers.add(this.emptySubst);
		// mock the MSCResult which will be the result of a mentalstate call.
		// in this case, the answers are stored under the key 'null' in the
		// MSCResult. (This is not documented in MSCResult)
		Set<MentalStateCondition> focusedGoals = new HashSet<>();
		focusedGoals.add(null);
		MSCResult substResult = mockMSCResult(answers, focusedGoals);

		when(this.mockedMentalStateConditionExecutor.evaluate(any(MentalStateWithEvents.class), any(FocusMethod.class),
				any(ExecutionEventGeneratorInterface.class))).thenReturn(substResult);

		final ActionComboStackExecutor mockedActionComboExecutor = mock(ActionComboStackExecutor.class);

		/**
		 * Finally, create the object to be tested. Stub the factories
		 */
		RuleStackExecutor executorReal = new RuleStackExecutor(parent_callstack, this.runstate, rule, this.emptySubst) {
			@Override
			protected StackExecutor getExecutor(Object object, Substitution substitution) {
				return mockedActionComboExecutor;
			}

		};
		// we need to spy calls to the StackExecutor factory
		RuleStackExecutor executor = spy(executorReal);

		executor.setContext(this.context);

		// first real test call.
		executor.popped();
		verifyRunstateUnchanged();

		// the substi given to the insert should be X/[]
		verify(executor).getExecutor(insert_q_X, this.emptySubst);

		// check the result as this should be last call to the executor
		Result result = executor.getResult();
		assertFalse(result.justPerformedAction());
		assertFalse(result.getStatus() != RunStatus.RUNNING);
	}

	/**
	 * test the call of listall X <- bel(p(Y)) do insert(q(X)). with beliefs
	 * p(1), p(3) and p(7). This should insert q([1,3,7]).
	 *
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 * @throws GOALActionFailedException
	 * @throws GOALLaunchFailureException
	 * @throws MSTTranslationException
	 */
	@Test
	public void listall_1var() throws MSTDatabaseException, MSTQueryException, GOALActionFailedException,
			GOALLaunchFailureException, MSTTranslationException {

		CallStack parent_callstack = mock(CallStack.class);

		when(this.context.getRuleEvaluationOrder()).thenReturn(RuleEvaluationOrder.LINEAR);

		Var X = mockVar("X");
		Var Y = mockVar("Y");
		Term t1 = mockTerm("1");
		Term t3 = mockTerm("3");
		Term t7 = mockTerm("7");
		Term t137 = mockTerm("[1,3,7]");
		Substitution subst_Y_1 = new SubstitutionStub(Y, t1);
		Substitution subst_Y_3 = new SubstitutionStub(Y, t3);
		Substitution subst_Y_7 = new SubstitutionStub(Y, t7);

		List<Term> set137 = new ArrayList<>();
		set137.add(t1);
		set137.add(t3);
		set137.add(t7);

		when(this.translator.makeList(eq(set137))).thenReturn(t137);
		Substitution subst_X_137 = new SubstitutionStub(X, t137);

		when(this.translator.makeList(any(List.class))).thenReturn(t137);

		MentalStateCondition bel_p_Y = mockMentalStateCondition("bel(p(Y))", Y);
		ActionCombo insert_q_X = mockActionCombo("insert(q(X))");
		ListallDoRule rule = mockListallDoRule(X, bel_p_Y, insert_q_X, FocusMethod.NONE);

		// [OBJ/a] substitution. MUST BE LinkedHashSet to retain insertion
		// order.
		Set<Substitution> answers = new LinkedHashSet<>();
		answers.add(subst_Y_1);
		answers.add(subst_Y_3);
		answers.add(subst_Y_7);

		// mock the MSCResult which will be the result of a mentalstate call.
		// in this case, the answers are stored under the key 'null' in the
		// MSCResult. (This is not documented in MSCResult)
		Set<MentalStateCondition> focusedGoals = new HashSet<>();
		focusedGoals.add(null);
		MSCResult substResult = mockMSCResult(answers, focusedGoals);

		when(this.mockedMentalStateConditionExecutor.evaluate(any(MentalStateWithEvents.class), any(FocusMethod.class),
				any(ExecutionEventGeneratorInterface.class))).thenReturn(substResult);

		final ActionComboStackExecutor mockedActionComboExecutor = mock(ActionComboStackExecutor.class);

		/**
		 * Finally, create the object to be tested. Stub the factories
		 */
		RuleStackExecutor executorReal = new RuleStackExecutor(parent_callstack, this.runstate, rule, this.emptySubst) {
			@Override
			protected StackExecutor getExecutor(Object object, Substitution substitution) {
				return mockedActionComboExecutor;
			}

		};
		// we need to spy calls to the StackExecutor factory
		RuleStackExecutor executor = spy(executorReal);

		executor.setContext(this.context);

		// first real test call.
		executor.popped();
		verifyRunstateUnchanged();
		InOrder inOrder = Mockito.inOrder(parent_callstack);
		// verify that the callstack has been modified properly.
		// top should be our executor
		inOrder.verify(parent_callstack).push(executor);

		// 2nd/last should be our mockedActionComboExecutor
		inOrder.verify(parent_callstack).push(mockedActionComboExecutor);

		// Check that mockedActionComboExecutor has received X/[1,3,7]
		verify(executor).getExecutor(insert_q_X, subst_X_137);

		// check setFocus was not called as we are using NONE focus.
		verify(mockedActionComboExecutor, never()).setFocus(any(MentalStateCondition.class));

		// we can not test getResult, as this is not for sure the
		// "last time the executor calls itself"
	}

	/**
	 * test the call of listall X <- bel(p(Y),r(Z)) do insert(q(X)). with
	 * beliefs p(3), p(7) , r(a), r(b). This should insert
	 * q([[3.a],[3,b],[7,a],[7,b]]).
	 *
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 * @throws GOALActionFailedException
	 * @throws GOALLaunchFailureException
	 * @throws MSTTranslationException
	 */
	@Test
	public void listall_2vars() throws MSTDatabaseException, MSTQueryException, GOALActionFailedException,
			GOALLaunchFailureException, MSTTranslationException {
		CallStack parent_callstack = mock(CallStack.class);

		when(this.context.getRuleEvaluationOrder()).thenReturn(RuleEvaluationOrder.LINEAR);

		Var X = mockVar("X");
		Var Y = mockVar("Y");
		Var Z = mockVar("Z");
		Term t3 = mockTerm("3");
		Term t7 = mockTerm("7");
		Term a = mockTerm("a");
		Term b = mockTerm("b");

		Substitution subst_Y3_Za = new SubstitutionStub(Y, t3);
		subst_Y3_Za.addBinding(Z, a);

		Substitution subst_Y3_Zb = new SubstitutionStub(Y, t3);
		subst_Y3_Zb.addBinding(Z, b);

		Substitution subst_Y7_Za = new SubstitutionStub(Y, t7);
		subst_Y7_Za.addBinding(Z, a);

		Substitution subst_Y7_Zb = new SubstitutionStub(Y, t7);
		subst_Y7_Zb.addBinding(Z, b);

		// mock makeList
		List<Term> set3a = new ArrayList<>();
		set3a.add(t3);
		set3a.add(a);
		Term t3a = mockTerm("[3,a]");
		when(this.translator.makeList(eq(set3a))).thenReturn(t3a);

		List<Term> set3b = new ArrayList<>();
		set3b.add(t3);
		set3b.add(b);
		Term t3b = mockTerm("[3,b]");
		when(this.translator.makeList(eq(set3b))).thenReturn(t3b);

		List<Term> set7a = new ArrayList<>();
		set7a.add(t7);
		set7a.add(a);
		Term t7a = mockTerm("[7,a]");
		when(this.translator.makeList(eq(set7a))).thenReturn(t7a);

		List<Term> set7b = new ArrayList<>();
		set7b.add(t7);
		set7b.add(b);
		Term t7b = mockTerm("[7,b]");
		when(this.translator.makeList(eq(set7b))).thenReturn(t7b);

		// [[3,a], [3,b], [7,a], [7,b]]
		List<Term> set_3a_3b_7a_7b = new ArrayList<>();
		set_3a_3b_7a_7b.add(t3a);
		set_3a_3b_7a_7b.add(t3b);
		set_3a_3b_7a_7b.add(t7a);
		set_3a_3b_7a_7b.add(t7b);
		Term t_3a3b7a7b = mockTerm("[[3,a],[3,b],[7,a],[7,b]]");
		SubstitutionStub subst_X_3a3b7a7b = new SubstitutionStub(X, t_3a3b7a7b);
		when(this.translator.makeList(eq(set_3a_3b_7a_7b))).thenReturn(t_3a3b7a7b);

		MentalStateCondition bel_pY_rZ = mockMentalStateCondition("bel(p(Y),r(Z))", Y, Z);
		ActionCombo insert_q_X = mockActionCombo("insert(q(X))");
		ListallDoRule rule = mockListallDoRule(X, bel_pY_rZ, insert_q_X, FocusMethod.NONE);

		// [OBJ/a] substitution. MUST BE LinkedHashSet to retain insertion
		// order.
		Set<Substitution> answers = new LinkedHashSet<>();
		answers.add(subst_Y3_Za);
		answers.add(subst_Y3_Zb);
		answers.add(subst_Y7_Za);
		answers.add(subst_Y7_Zb);

		// mock the MSCResult which will be the result of a mentalstate call.
		// in this case, the answers are stored under the key 'null' in the
		// MSCResult. (This is not documented in MSCResult)
		Set<MentalStateCondition> focusedGoals = new HashSet<>();
		focusedGoals.add(null);
		MSCResult substResult = mockMSCResult(answers, focusedGoals);

		when(this.mockedMentalStateConditionExecutor.evaluate(any(MentalStateWithEvents.class), any(FocusMethod.class),
				any(ExecutionEventGeneratorInterface.class))).thenReturn(substResult);

		final ActionComboStackExecutor mockedActionComboExecutor = mock(ActionComboStackExecutor.class);

		/**
		 * Finally, create the object to be tested. Stub the factories
		 */
		RuleStackExecutor executorReal = new RuleStackExecutor(parent_callstack, this.runstate, rule, this.emptySubst) {
			@Override
			protected StackExecutor getExecutor(Object object, Substitution substitution) {
				return mockedActionComboExecutor;
			}

		};
		// we need to spy calls to the StackExecutor factory
		RuleStackExecutor executor = spy(executorReal);

		executor.setContext(this.context);

		// first real test call.
		executor.popped();
		verifyRunstateUnchanged();
		InOrder inOrder = Mockito.inOrder(parent_callstack);
		// verify that the callstack has been modified properly.
		// top should be our executor
		inOrder.verify(parent_callstack).push(executor);

		// 2nd/last should be our mockedActionComboExecutor
		inOrder.verify(parent_callstack).push(mockedActionComboExecutor);

		// Check that mockedActionComboExecutor has received
		// X/[[3.a],[3,b],[7,a],[7,b]]
		verify(executor).getExecutor(insert_q_X, subst_X_3a3b7a7b);

		// check setFocus was not called as we are using NONE focus.
		verify(mockedActionComboExecutor, never()).setFocus(any(MentalStateCondition.class));

		// we can not test getResult, as this is not for sure the
		// "last time the executor calls itself"
	}

	/**
	 * Test the call of listall X <- bel(p(Y)) do insert(q(X)). with belief that
	 * p(X) does not hold. This should insert q([]).
	 */
	// @Test FIXME: fails atm.
	public void listall_NoSolution() throws MSTDatabaseException, MSTQueryException, GOALActionFailedException,
			GOALLaunchFailureException, MSTTranslationException {
		CallStack parent_callstack = mock(CallStack.class);

		when(this.context.getRuleEvaluationOrder()).thenReturn(RuleEvaluationOrder.LINEAR);

		Var X = mockVar("X");
		Var Y = mockVar("Y");
		Term empty = mockTerm("[]");

		List<Term> setEmpty = new ArrayList<>();

		when(this.translator.makeList(eq(setEmpty))).thenReturn(empty);
		Substitution subst_X_empty = new SubstitutionStub(X, empty);

		when(this.translator.makeList(any(List.class))).thenReturn(empty);

		MentalStateCondition bel_p_Y = mockMentalStateCondition("bel(p(Y))", Y);
		ActionCombo insert_q_X = mockActionCombo("insert(q(X))");
		ListallDoRule rule = mockListallDoRule(X, bel_p_Y, insert_q_X, FocusMethod.NONE);

		// mock the MSCResult which will be the result of a mentalstate call.
		// in this case, the answers are stored under the key 'null' in the
		// MSCResult. (This is not documented in MSCResult)
		Set<MentalStateCondition> focusedGoals = new HashSet<>();
		focusedGoals.add(null);
		Set<Substitution> answers = new LinkedHashSet<>();
		MSCResult substResult = mockMSCResult(answers, focusedGoals);

		when(this.mockedMentalStateConditionExecutor.evaluate(any(MentalStateWithEvents.class), any(FocusMethod.class),
				any(ExecutionEventGeneratorInterface.class))).thenReturn(substResult);

		final ActionComboStackExecutor mockedActionComboExecutor = mock(ActionComboStackExecutor.class);

		/**
		 * Finally, create the object to be tested. Stub the factories
		 */
		RuleStackExecutor executorReal = new RuleStackExecutor(parent_callstack, this.runstate, rule, this.emptySubst) {
			@Override
			protected StackExecutor getExecutor(Object object, Substitution substitution) {
				return mockedActionComboExecutor;
			}

		};
		// we need to spy calls to the StackExecutor factory
		RuleStackExecutor executor = spy(executorReal);
		executor.setContext(this.context);

		// first real test call.
		executor.popped();
		verifyRunstateUnchanged();
		InOrder inOrder = Mockito.inOrder(parent_callstack);
		// verify that the callstack has been modified properly.
		// top should be our executor
		inOrder.verify(parent_callstack).push(executor);

		// 2nd/last should be our mockedActionComboExecutor
		inOrder.verify(parent_callstack).push(mockedActionComboExecutor);

		// Check that mockedActionComboExecutor has received X/[1,3,7]
		verify(executor).getExecutor(insert_q_X, subst_X_empty);

		// check setFocus was not called as we are using NONE focus.
		verify(mockedActionComboExecutor, never()).setFocus(any(MentalStateCondition.class));

		// we can not test getResult, as this is not for sure the
		// "last time the executor calls itself"
	}

	/**
	 * test the call of if bel(false) then insert(q).
	 *
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 * @throws GOALActionFailedException
	 * @throws GOALLaunchFailureException
	 * @throws MSTTranslationException
	 */
	@Test
	public void doesNotHold()
			throws MSTDatabaseException, MSTQueryException, GOALActionFailedException, GOALLaunchFailureException {
		CallStack parent_callstack = mock(CallStack.class);
		when(this.context.getRuleEvaluationOrder()).thenReturn(RuleEvaluationOrder.LINEAR);

		MentalStateCondition belFalse = mockMentalStateCondition("bel(false)");
		ActionCombo insertQ = mockActionCombo("insert(q)");
		IfThenRule rule = mockIfThenRule(belFalse, insertQ, FocusMethod.NONE);
		// result object containing no solutions.
		Set<Substitution> answers = new HashSet<>();
		Set<MentalStateCondition> noResult = new HashSet<>();
		// mock the MSCResult which will be the result of a mentalstate call.
		MSCResult substResult = mockMSCResult(answers, noResult);
		when(this.mockedMentalStateConditionExecutor.evaluate(any(MentalStateWithEvents.class), eq(FocusMethod.NONE),
				any(ExecutionEventGeneratorInterface.class))).thenReturn(substResult);

		/**
		 * Finally, create the object to be tested. No override of factories (
		 * getExecutor), we will check that it's not used
		 */
		RuleStackExecutor executorReal = new RuleStackExecutor(parent_callstack, this.runstate, rule, this.emptySubst);

		// we spy calls to the StackExecutor factory to check the created
		// objects.
		RuleStackExecutor executor = spy(executorReal);

		executor.setContext(this.context);

		// TEST pushed.
		// executor.pushed();
		// verifyRunstateUnchanged();
		// checkBreakpointsReportedFail();
		// verify(this.runstate, never()).doLog(any(String.class));
		// assertEquals(this.context, this.runstate.getActiveModule());

		// TEST popped.
		executor.popped();
		verifyRunstateUnchanged();
		verify(executor, never()).getExecutor(any(), any(Substitution.class));
		verify(parent_callstack, never()).push(any(StackExecutor.class));
	}

	/**
	 * Test where the MS query throws an exception. This should result in a
	 * failure occuring in the pushed() call, which should be remembered and
	 * returned with getResult().
	 */
	@Test(expected = GOALActionFailedException.class)
	public void throwingMSQ()
			throws MSTDatabaseException, MSTQueryException, GOALActionFailedException, GOALLaunchFailureException {
		CallStack parent_callstack = mock(CallStack.class);
		when(this.context.getRuleEvaluationOrder()).thenReturn(RuleEvaluationOrder.LINEAR);

		MentalStateCondition badMSQ = mockMentalStateCondition("**badquery**");
		ActionCombo insertQ = mockActionCombo("insert(q)");
		IfThenRule rule = mockIfThenRule(badMSQ, insertQ, FocusMethod.NONE);
		when(this.mockedMentalStateConditionExecutor.evaluate(any(MentalStateWithEvents.class), eq(FocusMethod.NONE),
				any(ExecutionEventGeneratorInterface.class))).thenThrow(new MSTQueryException("failed"));

		/**
		 * Finally, create the object to be tested. No override of factories (
		 * getExecutor), we will check that it's not used
		 */
		RuleStackExecutor executor = new RuleStackExecutor(parent_callstack, this.runstate, rule, this.emptySubst);

		executor.setContext(this.context);

		executor.popped();
		executor.getResult();
	}

	@Test
	public void testRandom() throws MSTTranslationException, GOALActionFailedException, GOALLaunchFailureException,
			MSTDatabaseException, MSTQueryException {
		testRandomActionChoices(RuleEvaluationOrder.RANDOM);
	}

	@Test
	public void testLinearRandom() throws MSTTranslationException, GOALActionFailedException,
			GOALLaunchFailureException, MSTDatabaseException, MSTQueryException {
		testRandomActionChoices(RuleEvaluationOrder.LINEARRANDOM);
	}

	@Test
	public void testRandomAll() throws MSTTranslationException, GOALActionFailedException, GOALLaunchFailureException,
			MSTDatabaseException, MSTQueryException {
		testRandomActionChoices(RuleEvaluationOrder.RANDOMALL);
	}

	// @Test
	// public void testAdaptive() throws MSTTranslationException,
	// GOALActionFailedException, GOALLaunchFailureException,
	// MSTDatabaseException, MSTQueryException {
	// testFirstActionChosen(RuleEvaluationOrder.ADAPTIVE);
	// }

	@Test
	public void testLinear() throws MSTTranslationException, GOALActionFailedException, GOALLaunchFailureException,
			MSTDatabaseException, MSTQueryException {
		testFirstActionChosen(RuleEvaluationOrder.LINEAR);
	}

	// @Test
	// public void testLinearAdaptive() throws MSTTranslationException,
	// GOALActionFailedException,
	// GOALLaunchFailureException, MSTDatabaseException, MSTQueryException {
	// testFirstActionChosen(RuleEvaluationOrder.LINEARADAPTIVE);
	// }

	@Test
	public void testLinearAll() throws MSTTranslationException, GOALActionFailedException, GOALLaunchFailureException,
			MSTDatabaseException, MSTQueryException {
		testFirstActionChosen(RuleEvaluationOrder.LINEARALL);
	}

	/********************* MOCK SUPPORT FUNCTIONS **************************/
	/**
	 * @param mscName
	 * @param freeVarNames
	 *            the free variables in the {@link MentalStateCondition}
	 * @return Mocked {@link MentalStateCondition} containing just a string as
	 *         term.
	 */
	private MentalStateCondition mockMentalStateCondition(String mscName, Var... freeVars) {
		MentalStateCondition msc = mock(MentalStateCondition.class);
		when(msc.toString()).thenReturn(mscName);
		Set<Var> vars = new LinkedHashSet<>();
		for (Var v : freeVars) {
			vars.add(v);
		}
		when(msc.getFreeVar()).thenReturn(vars);

		return msc;

	}

	/**
	 * *
	 *
	 * @param termName
	 *            String holding the term.
	 * @return Mock of the term.
	 */
	private Term mockTerm(String termName) {
		/*
		 * We need to mock terms because {@link
		 * RuleStackExecutor#substitutionsToTerm} manipulates on that level...
		 */
		Term term = mock(Term.class);
		when(term.toString()).thenReturn(termName);
		return term;
	}

	/**
	 * Mock a rule. The rule does not react to substitution attempts.
	 *
	 * @param cond
	 * @param actions
	 * @param focus
	 * @return
	 */
	private IfThenRule mockIfThenRule(MentalStateCondition cond, ActionCombo actions, FocusMethod focus) {
		IfThenRule rule = mock(IfThenRule.class);
		// if goal(objective(OBJ) then objectiveHandler.
		when(rule.getCondition()).thenReturn(cond);
		when(rule.getAction()).thenReturn(actions);
		when(rule.getFocusMethod()).thenReturn(focus);
		when(rule.applySubst(any(Substitution.class))).thenReturn(rule);

		// we have to create the string first, because thenReturn will not
		// accept mock objects.
		String lookslike = "if " + cond.toString() + " then " + actions.toString();
		when(rule.toString()).thenReturn(lookslike);
		return rule;
	}

	/**
	 *
	 * @param comboName
	 * @return Mocked action combo with given name
	 */
	private ActionCombo mockActionCombo(String comboName) {
		ActionCombo combo = mock(ActionCombo.class);
		when(combo.toString()).thenReturn(comboName);
		return combo;
	}

	/**
	 *
	 * @param string
	 *            name of var
	 * @return mocked {@link Var}
	 */
	private Var mockVar(String string) {
		Var var = mock(Var.class);
		when(var.toString()).thenReturn(string);
		return var;
	}

	/**
	 *
	 *
	 * @param answers
	 * @param focusedGoals
	 * @return Mocked {@link MSCResult}. Assumes the result is not empty and
	 *         that this is also the result for getFocusedResults.
	 */
	private MSCResult mockMSCResult(Set<Substitution> answers, Set<MentalStateCondition> focusedGoals) {
		MSCResult substResult = mock(MSCResult.class);
		when(substResult.getAnswers()).thenReturn(answers);
		when(substResult.holds()).thenReturn(!answers.isEmpty());
		when(substResult.getFocusedGoals()).thenReturn(focusedGoals);
		when(substResult.getFocusedResults(any(MentalStateCondition.class))).thenReturn(answers);

		// bit weird check, similar to the original focus() function.
		when(substResult.focus()).thenReturn(!focusedGoals.contains(null));
		String pretty = "MSCResult(" + answers + "," + focusedGoals + ")";
		when(substResult.toString()).thenReturn(pretty);
		return substResult;
	}

	/**
	 * verify that the run state was not modified
	 *
	 * @throws GOALActionFailedException
	 * @throws GOALLaunchFailureException
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	private void verifyRunstateUnchanged()
			throws GOALActionFailedException, GOALLaunchFailureException, MSTDatabaseException, MSTQueryException {
		verify(this.runstate, never()).doPerformAction(any(UserSpecAction.class));
		verify(this.runstate, never()).enterModule(any(Module.class));
		verify(this.runstate, never()).exitModule(any(Module.class));
		verify(this.runstate, never()).incrementRoundCounter();
		verify(this.runstate, never()).reset();
		verify(this.runstate, never()).startCycle(anyBoolean());
		verify(this.runstate, never()).dispose();
	}

	/**
	 * Check that the breakpoints have been reported for a successful mental
	 * state query after the {@link RuleStackExecutor#pushed()}.
	 */
	private void checkBreakpointsReportedSuccess() {
		// check that the 'evaluating' breakpoint was reported
		verify(this.debugger).event(eq(Channel.RULE_CONDITIONAL_VIEW), any(Object.class), any(SourceInfo.class),
				matches("evaluating condition of .*"), any(String.class));

		// check that the hidden 'holds' breakpoint was reported
		verify(this.debugger).event(eq(Channel.HIDDEN_RULE_CONDITION_EVALUATION), any(Object.class),
				any(SourceInfo.class), matches("condition of .* holds."), any(String.class));

		// check that the public 'holds' breakpoint was reported
		verify(this.debugger).event(eq(Channel.RULE_CONDITION_EVALUATION), any(Object.class), any(SourceInfo.class),
				matches("condition of .* holds .*"), any(String.class), any(String.class));
	}

	/**
	 * Check that the breakpoints have been reported for a failed mental state
	 * query after the {@link RuleStackExecutor#pushed()}.
	 */
	private void checkBreakpointsReportedFail() {
		// check that the 'evaluating' breakpoint was reported
		verify(this.debugger).event(eq(Channel.RULE_CONDITIONAL_VIEW), any(Object.class), any(SourceInfo.class),
				matches("evaluating condition of .*"), any(String.class));

		// check that the public 'does not hold' breakpoint was reported
		verify(this.debugger).event(eq(Channel.RULE_CONDITION_EVALUATION), any(Object.class), any(SourceInfo.class),
				matches("condition of rule .* does not hold."), any(String.class));
	}

	private ListallDoRule mockListallDoRule(Var var, MentalStateCondition cond, ActionCombo actions,
			FocusMethod focus) {
		ListallDoRule rule = mock(ListallDoRule.class);
		// if goal(objective(OBJ) then objectiveHandler.
		when(rule.getCondition()).thenReturn(cond);
		when(rule.getAction()).thenReturn(actions);
		when(rule.getFocusMethod()).thenReturn(focus);
		when(rule.getVariable()).thenReturn(var);
		when(rule.applySubst(any(Substitution.class))).thenReturn(rule);

		// we have to create the string first, because thenReturn will not
		// accept mock objects.
		String lookslike = "listalldo  " + var + " <- " + cond.toString() + " do " + actions.toString();
		when(rule.toString()).thenReturn(lookslike);
		return rule;

	}

	/**
	 * We execute a rule if p(Y) then insert(q(Y)). The module is using the
	 * given {@link RuleEvaluationOrder}. Test that a random applicable
	 * instantiation is applied (even distribution of the possible actions 1,3
	 * and 7)
	 *
	 * @param randomorder
	 *            the {@link RuleEvaluationOrder}. Must be some order that
	 *            randomizes the rule evaluation.
	 *
	 */
	private void testRandomActionChoices(RuleEvaluationOrder randomorder) throws MSTTranslationException,
			GOALActionFailedException, GOALLaunchFailureException, MSTDatabaseException, MSTQueryException {
		List<String> actions = runMany(randomorder);

		checkFlatDistribution(actions, "1", "3", "7");
	}

	/**
	 * Test that always the first of the possible action choices is chosen.
	 *
	 * @param nonrandomorder
	 *            the {@link RuleEvaluationOrder}. Must be some order that does
	 *            not randomize the rule evaluation.
	 *
	 */
	private void testFirstActionChosen(RuleEvaluationOrder nonrandomorder) throws MSTTranslationException,
			GOALActionFailedException, GOALLaunchFailureException, MSTDatabaseException, MSTQueryException {
		for (String action : runMany(nonrandomorder)) {
			assertEquals("1", action);
		}
	}

	/**
	 * Execute a rule if p(Y) then insert(q(Y)) many times. The beliefs are
	 * p(1), p(3), p(7).
	 *
	 * @param randomorder
	 *            the {@link RuleEvaluationOrder}.
	 *
	 * @return a list with elements "1","3" and "7", where "1" indicates that
	 *         insert(q(1)) was selected, "3" that insert(q(3)) was selected,
	 *         etc.
	 * @throws MSTQueryException
	 * @throws MSTDatabaseException
	 */
	private List<String> runMany(RuleEvaluationOrder randomorder) throws MSTQueryException, MSTDatabaseException {
		final int LARGE = 100;

		CallStackStub parent_callstack = new CallStackStub();

		when(this.context.getRuleEvaluationOrder()).thenReturn(randomorder);

		final Var X = mockVar("X");
		Term t1 = mockTerm("1");
		Term t3 = mockTerm("3");
		Term t7 = mockTerm("7");
		Substitution subst_X_1 = new SubstitutionStub(X, t1);
		Substitution subst_X_3 = new SubstitutionStub(X, t3);
		Substitution subst_X_7 = new SubstitutionStub(X, t7);

		List<Term> set137 = new ArrayList<>();
		set137.add(t1);
		set137.add(t3);
		set137.add(t7);

		MentalStateCondition bel_p_X = mockMentalStateCondition("bel(p(X))", X);
		ActionCombo insert_q_X = mockActionCombo("insert(q(X))");
		IfThenRule rule = mockIfThenRule(bel_p_X, insert_q_X, FocusMethod.NONE);

		// [OBJ/a] substitution. MUST BE LinkedHashSet to retain insertion
		// order.
		Set<Substitution> answers = new LinkedHashSet<>();
		answers.add(subst_X_1);
		answers.add(subst_X_3);
		answers.add(subst_X_7);

		// mock the MSCResult which will be the result of a mentalstate call.
		// in this case, the answers are stored under the key 'null' in the
		// MSCResult. (This is not documented in MSCResult)
		Set<MentalStateCondition> focusedGoals = new HashSet<>();
		focusedGoals.add(null);
		MSCResult substResult = mockMSCResult(answers, focusedGoals);

		when(this.mockedMentalStateConditionExecutor.evaluate(any(MentalStateWithEvents.class), any(FocusMethod.class),
				any(ExecutionEventGeneratorInterface.class))).thenReturn(substResult);

		/**
		 * Execute it a large number of times.
		 */
		for (int n = 0; n < LARGE; n++) {

			/**
			 * Finally, create the object to be tested. Stub the factories.
			 */
			RuleStackExecutor executor = new RuleStackExecutor(parent_callstack, this.runstate, rule, this.emptySubst) {
				/**
				 * We let the mock object toString return the actual insert
				 * value. So if the action is insert(q(X)) we just return the X.
				 * This allows we can see the picked action and easily check if
				 * it's random between 1, 3 and 7.
				 */
				@Override
				protected StackExecutor getExecutor(Object object, final Substitution substitution) {
					ActionComboStackExecutor mockedActionComboExecutor = mock(ActionComboStackExecutor.class);
					String string = substitution.get(X).toString();
					when(mockedActionComboExecutor.toString()).thenReturn(string);
					return mockedActionComboExecutor;
				}
			};

			executor.setContext(this.context);
			executor.popped();
		}

		/**
		 * Count number of occurences of the actions.
		 */
		List<String> actions = new ArrayList<>();
		for (StackExecutor ex : parent_callstack.getStack()) {
			if (ex instanceof ActionComboStackExecutor) {
				actions.add(((ActionComboStackExecutor) ex).toString());
			}
		}
		return actions;
	}

	/**
	 * Check that a given list is approximately flat distributed. Possible
	 * values must occur approximately equal number of times. This test has a
	 * probability of about 0.001 of failing.
	 *
	 * @param actions
	 *            a list of simple string objects. Each one must compare equal
	 *            to one of the given values. Must be large, at least 100.
	 * @param values
	 *            possible values in the list of actions. must contain at least
	 *            2 values.
	 * @throws IllegalArgumentException
	 *             if <2 values are provided.
	 */
	private void checkFlatDistribution(List<String> actions, String... values) {
		if (values.length < 2) {
			throw new IllegalArgumentException("at least 2 values are required but got only " + values.length);
		}
		/**
		 * Mathematics: We are going to check that for each value v, the number
		 * of occurences O(v) of v in actions is at least some minimum value M.
		 *
		 * <p>
		 * Consider a single value v. Under flat distribution assumptions, the
		 * chance of each action to be of value v is p= 1/|values|.
		 * <p>
		 * We want to be very sure that our tests succeeds, so we have to make M
		 * small enough such P(O(v)<M) very small. We are doing the test for all
		 * values, and they ALL have to succeed. We therefore can aim at
		 * P(O(v)<M) < 0.001/|values|.
		 * <p>
		 * But to keep things simple we assume |values| is small, say at most
		 * 10, and we just aim for P(O(v)<M) < 0.001.
		 * <p>
		 * We assume the size of actions N is large. Then we can assume a Normal
		 * Probability distribution for O(v). expectation value E(O(v)) = N p.
		 * Var(O(v)) = N p (1-p). Then with Sigma = Sqrt(Var), P(O(v)<M) < 0.001
		 * if M< E - 3.1 Sigma.
		 * <p>
		 * Filling in, M = N p - 3.1 Sqrt[N p (1-p)]. For large N (N>100) this
		 * function behaves roughly as M=(N/2) p. This approximation for M is
		 * too small for large N, so we are on the safe side.
		 */
		double minimum = 0.5 * (actions.size() / values.length);

		for (String value : values) {
			assertTrue(Collections.frequency(actions, value) > minimum);
		}

	}

	/**
	 * hack the CallStack for checking. Just collects all pushed data. Would
	 * have been nicer if CallStack would be an interface.
	 *
	 * @author W.Pasman 5oct15
	 *
	 */
	private class CallStackStub extends CallStack {
		List<StackExecutor> executors = new ArrayList<>();

		@Override
		public boolean canExecute() {
			return false;
		}

		@Override
		public int getIndex() {
			return 0;

		}

		@Override
		public void push(StackExecutor executor) {
			this.executors.add(executor);
		}

		@Override
		public void pop() {
		}

		@Override
		public StackExecutor getPopped() {
			return null;
		}

		public List<StackExecutor> getStack() {
			return this.executors;
		}

	}

}
