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

package jasonMentalState;

import jason.asSyntax.Literal;
import jason.asSyntax.LiteralImpl;
import jason.asSyntax.Pred;
import jasonkri.JasonDatabase;
import jasonkri.language.JasonDatabaseFormula;
import jasonkri.language.JasonUpdate;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import krTools.database.Database;
import krTools.exceptions.KRDatabaseException;
import krTools.language.DatabaseFormula;
import krTools.language.Update;
import languageTools.program.agent.AgentId;
import languageTools.program.agent.msg.Message;
import languageTools.program.mas.AgentDefinition;
import languageTools.program.mas.UseClause.UseCase;
import mentalState.BASETYPE;
import mentalState.MentalBase;
import mentalState.MentalModel;
import mentalState.MentalState;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;
import mentalState.translator.Translator;
import eis.iilang.Percept;

/**
 * The knowledge representation (KR) interface with GOAL specific extra
 * functionality.
 */
public class JasonMentalState extends MentalState {
	/**
	 * @see {@link MentalState#MentalState(AgentDefinition, AgentId, AgentRegistry)}
	 */
	public JasonMentalState(AgentDefinition owner, AgentId agentId)
			throws MSTDatabaseException, MSTQueryException {
		super(owner, agentId);
	}

	/**
	 * @see {@link MentalState#MentalState(AgentDefinition, AgentId, AgentRegistry, boolean)}
	 */
	protected JasonMentalState(AgentDefinition owner, AgentId agentId,
			boolean addAgentModel) throws MSTDatabaseException,
			MSTQueryException {
		super(owner, agentId, addAgentModel);
	}

	@Override
	public Translator getTranslator() {
		return new JasonTranslator();
	}

	@Override
	protected MentalModel createMentalModel() {
		return new JasonMentalModel(this);
	}

	@Override
	public void createdDatabase(Database database, BASETYPE type)
			throws MSTDatabaseException {
		JasonDatabase db = (JasonDatabase) database;
		switch (type) {
		case BELIEFBASE:
		case GOALBASE:
			// Manually add the knowledge (of the state owner) to the database
			// that we have just created. The knowledge is only added to the
			// actual contents, and not the base's theory (thus not
			// retrievable/modifiable).
			try {
				db.addAll(getKnowledge());
			} catch (KRDatabaseException | MSTQueryException e) {
				throw new MSTDatabaseException(
						"unable to impose knowledge on SWI database '"
								+ db.getName() + "'.", e);
			}
		default:
			break;
		}
	}

	@Override
	public void insert(Update update, AgentId... agent)
			throws MSTDatabaseException, MSTQueryException {
		// FIXME copied from SWI. Why do we ignore the agent list?
		AgentId id = (agent.length == 0) ? this.agentId : agent[0];
		getModel(id).getBase(BASETYPE.BELIEFBASE).insert(update);
	}

	@Override
	public void insert(DatabaseFormula formula, AgentId... agent)
			throws MSTDatabaseException, MSTQueryException {
		// FIXME copied from SWI. Why do we ignore the agent list?
		AgentId id = (agent.length == 0) ? this.agentId : agent[0];
		getModel(id).getBase(BASETYPE.BELIEFBASE).insert(formula);
	}

	@Override
	public void received(Message message) throws MSTDatabaseException,
			MSTQueryException {
		JasonUpdate update = Converters.messageToUpdate(message);
		getOwnModel().getBase(BASETYPE.MESSAGEBASE).insert(update);
	}

	// FIXME why is GOAL not doing the getModel and getBase calls?
	// we can then remove this interfacing stuff.
	@Override
	public void delete(Update update, AgentId... agent)
			throws MSTDatabaseException, MSTQueryException {
		// FIXME copied from SWI. Why do we ignore the agent list?
		AgentId id = (agent.length == 0) ? this.agentId : agent[0];
		getModel(id).getBase(BASETYPE.BELIEFBASE).delete(update);
	}

	@Override
	public void delete(DatabaseFormula formula, AgentId... agent)
			throws MSTDatabaseException, MSTQueryException {
		AgentId id = (agent.length == 0) ? this.agentId : agent[0];
		getModel(id).getBase(BASETYPE.BELIEFBASE).delete(formula);
	}

	@Override
	public void removeMessage(Message message) throws MSTDatabaseException,
			MSTQueryException {
		Update update = Converters.messageToUpdate(message);
		getOwnModel().getBase(BASETYPE.MESSAGEBASE).delete(update);
	}

	@Override
	public void percept(Percept percept, AgentId... agent)
			throws MSTDatabaseException, MSTQueryException {
		AgentId id = (agent.length == 0) ? this.agentId : agent[0];
		MentalBase base = getModel(id).getBase(BASETYPE.PERCEPTBASE);
		base.insert(Converters.perceptToUpdate(percept));
	}

	@Override
	public void removePercept(Percept percept, AgentId... agent)
			throws MSTDatabaseException, MSTQueryException {
		AgentId id = (agent.length == 0) ? this.agentId : agent[0];
		MentalBase base = getModel(id).getBase(BASETYPE.PERCEPTBASE);
		base.delete(Converters.perceptToUpdate(percept));
	}

	@Override
	public Set<DatabaseFormula> getKnowledge() throws MSTDatabaseException,
			MSTQueryException {
		return getContents((JasonMentalBase) getOwnModel().getBase(
				BASETYPE.KNOWLEDGEBASE));
	}

	@Override
	public Set<DatabaseFormula> getBeliefs(AgentId... agent)
			throws MSTDatabaseException, MSTQueryException {
		AgentId id = (agent.length == 0) ? this.agentId : agent[0];
		return getContents((JasonMentalBase) getModel(id).getBase(
				BASETYPE.BELIEFBASE));
	}

	/**
	 * @param base
	 *            the base to get the data from
	 * @return contents from a JasonMentalBase, as a {@link LinkedHashSet} to
	 *         ensure ordering while using the required {@link Set}.
	 */
	private Set<DatabaseFormula> getContents(JasonMentalBase base) {
		LinkedHashSet<DatabaseFormula> list = new LinkedHashSet<DatabaseFormula>();
		for (LiteralImpl formula : base.getDatabase().getContents()) {
			list.add(new JasonDatabaseFormula(formula, null));
		}
		return list;
	}

	@Override
	public Set<Percept> getPercepts(AgentId... agent)
			throws MSTDatabaseException, MSTQueryException {
		AgentId id = (agent.length == 0) ? this.agentId : agent[0];
		JasonMentalBase base = (JasonMentalBase) getModel(id).getBase(
				BASETYPE.PERCEPTBASE);

		Set<Percept> percepts = new LinkedHashSet<>();
		for (Literal formula : base.getDatabase().getContents()) {
			percepts.add(Converters.predToPercept((Pred) formula));
		}
		return percepts;
	}

	@Override
	public Set<Message> getMessages() throws MSTDatabaseException,
			MSTQueryException {
		JasonMentalBase base = (JasonMentalBase) getOwnModel().getBase(
				BASETYPE.MESSAGEBASE);
		Set<Message> messages = new LinkedHashSet<>();
		for (Literal message : base.getDatabase().getContents()) {
			messages.add(Converters.termToMessage(message));
		}
		return messages;
	}

	/**
	 * Workaround for {@link #getKnowledge()} issue, ensuring we have the proper
	 * order.
	 * 
	 * @return knowledge of this agent, in the order as originally written
	 */
	protected List<DatabaseFormula> getOrderedKnowledge() {
		@SuppressWarnings("unchecked")
		List<DatabaseFormula> items = (List<DatabaseFormula>) getOwner()
				.getItems(UseCase.KNOWLEDGE);
		return (items == null) ? new ArrayList<DatabaseFormula>(0)
				: (List<DatabaseFormula>) items;
	}

}
