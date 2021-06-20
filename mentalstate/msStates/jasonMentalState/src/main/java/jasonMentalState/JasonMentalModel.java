package jasonMentalState;

import jason.asSyntax.Atom;
import jason.asSyntax.LogicalFormula;
import jason.asSyntax.Term;
import jason.asSyntax.UnnamedVar;
import jasonkri.Utils;
import jasonkri.language.JasonExpression;
import jasonkri.language.JasonQuery;
import jasonkri.language.JasonTerm;
import jasonkri.language.JasonVar;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import languageTools.program.agent.AgentId;
import languageTools.program.agent.msg.SentenceMood;
import mentalState.BASETYPE;
import mentalState.GoalBase;
import mentalState.MentalBase;
import mentalState.MentalModel;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;

public class JasonMentalModel extends MentalModel {
	JasonMentalModel(JasonMentalState owner) {
		super(owner);
	}

	@Override
	protected void initialize(AgentId agent) throws MSTDatabaseException,
			MSTQueryException {
		// Check if the agent model we want to add is for the agent that owns
		// this database. In that case, bases that we can never use selectors on
		// are created. The other bases are always created.
		boolean me = this.owner.getAgentId().equals(agent);
		if (me) {
			// Create a knowledge base and add the agent's knowledge to it
			MentalBase knowledge = new JasonMentalBase(this.owner, agent,
					BASETYPE.KNOWLEDGEBASE);
			for (DatabaseFormula dbf : ((JasonMentalState) owner)
					.getOrderedKnowledge()) {
				knowledge.insert(dbf);
			}
			addBase(knowledge, BASETYPE.KNOWLEDGEBASE);
			// Create a message base (only for ourselves)
			JasonMentalBase messages = new JasonMentalBase(this.owner, agent,
					BASETYPE.MESSAGEBASE);
			addBase(messages, BASETYPE.MESSAGEBASE);
		}

		// Create the belief, percept, and goal bases for which selectors are
		// applicable (i.e. apply to any agent we want to add a model for).
		JasonMentalBase beliefs = new JasonMentalBase(this.owner, agent,
				BASETYPE.BELIEFBASE);
		addBase(beliefs, BASETYPE.BELIEFBASE);
		JasonMentalBase percepts = new JasonMentalBase(this.owner, agent,
				BASETYPE.PERCEPTBASE);
		addBase(percepts, BASETYPE.PERCEPTBASE);
		JasonGoalBase goals = (JasonGoalBase) createGoalBase(me ? "main"
				: agent.getName());
		addGoalBase(goals);
	}

	@Override
	public GoalBase createGoalBase(String name) throws MSTDatabaseException {
		return new JasonGoalBase(this.owner, name);
	}

	@Override
	protected Set<Substitution> beliefQuery(Query query)
			throws MSTQueryException {
		return getBase(BASETYPE.BELIEFBASE).query(query);
	}

	@Override
	protected Set<Substitution> perceptQuery(Query query)
			throws MSTQueryException {
		Query percept = processPercept((JasonQuery) query);
		return getBase(BASETYPE.PERCEPTBASE).query(percept);
	}

	@Override
	protected Set<Substitution> messageQuery(Query q, SentenceMood mood,
			List<AgentId> senders) throws MSTQueryException {
		JasonQuery query = (JasonQuery) q;
		if (senders.isEmpty()) {
			// empty means ANY sender. Introduce anonymous var
			JasonVar from = new JasonVar(new UnnamedVar(), null);
			Query message = processMessage(query, mood, from);
			return getBase(BASETYPE.MESSAGEBASE).query(message);
		} else {
			Set<Substitution> returned = new LinkedHashSet<>();
			for (AgentId sender : senders) {
				JasonTerm from = new JasonTerm(new Atom(sender.getName()), null);
				Query message = processMessage(query, mood, from);
				returned.addAll(getBase(BASETYPE.MESSAGEBASE).query(message));
			}
			return returned;
		}
	}

	@Override
	protected Set<Substitution> messageQuery(Query query, SentenceMood mood,
			krTools.language.Term var) throws MSTQueryException {
		JasonVar from = (JasonVar) var;
		Query message = processMessage((JasonQuery) query, mood, from);
		return getBase(BASETYPE.MESSAGEBASE).query(message);
	}

	// Helper functions
	// TODO: partial duplication with functions in SwiprologMentalState
	/**
	 * wraps percepts into a "percept" functor.
	 * 
	 * @param percept
	 * @return
	 * @throws MSTQueryException
	 */
	private Query processPercept(JasonQuery percept) throws MSTQueryException {
		List<Term> conjuncts = Converters.getOperands(",",
				percept.getJasonTerm());
		List<LogicalFormula> percepts = new ArrayList<>(conjuncts.size());
		for (Term conjunct : conjuncts) {
			percepts.add(Utils.createPred("percept", conjunct));
		}
		return new JasonQuery(Utils.makeConjunct(percepts),
				percept.getSourceInfo());
	}

	/**
	 * Create a new JasonQuery, by wrapping the message, mood and sender in
	 * "received" predicate
	 * 
	 * @param message
	 *            the message content. can be a variable.
	 * @param mood
	 *            the {@link SentenceMood}
	 * @param sender
	 *            a term for the sender, can be a variable or term so we take
	 *            any JasonExpression
	 * @return new {@link JasonQuery} received((sender,content'). where content'
	 *         = imp(content) or int(content) if mood indicates so.
	 * @throws MSTQueryException
	 */
	private JasonQuery processMessage(JasonQuery message, SentenceMood mood,
			JasonExpression sender) {
		Term content = message.getJasonTerm();
		switch (mood) {
		case IMPERATIVE:
			content = Utils.createPred("imp", content);
			break;
		case INTERROGATIVE:
			content = Utils.createPred("int", content);
			break;
		default:
		}
		return new JasonQuery(Utils.createPred("received",
				sender.getJasonTerm(), content), message.getSourceInfo());
	}
}
