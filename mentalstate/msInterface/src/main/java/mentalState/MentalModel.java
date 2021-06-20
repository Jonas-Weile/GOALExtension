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
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import events.Channel;
import events.ExecutionEventGeneratorInterface;
import krTools.KRInterface;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Update;
import krTools.language.Var;
import languageTools.program.agent.AgentId;
import languageTools.program.agent.msc.AGoalLiteral;
import languageTools.program.agent.msc.BelLiteral;
import languageTools.program.agent.msc.GoalALiteral;
import languageTools.program.agent.msc.GoalLiteral;
import languageTools.program.agent.msc.MentalLiteral;
import languageTools.program.agent.msc.PerceptLiteral;
import languageTools.program.agent.msc.SentLiteral;
import languageTools.program.agent.msg.SentenceMood;
import languageTools.program.agent.selector.Selector.SelectorType;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;
import mentalState.error.MSTTranslationException;
import mentalState.executors.SelectorExecutor;
import mentalState.translator.Translator;

/**
 * A {@link MentalModel} is a set of mental bases and a stack of goal bases. The
 * top of the goal-base-stack represents the current focus.
 * <p>
 * An agent can maintain mental models not only for itself but also for other
 * agents. This is why we need to be able to create multiple instances of
 * {@link MentalModel}s which are stored in the {@link MentalState}.
 * </p>
 */
public abstract class MentalModel {
	/**
	 * The mental state that is managing this model
	 */
	protected final MentalState owner;
	protected final AgentId forAgent;
	/**
	 * A map containing the various {@link MentalBase}s of type {@link BASETYPE}
	 * maintained by this {@link MentalModel}.
	 * <p>
	 * The knowledge of the agent is stored in its knowledge base. This base is
	 * static and does not change during runtime.
	 * </p>
	 * <p>
	 * A knowledge base is only used for the agent itself (the owner of this
	 * mental model) and not for other agents whose mental state is may be
	 * modeling. The idea here is that an agent does not have any direct or
	 * indirect access to the knowledge of another agent.
	 * </p>
	 * <p>
	 * The percepts the agent receives from its environment are stored in its
	 * percept base. This base is cleaned every reasoning cycle and the new
	 * percepts received are inserted.
	 * </p>
	 * <p>
	 * A percept base is only used for the agent itself (the owner of this
	 * mental model) and not for other agents because the agent does not have
	 * access to what another agent observes (and should store what it believes
	 * another agent has observed in that agent's mental model's belief base).
	 * </p>
	 * <p>
	 * The messages the agent receives from and sents to other agents are stored
	 * in its message base. Mental models are used to identify a message's
	 * sender. This base is also cleaned every reasoning cycle and the new
	 * messages that are received are inserted.
	 * </p>
	 * <p>
	 * The beliefs of the agent that represent its environment are stored in its
	 * belief base. This is a base that is updated at runtime and used for
	 * maintaining an accurate picture of the actual state of affairs.
	 * </p>
	 * <p>
	 * A belief base is also used for other modeling the beliefs of other agents
	 * than the agent itself. That is, for maintaining a mental model of another
	 * agent's beliefs. The agent, of course, does not have direct access to
	 * another agent's beliefs and has to base such a mental model on what it
	 * observes the other agent is doing (from messages received, actions it
	 * performs).
	 * </p>
	 */
	private final Map<BASETYPE, MentalBase> mentalBases = new LinkedHashMap<>();
	/**
	 * The goals of an agent are stored in a goal base. Because an agent may
	 * have different so-called "attention sets" (goal bases) at different
	 * moments due to focusing on particular goals in a specific context (a
	 * module call), an agent maintains a stack of such sets.
	 * <p>
	 * A goal base is also used for modeling the goals another agent is supposed
	 * to have but in that case no stack is maintained but a single goal base is
	 * used. (The stack only maintains a top element and only that element is
	 * used.)
	 * </p>
	 */
	private final Deque<GoalBase> goalBases = new LinkedList<>();

	/**
	 * @param owner
	 *            The mental state that is creating this model.
	 */
	protected MentalModel(MentalState owner, AgentId forAgent) {
		this.owner = owner;
		this.forAgent = forAgent;
	}

	/**
	 * This function is called immediately after a mental model has been
	 * constructed and registered in the mental state.
	 *
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 *             TODO: put this in the constructor? this gives problem when
	 *             this model requests information about itself from the mental
	 *             state during construction though.
	 */
	abstract protected void initialize() throws MSTDatabaseException, MSTQueryException;

	/**
	 * @param name
	 *            The name of the goal base that is to be created.
	 * @return A new goal base with the given name.
	 * @throws MSTDatabaseException
	 */
	abstract public GoalBase createGoalBase(String name) throws MSTDatabaseException;

	protected abstract Translator getTranslator();

	/**
	 * @param type
	 *            The type to fetch. Do not use GOALBASE here.
	 * @return The base for the given type.
	 */
	public MentalBase getBase(BASETYPE type) {
		return this.mentalBases.get(type);
	}

	/**
	 * Adds a {@link MentalBase} of to this {@link MentalModel}. Use
	 * {@link #addGoalBase(String, KRInterface, Set, BASETYPE)} to add a goal
	 * base!
	 *
	 * @param base
	 *            The mental base.
	 * @param type
	 *            The type of the base.
	 * @throws MSTDatabaseException
	 *             When trying to add a goal base through this method.
	 */
	protected void addBase(MentalBase base, BASETYPE type) throws MSTDatabaseException {
		if (type == BASETYPE.GOALBASE) {
			throw new MSTDatabaseException("addBase was used to set a base of type " + BASETYPE.GOALBASE
					+ "but should only be used to add other bases; use the method addGoalBase to add a goal base.");
		} else {
			this.mentalBases.put(type, base);
		}
	}

	/**
	 * The goal base is still a quite different beast and we need to treat it
	 * differently from a belief base.
	 *
	 * @param base
	 *            The goal base.
	 */
	protected void addGoalBase(GoalBase base) {
		this.goalBases.push(base);
	}

	/**
	 * Cleans up all databases created by KR technology for the belief and goal
	 * base stack in this {@link MentalModel}.
	 *
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	void cleanUp() throws MSTDatabaseException, MSTQueryException {
		List<MentalBase> bases = new ArrayList<>(this.mentalBases.values());
		this.mentalBases.clear();
		for (MentalBase base : bases) {
			base.destroy();
		}

		while (!this.goalBases.isEmpty()) {
			this.goalBases.pop().cleanUp();
		}
	}

	/**
	 * Returns either the current attention set that the agent focuses on or
	 * else the top level goal base of the agent.
	 *
	 * @param use
	 *            {@code true} if we want to use the current focus;
	 *            {@code false} if we want to use the top level goal base.
	 * @return The current attention set, or the top level goal base.
	 */
	public GoalBase getAttentionSet(boolean use) {
		return use ? this.goalBases.getFirst() : this.goalBases.getLast();
	}

	/**
	 * Returns the stack of goal bases, called the attention stack.
	 *
	 * @return The attention stack.
	 */
	Deque<GoalBase> getAttentionStack() {
		return this.goalBases;
	}

	/************* query functionality ********************/

	/**
	 * @param literal
	 *            the mental atom to be checked.
	 * @param focus
	 *            is true if we need to use current focus, or false if we need
	 *            to reset to global focus.
	 * @param generator
	 *            the channel to report executed actions to .
	 * @return a list of substitutions that when applied to the mental atom
	 *         result in instances of it. If the mental atom is closed, and
	 *         follows from the mental state, then pSubst is an empty list.
	 *
	 * @throws MSTQueryException
	 */
	final Set<Substitution> query(MentalLiteral literal, boolean focus, ExecutionEventGeneratorInterface generator)
			throws MSTQueryException {
		Query formula = literal.getFormula();
		Set<Substitution> result = null;

		generator.event(Channel.DB_QUERY_START, literal, literal.getSourceInfo(), "starting query %s", formula);

		if (literal instanceof BelLiteral) {
			result = beliefQuery(formula);
		} else if (literal instanceof PerceptLiteral) {
			result = perceptQuery(formula);
		} else if (literal instanceof SentLiteral) {
			SelectorExecutor selector = new SelectorExecutor(literal.getSelector());
			List<AgentId> senders = selector.evaluate(this.owner, true);
			if (senders == null) { // variable
				result = messageQuery(formula, ((SentLiteral) literal).getMood(),
						literal.getSelector().getParameters().get(0));
			} else {
				result = messageQuery(formula, ((SentLiteral) literal).getMood(), senders);
			}
		} else if (literal instanceof GoalLiteral) {
			result = goalQuery(formula, focus);
		} else if (literal instanceof AGoalLiteral) {
			result = agoalQuery(formula, focus);
		} else if (literal instanceof GoalALiteral) {
			result = goalaQuery(formula, focus);
		}

		generator.event(Channel.DB_QUERY_END, literal, literal.getSourceInfo(), "query result: %s", result);

		if (result == null) {
			throw new MSTQueryException("unknown literal '" + literal + "'.");
		} else {
			if (!(literal instanceof SentLiteral) && literal.getSelector().getType() == SelectorType.VARIABLE) {
				Translator translator = getTranslator();
				Var var = literal.getSelector().getVariable();
				for (Substitution subst : result) {
					try {
						subst.addBinding(var, translator.convert(this.forAgent));
					} catch (MSTTranslationException e) {
						throw new MSTQueryException("failed to bind selector variable to agent name", e);
					}
				}
			}
			return result;
		}
	}

	/**
	 * Evaluates a query on the belief base.
	 *
	 * @param query
	 *            formula to be queried.
	 * @return (possibly empty) set of substitutions that when applied to the
	 *         formula ensure it follows from the belief base.
	 * @throws MSTQueryException
	 */
	protected abstract Set<Substitution> beliefQuery(Query query) throws MSTQueryException;

	/**
	 * Evaluates a query on the percept base.
	 *
	 * @param query
	 *            formula to be queried.
	 * @return (possibly empty) set of substitutions that when applied to the
	 *         formula ensure it follows from the percept base.
	 * @throws MSTQueryException
	 */
	protected abstract Set<Substitution> perceptQuery(Query query) throws MSTQueryException;

	/**
	 * Evaluates a query on the message base.
	 *
	 * @param query
	 *            the message content.
	 * @param mood
	 *            the message mood.
	 * @param senders
	 *            the requested senders of the message
	 * @return (possibly empty) set of substitutions that when applied to the
	 *         formula ensure it follows from the message base.
	 * @throws MSTQueryException
	 */
	protected abstract Set<Substitution> messageQuery(Query query, SentenceMood mood, List<AgentId> senders)
			throws MSTQueryException;

	/**
	 * Evaluates a query on the message base.
	 *
	 * @param query
	 *            the message content.
	 * @param mood
	 *            the message mood.
	 * @param var
	 *            any sender is allowed and should be unified with this variable
	 * @return (possibly empty) set of substitutions that when applied to the
	 *         formula ensure it follows from the message base.
	 * @throws MSTQueryException
	 */
	protected abstract Set<Substitution> messageQuery(Query query, SentenceMood mood, Term var)
			throws MSTQueryException;

	/**
	 * Evaluates a query on the goal base.
	 *
	 * @param query
	 *            formula to be queried.
	 * @param focus
	 *            is true if we need to use current focus, or false if we need
	 *            to reset to global focus.
	 * @return (possibly empty) set of substitutions that when applied to the
	 *         formula ensure it follows from the goal base.
	 * @throws MSTQueryException
	 */
	Set<Substitution> goalQuery(Query query, boolean focus) throws MSTQueryException {
		return getAttentionSet(focus).query(query);
	}

	/**
	 * Evaluates a query on the goal and belief base.
	 *
	 * @param query
	 *            formula to be queried.
	 * @param focus
	 *            is true if we need to use current focus, or false if we need
	 *            to reset to global focus.
	 * @return (possibly empty) set of substitutions that when applied to
	 *         formula ensure it follows from the goal base but NOT from the
	 *         belief base.
	 * @throws MSTQueryException
	 */
	Set<Substitution> agoalQuery(Query query, boolean focus) throws MSTQueryException {
		// First, check whether query follows from goal base.
		Set<Substitution> lSubstSet = goalQuery(query, focus);

		// Second, remove all substitutions for which query after applying
		// that substitution to it also follows from the belief base.
		Set<Substitution> removeSubstSet = new LinkedHashSet<>();
		Query instantiatedQuery;
		for (Substitution lSubst : lSubstSet) {
			instantiatedQuery = query.applySubst(lSubst);
			if (!instantiatedQuery.isClosed()) { // should be closed; see TRAC
				// #174.
				throw new MSTQueryException("goal query '" + query
						+ "' did not result in a closed formula but returned '" + instantiatedQuery + "'.");
			}
			if (!beliefQuery(instantiatedQuery).isEmpty()) {
				removeSubstSet.add(lSubst);
			}
		}

		lSubstSet.removeAll(removeSubstSet);
		return lSubstSet;
	}

	/**
	 * Evaluates a query on the goal and belief base.
	 *
	 * @param query
	 *            formula to be queried.
	 * @param focus
	 *            is true if we need to use current focus, or false if we need
	 *            to reset to global focus.
	 * @return (possibly empty) set of substitutions that when applied to
	 *         formula ensure it follows from the goal base AND from the belief
	 *         base.
	 * @throws MSTQueryException
	 */
	Set<Substitution> goalaQuery(Query query, boolean focus) throws MSTQueryException {
		// First, check whether pForm follows from goal base.
		Set<Substitution> lSubstSet = goalQuery(query, focus);

		// Second, check which substitutions such that when applied to pForm
		// pForm also follows from the belief base.
		Set<Substitution> retainSubstSet = new LinkedHashSet<>();
		Query instantiatedQuery;
		for (Substitution lSubst : lSubstSet) {
			instantiatedQuery = query.applySubst(lSubst);
			if (!instantiatedQuery.isClosed()) {
				throw new MSTQueryException("goal query '" + query
						+ "' did not result in a closed formula but returned '" + instantiatedQuery + "'.");
			}
			if (!beliefQuery(instantiatedQuery).isEmpty()) {
				retainSubstSet.add(lSubst);
			}
		}

		lSubstSet.retainAll(retainSubstSet);
		return lSubstSet;
	}

	/**
	 * Removes all goals from each {@link GoalBase} in the attention set
	 * {@link #goalBases} from which the goal to be dropped can be derived.
	 *
	 * @param goal
	 *            The goal to be dropped.
	 * @throws MSTQueryException
	 * @throws MSTDatabaseException
	 */
	List<Result> drop(Update goal) throws MSTQueryException, MSTDatabaseException {
		List<Result> results = new ArrayList<>(this.goalBases.size());
		for (GoalBase base : this.goalBases) {
			results.add(base.drop(goal));
		}
		return results;
	}

	/**
	 * Implements the blind commitment strategy of a GOAL agent. It removes
	 * goals when they are believed to be achieved completely.
	 *
	 * @return list of goals achieved and removed (per goalbase)
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	List<Result> updateGoalState() throws MSTQueryException, MSTDatabaseException {
		List<Result> results = new LinkedList<>();
		for (GoalBase goalbase : this.goalBases) {
			// Gather the actual goals matching the update in this base
			List<SingleGoal> goalsToBeRemoved = new LinkedList<>();
			for (SingleGoal goal : goalbase.getGoals()) {
				Set<Substitution> check = this.mentalBases.get(BASETYPE.BELIEFBASE).query(goal.getGoal().toQuery());
				if (!check.isEmpty()) {
					goalsToBeRemoved.add(goal);
				}
			}
			// Actually remove the goals (if any)
			if (!goalsToBeRemoved.isEmpty()) {
				Result result = this.owner.createResult(BASETYPE.GOALBASE, goalbase.getName());
				for (SingleGoal goal : goalsToBeRemoved) {
					result.merge(goalbase.remove(goal));
				}
				results.add(result);
			}
		}
		return results;
	}

	/**
	 * @return A string representation of the stack of attention sets stored in
	 *         this {@link MentalModel}. Contains the results of
	 *         {@link GoalBase#showContents()} for all sets.
	 */
	String printAttentionStack() {
		StringBuilder text = new StringBuilder();
		for (GoalBase base : this.goalBases) {
			text.append(base.getName() + ":\n");
			text.append(base.showContents() + "\n");
		}
		return text.toString();
	}

	/**
	 *
	 * @return True if we have a focus (other than main)
	 */
	boolean isFocussed(String name) {
		return this.goalBases.size() > 1 && this.goalBases.peek().getName().equals(name);
	}

	/**
	 * De-focuses the attention of the agent to which this model belongs to.
	 *
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	Result defocus() throws MSTDatabaseException, MSTQueryException {
		// Actually remove the goals
		GoalBase popped = this.goalBases.pop();
		Result result = this.owner.createResult(BASETYPE.GOALBASE, popped.getName());
		for (SingleGoal goal : popped.getGoals().toArray(new SingleGoal[popped.getGoals().size()])) {
			result.merge(popped.remove(goal));
		}
		return result;
	}

	/**
	 * Returns a string representation of this {@link MentalModel}.
	 *
	 * @return A string representation of the mental model.
	 */
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("MentalModel[");
		for (BASETYPE type : this.mentalBases.keySet()) {
			builder.append(this.mentalBases.get(type)).toString();
			builder.append(",\n");
		}
		builder.append(getAttentionSet(true).toString());
		builder.append("\n]");
		return builder.toString();
	}
}