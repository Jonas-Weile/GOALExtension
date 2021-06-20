package mentalState;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import eis.iilang.Percept;
import events.NoEventGenerator;
import krTools.KRInterface;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Update;
import languageTools.analyzer.FileRegistry;
import languageTools.program.agent.AgentId;
import languageTools.program.agent.Module;
import languageTools.program.agent.Module.FocusMethod;
import languageTools.program.agent.msc.BelLiteral;
import languageTools.program.agent.msc.GoalLiteral;
import languageTools.program.agent.msc.MentalFormula;
import languageTools.program.agent.msc.MentalLiteral;
import languageTools.program.agent.msc.MentalStateCondition;
import languageTools.program.agent.msc.PerceptLiteral;
import languageTools.program.agent.msc.SentLiteral;
import languageTools.program.agent.msg.Message;
import languageTools.program.agent.msg.SentenceMood;
import languageTools.program.agent.selector.Selector;
import languageTools.program.agent.selector.Selector.SelectorType;
import languageTools.program.mas.AgentDefinition;
import languageTools.program.mas.UseClause.UseCase;
import mentalState.error.MSTQueryException;

public abstract class MentalStateTest {
	protected KRInterface kri;
	protected AgentDefinition agent;
	protected AgentId agentId;
	protected AgentId otherId;
	protected MentalState mentalState;
	protected DatabaseFormula a;
	protected DatabaseFormula z;

	/** TO IMPLEMENT */

	/**
	 * @return A new KR instance.
	 */
	abstract protected KRInterface getKRI() throws Exception;

	/**
	 * @return A new mental state for the given agent.
	 */
	abstract protected MentalState getMentalState(AgentDefinition agent, AgentId agentId, boolean addAgentModel)
			throws Exception;

	/**
	 * @return A DatabaseFormula that represents a single character (term).
	 */
	abstract protected DatabaseFormula getDBFormula(char content) throws Exception;

	/**
	 * @return An Update that represents a single character (term).
	 */
	abstract protected Update getUpdate(char content) throws Exception;

	/**
	 * @return An Update that represents a compound (arity=1) of character(arg).
	 */
	abstract protected Query getQuery(char content, int arg) throws Exception;

	/**
	 * @return An EIS percept version of the query (by e.g. using the translator).
	 */
	abstract protected Percept getPercept(Query query) throws Exception;

	/**
	 * @return A database formula that is a valid message (by e.g. using the
	 *         translator) based on a single-character sender(term) and
	 *         single-character content(term).
	 */
	abstract protected DatabaseFormula getMessage(char sender, char content) throws Exception;

	/** END */

	@Before
	public void setUp() throws Exception {
		this.kri = getKRI();
		this.agentId = new AgentId("a");
		this.agent = createAgent(this.agentId);
		this.mentalState = getMentalState(this.agent, this.agentId, false);
		this.a = getDBFormula('a');
		this.z = getDBFormula('z');
		this.otherId = new AgentId("o");
	}

	private AgentDefinition createAgent(final AgentId id) throws Exception {
		final AgentDefinition agent = spy(new AgentDefinition(id.toString(), new FileRegistry(), null));
		agent.setKRInterface(this.kri);
		return agent;
	}

	@After
	public void tearDown() throws Exception {
		// this.mentalState.cleanUp();
	}

	/**
	 * Test method for {@link MentalState#addAgentModel()}.
	 */
	@Test
	public void testAddAgentModel() throws Exception {
		// Initially, there should be no model.
		// Note that this is can only be true for this test because
		// we do not automatically initialize the agent's own model.
		assertNull(this.mentalState.getOwnModel());
		this.mentalState.removeAgentModel(this.agentId);
		assertNull(this.mentalState.getOwnModel());

		// Add a model and validate fetching it
		this.mentalState.addAgentModel(this.agentId);
		final MentalModel own = this.mentalState.getOwnModel();
		assertNotNull(own);
		assertEquals(this.mentalState.getModel(this.agentId), own);

		// Quickly test the lazy-loading of mental models.
		createAgent(this.otherId);
		assertNotNull(this.mentalState.getModel(this.otherId));

		// Verify that we can delete our own model as well.
		this.mentalState.removeAgentModel(this.agentId);
		assertNull(this.mentalState.getOwnModel());
	}

	/**
	 * Test that {@link MentalState#getKnowledge()} is a static database
	 */
	@Test(expected = MSTQueryException.class)
	public void testChangeKnowledge() throws Exception {
		// We should have no knowledge initially.
		this.mentalState.addAgentModel(this.agentId);
		assertTrue(this.mentalState.getKnowledge().isEmpty());

		// Try insert a single fact in the knowledgebase. Should fail
		this.mentalState.getOwnModel().getBase(BASETYPE.KNOWLEDGEBASE).insert(this.a);
	}

	/**
	 * Test method for {@link MentalState#getKnowledge()}.
	 */
	@Test
	public void testGetKnowledge() throws Exception {
		final List<DatabaseFormula> knowledge = new ArrayList<>();
		knowledge.add(this.a);
		when(this.agent.getAllKnowledge()).thenReturn(knowledge);
		// We should have no knowledge initially.
		this.mentalState.addAgentModel(this.agentId);

		// Verify that we can get that same base back.
		// it's a set, not a list this time, so check carefully.
		assertEquals(1, this.mentalState.getKnowledge().size());
		assertEquals(this.a, this.mentalState.getKnowledge().iterator().next());
	}

	/**
	 * Test method for {@link MentalState#getBeliefs()}.
	 */
	@Test
	public void testGetBeliefs() throws Exception {
		// We should have no beliefs initially.
		this.mentalState.addAgentModel(this.agentId);
		assertTrue(this.mentalState.getBeliefs().isEmpty());

		// Insert a single fact in the beliefbase.
		this.mentalState.insert(this.a.toQuery().toUpdate());

		// Verify that we can get that same base back.
		final Set<DatabaseFormula> beliefs = new HashSet<>(2);
		beliefs.add(this.a);
		assertEquals(beliefs, this.mentalState.getBeliefs());

		// Create a slightly different beliefbase model for another agent.
		createAgent(this.otherId);
		this.mentalState.addAgentModel(this.otherId);
		this.mentalState.insert(this.a.toQuery().toUpdate(), this.otherId);
		this.mentalState.insert(this.z.toQuery().toUpdate(), this.otherId);

		// Verify that we can get that base back as well,
		// and that it is different from the other agent's base.
		beliefs.add(this.z);
		assertEquals(beliefs, this.mentalState.getBeliefs(this.otherId));
		assertNotEquals(beliefs, this.mentalState.getBeliefs());

		// Now remove a belief again from our own base.
		this.mentalState.delete(this.a.toQuery().toUpdate());
		assertTrue(this.mentalState.getBeliefs().isEmpty());

		// And remove a belief from the mental model as well.
		this.mentalState.delete(this.z.toQuery().toUpdate(), this.otherId);
		beliefs.remove(this.z);
		assertEquals(beliefs, this.mentalState.getBeliefs(this.otherId));
	}

	/**
	 * Test method for {@link MentalState#getMessages()}.
	 */
	@Test
	public void testGetMessages() throws Exception {
		// We should have no messages initially.
		this.mentalState.addAgentModel(this.agentId);
		assertTrue(this.mentalState.getMessages().isEmpty());

		// Insert a single fact in the messagebase.
		final DatabaseFormula message = getMessage('a', 'm');
		this.mentalState.getOwnModel().getBase(BASETYPE.MESSAGEBASE).insert(message);

		// Verify we can get that same base back. FIXME
		// final Set<DatabaseFormula> messages = new HashSet<>(1);
		// messages.add(message);
		// assertEquals(messages, this.mentalState.getMessages());
		assertEquals(1, this.mentalState.getMessages().size());
	}

	/**
	 * Test method for {@link MentalState#getPercepts()}.
	 */
	@Test
	public void testGetPercepts() throws Exception {
		// We should have no percepts initially.
		this.mentalState.addAgentModel(this.agentId);
		assertTrue(this.mentalState.getPercepts().isEmpty());

		// Insert a single fact in the perceptbase.
		this.mentalState.getOwnModel().getBase(BASETYPE.PERCEPTBASE).insert(this.a);

		// Verify that we can get that same base back. FIXME
		// final Set<DatabaseFormula> percepts = new HashSet<>(2);
		// percepts.add(this.a);
		// assertEquals(percepts, this.mentalState.getPercepts());

		// Create a slightly different perceptbase model for another agent.
		createAgent(this.otherId);
		this.mentalState.addAgentModel(this.otherId);
		this.mentalState.getModel(this.otherId).getBase(BASETYPE.PERCEPTBASE).insert(this.a);
		this.mentalState.getModel(this.otherId).getBase(BASETYPE.PERCEPTBASE).insert(this.z);

		// Verify that we can get that base back as well,
		// and that it is different from the other agent's base. FIXME
		// percepts.add(this.z);
		// assertEquals(percepts, this.mentalState.getPercepts(this.otherId));
		// assertNotEquals(percepts, this.mentalState.getPercepts());
	}

	/**
	 * Test method for {@link MentalState#received(Message)}.
	 */
	@Test
	public void testReceived() throws Exception {
		// Initialize a mental model for ourself.
		// Note that this is only needed because we disable
		// the automatic creation of our own model in these tests.
		this.mentalState.addAgentModel(this.agentId);
		assertTrue(this.mentalState.getMessages().isEmpty());

		// Receive a message from ourself.
		final Update content = getUpdate('m');
		Message message = new Message(content, SentenceMood.INDICATIVE);
		message.setSender(this.agentId);
		this.mentalState.received(message);

		// Check if we have received that message and validate it.
		Set<Message> messages = this.mentalState.getMessages();
		assertEquals(1, messages.size());
		// TODO: KR-specific message content check?

		// Try receiving a message from someone else.
		// First initialize a mental model for it.
		createAgent(this.otherId);
		this.mentalState.addAgentModel(this.otherId);

		// Construct the message, using a special mood now as well.
		message = new Message(content, SentenceMood.INTERROGATIVE);
		message.setSender(this.otherId);
		this.mentalState.received(message);

		// Validate the (hopefully) newly received message.
		messages = this.mentalState.getMessages();
		assertEquals(2, messages.size());
		// TODO: KR-specific message content check?

		// Yet another message with another mood
		message = new Message(content, SentenceMood.IMPERATIVE);
		message.setSender(this.otherId);
		this.mentalState.received(message);

		// Validate the (hopefully) newly received message,
		// and ensure that we still have the previous message too.
		messages = this.mentalState.getMessages();
		assertEquals(3, messages.size());
		// TODO: KR-specific message content check?

		// Check if we can remove the last message.
		this.mentalState.removeMessage(message);
		messages = this.mentalState.getMessages();
		assertEquals(2, messages.size());
		// TODO: KR-specific message content check?
	}

	/**
	 * Test method for {@link MentalState#getGoals()}.
	 */
	@Test
	public void testGetGoals() throws Exception {
		// Initialize a mental model for ourself, including a definition.
		// Note that this is only needed because we disable
		// the automatic creation of our own model in these tests.
		final char goalString = 'p';
		final List<DatabaseFormula> knowledge = new ArrayList<>(1);
		knowledge.add(getDBFormula(goalString));
		doReturn(knowledge).when(this.agent).getAllKnowledge();
		this.mentalState.addAgentModel(this.agentId);
		assertFalse(this.mentalState.hasGoals());

		// Create a goal and adopt it (without focus).
		// We shouldn't be able to adopt the same goal twice.
		final Update goal = getUpdate(goalString);
		this.mentalState.adopt(goal, false);
		this.mentalState.adopt(goal, false);
		final Set<Update> goals = new HashSet<>(1);
		goals.add(goal);
		assertEquals(goals, this.mentalState.getGoals());

		// Test if we can drop a goal and then adopt it again
		this.mentalState.drop(goal);
		assertTrue(this.mentalState.getGoals().isEmpty());
		this.mentalState.adopt(goal, false);
		assertEquals(goals, this.mentalState.getGoals());

		// We should be able to adopt the same goal for another agent,
		// but again not twice.
		final AgentDefinition other = createAgent(this.otherId);
		doReturn(knowledge).when(other).getItems(UseCase.KNOWLEDGE);
		this.mentalState.addAgentModel(this.otherId);
		assertFalse(this.mentalState.hasGoals(this.otherId));
		this.mentalState.adopt(goal, false, this.otherId);
		this.mentalState.adopt(goal, false, this.otherId);
		assertEquals(goals, this.mentalState.getGoals(this.otherId));

		// Test dropping a goal for another agent.
		this.mentalState.drop(goal, this.otherId);
		assertFalse(this.mentalState.hasGoals(this.otherId));
	}

	/**
	 * Test method for
	 * {@link.MentalState#evaluate(MentalStateCondition, Substitution)} .
	 */
	@Ignore("Not yet implemented")
	@Test
	public void testEvaluate() {
	}

	/**
	 * Test method for
	 * {@link MentalState#focusEvaluate(MentalStateCondition, Substitution)} .
	 */
	@Ignore("Not yet implemented")
	@Test
	public void testFocusEvaluate() {
	}

	/**
	 * Test method for
	 * {@link MentalState#filterEvaluate(MentalStateCondition, Substitution)} .
	 */
	@Ignore("Not yet implemented")
	@Test
	public void testFilterEvaluate() {
	}

	/**
	 * Test method for {@link MentalState#query(MentalLiteral)} .
	 */
	@Test
	public void testQuery() throws Exception {
		// Set up a mental state.
		this.mentalState.addAgentModel(this.agentId);
		final NoEventGenerator nogenerator = new NoEventGenerator();
		final Query a1 = getQuery('a', 1);
		final Query a2 = getQuery('a', 2);

		final BASETYPE[] toTest = new BASETYPE[] { BASETYPE.BELIEFBASE, BASETYPE.MESSAGEBASE, BASETYPE.PERCEPTBASE };
		for (final BASETYPE base : toTest) {
			// System.out.println("TESTING ON " + base);
			// Insert something and try if we can query it.
			MentalLiteral query = getLiteral(base, SelectorType.SELF, a1);
			addToBase(base, this.agentId, a1);

			assertEquals(1, this.mentalState.query(query, nogenerator).size());

			// Try negative queries too.
			query = getLiteral(base, SelectorType.SELF, a1, false);
			assertEquals(0, this.mentalState.query(query, nogenerator).size());
			query = getLiteral(base, SelectorType.SELF, a2, false);
			assertEquals(1, this.mentalState.query(query, nogenerator).size());

			// Insert something else and see if the result of the initial query
			// remains the same and the second query works as well.
			addToBase(base, this.agentId, a2);
			query = getLiteral(base, SelectorType.SELF, a1);
			assertEquals(1, this.mentalState.query(query, nogenerator).size());
			query = getLiteral(base, SelectorType.SELF, a2);
			assertEquals(1, this.mentalState.query(query, nogenerator).size());

			if (base == BASETYPE.MESSAGEBASE) {
				// Messages do not support selectors...
				continue;
			}

			// Try some selectors.
			query = getLiteral(base, SelectorType.THIS, a1);
			assertEquals(1, this.mentalState.query(query, nogenerator).size());
			query = getLiteral(base, SelectorType.ALL, a1);
			assertEquals(1, this.mentalState.query(query, nogenerator).size());
			query = getLiteral(base, SelectorType.SOME, a1);
			assertEquals(1, this.mentalState.query(query, nogenerator).size());
			query = getLiteral(base, SelectorType.ALLOTHER, a1);
			assertEquals(0, this.mentalState.query(query, nogenerator).size());
			query = getLiteral(base, SelectorType.SOMEOTHER, a1);
			assertEquals(0, this.mentalState.query(query, nogenerator).size());

			// Add a mental model of an agent with a slightly different base and
			// try more selectors.
			final AgentId baseAgent = new AgentId(base.name());
			createAgent(baseAgent);
			this.mentalState.addAgentModel(baseAgent);
			addToBase(base, baseAgent, a1);
			query = getLiteral(base, SelectorType.THIS, a1);
			assertEquals(1, this.mentalState.query(query, nogenerator).size());
			query = getLiteral(base, SelectorType.THIS, a2);
			assertEquals(1, this.mentalState.query(query, nogenerator).size());
			query = getLiteral(base, SelectorType.ALL, a1);
			assertEquals(1, this.mentalState.query(query, nogenerator).size());
			// query = getLiteral(base, SelectorType.ALL, a2); FIXME: needs threads
			// assertEquals(0, this.mentalState.query(query, nogenerator).size());
			query = getLiteral(base, SelectorType.SOME, a1);
			assertEquals(1, this.mentalState.query(query, nogenerator).size());
			query = getLiteral(base, SelectorType.SOME, a2);
			assertEquals(1, this.mentalState.query(query, nogenerator).size());
			query = getLiteral(base, SelectorType.ALLOTHER, a1);
			assertEquals(1, this.mentalState.query(query, nogenerator).size());
			// query = getLiteral(base, SelectorType.ALLOTHER, a2); FIXME: needs threads
			// assertEquals(0, this.mentalState.query(query, nogenerator).size());
			query = getLiteral(base, SelectorType.SOMEOTHER, a1);
			assertEquals(1, this.mentalState.query(query, nogenerator).size());
			// query = getLiteral(base, SelectorType.SOMEOTHER, a2); FIXME: needs threads
			// assertEquals(0, this.mentalState.query(query, nogenerator).size());

			// Clean-up the agent for the next loop.
			this.mentalState.removeAgentModel(baseAgent);
		}
	}

	/**
	 * Test method for {@link MentalState#query(MentalLiteral)} .
	 */
	@Ignore("Not yet implemented")
	@Test
	public void testVariableQuery() throws Exception {
		// TODO: use variables in the queries here...
	}

	private static MentalLiteral getLiteral(final BASETYPE base, final SelectorType selector, final Query query) {
		return getLiteral(base, selector, query, true);
	}

	private static MentalLiteral getLiteral(final BASETYPE base, final SelectorType selector, final Query query,
			final boolean polarity) {
		switch (base) {
		case BELIEFBASE:
			return new BelLiteral(polarity, new Selector(selector, null), query, null, null);
		case MESSAGEBASE:
			return new SentLiteral(polarity, new Selector(selector, null), query, SentenceMood.INDICATIVE, null, null);
		case PERCEPTBASE:
			return new PerceptLiteral(polarity, new Selector(selector, null), query, null, null);
		default:
		case GOALBASE:
		case KNOWLEDGEBASE:
			return null;
		}
	}

	private void addToBase(final BASETYPE base, final AgentId agent, final Query query) throws Exception {
		switch (base) {
		case BELIEFBASE:
			this.mentalState.insert(query.toUpdate(), agent);
			break;
		case MESSAGEBASE:
			final Message msg = new Message(query.toUpdate(), SentenceMood.INDICATIVE);
			msg.setSender(agent);
			this.mentalState.received(msg);
			break;
		case PERCEPTBASE:
			final Percept percept = getPercept(query);
			this.mentalState.percept(percept, agent);
			break;
		default:
		case GOALBASE:
		case KNOWLEDGEBASE:
			break;
		}
	}

	/**
	 * Test method for {@link MentalState#updatePercepts(Set, Set)} .
	 */
	@Ignore("Not yet implemented")
	@Test
	public void testUpdatePercepts() {
	}

	/**
	 * Test method for
	 * {@link MentalState#setFocus(String, MentalStateCondition, Module.FocusMethod)}
	 * .
	 */
	@Test
	public void testSetFocus() throws Exception {
		// Initialize a mental model for ourself, including a definition.
		// Note that this is only needed because we disable
		// the automatic creation of our own model in these tests.
		final char goalString = 'p';
		final List<DatabaseFormula> knowledge = new ArrayList<>(1);
		knowledge.add(getDBFormula(goalString));
		doReturn(knowledge).when(this.agent).getItems(UseCase.KNOWLEDGE);
		this.mentalState.addAgentModel(this.agentId);
		assertEquals("main", this.mentalState.getAttentionSet().getName());
		assertFalse(this.mentalState.hasGoals());

		// First try setFocus without actually focusing.
		final String focus = "focus";
		assertNull(this.mentalState.setFocus(focus, null, FocusMethod.NONE));
		assertTrue(this.mentalState.getGoals().isEmpty());
		assertEquals("main", this.mentalState.getAttentionSet().getName());

		// Then try setFocus with a NEW method, and then immediately defocus.
		assertEquals(focus, this.mentalState.setFocus(focus, null, FocusMethod.NEW).getFocus());
		assertEquals(focus, this.mentalState.getAttentionSet().getName());
		this.mentalState.defocus();
		assertEquals("main", this.mentalState.getAttentionSet().getName());

		// Now do an actual focus (TODO: FILTER does the same as SELECT?).
		final MentalFormula formula = new GoalLiteral(true, new Selector(SelectorType.SELF, null),
				getUpdate(goalString).toQuery(), null, null);
		final List<MentalFormula> formulas = new ArrayList<>(1);
		formulas.add(formula);
		final MentalStateCondition context = new MentalStateCondition(formulas, null);
		assertEquals(focus, this.mentalState.setFocus(focus, context, FocusMethod.SELECT).getFocus());
		assertEquals(focus, this.mentalState.getAttentionSet().getName());
		assertTrue(this.mentalState.hasGoals());
	}

	/**
	 * Test method for {@link MentalState#updateGoalState(AgentDefinition[])} .
	 */
	@Ignore("Not yet implemented")
	@Test
	public void testUpdateGoalState() {
	}
}
