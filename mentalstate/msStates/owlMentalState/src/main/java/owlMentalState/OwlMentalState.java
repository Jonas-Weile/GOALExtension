package owlMentalState;

import java.util.HashSet;
import java.util.Set;

import krTools.database.Database;
import krTools.exceptions.KRDatabaseException;
import krTools.language.DatabaseFormula;
import krTools.language.Update;
import languageTools.program.agent.AgentId;
import languageTools.program.agent.msg.Message;
import languageTools.program.mas.AgentDefinition;
import mentalState.BASETYPE;
import mentalState.MentalBase;
import mentalState.MentalModel;
import mentalState.MentalState;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;
import mentalState.error.MSTTranslationException;
import mentalState.translator.Translator;
import owlMentalState.translator.OwlEISTranslator;
import owlrepo.OWLRepoKRInterface;
import eis.iilang.Percept;

public class OwlMentalState extends MentalState {

	public OwlMentalState(AgentDefinition agent, AgentId agentId) throws MSTDatabaseException, MSTQueryException {
		super(agent, agentId);
	}

	public OwlMentalState(AgentDefinition agent, AgentId agentId, boolean addAgentDefinition)
			throws MSTDatabaseException, MSTQueryException {
		super(agent, agentId, addAgentDefinition);
	}

	@Override
	public Translator getTranslator() {
		OwlMentalBase base = (OwlMentalBase) getOwnModel().getBase(BASETYPE.KNOWLEDGEBASE);
		return base.getTranslator();
	}

	@Override
	protected MentalModel createMentalModel() {
		return new OwlMentalModel(this);
	}

	@Override
	public Set<DatabaseFormula> getKnowledge() throws MSTDatabaseException, MSTQueryException {
		OwlMentalBase base = (OwlMentalBase) getOwnModel().getBase(BASETYPE.KNOWLEDGEBASE);
		return base.getAllDBFormulas();
	}

	@Override
	public Set<DatabaseFormula> getBeliefs(AgentId... agent) throws MSTDatabaseException, MSTQueryException {
		AgentId id = (agent.length == 0) ? this.agentId : agent[0];
		OwlMentalBase base = (OwlMentalBase) getModel(id).getBase(BASETYPE.BELIEFBASE);

		return base.getAllDBFormulas();
	}

	@Override
	public void insert(Update update, AgentId... agent) throws MSTDatabaseException, MSTQueryException {
		AgentId id = (agent.length == 0) ? this.agentId : agent[0];
		// System.out.println("Inserting update in the BELIEFBASE of "+id+":
		// "+update);
		getModel(id).getBase(BASETYPE.BELIEFBASE).insert(update);
	}

	@Override
	public void insert(DatabaseFormula formula, AgentId... agent) throws MSTDatabaseException, MSTQueryException {
		AgentId id = (agent.length == 0) ? this.agentId : agent[0];
		// System.out.println("Inserting dbf in the BELIEFBASE of: " + id + ", "
		// + formula);
		getModel(id).getBase(BASETYPE.BELIEFBASE).insert(formula);
	}

	@Override
	public void received(Message message) throws MSTDatabaseException, MSTQueryException {
		// convert message to axioms to insert into message base of this agent
		try {
			// System.out.println("Inserting in the MESSAGEBASE of "+this.agentId+": "+message);
			OwlMessageBase messageBase = (OwlMessageBase) getOwnModel().getBase(BASETYPE.MESSAGEBASE);
			DatabaseFormula messageDbf = messageBase.messageToFormulas(message);
			messageBase.insert(messageDbf);
		} catch (KRDatabaseException e) {
			throw new MSTDatabaseException("Could not insert received message " + message);
		}
	}

	@Override
	public void createdDatabase(Database database, BASETYPE type) throws MSTDatabaseException {
		// we have nothing to do here,
		// since the KRI is already initialized with an ontology (must)
		// hence it has already the basic knowledge = vocabulary of terms
	}

	@Override
	public void delete(Update update, AgentId... agent) throws MSTDatabaseException, MSTQueryException {
		AgentId id = (agent.length == 0) ? this.agentId : agent[0];
		getModel(id).getBase(BASETYPE.BELIEFBASE).delete(update);
	}

	@Override
	public void delete(DatabaseFormula formula, AgentId... agent) throws MSTDatabaseException, MSTQueryException {
		AgentId id = (agent.length == 0) ? this.agentId : agent[0];
		getModel(id).getBase(BASETYPE.BELIEFBASE).delete(formula);
	}

	@Override
	public void removeMessage(Message message) throws MSTDatabaseException, MSTQueryException {
		// convert message to axioms to delete from message base of this agent
		try {
			// System.out.println("Deleting from the MESSAGEBASE of "+this.agentId+": "+message);
			OwlMessageBase messageBase = (OwlMessageBase) getOwnModel().getBase(BASETYPE.MESSAGEBASE);
			DatabaseFormula messageDbf = messageBase.messageToFormulas(message);
			messageBase.delete(messageDbf);
		} catch (Exception e) {
			e.printStackTrace();
			throw new MSTDatabaseException("Could not remove message " + message);
		}
	}

	@Override
	public Set<Percept> getPercepts(AgentId... agent)
			throws MSTDatabaseException, MSTQueryException {
		Set<Percept> percepts = new HashSet<Percept>();

		AgentId id = (agent.length == 0) ? this.agentId : agent[0];
		OwlMentalBase base = (OwlMentalBase) getModel(id).getBase(BASETYPE.PERCEPTBASE);
		for (DatabaseFormula dbf : base.getAllDBFormulas()) {
			try {
				percepts.add(((OwlEISTranslator) getTranslator()).convert(dbf));
			} catch (MSTTranslationException e) {
				throw new MSTDatabaseException(e.getMessage(), e.getCause());
			}
		}
		return percepts;
	}

	@Override
	public Set<Message> getMessages() throws MSTDatabaseException,
			MSTQueryException {
		OwlEISTranslator trans = (OwlEISTranslator) getTranslator();
		OwlMessageBase base = (OwlMessageBase) getOwnModel().getBase(BASETYPE.MESSAGEBASE);
		Set<Message> messages = new HashSet<Message>();
		for (DatabaseFormula dbf : base.getMessageFormulas()) {
			// System.out.println(dbf);
			messages.add(trans.convertToMessage(dbf, this.agentId));
		}
		return messages;
	}

	@Override
	public void percept(Percept percept, AgentId... agent) throws MSTDatabaseException, MSTQueryException {
		AgentId id = (agent.length == 0) ? this.agentId : agent[0];
		MentalBase base = getModel(id).getBase(BASETYPE.PERCEPTBASE);
		try {
			Update update = getTranslator().convert(percept);
			// System.out.println("Inserting into PerceptBase: " + update +
			// " of " + id);
			base.insert(update);
		} catch (MSTTranslationException e) {
			e.printStackTrace();
			throw new MSTQueryException(e.getMessage());
		}
	}

	@Override
	public void removePercept(Percept percept, AgentId... agent) throws MSTDatabaseException, MSTQueryException {
		AgentId id = (agent.length == 0) ? this.agentId : agent[0];
		MentalBase base = getModel(id).getBase(BASETYPE.PERCEPTBASE);
		try {
			Update update = getTranslator().convert(percept);
			// System.out.println("Deleting from PerceptBase: " + update + "
			// of " + id);
			base.delete(update);
		} catch (MSTTranslationException e) {
			e.printStackTrace();
			throw new MSTQueryException(e.getMessage());
		}
	}

	@Override
	public void cleanUp() throws MSTDatabaseException, MSTQueryException {
		super.cleanUp();
		try {
			((OWLRepoKRInterface) (this.owner.getKRInterface())).release();
		} catch (KRDatabaseException e) {
			e.printStackTrace();
			throw new MSTDatabaseException("Could not shut down Mental state of: " + this.agentId);
		}
	}
}
