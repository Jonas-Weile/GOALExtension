package owlMentalState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;
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

import org.semanticweb.owlapi.model.SWRLLiteralArgument;

import owlrepo.language.SWRLQuery;
import owlrepo.language.SWRLSubstitution;
import owlrepo.language.SWRLTerm;
import owlrepo.language.SWRLVar;

public class OwlMentalModel extends MentalModel {

	protected OwlMentalModel(MentalState owner) {
		super(owner);
	}

	@Override
	protected void initialize(AgentId agent) throws MSTDatabaseException, MSTQueryException {
		// Check if the agent model we want to add is for the agent that owns
		// this database. In that case, bases that we can never use selectors on
		// are created.
	//	System.out.println("Initializing agent: " + agent.getName());
		if (this.owner.getAgentId().equals(agent)) {
			// Create a knowledge base and add the agent's knowledge to it
			MentalBase knowledge = new OwlMentalBase(this.owner, agent, BASETYPE.KNOWLEDGEBASE);
			List<?> items = this.owner.getOwner().getItems(UseCase.KNOWLEDGE);
			List<DatabaseFormula> dbfs = (items == null) ? new ArrayList<DatabaseFormula>(0)
					: (List<DatabaseFormula>) items;
			for (DatabaseFormula dbf : dbfs) {
				knowledge.insert(dbf);
			}
			addBase(knowledge, BASETYPE.KNOWLEDGEBASE);

		}

		// Create the belief, message, and goal bases for which selectors are
		// applicable (i.e. apply to any agent we want to add a model for).
		OwlMentalBase beliefs = new OwlMentalBase(this.owner, agent, BASETYPE.BELIEFBASE);
		addBase(beliefs, BASETYPE.BELIEFBASE);

		// take message base if already exists, use it as own
		OwlMentalBase messages = new OwlMessageBase(this.owner, agent, BASETYPE.MESSAGEBASE);
		addBase(messages, BASETYPE.MESSAGEBASE);

		// Create a percept base
		OwlMentalBase percepts = new OwlMentalBase(this.owner, agent, BASETYPE.PERCEPTBASE);
		addBase(percepts, BASETYPE.PERCEPTBASE);

		// create single goal base main
		OwlGoalBase goals = (OwlGoalBase) createGoalBase("main");
		addGoalBase(goals);
	}

	@Override
	public GoalBase createGoalBase(String name) throws MSTDatabaseException {
		return new OwlGoalBase(this.owner, name);
	}

	@Override
	protected Set<Substitution> beliefQuery(Query query) throws MSTQueryException {
//		System.out.println("\nQuerying belief: " + query + " of: "
//				+ this.owner.getAgentId());
		return getBase(BASETYPE.BELIEFBASE).query(query);
	}

	@Override
	protected Set<Substitution> perceptQuery(Query query) throws MSTQueryException {
//		System.out.println("\nQuerying percept: " + query + " of: "
//				+ this.owner.getAgentId());
		return getBase(BASETYPE.PERCEPTBASE).query(query);
	}

	private Set<Substitution> messageQuerySender(Query query, SentenceMood mood,
			AgentId sender) throws MSTQueryException {
//		System.out.println("\nQuerying message: " + query + " of: "
//				+ this.owner.getAgentId());
		Set<Substitution> substitutions = new HashSet<Substitution>();

		// construct a query to get all messages sent by sender to me with mood
		OwlMessageBase msgBase = ((OwlMessageBase) getBase(BASETYPE.MESSAGEBASE));
		String messageQuery = msgBase.messageToContentQueryString(query, mood, sender);
		Set<Substitution> msgResult = msgBase.query(messageQuery);

		Set<String> contents = new HashSet<String>();
		// extract content string from resulting substitution
		Set<SWRLTerm> contentTerms = new HashSet<SWRLTerm>();
		for (Substitution subs : msgResult) {
			for (Var var : subs.getVariables()) {
				// we got content
				SWRLTerm cont = (SWRLTerm) subs.get(var);
				// process content literal argument back to full term
				contentTerms.add(cont);

				// create real object from string content - literal argument
				// " "^^xsd:string
				SWRLLiteralArgument literal = (SWRLLiteralArgument) cont.getArgument();
				contents.add(literal.getLiteral().getLiteral());
			}
		}

		// System.out.println("unify content with: " + query.toString());
		// unify with current query content
		if (query instanceof SWRLQuery) {
			SWRLQuery query2 = (SWRLQuery) query;
			if (query2.getFreeVar().isEmpty()) {
				// ground query content, we can compare by the string
				if (contents.contains(query2.toString())) {
					// return empty substitution
					// System.out.println("MGU:: empty");
					substitutions.add(new SWRLSubstitution());
				} // if there is no such content, the message is really missing,
					// return no subst
			} else {
				// original content query has variables
				// we need to unify with the got contents - but they are string
				// literals
				for (String content : contents) {
					SWRLQuery contentQ = ((OwlMentalBase) getBase(BASETYPE.KNOWLEDGEBASE)).getContentTermQuery(content);
					Substitution mgu = query2.mgu(contentQ);
					// for (Var v : mgu.getVariables())
					// System.out.println("MGU::" + v + " / " + mgu.get(v));
					substitutions.add(mgu);
				}
			}
		}
		// return mgu substitutions
		return substitutions;
	}

	private Set<Substitution> messageQueryNoSender(Query query, SentenceMood mood,
 Term sendervar) throws MSTQueryException {
		// System.out.println("\nQuerying message: " + query + " of: "
		// + this.owner.getAgentId());
		Set<Substitution> substitutions = new HashSet<Substitution>();

		// construct a query to get all messages sent by anybody to me with mood
		OwlMessageBase msgBase = ((OwlMessageBase) getBase(BASETYPE.MESSAGEBASE));
		// get sender variable
		SWRLVar senderv = ((SWRLTerm) sendervar).getVariable();
		String messageQuery = msgBase.messageToContentQueryStringAnySender(
				query, mood, senderv);
		Set<Substitution> msgResult = msgBase.query(messageQuery);

		Set<String> contents = new HashSet<String>();
		// extract content string from resulting substitution
		Set<SWRLTerm> contentTerms = new HashSet<SWRLTerm>();
		for (Substitution subs : msgResult) {
			for (Var var : subs.getVariables()) {
				if (var.toString().equals("c")) {
				// we got content
				SWRLTerm cont = (SWRLTerm) subs.get(var);
				// process content literal argument back to full term
				contentTerms.add(cont);

				// create real object from string content - literal argument
				// " "^^xsd:string
				SWRLLiteralArgument literal = (SWRLLiteralArgument) cont.getArgument();
				contents.add(literal.getLiteral().getLiteral());

				} else { // sender matched
				//	System.out.println("Sender matched: " + subs);
					substitutions.add(subs);
				}
			}
		}

		// System.out.println("unify content with: " + query.toString());
		// unify with current query content
		if (query instanceof SWRLQuery) {
			SWRLQuery query2 = (SWRLQuery) query;
			if (query2.getFreeVar().isEmpty()) {
				// ground query content, we can compare by the string
				if (contents.contains(query2.toString())) {
					// return empty substitution
					// System.out.println("MGU:: empty");
					substitutions.add(new SWRLSubstitution());
				} // if there is no such content, the message is really missing,
					// return no subst
			} else {
				// original content query has variables
				// we need to unify with the got contents - but they are string
				// literals
				for (String content : contents) {
					SWRLQuery contentQ = ((OwlMentalBase) getBase(BASETYPE.KNOWLEDGEBASE)).getContentTermQuery(content);
					Substitution mgu = query2.mgu(contentQ);
					// for (Var v : mgu.getVariables())
					// System.out.println("MGU::" + v + " / " + mgu.get(v));
					substitutions.add(mgu);
				}
			}
		}
		// return mgu substitutions
		return substitutions;
	}
	
	@Override
	protected Set<Substitution> messageQuery(Query query, SentenceMood mood,
			List<AgentId> senders) throws MSTQueryException {
		Set<Substitution> substitutions = new HashSet<Substitution>();
		// return list of agent ids = match senders
		// senderlist.sent(thequery)
		for (AgentId id : senders) {
			// for each senders query sender.sent(thequery)?
			substitutions.addAll(messageQuerySender(query, mood, id));
		}
		return substitutions;
	}

	@Override
	protected Set<Substitution> messageQuery(Query query, SentenceMood mood,
			Term var) throws MSTQueryException {
		// var.sent(something)
		// var = any sender is allowed and should be unified with this variable
		// include var/sender substitutions in return
		
		return messageQueryNoSender(query, mood, var);
		
	}

}
