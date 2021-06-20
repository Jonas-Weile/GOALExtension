package owlMentalState;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import krTools.exceptions.KRDatabaseException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Update;
import krTools.language.Var;
import languageTools.program.agent.AgentId;
import languageTools.program.agent.msg.Message;
import languageTools.program.agent.msg.SentenceMood;
import mentalState.BASETYPE;
import mentalState.MentalState;
import mentalState.error.MSTDatabaseException;

import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.SWRLAtom;
import org.semanticweb.owlapi.model.SWRLDArgument;
import org.semanticweb.owlapi.model.SWRLIndividualArgument;
import org.semanticweb.owlapi.model.SWRLRule;

import owlrepo.language.SWRLDatabaseFormula;
import uk.ac.manchester.cs.owl.owlapi.OWLDataFactoryImpl;

public class OwlMessageBase extends OwlMentalBase {

	OWLClass msgClass, agentClass, owlNamedIndiv;
	OWLDataProperty contentProperty, moodProperty;
	OWLObjectProperty sentBy, recievedBy;
	private OWLDataFactory owlFactory = new OWLDataFactoryImpl();
	private String goalBaseURI = "http://ii.tudelft.nl/goal#";
	private int msgNr = 1;
	private Map<String, String> msgMap = new HashMap<String, String>();

	protected OwlMessageBase(MentalState owner, AgentId agentMM, BASETYPE type) throws MSTDatabaseException {
		super(owner, agentMM, type);
		initMessage();
	}

	public Set<DatabaseFormula> getMessageFormulas() {
		Set<DatabaseFormula> dbfs = new HashSet<DatabaseFormula>();
		// System.out.println("\nQuerying all messages from "+ id);

		Set<Statement> sts = this.database.getAllStatements(id);
		List<Statement> stlist = new LinkedList<Statement>();

		do{
			stlist.clear();
			Resource subj = null;
			for (Statement st : sts) {
				if (subj == null)
					subj = st.getSubject();
				if (st.getObject().stringValue().equals(this.agentClass.toStringID())
						|| st.getObject().stringValue().equals(this.owlNamedIndiv.toStringID()))
					continue;
				if (st.getSubject().equals(subj))
					stlist.add(st);
			}
			if (!stlist.isEmpty()) {
				DatabaseFormula dbf = this.database.statementsToRuleFormula(stlist);
				dbfs.add(dbf);
			}
			sts.removeAll(stlist);
		} while (!stlist.isEmpty());
//			// // TODO: proper turn content literal argument into dbf
//			//FILTER OUT ONLY CONTENT DBFS, SO THE COUNT OF MESSAGES MATCHES
//			//WILL NOT MATCH CONTENT OR MOOD OF MSGS
//			//NNED TO CREATE PROPER RULES OF COLLECTiON OF DBFS RELATED TO ONE MSG INSTANCE
//			if (form.toString().startsWith(":content")) {
//				dbfs.add(form);
//			} else if (!(form.toString().startsWith(":Message") || form.toString().startsWith(":mood")
//					|| form.toString().startsWith(":Agent") || form.toString().startsWith(":sentBy")
//					|| form.toString().startsWith(":receivedBy") || form.toString().startsWith("owl:"))) {
//				dbfs.add(form);
//			}
//		}
		return dbfs;
	}

	public String getGoalBaseURI() {
		return this.goalBaseURI;
	}

	private void initMessage() {
		this.owlNamedIndiv = this.owlFactory
				.getOWLClass(IRI.create("http://www.w3.org/2002/07/owl#" + "NamedIndividual"));
		this.msgClass = this.owlFactory.getOWLClass(IRI.create(this.goalBaseURI + "Message"));
		this.agentClass = this.owlFactory.getOWLClass(IRI.create(this.goalBaseURI + "Agent"));
		this.contentProperty = this.owlFactory.getOWLDataProperty(IRI.create(this.goalBaseURI + "content"));
		this.moodProperty = this.owlFactory.getOWLDataProperty(IRI.create(this.goalBaseURI + "mood"));
		this.sentBy = this.owlFactory.getOWLObjectProperty(IRI.create(this.goalBaseURI + "sentBy"));
		this.recievedBy = this.owlFactory.getOWLObjectProperty(IRI.create(this.goalBaseURI + "receivedBy"));
	}

	public DatabaseFormula messageToFormulas(Message message) throws KRDatabaseException {
		Set<SWRLAtom> body = new HashSet<SWRLAtom>();

		Update content = message.getContent();
		AgentId sender = message.getSender();
		SentenceMood mood = message.getMood();
		Set<AgentId> receivers = message.getReceivers();

		// create message individual
		String msgName = "";
		AgentId agentId = this.owner.getAgentId();

		String msgToString = "received(" + agentId.getName() + "," + message.toString() + ")";
		if (this.msgMap.containsKey(msgToString)) {
			msgName = this.msgMap.get(msgToString);
		} else {
			msgName = "msg_" + sender + this.msgNr;
			this.msgMap.put(msgToString, msgName);
			this.msgNr++;
		}
		OWLIndividual messageIndiv = getIndividual(msgName);
		SWRLIndividualArgument messageArg = this.owlFactory.getSWRLIndividualArgument(messageIndiv);

		body.add(this.owlFactory.getSWRLClassAtom(this.owlNamedIndiv, messageArg));
		// goal:message rdf:type goal:Message
		body.add(this.owlFactory.getSWRLClassAtom(this.msgClass, messageArg));

		// process content
		// if (content instanceof SWRLUpdate) {
		// ((SWRLUpdate) content).getRule();
		// }
		SWRLDArgument contentArg = this.owlFactory
				.getSWRLLiteralArgument(this.owlFactory.getOWLLiteral(content.toString()));
		// goal:message goal:content content
		body.add(this.owlFactory.getSWRLDataPropertyAtom(this.contentProperty, messageArg, contentArg));

		// process mood
		// goal:message goal:mood mood
		SWRLDArgument moodArg = this.owlFactory
				.getSWRLLiteralArgument(this.owlFactory.getOWLLiteral(getMessageMood(mood)));
		body.add(this.owlFactory.getSWRLDataPropertyAtom(this.moodProperty, messageArg, moodArg));

		// process sender
		OWLNamedIndividual agentIndiv = getIndividual(sender.getName());
		// goal:senderAgent rdf:type goal:Agent

		SWRLIndividualArgument agentArg = this.owlFactory.getSWRLIndividualArgument(agentIndiv);
		body.add(this.owlFactory.getSWRLClassAtom(this.owlNamedIndiv, agentArg));

		// goal:senderAgent rdf:type goal:Agent
		body.add(this.owlFactory.getSWRLClassAtom(this.agentClass, agentArg));
		// goal:message goal:sentBy goal:senderAgent
		body.add(this.owlFactory.getSWRLObjectPropertyAtom(this.sentBy, messageArg, agentArg));

		// process recievers
		if (!receivers.contains(agentId)) {
			receivers.add(agentId); // always add myself as receiver
		}
		for (AgentId reciever : receivers) {
			agentIndiv = getIndividual(reciever.getName());
			// goal:receiverAgent rdf:type goal:Agent
			agentArg = this.owlFactory.getSWRLIndividualArgument(agentIndiv);
			// goal:receiverAgent rdf:type goal:Agent
			body.add(this.owlFactory.getSWRLClassAtom(this.owlNamedIndiv, agentArg));

			body.add(this.owlFactory.getSWRLClassAtom(this.agentClass, agentArg));
			// goal:message goal:receivedBy goal:receiverAgent
			body.add(this.owlFactory.getSWRLObjectPropertyAtom(this.recievedBy, messageArg, agentArg));
		}
		SWRLRule rule = this.owlFactory.getSWRLRule(body, new HashSet<SWRLAtom>());
		// System.out.println("MESSAGE:: " + rule);
		return new SWRLDatabaseFormula(rule);
	}

	private OWLNamedIndividual getIndividual(String name) {
		return this.owlFactory.getOWLNamedIndividual(IRI.create(this.goalBaseURI + name));
	}

	private String getMessageMood(SentenceMood mood) {
		switch (mood) {
		case IMPERATIVE:
			return "IMP";
		case INTERROGATIVE:
			return "INT";
		case INDICATIVE:
			return "IND";
		default:
			return "IND";
		}
	}


	/*
	 * public SWRLRule messageToQuery(Query query, SentenceMood mood, AgentId
	 * sender) { AgentId me = getAgentId();
	 *
	 * // goal:Message(?m) ^ goal:Agent(sender) ^ goal:Agent(me) ^ //
	 * goal:sentBy(?m, sender) ^ goal:receivedBy(?m, me) ^ // goal:mood(?m,
	 * mood) ^ goal:content(?m,?c).
	 *
	 * //process content SWRLDArgument content = null; SWRLQuery sq =
	 * ((SWRLQuery)query); if (sq.isVar()){ //we are looking for any content
	 * SWRLVariable var = (SWRLVariable)sq.getArgument(); SWRLVariable varC =
	 * owlFactory.getSWRLVariable(IRI.create(goalBaseURI + "c")); content =
	 * varC; } else { // it's a term or a rule that still might contain
	 * variable(s) // treat it as owl literal string String contentString =
	 * sq.toString(); // contentString =
	 * owlFactory.getOWLLiteral(contentString).; contentString =
	 * contentString.replaceAll("\"", "\\\"");
	 *
	 * System.out.println(contentString); content =
	 * owlFactory.getSWRLLiteralArgument(owlFactory.getOWLLiteral(contentString)
	 * ); }
	 *
	 *
	 * Set<SWRLAtom> body = new HashSet<SWRLAtom>(); // goal:Message(?m)
	 * SWRLVariable varM = owlFactory.getSWRLVariable(IRI.create(goalBaseURI +
	 * "m")); body.add(owlFactory.getSWRLClassAtom(msgClass, varM)); //
	 * goal:Agent(sender) - sender always known at this point
	 * SWRLIndividualArgument senderIndiv = owlFactory
	 * .getSWRLIndividualArgument(getIndividual(sender.getName()));
	 * body.add(owlFactory.getSWRLClassAtom(agentClass, senderIndiv)); //
	 * goal:Agent(me) SWRLIndividualArgument meIndiv = owlFactory
	 * .getSWRLIndividualArgument(getIndividual(me.getName()));
	 * body.add(owlFactory.getSWRLClassAtom(agentClass, meIndiv)); //
	 * goal:sentBy(?m, sender) body.add(owlFactory
	 * .getSWRLObjectPropertyAtom(sentBy, varM, senderIndiv)); //
	 * goal:receivedBy(?m, me) body.add(owlFactory
	 * .getSWRLObjectPropertyAtom(recievedBy, varM, meIndiv)); //
	 * goal:content(?m, content) body.add(owlFactory
	 * .getSWRLDataPropertyAtom(contentProperty, varM, content)); //
	 * goal:mood(?m, mood) String moodstring = getMessageMood(mood);
	 * body.add(owlFactory.getSWRLDataPropertyAtom(moodProperty, varM,
	 * owlFactory.getSWRLLiteralArgument(owlFactory
	 * .getOWLLiteral(moodstring))));
	 *
	 * SWRLRule rule = owlFactory.getSWRLRule(body, new HashSet<SWRLAtom>());
	 * return rule; }
	 */

	public String messageToContentQueryString(Query query, SentenceMood mood, AgentId sender) {
		AgentId me = this.owner.getAgentId();
		String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" + "PREFIX : <http://ii.tudelft.nl/goal#>\n"
				+ "SELECT ?c " + "\nFROM <http://ii.tudelft.nl/goal#" + this.id + ">" + "\nWHERE {"
				+ "?m rdf:type :Message; " + ":content ?c; " + ":sentBy :" + sender.getName() + "; " + ":receivedBy :"
				+ me.getName() + "; " + ":mood \"" + getMessageMood(mood) + "\". }";
		// goal:Message(?m) ^
		// goal:sentBy(?m, sender) ^ goal:receivedBy(?m, me) ^
		// goal:mood(?m, mood) ^ goal:content(?m,?c).

		return queryString;
	}
	
	public String messageToContentQueryStringAnySender(Query query,
			SentenceMood mood, Var sendervar) {
		AgentId me = this.owner.getAgentId();
		System.out.println("Getting msg query : " + query.toString());
		String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" + "PREFIX : <http://ii.tudelft.nl/goal#>\n"
				+ "SELECT ?c " + sendervar  
				+ "\nFROM <http://ii.tudelft.nl/goal#" + this.id + ">" 
				+ "\nWHERE {"
				+ "?m rdf:type :Message; "
				+ ":content ?c; " // can we ask :content "query"?
				+ ":sentBy "+ sendervar+ "; "
				+ ":receivedBy :"+ me.getName() + "; " 
				+ ":mood \"" + getMessageMood(mood) + "\". }";
		// goal:Message(?m) ^
		// goal:sentBy(?m, ?sender) ^ goal:receivedBy(?m, me) ^
		// goal:mood(?m, mood) ^ goal:content(?m,?c).

		return queryString;
	}

	public String getMessageContentQueryString() {
		AgentId me = this.owner.getAgentId();
		String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" + "PREFIX : <http://ii.tudelft.nl/goal#>\n"
				+ "SELECT ?c " + "\nFROM <http://ii.tudelft.nl/goal#" + this.id + ">" + "\nWHERE {"
				+ "?m rdf:type :Message; " + ":content ?c; " + ":receivedBy :" + me.getName() + ". }";
		// goal:Message(?m) ^ goal:Agent(sender) ^ goal:Agent(me) ^
		// goal:sentBy(?m, sender) ^ goal:receivedBy(?m, me) ^
		// goal:mood(?m, mood) ^ goal:content(?m,?c).

		return queryString;
	}

	public String getAllMessagesQuery(){
		String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
				"PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" + 
				"PREFIX : <http://ii.tudelft.nl/goal#>\n" +
				"SELECT * "+ "\nFROM <http://ii.tudelft.nl/goal#" + this.id + ">" + "\nWHERE {"+
				"?m a Message; ?p ?o.}";
		return queryString;
	}

	public String messageToQueryString(Query query, SentenceMood mood, AgentId sender) {
		AgentId me = this.owner.getAgentId();
		String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" + "PREFIX : <http://ii.tudelft.nl/goal#>\n"
				+ "SELECT ?m " + "\nFROM <http://ii.tudelft.nl/goal#" + this.id + ">" + "\nWHERE {"
				+ "?m rdf:type :Message; " + ":content " + escapeString(query.toString()) + "; " + ":sentBy :"
				+ sender.getName() + "; " + ":receivedBy :" + me.getName() + "; " + ":mood \"" + getMessageMood(mood)
				+ "\". }";
		// goal:Message(?m) ^ goal:Agent(sender) ^ goal:Agent(me) ^
		// goal:sentBy(?m, sender) ^ goal:receivedBy(?m, me) ^
		// goal:mood(?m, mood) ^ goal:content(?m,?c).

		return queryString;
	}

	private String escapeString(String s) {
		String[] contents = s.split("\"");
		String content = "";
		for (int i = 0; i < contents.length - 1; i++) {
			content += contents[i] + "\\" + "\"";
		}
		content += contents[contents.length - 1];
		return content;
	}

	public String messageToDeleteString(Query query, SentenceMood mood, AgentId sender) {
		AgentId me = this.owner.getAgentId();
		String queryString = "PREFIX rdf: <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"
				+ "PREFIX owl: <http://www.w3.org/2002/07/owl#>\n" + "PREFIX : <http://ii.tudelft.nl/goal#>\n"
				+ "DELETE {?m ?p ?o} " + "\nFROM <" + this.id + ">" + "\nWHERE {" + "?m rdf:type :Message; "
				+ ":content  \"" + escapeString(query.toString()) + "\";" + ":sentBy :" + sender.getName() + "; "
				+ ":receivedBy :" + me.getName() + "; " + ":mood \"" + getMessageMood(mood) + "\". }";
		// goal:Message(?m) ^ goal:Agent(sender) ^ goal:Agent(me) ^
		// goal:sentBy(?m, sender) ^ goal:receivedBy(?m, me) ^
		// goal:mood(?m, mood) ^ goal:content(?m,?c).
		return queryString;
	}

}
