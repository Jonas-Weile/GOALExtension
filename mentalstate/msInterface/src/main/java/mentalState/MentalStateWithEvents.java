/**
 * The GOAL Mental State. Copyright (C) 2014 Koen Hindriks.
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
package mentalState;

import java.util.Deque;
import java.util.List;
import java.util.Set;

import eis.iilang.Percept;
import events.Channel;
import events.ExecutionEventGeneratorInterface;
import krTools.language.DatabaseFormula;
import krTools.language.Substitution;
import krTools.language.Update;
import languageTools.program.agent.AgentId;
import languageTools.program.agent.Module.FocusMethod;
import languageTools.program.agent.msc.MentalStateCondition;
import languageTools.program.agent.msg.Message;
import languageTools.program.mas.AgentDefinition;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;
import mentalState.executors.MentalStateConditionExecutor;

/**
 * wraps {@link MentalState}, generating events with the
 * {@link ExecutionEventGeneratorInterface}.
 */
public class MentalStateWithEvents {
	private final MentalState mentalState;
	private int queryCount = 0;

	/**
	 * wraps around given {@link MentalState}.
	 *
	 * @param ms
	 *            the {@link MentalState} to wrap
	 */
	public MentalStateWithEvents(MentalState ms) {
		this.mentalState = ms;
	}

	public Result createResult(BASETYPE base, String focus) {
		return this.mentalState.createResult(base, focus);
	}

	/**
	 * @return The agent that owns this mental state, i.e. the agent that actually
	 *         runs queries/updates on it.
	 */
	public AgentDefinition getOwner() {
		return this.mentalState.getOwner();
	}

	/**
	 * Returns the agent's own {@link MentalModel}.
	 *
	 * @return The agent's own mental model.
	 *
	 *         Public for use in agent history transition...
	 */
	public MentalModel getOwnModel() {
		return this.mentalState.getOwnModel();
	}

	/**
	 * @return All known agents, i.e. agents of which the agent maintains a mental
	 *         model. Includes the agent's own aid.
	 */
	public Set<AgentId> getKnownAgents() {
		return this.mentalState.getKnownAgents();
	}

	/**
	 * @param aid
	 *            An agent id.
	 * @return {@code true} if the agent id corresponds to a known aid, i.e. an
	 *         agent for which a mental model is maintained (including owner of this
	 *         mental state).
	 */
	public boolean isKnownAgent(AgentId aid) {
		return this.mentalState.isKnownAgent(aid);
	}

	/**
	 * @return The agent identifier for the mental state.
	 */
	public AgentId getAgentId() {
		return this.mentalState.getAgentId();
	}

	/**
	 * @return The set of formulas that are in the knowledge base of the agent that
	 *         owns this mental state.
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	public Set<DatabaseFormula> getKnowledge() throws MSTDatabaseException, MSTQueryException {
		return this.mentalState.getKnowledge();
	};

	/**
	 * @param agent
	 *            An optional agent to indicate that we want to get the beliefs for
	 *            a specific mental model.
	 * @return Unmodifiable set of formulas that are in the belief base of the agent
	 *         that owns this mental state or in the mental model that this agent
	 *         has of another agent.
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	public Set<DatabaseFormula> getBeliefs(AgentId... agent) throws MSTDatabaseException, MSTQueryException {
		return this.mentalState.getBeliefs(agent);
	}

	public int getBeliefCount() {
		return this.mentalState.getBeliefCount();
	}

	/**
	 * @param agent
	 *            An optional agent to indicate that we want to get the percepts for
	 *            a specific mental model.
	 * @return The set of percepts that are in the percept base of the agent that
	 *         owns this mental state. Note that a percept base is cleared after
	 *         every decision cycle.
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	public Set<Percept> getPercepts(AgentId... agent) throws MSTDatabaseException, MSTQueryException {
		return this.mentalState.getPercepts(agent);
	}

	public int getPerceptCount() {
		return this.mentalState.getPerceptCount();
	}

	/**
	 * @return The set of messages that are in the message base of the agent that
	 *         owns this mental state (sent-to-self) or in the mental model that
	 *         this agent has of another agent (received from that agent). Note that
	 *         a message base is cleared after every decision cycle.
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	public Set<Message> getMessages() throws MSTDatabaseException, MSTQueryException {
		return this.mentalState.getMessages();
	}

	public int getMessageCount() {
		return this.mentalState.getMessageCount();
	}

	/**
	 * @param agent
	 *            An optional agent to indicate that we want to get the goals for a
	 *            specific mental model.
	 * @return The set of updates that are in the goal base of the agent that owns
	 *         this mental state or in the mental model that this agent has of
	 *         another agent.
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	public Set<Update> getGoals(AgentId... agent) throws MSTDatabaseException, MSTQueryException {
		return this.mentalState.getGoals(agent);
	}

	public int getGoalCount() {
		return this.mentalState.getGoalCount();
	}

	/**
	 * Returns the current attention set of the agent. Always returns the attention
	 * set at the deepest focus level.
	 *
	 * @param agent
	 *            An optional agent to indicate that we want to get the attention
	 *            set for a specific mental model.
	 *
	 * @return The current attention set (set of goals) of the indicated agent.
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	public GoalBase getAttentionSet(AgentId... agent) throws MSTDatabaseException, MSTQueryException {
		return this.mentalState.getAttentionSet(agent);
	}

	/**
	 * Returns the stack of goal bases in which all goals the agent currently has
	 * (implicit if not in current attention set, explicit if present).
	 *
	 * @return The stack of goal bases (attention sets) of the agent that owns this
	 *         state.
	 * @todo public for use in the converter package
	 */
	public Deque<GoalBase> getAttentionStack() {
		return this.mentalState.getAttentionStack();
	}

	/**
	 * See {@link MentalModel#isFocussed()}.
	 *
	 * @return True if we have a focus
	 * @throws MSTQueryException
	 * @throws MSTDatabaseException
	 */
	public boolean isFocussedOn(String name) {
		return this.mentalState.isFocussed(name);
	}

	/**
	 * See {@link MentalModel#defocus()}.
	 *
	 * @return A list of achieved and removed goals for the agent.
	 * @throws MSTQueryException
	 * @throws MSTDatabaseException
	 */
	public Result defocus() throws MSTQueryException, MSTDatabaseException {
		return this.mentalState.defocus();
	}

	/**
	 * @return {@code true} if the current attention set is not empty.
	 * @throws MSTQueryException
	 * @throws MSTDatabaseException
	 */
	public boolean hasGoals(AgentId... agent) throws MSTDatabaseException, MSTQueryException {
		return this.mentalState.hasGoals(agent);
	}

	/**
	 * Adds a {@link MentalModel} for a (new) agent. Also used to create a mental
	 * model for the owner of this {@link MentalStateWithEvents}.
	 *
	 * CHECK that this method is thread safe. The agent may be running when this is
	 * called!
	 *
	 * @param agent
	 *            The agent for which a mental model should be created.
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	void addAgentModel(AgentId agent) throws MSTDatabaseException, MSTQueryException {
		this.mentalState.addAgentModel(agent);
	}

	/**
	 * Removes a {@link MentalModel} of another agent from this
	 * {@link MentalStateWithEvents}. Also deletes any references to this agent in
	 * the belief base of the owner of this mental state (i.e. also removes the
	 * related 'agent(name)' fact).
	 *
	 * SHOULD ONLY BE USED TO REMOVE OTHER AGENTS' MODELS.
	 *
	 * @param agent
	 *            The agent whose model needs to be removed.
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	void removeAgentModel(AgentId agent) throws MSTDatabaseException, MSTQueryException {
		this.mentalState.removeAgentModel(agent);
	}

	/**
	 * Cleans up all databases maintained in this agent's {@link MentalModel} and
	 * removes the agent's mental model.
	 *
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	public void cleanUp() throws MSTDatabaseException, MSTQueryException {
		this.mentalState.cleanUp();
	}

	/***********
	 * query and update methods (interface to {@link MentalModel}s)
	 ************/

	/**
	 *
	 *
	 * @param condition
	 *            The mental state condition to be evaluated.
	 * @param substitution
	 *            Substitution for instantiating (free) variables in the mental
	 *            state condition.
	 * @return An executor for evaluating a mental state condition.
	 */
	public MentalStateConditionExecutor getConditionExecutor(MentalStateCondition condition,
			Substitution substitution) {
		return new MentalStateConditionExecutor(condition, substitution);
	}

	/**
	 * Evaluates the mental state condition on this mental state.
	 *
	 * @param msc
	 *            The mental state condition to evaluate.
	 * @param substitution
	 *            The substitution to impose on the condition.
	 * @param generator
	 *            the channel to report executed actions to .
	 * @return MSCResult The result of the evaluation.
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	public MSCResult evaluate(MentalStateCondition msc, Substitution substitution,
			ExecutionEventGeneratorInterface generator) throws MSTDatabaseException, MSTQueryException {
		generator.event(Channel.MSQUERY_START, msc, msc.getSourceInfo(), "evaluating mentalstate '%s'.", msc);

		MSCResult result = this.mentalState.evaluate(msc, substitution, generator);
		++this.queryCount;

		generator.event(Channel.MSQUERY_END, msc, msc.getSourceInfo(), "evaluated mentalstate '%s'.", msc);

		return result;
	}

	public int getAndResetQueryCount() {
		final int count = this.queryCount;
		this.queryCount = 0;
		return count;
	}

	/**
	 * Evaluates the mental state condition in the context of a focus goal. I.e.,
	 * the condition is evaluated with the additional restriction that only one goal
	 * present in the current goal base may be used to evaluate it.
	 * <p>
	 * In addition to the answers returned by
	 * {@link #evaluate(MentalStateWithEvents)}, this method returns a map from each
	 * single goal used to evaluate the condition to the set of substitutions for
	 * which the condition holds (where only that goal has been used to evaluate the
	 * condition).
	 * </p>
	 *
	 * @param msc
	 *            The mental state condition to evaluate.
	 * @param substitution
	 *            The substitution to impose on the condition.
	 * @param generator
	 *            the channel to report executed actions to .
	 *
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 *             If the query on the mental state failed.
	 */
	public MSCResult focusEvaluate(MentalStateCondition msc, Substitution substitution,
			ExecutionEventGeneratorInterface generator) throws MSTDatabaseException, MSTQueryException {
		generator.event(Channel.MSQUERY_START, msc, msc.getSourceInfo(), "evaluating mentalstate '%s'.", msc);

		MSCResult result = this.mentalState.focusEvaluate(msc, substitution, generator);

		generator.event(Channel.MSQUERY_END, msc, msc.getSourceInfo(), "evaluated mentalstate '%s'.", msc);

		return result;
	}

	/**
	 * Evaluates the mental state condition but also computes a 'filtered goal',
	 * i.e., all positive goal literals in the condition of the rule.
	 *
	 * @param msc
	 *            The mental state condition to evaluate.
	 * @param substitution
	 *            The substitution to impose on the condition.
	 * @param generator
	 *            the channel to report executed actions to .
	 * @return The result of evaluation, including, e.g., the set of answers, i.e.,
	 *         bindings for variables in the condition, obtained by querying it.
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 *             If the evaluation failed.
	 */
	public MSCResult filterEvaluate(MentalStateCondition msc, Substitution substitution,
			ExecutionEventGeneratorInterface generator) throws MSTDatabaseException, MSTQueryException {
		generator.event(Channel.MSQUERY_START, msc, msc.getSourceInfo(), "evaluating mentalstate '%s'.", msc);

		MSCResult result = this.mentalState.filterEvaluate(msc, substitution, generator);

		generator.event(Channel.MSQUERY_END, msc, msc.getSourceInfo(), "evaluated mentalstate '%s'.", msc);

		return result;
	}

	/**
	 * The update is inserted into the belief base of the agent.
	 *
	 * @param update
	 *            The update to be inserted.
	 * @param generator
	 *            the channel to report executed actions to .
	 * @param agent
	 *            An optional agent to do the insertion (in a model) for; the
	 *            current agent is used otherwise.
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	public List<Result> insert(Update update, ExecutionEventGeneratorInterface generator, AgentId... agent)
			throws MSTDatabaseException, MSTQueryException {
		generator.event(Channel.INSERT_START, update, update.getSourceInfo(), "insert %s", update);

		List<Result> result = this.mentalState.insert(update, agent);

		generator.event(Channel.INSERT_END, update, update.getSourceInfo(), "inserted %s", update);

		return result;
	}

	/**
	 * Percept is inserted into the percept base of the agent.
	 *
	 * @param percept
	 *            The percept to be inserted.
	 * @param generator
	 *            the channel to report executed actions to .
	 * @param agent
	 *            An optional agent to do the insertion (in a model) for; the
	 *            current agent is used otherwise.
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	public List<Result> percept(Percept percept, ExecutionEventGeneratorInterface generator, AgentId... agent)
			throws MSTDatabaseException, MSTQueryException {
		generator.event(Channel.INSERT_START, percept, null, "insert %s", percept);

		List<Result> result = this.mentalState.percept(percept, agent);

		generator.event(Channel.INSERT_END, percept, null, "inserted %s", percept);

		return result;
	}

	/**
	 * This function is called when the agent owning the mental state has received a
	 * message from another agent (or even itself). The implementation should store
	 * the triple of message sender, message mood, and message content in some way
	 * such that it is queryable later, like 'sender.sent!(content)', where !
	 * represents an example of a mood. In general, all this information should be
	 * stored in the message base in some representation of choice.
	 *
	 * @param message
	 *            The message that has been received
	 * @param generator
	 *            the channel to report executed actions to .
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 *             if the handling of the message failed.
	 */
	public Result received(Message message, ExecutionEventGeneratorInterface generator)
			throws MSTDatabaseException, MSTQueryException {
		generator.event(Channel.INSERT_START, message, null, "insert %s", message);

		Result result = this.mentalState.received(message);

		generator.event(Channel.INSERT_END, message, null, "inserted %s", message);

		return result;
	}

	/**
	 * The update is removed from the beliefbase of the agent.
	 *
	 * @param update
	 *            The belief to be removed.
	 * @param generator
	 *            the channel to report executed actions to .
	 * @param agent
	 *            An optional agent to do the deletion (in a model) for; the current
	 *            agent is used otherwise.
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	public List<Result> delete(Update update, ExecutionEventGeneratorInterface generator, AgentId... agent)
			throws MSTDatabaseException, MSTQueryException {
		generator.event(Channel.DELETE_START, update, update.getSourceInfo(), "delete %s", update);

		List<Result> result = this.mentalState.delete(update, agent);

		generator.event(Channel.DELETE_END, update, update.getSourceInfo(), "deleted %s", update);

		return result;
	}

	/**
	 * The message is removed from the messagebase of the agent.
	 *
	 * @param message
	 *            The message to be removed.
	 * @param generator
	 *            the channel to report executed actions to .
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	public Result removeMessage(Message message, ExecutionEventGeneratorInterface generator)
			throws MSTDatabaseException, MSTQueryException {
		generator.event(Channel.DELETE_START, message, null, "delete %s", message);

		Result result = this.mentalState.removeMessage(message);

		generator.event(Channel.DELETE_END, message, null, "deleted %s", message);

		return result;
	}

	/**
	 * The percept is removed from the perceptbase of the agent.
	 *
	 * @param percept
	 *            The percept to be removed.
	 * @param generator
	 *            the channel to report executed actions to .
	 * @param agent
	 *            An optional agent to do the deletion (in a model) for; the current
	 *            agent is used otherwise.
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	public List<Result> removePercept(Percept percept, ExecutionEventGeneratorInterface generator, AgentId... agent)
			throws MSTDatabaseException, MSTQueryException {
		generator.event(Channel.DELETE_START, percept, null, "delete %s", percept);

		List<Result> result = this.mentalState.removePercept(percept, agent);

		generator.event(Channel.DELETE_END, percept, null, "deleted %s", percept);

		return result;
	}

	/**
	 * Goal is inserted in goal base associated with the agent, but only if goal is
	 * not already present.
	 *
	 * @param update
	 *            The goal to be adopted.
	 * @param focus
	 *            Indicates whether focus attention set should be used or not.
	 * @param agent
	 *            An optional agent to do the adoption (in a model) for; the current
	 *            agent is used otherwise
	 * @return success or failure.
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	public List<Result> adopt(Update update, boolean focus, ExecutionEventGeneratorInterface generator,
			AgentId... agent) throws MSTDatabaseException, MSTQueryException {
		generator.event(Channel.ADOPT_START, update, update.getSourceInfo(), "adopt %s", update);

		List<Result> result = this.mentalState.adopt(update, focus, agent);

		generator.event(Channel.ADOPT_END, update, update.getSourceInfo(), "adopted %s", update);

		return result;
	}

	/**
	 * Goals in goal base associated with the agent that entail the given goal are
	 * removed.
	 *
	 * @param update
	 *            The goal to be dropped.
	 * @param agent
	 *            An optional agent to do the drop (in a model) for; the current
	 *            agent is used otherwise.
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	public List<Result> drop(Update update, ExecutionEventGeneratorInterface generator, AgentId... agent)
			throws MSTDatabaseException, MSTQueryException {
		generator.event(Channel.DROP_START, update, update.getSourceInfo(), "drop %s", update);

		List<Result> dropped = this.mentalState.drop(update, agent);

		generator.event(Channel.DROP_END, update, update.getSourceInfo(), "dropped %s", update);

		return dropped;
	}

	/**
	 * Checks whether any goals have been achieved in the mean time, and, if so,
	 * removes those from the goal base of the agent.
	 *
	 * @param agent
	 *            An optional agent to do the update (in a model) for; the current
	 *            agent is used otherwise.
	 * @return the list of achieved and removed goals (per goalbase)
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	public List<Result> updateGoalState(AgentId... agent) throws MSTDatabaseException, MSTQueryException {
		return this.mentalState.updateGoalState(agent);
	}

	/**
	 * Creates a new focus of attention on a particular set of goals.
	 *
	 * @param attentionSet
	 *            The set of goals the agent that owns this state should focus on.
	 */
	protected void focus(GoalBase attentionSet) {
		this.mentalState.focus(attentionSet);
	}

	/**
	 * Sets a new focus based on the focus method used.
	 *
	 * @param name
	 *            The name of the new base.
	 * @param msc
	 *            The condition under which the focus will be created.
	 * @param focus
	 *            The method of focusing as indicated by the module we are going to
	 *            enter.
	 * @return The focus result (possibly null if we did not focus at all)
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	public Result setFocus(String name, MentalStateCondition msc, FocusMethod focus)
			throws MSTDatabaseException, MSTQueryException {
		return this.mentalState.setFocus(name, msc, focus);
	}

	/**
	 * Get the reward from the environment.
	 *
	 * @param envReward
	 *            is the award that the env gives to the current state. May be null
	 *            if there is no env reward.
	 *
	 * @return The reward
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	public double getReward(Double envReward) throws MSTDatabaseException, MSTQueryException {
		return this.mentalState.getReward(envReward);
	}

	/*********** helper methods ****************/

	/**
	 * Returns the content of this mental state as a string.
	 *
	 * @return A string representation of this mental state's content.
	 */
	@Override
	public String toString() {
		return this.mentalState.toString();
	}

	/**
	 * Converts belief base and/or goal base to text string.
	 *
	 * @param addknowledge
	 *            set to true if knowledge should be included
	 * @param addbeliefs
	 *            set to true if beliefs should be included
	 * @param addpercepts
	 *            set to true if percepts should be included
	 * @param addmessages
	 *            set to true if messages should be included
	 * @param addgoals
	 *            set to true if goals should be included
	 * @param focus
	 *            is true if we need to use current focus, or false if we need to
	 *            reset to global focus.
	 *
	 * @return The text string
	 */
	public String toString(boolean addknowledge, boolean addbeliefs, boolean addpercepts, boolean addmessages,
			boolean addgoals, boolean focus) throws MSTDatabaseException, MSTQueryException {
		return this.mentalState.toString(addknowledge, addbeliefs, addpercepts, addmessages, addgoals, focus);
	}

	/**
	 * @return See {@link MentalModel#printAttentionStack()}.
	 */
	public String printAttentionStack() {
		return this.mentalState.printAttentionStack();
	}
}
