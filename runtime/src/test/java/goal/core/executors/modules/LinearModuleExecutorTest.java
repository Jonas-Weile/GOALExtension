package goal.core.executors.modules;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import events.ExecutionEventGeneratorInterface;
import goal.core.agent.Controller;
import goal.core.executors.stack.CallStack;
import goal.core.executors.stack.RuleStackExecutor;
import goal.core.runtime.service.agent.Result;
import goal.core.runtime.service.agent.Result.RunStatus;
import goal.core.runtime.service.agent.RunState;
import goal.tools.errorhandling.exceptions.GOALActionFailedException;
import krTools.language.Substitution;
import languageTools.program.agent.Module;
import languageTools.program.agent.Module.ExitCondition;
import languageTools.program.agent.Module.RuleEvaluationOrder;
import languageTools.program.agent.rules.Rule;

public class LinearModuleExecutorTest {
	/**
	 * Default test. We pop the next executor but the module has no rules so the
	 * module should exit soft.
	 */
	@Test
	public void testExitModule() throws GOALActionFailedException {

		CallStack stack = mock(CallStack.class);
		RunState runstate = mock(RunState.class);
		ExecutionEventGeneratorInterface eventgenerator = mock(ExecutionEventGeneratorInterface.class);
		when(runstate.getEventGenerator()).thenReturn(eventgenerator);
		Module module = mock(Module.class);
		Substitution subst = mock(Substitution.class);
		LinearModuleExecutor executor = new LinearModuleExecutor(stack, runstate, module, subst,
				RuleEvaluationOrder.LINEARALL);

		// this mock module has no rules so it will terminate when popped
		executor.popped();
		assertEquals(RunStatus.SOFT_TERMINATED, executor.getResult().getStatus());
	}

	/**
	 * Hard exit test. We pop the next executor but the parent indicates that
	 * the previous call exited hard. The module should now exit hard too (even
	 * though we're at the end of the module anyway which would trigger a soft
	 * exit).
	 */
	@Test
	public void testExitModuleHard() throws GOALActionFailedException {
		Controller parentController = mock(Controller.class);

		// in the parent, the rule executor terminated hard. This means the
		// previous action terminated hard.
		RuleStackExecutor parentexecutor = mock(RuleStackExecutor.class);
		when(parentexecutor.getResult()).thenReturn(new Result(false, false, RunStatus.HARD_TERMINATED));

		CallStack stack = mock(CallStack.class);
		when(stack.getPopped()).thenReturn(parentexecutor);

		RunState runstate = mock(RunState.class);
		when(runstate.getParent()).thenReturn(parentController);

		ExecutionEventGeneratorInterface eventgenerator = mock(ExecutionEventGeneratorInterface.class);
		when(runstate.getEventGenerator()).thenReturn(eventgenerator);

		Module module = mock(Module.class);
		when(module.getExitCondition()).thenReturn(ExitCondition.NEVER);
		Substitution subst = mock(Substitution.class);
		LinearModuleExecutor executor = new LinearModuleExecutor(stack, runstate, module, subst,
				RuleEvaluationOrder.LINEARALL);
		// this mock module has no rules so it will terminate when popped
		executor.popped();
		assertEquals(RunStatus.HARD_TERMINATED, executor.getResult().getStatus());
	}

	/**
	 * Hard exit test 2. We pop the next executor but the parent indicates that
	 * the previous call exited hard. The module should now exit hard even
	 * though there are more rules availale in the module
	 */
	@Test
	public void testExitModuleHardMoreRules() throws GOALActionFailedException {
		Controller parentController = mock(Controller.class);

		// in the parent, the rule executor terminated hard. This means the
		// previous action terminated hard.
		RuleStackExecutor parentexecutor = mock(RuleStackExecutor.class);
		when(parentexecutor.getResult()).thenReturn(new Result(false, false, RunStatus.HARD_TERMINATED));

		CallStack stack = mock(CallStack.class);
		when(stack.getPopped()).thenReturn(parentexecutor);

		RunState runstate = mock(RunState.class);
		when(runstate.getParent()).thenReturn(parentController);

		ExecutionEventGeneratorInterface eventgenerator = mock(ExecutionEventGeneratorInterface.class);
		when(runstate.getEventGenerator()).thenReturn(eventgenerator);

		Module module = mock(Module.class);
		List<Rule> rules = new ArrayList<>();
		Rule rule1 = mock(Rule.class);
		rules.add(rule1);
		when(module.getExitCondition()).thenReturn(ExitCondition.NEVER);
		when(module.getRules()).thenReturn(rules);

		Substitution subst = mock(Substitution.class);
		LinearModuleExecutor executor = new LinearModuleExecutor(stack, runstate, module, subst,
				RuleEvaluationOrder.LINEARALL);
		// this mock module has no rules so it will terminate when popped
		executor.popped();
		assertEquals(RunStatus.HARD_TERMINATED, executor.getResult().getStatus());
	}

}
