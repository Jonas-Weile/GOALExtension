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

package tuPrologMentalState;

import java.util.LinkedHashSet;
import java.util.Set;

import eis.iilang.Percept;
import krTools.database.Database;
import krTools.exceptions.KRDatabaseException;
import krTools.exceptions.KRQueryFailedException;
import krTools.language.DatabaseFormula;
import krTools.language.Update;
import languageTools.program.agent.AgentId;
import languageTools.program.agent.msg.Message;
import languageTools.program.agent.msg.SentenceMood;
import languageTools.program.mas.AgentDefinition;
import mentalState.BASETYPE;
import mentalState.MentalBase;
import mentalState.MentalModel;
import mentalState.MentalState;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;
import mentalState.error.MSTTranslationException;
import mentalState.translator.Translator;
import tuPrologMentalState.translator.TuPrologTranslator;
import tuprolog.database.PrologDatabase;
import tuprolog.language.JPLUtils;
import tuprolog.language.PrologDBFormula;
import tuprolog.language.PrologQuery;
import tuprolog.language.PrologTerm;
import tuprolog.language.PrologUpdate;

/**
 * The knowledge representation (KR) interface with GOAL specific extra
 * functionality.
 */
public class TuPrologMentalState extends MentalState {
	/**
	 * @see {@link MentalState#MentalState(AgentDefinition, AgentId, AgentRegistry)}
	 */
	public TuPrologMentalState(AgentDefinition owner, AgentId agentId) throws MSTDatabaseException, MSTQueryException {
		super(owner, agentId);
	}

	/**
	 * @see {@link MentalState#MentalState(AgentDefinition, AgentId, AgentRegistry, boolean)}
	 */
	protected TuPrologMentalState(AgentDefinition owner, AgentId agentId, boolean addAgentModel)
			throws MSTDatabaseException, MSTQueryException {
		super(owner, agentId, addAgentModel);
	}

	@Override
	public Translator getTranslator() {
		return new TuPrologTranslator();
	}

	@Override
	protected MentalModel createMentalModel() {
		return new TuPrologMentalModel(this);
	}

	@Override
	public void createdDatabase(Database database, BASETYPE type) throws MSTDatabaseException {
		PrologDatabase db = (PrologDatabase) database;
		switch (type) {
		case BELIEFBASE:
		case GOALBASE:
			// Manually add the knowledge (of the state owner) to the database
			// that we have just created. The knowledge is only added to the
			// actual contents, and not the base's theory (thus not
			// retrievable/modifiable).
			try {
				Set<DatabaseFormula> knowledge = new LinkedHashSet<>(getKnowledge());
				knowledge.add(new PrologDBFormula(
						JPLUtils.createCompound("me", new alice.tuprolog.Struct(this.agentId.getName())), null));
				db.addKnowledge(knowledge);
				break;
			} catch (KRDatabaseException | MSTQueryException e) {
				throw new MSTDatabaseException("unable to impose knowledge on TU database '" + db.getName() + "'.", e);
			}
		case PERCEPTBASE:
			try {
				db.query(new PrologQuery(JPLUtils.createCompound("dynamic",
						JPLUtils.createCompound("/", new alice.tuprolog.Struct("percept"), new alice.tuprolog.Int(1))),
						null));
				break;
			} catch (KRQueryFailedException e) {
				throw new MSTDatabaseException("unable to declare percept/1 in TU database '" + db.getName() + "'.", e);
			}
		case MESSAGEBASE:
			try {
				db.query(new PrologQuery(JPLUtils.createCompound("dynamic",
						JPLUtils.createCompound("/", new alice.tuprolog.Struct("received"), new alice.tuprolog.Int(2))),
						null));
				break;
			} catch (KRQueryFailedException e) {
				throw new MSTDatabaseException("unable to declare received/2 in TU database '" + db.getName() + "'.",
						e);
			}
		default:
			break;
		}
		// TODO (sometime): how to know who I am (and who the other agents are)?
	}

	@Override
	public void insert(Update update, AgentId... agent) throws MSTDatabaseException, MSTQueryException {
		AgentId id = (agent.length == 0) ? this.agentId : agent[0];
		getModel(id).getBase(BASETYPE.BELIEFBASE).insert(update);
	}

	@Override
	public void insert(DatabaseFormula formula, AgentId... agent) throws MSTDatabaseException, MSTQueryException {
		AgentId id = (agent.length == 0) ? this.agentId : agent[0];
		getModel(id).getBase(BASETYPE.BELIEFBASE).insert(formula);
	}

	@Override
	public void received(Message message) throws MSTDatabaseException, MSTQueryException {
		Update update = messageToUpdate(message);
		// TODO: in the future we would want to support mental models here
		getOwnModel().getBase(BASETYPE.MESSAGEBASE).insert(update);
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
		Update update = messageToUpdate(message);
		// TODO: in the future we would want to support mental models here
		getOwnModel().getBase(BASETYPE.MESSAGEBASE).delete(update);
	}

	@Override
	public void percept(Percept percept, AgentId... agent) throws MSTDatabaseException, MSTQueryException {
		AgentId id = (agent.length == 0) ? this.agentId : agent[0];
		MentalBase base = getModel(id).getBase(BASETYPE.PERCEPTBASE);
		Update update = perceptToUpdate(percept);
		base.insert(update);
	}

	@Override
	public void removePercept(Percept percept, AgentId... agent) throws MSTDatabaseException, MSTQueryException {
		AgentId id = (agent.length == 0) ? this.agentId : agent[0];
		MentalBase base = getModel(id).getBase(BASETYPE.PERCEPTBASE);
		base.delete(perceptToUpdate(percept));
	}

	@Override
	public Set<DatabaseFormula> getKnowledge() throws MSTDatabaseException, MSTQueryException {
		TuPrologMentalBase base = (TuPrologMentalBase) getOwnModel().getBase(BASETYPE.KNOWLEDGEBASE);
		return base.getDatabase().getTheory().getFormulas();
	}

	@Override
	public Set<DatabaseFormula> getBeliefs(AgentId... agent) throws MSTDatabaseException, MSTQueryException {
		AgentId id = (agent.length == 0) ? this.agentId : agent[0];
		TuPrologMentalBase base = (TuPrologMentalBase) getModel(id).getBase(BASETYPE.BELIEFBASE);
		return base.getDatabase().getTheory().getFormulas();
	}

	@Override
	public Set<Percept> getPercepts(AgentId... agent) throws MSTDatabaseException, MSTQueryException {
		AgentId id = (agent.length == 0) ? this.agentId : agent[0];
		TuPrologMentalBase base = (TuPrologMentalBase) getModel(id).getBase(BASETYPE.PERCEPTBASE);
		Set<Percept> returned = new LinkedHashSet<>();
		for (DatabaseFormula percept : base.getDatabase().getTheory().getFormulas()) {
			returned.add(updateToPercept(percept));
		}
		return returned;
	}

	@Override
	public Set<Message> getMessages() throws MSTDatabaseException, MSTQueryException {
		TuPrologMentalBase base = (TuPrologMentalBase) getOwnModel().getBase(BASETYPE.MESSAGEBASE);
		Set<Message> returned = new LinkedHashSet<>();
		for (DatabaseFormula message : base.getDatabase().getTheory().getFormulas()) {
			returned.add(updateToMessage(message));
		}
		return returned;
	}

	// Helper functions
	private Update perceptToUpdate(Percept percept) throws MSTQueryException {
		try {
			PrologUpdate update = (PrologUpdate) getTranslator().convert(percept);
			return new PrologUpdate(JPLUtils.createCompound("percept", update.getTerm()), null);
		} catch (MSTTranslationException e) {
			throw new MSTQueryException("unable to process EIS percept '" + percept + "'.", e);
		}
	}

	private Percept updateToPercept(DatabaseFormula update) throws MSTQueryException {
		try {
			alice.tuprolog.Term content = ((PrologDBFormula) update).getTerm();
			if (content instanceof alice.tuprolog.Struct) {
				PrologUpdate percept = new PrologUpdate(((alice.tuprolog.Struct) content).getArg(0), null);
				return getTranslator().convert(percept);
			} else {
				throw new MSTQueryException("'" + content + "' is not a struct.");
			}
		} catch (MSTTranslationException e) {
			throw new MSTQueryException("unable to process '" + update + "' into a percept.", e);
		}
	}

	private Update messageToUpdate(Message message) throws MSTQueryException {
		try {
			alice.tuprolog.Term content = ((PrologUpdate) message.getContent()).getTerm();
			switch (message.getMood()) {
			case IMPERATIVE:
				content = JPLUtils.createCompound("imp", content);
				break;
			case INTERROGATIVE:
				content = JPLUtils.createCompound("int", content);
				break;
			default:
			}
			alice.tuprolog.Term sender = ((PrologTerm) getTranslator().convert(message.getSender())).getTerm();
			return new PrologUpdate(JPLUtils.createCompound("received", sender, content),
					message.getContent().getSourceInfo());
		} catch (MSTTranslationException e) {
			throw new MSTQueryException("unable to process message '" + message + "'.", e);
		}
	}

	private Message updateToMessage(DatabaseFormula update) throws MSTQueryException {
		alice.tuprolog.Term message = ((PrologDBFormula) update).getTerm();
		if (!(message instanceof alice.tuprolog.Struct)) {
			throw new MSTQueryException("'" + message + "' is not a struct.");
		}
		alice.tuprolog.Term sender = ((alice.tuprolog.Struct) message).getArg(0);
		alice.tuprolog.Term content = ((alice.tuprolog.Struct) message).getArg(1);

		SentenceMood mood = SentenceMood.INDICATIVE;
		if (content instanceof alice.tuprolog.Struct && ((alice.tuprolog.Struct) content).getArity() == 1) {
			switch (((alice.tuprolog.Struct) content).getName()) {
			case "imp":
				mood = SentenceMood.IMPERATIVE;
				content = ((alice.tuprolog.Struct) content).getArg(0);
				break;
			case "int":
				mood = SentenceMood.INDICATIVE;
				content = ((alice.tuprolog.Struct) content).getArg(0);
				break;
			default:
				break;
			}
		}

		Message returned = new Message(new PrologUpdate(content, null), mood);
		returned.setSender(new AgentId(JPLUtils.toString(sender)));
		return returned;
	}
}
