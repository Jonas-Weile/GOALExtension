/**
 * The GOAL Runtime Environment. Copyright (C) 2015 Koen Hindriks.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package goal.tools.adapt;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import goal.core.executors.stack.ActionComboStackExecutor;
import goal.preferences.DebugPreferences;
import goal.tools.Run;
import krTools.KRInterface;
import krTools.dependency.DependencyGraph;
import krTools.language.Expression;
import krTools.language.Substitution;
import languageTools.dependency.ModuleGraphGenerator;
import languageTools.program.agent.Module;
import languageTools.program.agent.actions.ActionCombo;
import languageTools.program.mas.AgentDefinition;
import languageTools.program.mas.LaunchInstruction;
import mentalState.MentalStateWithEvents;
import mentalState.converter.GOALMentalStateConverter;

@SuppressWarnings("unchecked")
public class FileLearnerTest {
	private MockedFileLearner filelearner;
	private Module mainmodule = mock(Module.class);
	private AgentDefinition agentDef = mock(AgentDefinition.class);
	private GOALMentalStateConverter converter = mock(GOALMentalStateConverter.class);
	private LearnerAlgorithm learner = mock(LearnerAlgorithm.class);
	private ModuleGraphGenerator graphgen = mock(ModuleGraphGenerator.class);
	private KRInterface lang = mock(KRInterface.class);

	private DependencyGraph<Expression> depgraph = mock(DependencyGraph.class);

	final static String MODULE_SIGNATURE = "module/0";
	final static ModuleID MODULE = new ModuleID(MODULE_SIGNATURE);

	@Before
	public void before() throws Exception {
		DebugPreferences.setDefault(Run.getDefaultPrefs());

		// hook in some minimal logic.
		when(this.mainmodule.getSignature()).thenReturn(MODULE_SIGNATURE);
		when(this.mainmodule.getKRInterface()).thenReturn(this.lang);

		Set<Module> modules = new HashSet<>(1);
		modules.add(this.mainmodule);
		when(this.agentDef.getAllReferencedModules()).thenReturn(modules);
		when(this.converter.getStateString(any(MentalStateWithEvents.class), any(Map.class), any(Map.class),
				any(Set.class), any(Set.class))).thenReturn("statestring");

		when(this.graphgen.getKRlanguage()).thenReturn(this.lang);
		when(this.graphgen.getDependencyGraph(any(KRInterface.class))).thenReturn(this.depgraph);
	}

	/**
	 * Check robustness for learning non-adaptive modules. FIXME: not yet ok
	 */
	// @Test
	public void testActNonAdaptiveModule() throws Exception {
		when(this.mainmodule.isAdaptive()).thenReturn(false);

		// mock nextAction in the learner.
		// for some reason, the first option in the Integer[] is integer 1.
		when(this.learner.nextAction(any(Integer.class), any(Integer[].class))).thenReturn(1);

		this.filelearner = MockedFileLearner.getMockedFileLearner("nofile", this.agentDef, this.converter, this.learner,
				this.graphgen);

		ActionComboStackExecutor actionExecutor1 = mock(ActionComboStackExecutor.class);
		ActionCombo actioncombo1 = mock(ActionCombo.class);
		when(actioncombo1.applySubst(any(Substitution.class))).thenReturn(actioncombo1);
		when(actioncombo1.toString()).thenReturn("actioncombo1");
		when(actionExecutor1.getAction()).thenReturn(actioncombo1);

		MentalStateWithEvents mentalstate = mock(MentalStateWithEvents.class);

		List<ActionComboStackExecutor> actOptions = new ArrayList<>();
		actOptions.add(actionExecutor1);
		ActionComboStackExecutor chosenactionexecutor = this.filelearner.act(MODULE, mentalstate, actOptions);

		assertEquals(actionExecutor1, chosenactionexecutor);
	}

	@Test
	public void testAct() throws Exception {
		when(this.mainmodule.isAdaptive()).thenReturn(true);

		// mock nextAction in the learner.
		// for some reason, the first option in the Integer[] is integer 1.
		when(this.learner.nextAction(any(Integer.class), any(Integer[].class))).thenReturn(1);

		this.filelearner = MockedFileLearner.getMockedFileLearner("nofile", this.agentDef, this.converter, this.learner,
				this.graphgen);

		ActionComboStackExecutor actionExecutor1 = mock(ActionComboStackExecutor.class);
		ActionCombo actioncombo1 = mock(ActionCombo.class);
		when(actioncombo1.applySubst(any(Substitution.class))).thenReturn(actioncombo1);
		when(actioncombo1.toString()).thenReturn("actioncombo1");
		when(actionExecutor1.getAction()).thenReturn(actioncombo1);

		MentalStateWithEvents mentalstate = mock(MentalStateWithEvents.class);

		List<ActionComboStackExecutor> actOptions = new ArrayList<>();
		actOptions.add(actionExecutor1);
		ActionComboStackExecutor chosenactionexecutor = this.filelearner.act(MODULE, mentalstate, actOptions);

		assertEquals(actionExecutor1, chosenactionexecutor);
	}

	/**
	 * Check if the substitution is applied to the action.
	 */
	@Test
	public void testSubstAppliedToAct() throws Exception {
		when(this.mainmodule.isAdaptive()).thenReturn(true);

		// mock nextAction in the learner.
		// for some reason, the first option in the Integer[] is integer 1.
		when(this.learner.nextAction(any(Integer.class), any(Integer[].class))).thenReturn(1);

		this.filelearner = MockedFileLearner.getMockedFileLearner("nofile", this.agentDef, this.converter, this.learner,
				this.graphgen);

		ActionComboStackExecutor actionExecutor1 = mock(ActionComboStackExecutor.class);
		Substitution actionsubst = mock(Substitution.class);
		when(actionsubst.toString()).thenReturn("[X/3]");// just for debugging
															// convenience.
		ActionCombo actioncombo1 = mock(ActionCombo.class);
		when(actioncombo1.toString()).thenReturn("actioncombo(X)");
		ActionCombo actioncombo2 = mock(ActionCombo.class);
		when(actioncombo2.toString()).thenReturn("actioncombo(3)");

		when(actioncombo1.applySubst(eq(actionsubst))).thenReturn(actioncombo2);
		when(actionExecutor1.getAction()).thenReturn(actioncombo1);
		when(actionExecutor1.getParameters()).thenReturn(actionsubst);

		MentalStateWithEvents mentalstate = mock(MentalStateWithEvents.class);

		List<ActionComboStackExecutor> actOptions = new ArrayList<>();
		actOptions.add(actionExecutor1);
		ActionComboStackExecutor chosenactionexecutor = this.filelearner.act(MODULE, mentalstate, actOptions);

		assertEquals(actionExecutor1, chosenactionexecutor);
		verify(actioncombo1).applySubst(actionsubst);
	}
}

/**
 * Extend filelearner with mocked converter and learner.
 */
@SuppressWarnings("serial")
class MockedFileLearner extends FileLearner {
	private GOALMentalStateConverter c;
	private LearnerAlgorithm l;
	private ModuleGraphGenerator g;

	public static MockedFileLearner getMockedFileLearner(String name, AgentDefinition program,
			GOALMentalStateConverter c, LearnerAlgorithm a, ModuleGraphGenerator gen) {
		return new MockedFileLearner().init(name, program, c, a, gen);
	}

	private MockedFileLearner init(String name, AgentDefinition program, GOALMentalStateConverter c, LearnerAlgorithm a,
			ModuleGraphGenerator gen) {
		this.c = c;
		this.l = a;
		this.g = gen;
		init(new LaunchInstruction(name), program);
		return this;

	}

	@Override
	public GOALMentalStateConverter getMentalStateConverter() {
		return this.c;
	}

	@Override
	public LearnerAlgorithm getLearner(ModuleID modulename) {
		return this.l;
	}

	@Override
	public ModuleGraphGenerator getGraphGenerator() {
		return this.g;
	}
}
