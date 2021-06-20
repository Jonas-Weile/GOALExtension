package mentalState.converter;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;

import krTools.language.DatabaseFormula;
import krTools.language.Update;
import mentalState.GoalBase;
import mentalState.MentalModel;
import mentalState.MentalStateWithEvents;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;

public class MentalstateConverterTest {
	private MentalStateWithEvents mentalState;
	private GOALMentalStateConverter converter;
	private Set<DatabaseFormula> beliefs = new HashSet<>();
	private Deque<GoalBase> attentionStack = new ArrayDeque<>();
	private Set<String> filterNothing;

	@SuppressWarnings("unchecked")
	@Before
	public void init() throws MSTDatabaseException, MSTQueryException {
		this.mentalState = mock(MentalStateWithEvents.class);
		this.converter = new GOALMentalStateConverter();
		// default mock: returns empty goalbase and beliefbase.
		when(this.mentalState.getBeliefs()).thenReturn(this.beliefs);
		when(this.mentalState.getAttentionStack()).thenReturn(this.attentionStack);

		this.filterNothing = mock(Set.class);
		when(this.filterNothing.contains(any(String.class))).thenReturn(true);
	}

	@Test
	public void smokeTest() {
		this.converter.getUniverse();
	}

	@Test
	public void testEmptyGoalsBeliefs() throws MSTDatabaseException, MSTQueryException {
		GOALState state = this.converter.translate(this.mentalState, this.filterNothing, this.filterNothing);

		// MS is completely empty, no beliefs, no goals. All bits should be
		// false.
		for (int n = 0; n < state.size(); n++) {
			assertEquals(false, state.get(n));
		}
	}

	@Test
	public void testOneBelief() throws MSTDatabaseException, MSTQueryException {
		DatabaseFormula formula = mock(DatabaseFormula.class);
		this.beliefs.add(formula);

		GOALState state = this.converter.translate(this.mentalState, this.filterNothing, this.filterNothing);

		// we have one belief now, it should be in bit 0 of the state.
		assertEquals(true, state.get(0));
		for (int n = 1; n < state.size(); n++) {
			assertEquals(false, state.get(n)); // bit set should be empty
		}
	}

	@Test
	public void testFilter() throws MSTDatabaseException, MSTQueryException {
		DatabaseFormula formula = mock(DatabaseFormula.class);
		this.beliefs.add(formula);

		// we added one belief, but we filter out all for the translator.
		GOALState state = this.converter.translate(this.mentalState, new HashSet<>(0), new HashSet<>(0));

		// so no bits should remain after filter out all
		for (int n = 0; n < state.size(); n++) {
			assertEquals(false, state.get(n)); // bit set should be empty
		}
	}

	@Test
	public void testTwoBeliefs() throws MSTDatabaseException, MSTQueryException {
		DatabaseFormula formula1 = mock(DatabaseFormula.class);
		this.beliefs.add(formula1);
		DatabaseFormula formula2 = mock(DatabaseFormula.class);
		this.beliefs.add(formula2);

		GOALState state = this.converter.translate(this.mentalState, this.filterNothing, this.filterNothing);

		// The first 2 bits should be set, representing the first two formulas.
		assertEquals(true, state.get(0));
		assertEquals(true, state.get(1));
		for (int n = 2; n < state.size(); n++) {
			assertEquals(false, state.get(n)); // bit set should be empty
		}
	}

	@Test
	public void testSecondBelief() throws MSTDatabaseException, MSTQueryException {
		DatabaseFormula formula1 = mock(DatabaseFormula.class);
		this.beliefs.add(formula1);
		// let formula1 occupy bit 0.
		GOALState state = this.converter.translate(this.mentalState, this.filterNothing, this.filterNothing);
		this.beliefs.remove(formula1);
		// enter new formula, should get bit 1.
		DatabaseFormula formula2 = mock(DatabaseFormula.class);
		this.beliefs.add(formula2);

		state = this.converter.translate(this.mentalState, this.filterNothing, this.filterNothing);
		state = this.converter.translate(this.mentalState, this.filterNothing, this.filterNothing);

		// after removing formula1, only 2nd bit should remain.
		assertEquals(false, state.get(0));
		assertEquals(true, state.get(1));
		// remainng bit set should be empty
		for (int n = 2; n < state.size(); n++) {
			assertEquals(false, state.get(n));
		}
	}

	@Test
	public void testOneEmptyGoalbase() throws MSTDatabaseException, MSTQueryException {
		// create one goalbase, containing no goals.
		GoalBase goalBase = mock(GoalBase.class);
		when(goalBase.getName()).thenReturn("base1");
		when(goalBase.getUpdates()).thenReturn(new HashSet<>(0));

		// FIXME: this is a non-proper fix for this test
		MentalModel model = mock(MentalModel.class);
		when(model.createGoalBase(any(String.class))).thenReturn(goalBase);
		when(this.mentalState.getOwnModel()).thenReturn(model);

		this.attentionStack.add(goalBase);

		GOALState state = this.converter.translate(this.mentalState, this.filterNothing, this.filterNothing);

		// We have no goals, but one focus, which is stored in the first bit.
		assertEquals(true, state.get(0));
		for (int n = 1; n < state.size(); n++) {
			assertEquals(false, state.get(n)); // bit set should be empty
		}
	}

	@Test
	public void testOneGoal() throws MSTDatabaseException, MSTQueryException {
		// create 1 goalbase, containing one goal.
		GoalBase goalBase = mock(GoalBase.class);
		when(goalBase.getName()).thenReturn("base1");
		Set<Update> goals = new HashSet<>();
		Update goal = mock(Update.class);
		goals.add(goal);
		when(goalBase.getUpdates()).thenReturn(goals);

		// FIXME: this is a non-proper fix for this test
		MentalModel model = mock(MentalModel.class);
		when(model.createGoalBase(any(String.class))).thenReturn(goalBase);
		when(this.mentalState.getOwnModel()).thenReturn(model);

		this.attentionStack.add(goalBase);

		GOALState state = this.converter.translate(this.mentalState, this.filterNothing, this.filterNothing);

		// one focus, and one goal.
		assertEquals(true, state.get(0));
		assertEquals(true, state.get(1));
		for (int n = 2; n < state.size(); n++) {
			assertEquals(false, state.get(n)); // bit set should be empty
		}
	}
}
