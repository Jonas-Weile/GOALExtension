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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import eis.iilang.Percept;
import events.ExecutionEventGeneratorInterface;
import krTools.KRInterface;
import krTools.database.Database;
import krTools.language.DatabaseFormula;
import krTools.language.Substitution;
import krTools.language.Update;
import languageTools.program.agent.AgentId;
import languageTools.program.agent.Module.FocusMethod;
import languageTools.program.agent.msc.AGoalLiteral;
import languageTools.program.agent.msc.GoalLiteral;
import languageTools.program.agent.msc.MentalFormula;
import languageTools.program.agent.msc.MentalLiteral;
import languageTools.program.agent.msc.MentalStateCondition;
import languageTools.program.agent.msc.SentLiteral;
import languageTools.program.agent.msg.Message;
import languageTools.program.agent.selector.Selector;
import languageTools.program.agent.selector.Selector.SelectorType;
import languageTools.program.mas.AgentDefinition;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;
import mentalState.executors.MentalStateConditionExecutor;
import mentalState.executors.SelectorExecutor;

/**
 * Represents a mental state of an agent and provides query and update
 * functionality. A mental state consists of one or more {@link MentalModel}s: A
 * model to represent the percepts, messages, knowledge, beliefs, and goals of
 * the agent owner of this state and models for representing other agent's
 * beliefs and goals.
 * <p>
 * Assumes that a mental state is represented using a single knowledge
 * representation language, i.e. {@link KRInterface}.
 * </p>
 * <p>
 * To get notified about changes you can subscribe as Observer to the individual
 * {@link MentalBase}s. You can also subscribe to a top-level {@link GoalBase}
 * and receive all sub-module change info too.
 * </p>
 */
public abstract class MentalState {
	/**
	 * Allows fetching the mental model of a certain agent.
	 */
	private final Map<AgentId, MentalModel> models = new ConcurrentHashMap<>();
	/**
	 * The agent that owns this mental state.
	 */
	protected final AgentDefinition owner;
	/**
	 * The agent identifier for the mental state.
	 */
	protected final AgentId agentId;

	/**
	 * Creates a mental state of an agent, including the initial belief, goal and
	 * message bases and a percept base. Knowledge is added to both the belief base
	 * of the agent and each of the individual goals included in the goal base.
	 *
	 * @param owner
	 *            The agent that owns this state.
	 * @param aid
	 *            The agent identifier for this state.
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	public MentalState(AgentDefinition agent, AgentId aid) throws MSTDatabaseException, MSTQueryException {
		this(agent, aid, true);
	}

	/**
	 * A constructor to facilitate unit tests.
	 *
	 * @param owner
	 *            The agent that owns this state.
	 * @param addAgentModel
	 *            When set to false, the mental state for the agent is not
	 *            automatically initialized, as it always is through the public
	 *            constructor (i.e. when set to true).
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	protected MentalState(AgentDefinition owner, AgentId aid, boolean addAgentModel)
			throws MSTDatabaseException, MSTQueryException {
		this.owner = owner;
		this.agentId = aid;
		if (addAgentModel) {
			addAgentModel(this.agentId);
		}
	}

	abstract public Result createResult(BASETYPE base, String focus);

	/**
	 * @return The agent that owns this mental state, i.e. the agent that actually
	 *         runs queries/updates on it.
	 */
	public AgentDefinition getOwner() {
		return this.owner;
	}

	/**
	 * @return All known agents, i.e. agents of which the agent maintains a mental
	 *         model. Includes the agent's own aid.
	 */
	public Set<AgentId> getKnownAgents() {
		return Collections.unmodifiableSet(this.models.keySet());
	}

	/**
	 * @param aid
	 *            An agent id.
	 * @return {@code true} if the agent id corresponds to a known aid, i.e. an
	 *         agent for which a mental model is maintained (including owner of this
	 *         mental state).
	 */
	public boolean isKnownAgent(AgentId aid) {
		return this.models.containsKey(aid);
	}

	/**
	 * @return The agent identifier for the mental state.
	 */
	public AgentId getAgentId() {
		return this.agentId;
	}

	/**
	 * @return A new mental model.
	 */
	abstract protected MentalModel createMentalModel(AgentId forAgent);

	/**
	 * Returns the agent's own {@link MentalModel}.
	 *
	 * @return The agent's own mental model.
	 */
	protected MentalModel getOwnModel() {
		return this.models.get(this.agentId);
	}

	/**
	 * Returns the the {@link MentalModel} the agent has of the given agent. Such a
	 * model is created if it is not present yet, i.e. lazily loaded.
	 *
	 * @param agent
	 * @return The mental model of the given agent.
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	protected MentalModel getModel(AgentId agent) throws MSTDatabaseException, MSTQueryException {
		if (!this.models.containsKey(agent)) {
			addAgentModel(agent);
		}
		return this.models.get(agent);
	}

	protected List<MentalModel> getActiveModels() {
		return new ArrayList<>(this.models.values());
	}

	/**
	 * @return The set of formulas that are in the knowledge base of the agent that
	 *         owns this mental state.
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	abstract public Set<DatabaseFormula> getKnowledge() throws MSTDatabaseException, MSTQueryException;

	/**
	 * @param agent
	 *            An optional agent to indicate that we want to get the beliefs for
	 *            a specific mental model.
	 * @return The set of formulas that are in the belief base of the agent that
	 *         owns this mental state or in the mental model that this agent has of
	 *         another agent.
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	abstract public Set<DatabaseFormula> getBeliefs(AgentId... agent) throws MSTDatabaseException, MSTQueryException;

	abstract public int getBeliefCount();

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
	abstract public Set<Percept> getPercepts(AgentId... agent) throws MSTDatabaseException, MSTQueryException;

	abstract public int getPerceptCount();

	/**
	 * @return The set of messages that are in the message base of the agent that
	 *         owns this mental state (sent-to-self) or in the mental model that
	 *         this agent has of another agent (received from that agent). Note that
	 *         a message base is cleared after every decision cycle.
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	abstract public Set<Message> getMessages() throws MSTDatabaseException, MSTQueryException;

	abstract public int getMessageCount();

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
		return getAttentionSet().getUpdates();
	}

	public int getGoalCount() {
		int count = 0;
		for (GoalBase base : getAttentionStack()) {
			count += base.getUpdates().size();
		}
		return count;
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
		AgentId id = (agent.length == 0) ? this.agentId : agent[0];
		return getModel(id).getAttentionSet(true);
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
		return getOwnModel().getAttentionStack();
	}

	/**
	 * See {@link MentalModel#isFocussed()}.
	 *
	 * @return True if we have a focus.
	 * @throws MSTQueryException
	 * @throws MSTDatabaseException
	 */
	public boolean isFocussed(String name) {
		return getOwnModel().isFocussed(name);
	}

	/**
	 * See {@link MentalModel#defocus()}.
	 *
	 * @return A list of achieved and removed goals for the agent.
	 * @throws MSTQueryException
	 * @throws MSTDatabaseException
	 */
	public Result defocus() throws MSTQueryException, MSTDatabaseException {
		return getOwnModel().defocus();
	}

	/**
	 * @return {@code true} if the current attention set is not empty.
	 * @throws MSTQueryException
	 * @throws MSTDatabaseException
	 */
	public boolean hasGoals(AgentId... agent) throws MSTDatabaseException, MSTQueryException {
		return !getAttentionSet(agent).getGoals().isEmpty();
	}

	/**
	 * Adds a {@link MentalModel} for a (new) agent. Also used to create a mental
	 * model for the owner of this {@link MentalState}.
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
		MentalModel model = createMentalModel(agent);
		this.models.put(agent, model);
		model.initialize();
	}

	/**
	 * Removes a {@link MentalModel} of another agent from this {@link MentalState}.
	 * Also deletes any references to this agent in the belief base of the owner of
	 * this mental state (i.e. also removes the related 'agent(name)' fact).
	 *
	 * SHOULD ONLY BE USED TO REMOVE OTHER AGENTS' MODELS.
	 *
	 * @param agent
	 *            The agent whose model needs to be removed.
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	void removeAgentModel(AgentId agent) throws MSTDatabaseException, MSTQueryException {
		if (this.models.containsKey(agent)) {
			this.models.remove(agent).cleanUp();
		}
	}

	/**
	 * Cleans up all databases maintained in this agent's {@link MentalModel} and
	 * removes the agent's mental model.
	 *
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	public void cleanUp() throws MSTDatabaseException, MSTQueryException {
		for (AgentId id : this.models.keySet()) {
			this.models.get(id).cleanUp();
		}
		this.models.clear();
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
		// Set result contains current results that have been obtained so far.
		// Initialize with the substitution that was provided when creating this
		// executor.
		MSCResult evaluationResult = new MSCResult();
		Set<Substitution> result = new LinkedHashSet<>(1);
		result.add(substitution);

		// Evaluate each mental formula that is part of the mental state
		// condition one by one.
		Set<Substitution> answers, tempResult;
		for (MentalLiteral literal : msc.getAllLiterals()) {
			tempResult = new LinkedHashSet<>();
			for (Substitution currentSubstitution : result) {
				answers = query(literal.applySubst(currentSubstitution), generator);
				// Combine answers with results found so far.
				for (Substitution answer : answers) {
					tempResult.add(currentSubstitution.combine(answer));
				}
			}
			// Update results found so far.
			result = tempResult;
		}

		// Note that a condition without any sub-formulas represents the
		// condition 'true';
		// in that case we return the substitution that was initially provided.
		evaluationResult.setAnswers(result);
		return evaluationResult;
	}

	/**
	 * Evaluates the mental state condition in the context of a focus goal. I.e.,
	 * the condition is evaluated with the additional restriction that only one goal
	 * present in the current goal base may be used to evaluate it.
	 * <p>
	 * In addition to the answers returned by {@link #evaluate(MentalState)}, this
	 * method returns a map from each single goal used to evaluate the condition to
	 * the set of substitutions for which the condition holds (where only that goal
	 * has been used to evaluate the condition).
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
		MSCResult evaluationResult = new MSCResult(), tempResult;
		GoalBase attentionSet = getAttentionSet();

		// Iterate over the goals in the current attention set and evaluate
		// the condition on a mental state with a goal base that consists of
		// only the (single) goal of the current iteration.
		for (SingleGoal goal : attentionSet) {
			// Push current goal onto attention stack in new goal base, which
			// yields a new mental state with the current goal in the focus of
			// attention.
			GoalBase goals = getOwnModel().createGoalBase(goal.toString() + " " + msc.toString());
			goals.insert(goal.getGoal());
			focus(goals);
			// Evaluate the condition on the new mental state and combine with
			// previously obtained answers.
			try {
				tempResult = evaluate(msc, substitution, generator);
				Set<Substitution> answers = new LinkedHashSet<>(tempResult.getAnswers());
				answers.addAll(evaluationResult.getAnswers());

				// TODO: Hacky
				GoalLiteral literal = new GoalLiteral(true, Selector.getDefault(msc.getSourceInfo()),
						goal.getGoal().toQuery(), null, null);
				List<MentalFormula> formulas = new ArrayList<>(1);
				formulas.add(literal);
				MentalStateCondition focusGoal = new MentalStateCondition(formulas, msc.getSourceInfo());

				// Add information about which goal yielded the answers
				// retrieved.
				evaluationResult.addFocusResult(focusGoal, tempResult.getAnswers());
			} finally {
				// Remove the temporary goal base before exiting the method,
				// whether or not an exception was thrown.
				defocus();
			}
		}

		return evaluationResult;
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
		// Collect the relevant goal literals from the condition.
		List<MentalFormula> formulas = new LinkedList<>();
		for (MentalLiteral literal : msc.getAllLiterals()) {
			// Only (positive) goal and a-goal literals can result in 'filtered
			// goals'.
			if (literal.isPositive() && (literal instanceof AGoalLiteral || literal instanceof GoalLiteral)) {
				formulas.add(literal);
			}
		}

		MentalStateCondition filteredGoal = new MentalStateCondition(formulas, msc.getSourceInfo());
		MSCResult result = new MSCResult();
		Set<Substitution> answers = evaluate(msc, substitution, generator).getAnswers();
		result.addFocusResult(filteredGoal, answers);
		return result;
	}

	/**
	 * Evaluate a {@link MentalLiteral} on this state. Such a literal can include
	 * selectors and involves a query on a certain base.
	 *
	 * @param literal
	 *            The literal to evaluate.
	 * @param generator
	 *            the channel to report executed actions to .
	 * @return A set of substitutions, each of which make the evaluation true (or an
	 *         empty set otherwise). When the literal is a negative one, an empty
	 *         substitution set is returned if there was a result (i.e. there was a
	 *         solution), and a set containing one empty substitution is returned to
	 *         indicate a positive result (i.e. there was no solution).
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	protected Set<Substitution> query(MentalLiteral literal, ExecutionEventGeneratorInterface generator)
			throws MSTDatabaseException, MSTQueryException {
		// Process selector.
		Selector selector = (literal instanceof SentLiteral) ? Selector.getDefault(literal.getSourceInfo())
				: literal.getSelector();
		boolean all = selector.getType() == SelectorType.ALL || selector.getType() == SelectorType.ALLOTHER;
		boolean list = selector.getType() == SelectorType.VARIABLE || selector.getType() == SelectorType.PARAMETERLIST;
		boolean focus = selector.getType() != SelectorType.SELF;

		// Evaluate query and compute solutions.
		Iterator<AgentId> agents = new SelectorExecutor(selector).evaluate(this, false).iterator();
		Set<Substitution> result = new LinkedHashSet<>();
		if (all) {
			// We need to verify that all models of agents in the set satisfy
			// this literal for the same solution(s).
			if (agents.hasNext()) {
				AgentId next = agents.next();
				result = getModel(next).query(literal, focus, generator);
			}
			while (agents.hasNext() && result.size() > 0) {
				Set<Substitution> currentResults = new LinkedHashSet<>();
				for (Substitution subst : result) {
					Set<Substitution> tempResult = getModel(agents.next()).query(literal.applySubst(subst), focus,
							generator);
					for (Substitution tempSubst : tempResult) {
						currentResults.add(subst.combine(tempSubst));
					}
				}
				result = currentResults;
			}
		} else if (list) {
			// We need to run the query for all models of agents,
			// though the query does not need to succeed for all of them.
			while (agents.hasNext()) {
				result.addAll(getModel(agents.next()).query(literal, focus, generator));
			}
		} else {
			// We need to find only one agent whose mental model satisfies this
			// literal.
			while (agents.hasNext() && result.isEmpty()) {
				result = getModel(agents.next()).query(literal, focus, generator);
			}
		}

		/*
		 * Negate the result if the literal is negated (by operator 'not') by returning
		 * an empty substitution set if there was a result, else returning a set
		 * containing one empty substitution to indicate a positive result.
		 */
		if (!literal.isPositive()) {
			if (result.isEmpty()) {
				result.add(this.owner.getKRInterface().getSubstitution(null));
			} else {
				return new LinkedHashSet<>(0);
			}
		}

		return result;
	}

	/**
	 * This function is called whenever a new database has been created, optionally
	 * allowing an implementor to do KR-specific stuff. For example, SWI Prolog
	 * needs to impose the knowledge on every database.
	 *
	 * @param database
	 *            A database that was just created.
	 * @param type
	 *            Indicates how the database will be used.
	 * @throws MSTDatabaseException
	 */
	abstract public void createdDatabase(Database database, BASETYPE type) throws MSTDatabaseException;

	/**
	 * The update is inserted into the belief base of the agent.
	 *
	 * @param update
	 *            The update to be inserted.
	 * @param agent
	 *            Optional agent(s) to do the insertion (in a model) for; the
	 *            current agent is used otherwise.
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	abstract public List<Result> insert(Update update, AgentId... agent) throws MSTDatabaseException, MSTQueryException;

	/**
	 * Percept is inserted into the percept base of the agent.
	 *
	 * @param percept
	 *            The percept to be inserted.
	 * @param agent
	 *            Optional agent(s) to do the insertion (in a model) for; the
	 *            current agent is used otherwise.
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	abstract public List<Result> percept(Percept percept, AgentId... agent)
			throws MSTDatabaseException, MSTQueryException;

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
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 *             if the handling of the message failed.
	 */
	abstract public Result received(Message message) throws MSTDatabaseException, MSTQueryException;

	/**
	 * The update is removed from the beliefbase of the agent.
	 *
	 * @param update
	 *            The belief to be removed.
	 * @param agent
	 *            Optional agent(s) to do the deletion (in a model) for; the current
	 *            agent is used otherwise.
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	abstract public List<Result> delete(Update update, AgentId... agent) throws MSTDatabaseException, MSTQueryException;

	/**
	 * The message is removed from the messagebase of the agent.
	 *
	 * @param message
	 *            The message to be removed.
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	abstract public Result removeMessage(Message message) throws MSTDatabaseException, MSTQueryException;

	/**
	 * The percept is removed from the perceptbase of the agent.
	 *
	 * @param percept
	 *            The percept to be removed.
	 * @param agent
	 *            Optional agent(s) to do the deletion (in a model) for; the current
	 *            agent is used otherwise.
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	abstract public List<Result> removePercept(Percept percept, AgentId... agent)
			throws MSTDatabaseException, MSTQueryException;

	/**
	 * Goal is inserted in goal base associated with the agent, but only if goal is
	 * not already present.
	 *
	 * @param update
	 *            The goal to be adopted.
	 * @param focus
	 *            Indicates whether focus attention set should be used or not.
	 * @param agent
	 *            Optional agent(s) to do the adoption (in a model) for; the current
	 *            agent is used otherwise
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	public List<Result> adopt(Update update, boolean focus, AgentId... agent)
			throws MSTDatabaseException, MSTQueryException {
		if (agent.length == 0) {
			agent = new AgentId[] { this.agentId };
		}
		List<Result> results = new LinkedList<>();
		for (AgentId id : agent) {
			GoalBase base = getModel(id).getAttentionSet(focus);
			results.add(base.insert(update));
		}
		return results;
	}

	/**
	 * Goals in goal base associated with the agent that entail the given goal are
	 * removed.
	 *
	 * @param update
	 *            The goal to be dropped.
	 * @param agent
	 *            Optional agent(s) to do the drop (in a model) for; the current
	 *            agent is used otherwise.
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	public List<Result> drop(Update update, AgentId... agent) throws MSTDatabaseException, MSTQueryException {
		if (agent.length == 0) {
			agent = new AgentId[] { this.agentId };
		}
		List<Result> results = new LinkedList<>();
		for (AgentId id : agent) {
			results.addAll(getModel(id).drop(update));
		}
		return results;
	}

	/**
	 * Checks whether any goals have been achieved in the mean time, and, if so,
	 * removes those from the goal base of the agent.
	 *
	 * @param agent
	 *            Optional agent(s) to do the update (in a model) for; the current
	 *            agent is used otherwise.
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	public List<Result> updateGoalState(AgentId... agent) throws MSTDatabaseException, MSTQueryException {
		if (agent.length == 0) {
			agent = new AgentId[] { this.agentId };
		}
		List<Result> results = new LinkedList<>();
		for (AgentId id : agent) {
			results.addAll(getModel(id).updateGoalState());
		}
		return results;
	}

	/**
	 * Creates a new focus of attention on a particular set of goals.
	 *
	 * @param attentionSet
	 *            The set of goals the agent that owns this state should focus on.
	 */
	protected void focus(GoalBase attentionSet) {
		getOwnModel().addGoalBase(attentionSet);
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
		Result result = createResult(BASETYPE.GOALBASE, name);
		GoalBase attentionSet = null;
		switch (focus) {
		case NEW: // Create an empty goal base to construct a new attention set
			attentionSet = getOwnModel().createGoalBase(name);
			break;
		case SELECT:
		case FILTER: // FIXME: should these be equal?!
			attentionSet = getOwnModel().createGoalBase(name);
			if (msc != null) {
				for (MentalLiteral literal : msc.getAllLiterals()) {
					result.merge(attentionSet.insert(literal.getFormula().toUpdate()));
				}
			}
			break;
		default: // Nothing to do
			return null;
		}

		focus(attentionSet);
		return result;
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
		if (envReward == null) {
			return getAttentionSet().getGoals().isEmpty() ? 1.0 : 0.0;
		} else {
			return envReward;
		}
	}

	/*********** helper methods ****************/

	/**
	 * Returns the content of this mental state as a string.
	 *
	 * @return A string representation of this mental state's content.
	 */
	@Override
	public String toString() {
		return "MentalState[" + this.models + "]";
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
		String text = "";
		if (addknowledge) {
			// first convert the KB to string.
			text += "% ----- Knowledge -----\n";
			text += getKnowledge();
		}
		if (addbeliefs) {
			text += "% ----- beliefs -----\n";
			text += getBeliefs();
		}
		if (addpercepts) {
			text += "% ----- percepts -----\n";
			text += getPercepts();
		}
		if (addmessages) {
			text += "% ----- messages -----\n";
			text += getMessages();
		}
		if (addgoals) {
			text += "% ----- goals -----\n";
			text = text + getOwnModel().getAttentionSet(focus).showContents();
		}
		return text;
	}

	/**
	 * @return See {@link MentalModel#printAttentionStack()}.
	 */
	public String printAttentionStack() {
		return getOwnModel().printAttentionStack();
	}
}
