package goal.core.executors.stack;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collection;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import events.ExecutionEventGeneratorInterface;
import goal.core.executors.modules.ModuleExecutor;
import goal.core.runtime.service.agent.Result;
import goal.core.runtime.service.agent.Result.RunStatus;
import goal.core.runtime.service.agent.RunState;
import goal.tools.errorhandling.exceptions.GOALActionFailedException;
import krTools.language.Substitution;
import languageTools.program.agent.Module;
import languageTools.program.agent.actions.ModuleCallAction;
import mentalState.MentalStateWithEvents;

@RunWith(Parameterized.class)
public class ActionStackExecutorTest {
	@Parameters
	public static Collection<Object[]> data() {
		return Arrays.asList(new Object[][] { { new Result(false, false, RunStatus.HARD_TERMINATED) },
				{ new Result(true, false, RunStatus.RUNNING) }, { new Result(true, false, RunStatus.SOFT_TERMINATED) },
				{ new Result(false, false, RunStatus.SOFT_TERMINATED) } });
	}

	/**
	 * result from the module that we just called (we simulate this result by
	 * mocking getResult)
	 */
	@Parameter
	public Result moduleResult;
	private Module completedModule;
	private ActionStackExecutor executor;

	@Before
	public void before() throws GOALActionFailedException {
		CallStack stack = mock(CallStack.class);
		ModuleExecutor parentexecutor = mock(ModuleExecutor.class);
		when(parentexecutor.getResult()).thenReturn(moduleResult);
		when(stack.getPopped()).thenReturn(parentexecutor);

		RunState runstate = mock(RunState.class);
		ExecutionEventGeneratorInterface eventgenerator = mock(ExecutionEventGeneratorInterface.class);
		when(runstate.getEventGenerator()).thenReturn(eventgenerator);
		MentalStateWithEvents mentalstate = mock(MentalStateWithEvents.class);
		when(runstate.getMentalState()).thenReturn(mentalstate);

		ModuleCallAction modulecallaction = mock(ModuleCallAction.class);
		completedModule = mock(Module.class);
		when(modulecallaction.getTarget()).thenReturn(completedModule);

		Substitution substitution = mock(Substitution.class);
		executor = new ActionStackExecutor(stack, runstate, modulecallaction, substitution, false);
		executor.result = Result.START;
	}

	/**
	 * Test the result from the module call is derived from the
	 * {@link #moduleResult} if the module is anonymous.
	 * 
	 * @throws GOALActionFailedException
	 */
	@Test
	public void testPoppedAnonymousModuleCall() throws GOALActionFailedException {
		when(completedModule.isAnonymous()).thenReturn(true);

		executor.popped();
		assertEquals(moduleResult, executor.getResult());
	}

	/**
	 * Test the result from the module call is derived from the
	 * {@link #moduleResult}. Here the module is normal, not anonymous
	 * 
	 * @throws GOALActionFailedException
	 */
	@Test
	public void testPoppedModuleCall() throws GOALActionFailedException {
		when(completedModule.isAnonymous()).thenReturn(false);

		executor.popped();

		// With normal modules, the runmode is not affected by the result from
		// the submodule but the action status is.
		Result expectedResult = new Result(moduleResult.justPerformedAction(), moduleResult.justPerformedRealAction(),
				RunStatus.RUNNING);
		assertEquals(expectedResult, executor.getResult());
	}
}
