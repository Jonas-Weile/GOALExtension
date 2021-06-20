package swiPrologMentalState;

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
import mentalState.BASETYPE;
import mentalState.GoalBase;
import mentalState.MentalBase;
import mentalState.MentalModel;
import mentalState.MentalState;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;
import mentalState.error.MSTTranslationException;
import mentalState.translator.Translator;
import swiPrologMentalState.translator.SwiPrologTranslator;
import swiprolog.language.PrologCompound;
import swiprolog.language.PrologQuery;
import swiprolog.language.impl.PrologImplFactory;
import swiprolog.parser.PrologOperators;

public class SwiPrologMentalModel extends MentalModel {
	SwiPrologMentalModel(final MentalState owner, final AgentId forAgent) {
		super(owner, forAgent);
	}

	@Override
	protected void initialize() throws MSTDatabaseException, MSTQueryException {
		// Check if the agent model we want to add is for the agent that owns
		// this database. In that case, bases that we can never use selectors on
		// are created. The other bases are always created.
		final boolean me = this.owner.getAgentId().equals(this.forAgent);
		if (me) {
			// Create a knowledge base and add the agent's knowledge to it
			final List<DatabaseFormula> dbfs = this.owner.getOwner().getAllKnowledge();
			final MentalBase knowledge = new SwiPrologMentalBase(this.owner, this.forAgent, BASETYPE.KNOWLEDGEBASE,
					dbfs);
			addBase(knowledge, BASETYPE.KNOWLEDGEBASE);
			// Create a message base (only for ourselves)
			final SwiPrologMentalBase messages = new SwiPrologMentalBase(this.owner, this.forAgent,
					BASETYPE.MESSAGEBASE, null);
			addBase(messages, BASETYPE.MESSAGEBASE);
		}

		// Create the belief, percept, and goal bases for which selectors are
		// applicable (i.e. apply to any agent we want to add a model for).
		// Note that the order here is important...
		final SwiPrologMentalBase percepts = new SwiPrologMentalBase(this.owner, this.forAgent, BASETYPE.PERCEPTBASE,
				null);
		addBase(percepts, BASETYPE.PERCEPTBASE);
		final SwiPrologGoalBase goals = (SwiPrologGoalBase) createGoalBase(me ? "main" : this.forAgent.toString());
		addGoalBase(goals);
		final SwiPrologMentalBase beliefs = new SwiPrologMentalBase(this.owner, this.forAgent, BASETYPE.BELIEFBASE,
				null);
		addBase(beliefs, BASETYPE.BELIEFBASE);
	}

	@Override
	public GoalBase createGoalBase(final String name) throws MSTDatabaseException {
		return new SwiPrologGoalBase(this.owner, name);
	}

	@Override
	public Translator getTranslator() {
		return new SwiPrologTranslator();
	}

	@Override
	protected Set<Substitution> beliefQuery(final Query query) throws MSTQueryException {
		return getBase(BASETYPE.BELIEFBASE).query(query);
	}

	@Override
	protected Set<Substitution> perceptQuery(final Query query) throws MSTQueryException {
		final Query percept = processPercept((PrologQuery) query);
		return getBase(BASETYPE.PERCEPTBASE).query(percept);
	}

	@Override
	protected Set<Substitution> messageQuery(final Query query, final SentenceMood mood, final List<AgentId> senders)
			throws MSTQueryException {
		if (senders.isEmpty()) {
			final Term from = PrologImplFactory.getVar("_", null);
			final Query message = processMessage((PrologQuery) query, mood, from);
			return getBase(BASETYPE.MESSAGEBASE).query(message);
		} else {
			final Translator translator = getTranslator();
			final Set<Substitution> returned = new LinkedHashSet<>();
			for (final AgentId sender : senders) {
				try {
					final Term from = translator.convert(sender);
					final Query message = processMessage((PrologQuery) query, mood, from);
					returned.addAll(getBase(BASETYPE.MESSAGEBASE).query(message));
				} catch (final MSTTranslationException e) {
					throw new MSTQueryException("unable to translate message sender '" + sender + "' to Prolog.", e);
				}
			}
			return returned;
		}
	}

	@Override
	protected Set<Substitution> messageQuery(final Query query, final SentenceMood mood, final Term var)
			throws MSTQueryException {
		final Query message = processMessage((PrologQuery) query, mood, var);
		return getBase(BASETYPE.MESSAGEBASE).query(message);
	}

	// Helper functions
	// TODO: partial duplication with functions in SwiprologMentalState
	private Query processPercept(final PrologQuery percept) throws MSTQueryException {
		final List<Term> conjuncts = percept.getCompound().getOperands(",");
		final List<Term> percepts = new ArrayList<>(conjuncts.size());
		for (final Term conjunct : conjuncts) {
			if (isBuiltIn(conjunct)) {
				percepts.add(conjunct);
			} else {
				final Term compound = PrologImplFactory.getCompound("percept", new Term[] { conjunct },
						percept.getSourceInfo());
				percepts.add(compound);
			}
		}
		return PrologImplFactory.getQuery(SwiPrologTranslator.termsToConjunct(percepts, percept.getSourceInfo()));
	}

	private Query processMessage(final PrologQuery message, final SentenceMood mood, final Term sender)
			throws MSTQueryException {
		final List<Term> conjuncts = message.getCompound().getOperands(",");
		final List<Term> messages = new ArrayList<>(conjuncts.size());
		for (final Term conjunct : conjuncts) {
			if (isBuiltIn(conjunct)) {
				messages.add(conjunct);
			} else {
				Term content;
				switch (mood) {
				case IMPERATIVE:
					content = PrologImplFactory.getCompound("imp", conjuncts.toArray(new Term[conjuncts.size()]),
							message.getSourceInfo());
					break;
				case INTERROGATIVE:
					content = PrologImplFactory.getCompound("int", conjuncts.toArray(new Term[conjuncts.size()]),
							message.getSourceInfo());
					break;
				default:
					content = conjunct;
					break;
				}
				final PrologCompound compound = PrologImplFactory.getCompound("received",
						new Term[] { sender, content }, message.getSourceInfo());
				messages.add(compound);
			}
		}
		return PrologImplFactory.getQuery(SwiPrologTranslator.termsToConjunct(messages, message.getSourceInfo()));
	}

	private static boolean isBuiltIn(final Term conjunct) {
		final String sig = conjunct.getSignature();
		if (PrologOperators.prologBuiltin(sig)) {
			if (sig.equals("not/1")) {
				final Term content = ((PrologCompound) conjunct).getArg(0);
				return PrologOperators.prologBuiltin(content.getSignature());
			} else {
				return true;
			}
		} else {
			return false;
		}
	}
}
