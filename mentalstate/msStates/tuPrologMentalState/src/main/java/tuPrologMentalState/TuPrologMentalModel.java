package tuPrologMentalState;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Term;
import languageTools.program.agent.AgentId;
import languageTools.program.agent.msg.SentenceMood;
import languageTools.program.mas.UseClause.UseCase;
import mentalState.BASETYPE;
import mentalState.GoalBase;
import mentalState.MentalBase;
import mentalState.MentalModel;
import mentalState.MentalState;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;
import mentalState.error.MSTTranslationException;
import tuprolog.language.JPLUtils;
import tuprolog.language.PrologQuery;
import tuprolog.language.PrologTerm;

public class TuPrologMentalModel extends MentalModel {
	TuPrologMentalModel(MentalState owner) {
		super(owner);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void initialize(AgentId agent) throws MSTDatabaseException, MSTQueryException {
		// Check if the agent model we want to add is for the agent that owns
		// this database. In that case, bases that we can never use selectors on
		// are created. The other bases are always created.
		boolean me = this.owner.getAgentId().equals(agent);
		if (me) {
			// Create a knowledge base and add the agent's knowledge to it
			MentalBase knowledge = new TuPrologMentalBase(this.owner, agent, BASETYPE.KNOWLEDGEBASE);
			List<?> items = this.owner.getOwner().getItems(UseCase.KNOWLEDGE);
			List<DatabaseFormula> dbfs = (items == null) ? new ArrayList<DatabaseFormula>(0)
					: (List<DatabaseFormula>) items;
			for (DatabaseFormula dbf : dbfs) {
				knowledge.insert(dbf);
			}
			addBase(knowledge, BASETYPE.KNOWLEDGEBASE);
			// Create a message base (only for ourselves)
			TuPrologMentalBase messages = new TuPrologMentalBase(this.owner, agent, BASETYPE.MESSAGEBASE);
			addBase(messages, BASETYPE.MESSAGEBASE);
		}

		// Create the belief, percept, and goal bases for which selectors are
		// applicable (i.e. apply to any agent we want to add a model for).
		TuPrologMentalBase beliefs = new TuPrologMentalBase(this.owner, agent, BASETYPE.BELIEFBASE);
		addBase(beliefs, BASETYPE.BELIEFBASE);
		TuPrologMentalBase percepts = new TuPrologMentalBase(this.owner, agent, BASETYPE.PERCEPTBASE);
		addBase(percepts, BASETYPE.PERCEPTBASE);
		TuPrologGoalBase goals = (TuPrologGoalBase) createGoalBase(me ? "main" : agent.getName());
		addGoalBase(goals);
	}

	@Override
	public GoalBase createGoalBase(String name) throws MSTDatabaseException {
		return new TuPrologGoalBase(this.owner, name);
	}

	@Override
	protected Set<Substitution> beliefQuery(Query query) throws MSTQueryException {
		return getBase(BASETYPE.BELIEFBASE).query(query);
	}

	@Override
	protected Set<Substitution> perceptQuery(Query query) throws MSTQueryException {
		Query percept = processPercept((PrologQuery) query);
		return getBase(BASETYPE.PERCEPTBASE).query(percept);
	}

	@Override
	protected Set<Substitution> messageQuery(Query query, SentenceMood mood, List<AgentId> senders)
			throws MSTQueryException {
		if (senders.isEmpty()) {
			alice.tuprolog.Term from = new alice.tuprolog.Var();
			Query message = processMessage((PrologQuery) query, mood, from);
			return getBase(BASETYPE.MESSAGEBASE).query(message);
		} else {
			Set<Substitution> returned = new LinkedHashSet<>();
			for (AgentId sender : senders) {
				try {
					alice.tuprolog.Term from = ((PrologTerm) this.owner.getTranslator().convert(sender)).getTerm();
					Query message = processMessage((PrologQuery) query, mood, from);
					returned.addAll(getBase(BASETYPE.MESSAGEBASE).query(message));
				} catch (MSTTranslationException e) {
					throw new MSTQueryException("unable to translate message sender '" + sender + "' to Prolog.", e);
				}
			}
			return returned;
		}
	}

	@Override
	protected Set<Substitution> messageQuery(Query query, SentenceMood mood, Term var) throws MSTQueryException {
		alice.tuprolog.Term from = ((PrologTerm) var).getTerm();
		Query message = processMessage((PrologQuery) query, mood, from);
		return getBase(BASETYPE.MESSAGEBASE).query(message);
	}

	// Helper functions
	// TODO: partial duplication with functions in TuPrologMentalState
	private Query processPercept(PrologQuery percept) throws MSTQueryException {
		List<alice.tuprolog.Term> conjuncts = JPLUtils.getOperands(",", percept.getTerm());
		List<alice.tuprolog.Term> percepts = new ArrayList<>(conjuncts.size());
		for (alice.tuprolog.Term conjunct : conjuncts) {
			percepts.add(JPLUtils.createCompound("percept", conjunct));
		}
		return new PrologQuery(JPLUtils.termsToConjunct(percepts), percept.getSourceInfo());
	}

	private Query processMessage(PrologQuery message, SentenceMood mood, alice.tuprolog.Term sender)
			throws MSTQueryException {
		alice.tuprolog.Term content = message.getTerm();
		switch (mood) {
		case IMPERATIVE:
			content = JPLUtils.createCompound("imp", content);
			break;
		case INTERROGATIVE:
			content = JPLUtils.createCompound("int", content);
			break;
		default:
		}
		return new PrologQuery(JPLUtils.createCompound("received", sender, content), message.getSourceInfo());
	}
}
