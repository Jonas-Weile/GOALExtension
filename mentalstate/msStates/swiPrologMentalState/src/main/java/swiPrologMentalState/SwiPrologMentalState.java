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

package swiPrologMentalState;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import eis.iilang.Percept;
import krTools.database.Database;
import krTools.exceptions.KRDatabaseException;
import krTools.exceptions.KRQueryFailedException;
import krTools.language.DatabaseFormula;
import krTools.language.Term;
import krTools.language.Update;
import languageTools.program.agent.AgentId;
import languageTools.program.agent.msg.Message;
import languageTools.program.mas.AgentDefinition;
import mentalState.BASETYPE;
import mentalState.MentalBase;
import mentalState.MentalModel;
import mentalState.MentalState;
import mentalState.Result;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;
import mentalState.error.MSTTranslationException;
import mentalState.translator.Translator;
import swiPrologMentalState.translator.SwiPrologTranslator;
import swiprolog.database.PrologDatabase;
import swiprolog.language.PrologQuery;
import swiprolog.language.impl.PrologImplFactory;

/**
 * The knowledge representation (KR) interface with GOAL specific extra
 * functionality.
 */
public class SwiPrologMentalState extends MentalState {
	private static final PrologQuery declarePercept = PrologImplFactory.getQuery(PrologImplFactory.getCompound(
			"thread_local",
			new Term[] { PrologImplFactory.getCompound("/",
					new Term[] { PrologImplFactory.getAtom("percept", null), PrologImplFactory.getNumber(1, null) },
					null) },
			null));
	private static final PrologQuery declareReceived = PrologImplFactory.getQuery(PrologImplFactory.getCompound(
			"thread_local",
			new Term[] { PrologImplFactory.getCompound("/",
					new Term[] { PrologImplFactory.getAtom("received", null), PrologImplFactory.getNumber(2, null) },
					null) },
			null));
	protected Translator translator = new SwiPrologTranslator();

	/**
	 * @see {@link MentalState#MentalState(AgentDefinition, AgentId, AgentRegistry)}
	 */
	public SwiPrologMentalState(final AgentDefinition owner, final AgentId agentId)
			throws MSTDatabaseException, MSTQueryException {
		super(owner, agentId);
	}

	/**
	 * @see {@link MentalState#MentalState(AgentDefinition, AgentId, AgentRegistry, boolean)}
	 */
	protected SwiPrologMentalState(final AgentDefinition owner, final AgentId agentId, final boolean addAgentModel)
			throws MSTDatabaseException, MSTQueryException {
		super(owner, agentId, addAgentModel);
	}

	@Override
	public Result createResult(final BASETYPE base, final String focus) {
		return new SwiPrologResult(base, focus);
	}

	@Override
	protected MentalModel createMentalModel(final AgentId forAgent) {
		return new SwiPrologMentalModel(this, forAgent);
	}

	@Override
	public void createdDatabase(final Database database, final BASETYPE type) throws MSTDatabaseException {
		final PrologDatabase db = (PrologDatabase) database;
		switch (type) {
		case BELIEFBASE:
		case GOALBASE:
			// In the belief and goal bases, all knowledge is added.
			try {
				db.addKnowledge(getKnowledge());
				break;
			} catch (final KRDatabaseException | MSTQueryException e) {
				throw new MSTDatabaseException("unable to impose knowledge on SWI database '" + db.getName() + "'.", e);
			}
		case PERCEPTBASE:
			// In the perceptbase we declare percept/1,
			// which is actually a bit of a hack to support
			// storing and querying any percept (i.e. wrapping it inside a
			// percept(Percept) predicate every time).
			// No knowledge is added here.
			try {
				db.query(declarePercept);
				break;
			} catch (final KRQueryFailedException e) {
				throw new MSTDatabaseException("unable to declare percept/1 in SWI database '" + db.getName() + "'.",
						e);
			}
		case MESSAGEBASE:
			// In the messagebase we declare received/2,
			// which is actually a bit of a hack to support
			// storing and querying any message along with its sender (i.e.
			// wrapping it inside a received(Sender,Msg) predicate every time).
			// No knowledge is added here.
			try {
				db.query(declareReceived);
				break;
			} catch (final KRQueryFailedException e) {
				throw new MSTDatabaseException("unable to declare received/2 in SWI database '" + db.getName() + "'.",
						e);
			}
		default:
			break;
		}
		// TODO (sometime): how to know who I am (and who the other agents are)?
	}

	@Override
	public List<Result> insert(final Update update, AgentId... agent) throws MSTDatabaseException, MSTQueryException {
		if (agent.length == 0) {
			agent = new AgentId[] { this.agentId };
		}
		final List<Result> results = new ArrayList<>(agent.length);
		for (final AgentId id : agent) {
			final MentalBase base = getModel(id).getBase(BASETYPE.BELIEFBASE);
			results.add(base.insert(update));
		}
		return results;
	}

	@Override
	public Result received(final Message message) throws MSTDatabaseException, MSTQueryException {
		try {
			final DatabaseFormula formula = this.translator.convertMessage(message);
			// TODO: in the future we would want to support mental models here
			return getOwnModel().getBase(BASETYPE.MESSAGEBASE).insert(formula);
		} catch (final MSTTranslationException e) {
			throw new MSTQueryException("unable to process message '" + message + "'.", e);
		}
	}

	@Override
	public List<Result> delete(final Update update, AgentId... agent) throws MSTDatabaseException, MSTQueryException {
		if (agent.length == 0) {
			agent = new AgentId[] { this.agentId };
		}
		final List<Result> results = new ArrayList<>(agent.length);
		for (final AgentId id : agent) {
			final MentalBase base = getModel(id).getBase(BASETYPE.BELIEFBASE);
			results.add(base.delete(update));
		}
		return results;
	}

	@Override
	public Result removeMessage(final Message message) throws MSTDatabaseException, MSTQueryException {
		try {
			final DatabaseFormula formula = this.translator.convertMessage(message);
			// TODO: in the future we would want to support mental models here
			return getOwnModel().getBase(BASETYPE.MESSAGEBASE).delete(formula);
		} catch (final MSTTranslationException e) {
			throw new MSTQueryException("unable to process message '" + message + "'.", e);
		}
	}

	@Override
	public List<Result> percept(final Percept percept, AgentId... agent)
			throws MSTDatabaseException, MSTQueryException {
		if (agent.length == 0) {
			agent = new AgentId[] { this.agentId };
		}
		final List<Result> results = new ArrayList<>(agent.length);
		for (final AgentId id : agent) {
			try {
				final MentalBase base = getModel(id).getBase(BASETYPE.PERCEPTBASE);
				final DatabaseFormula formula = this.translator.convertPercept(percept);
				results.add(base.insert(formula));
			} catch (final MSTTranslationException e) {
				throw new MSTQueryException("unable to process percept '" + percept + "'.", e);
			}
		}
		return results;
	}

	@Override
	public List<Result> removePercept(final Percept percept, AgentId... agent)
			throws MSTDatabaseException, MSTQueryException {
		if (agent.length == 0) {
			agent = new AgentId[] { this.agentId };
		}
		final List<Result> results = new ArrayList<>(agent.length);
		for (final AgentId id : agent) {
			try {
				final MentalBase base = getModel(id).getBase(BASETYPE.PERCEPTBASE);
				final DatabaseFormula formula = this.translator.convertPercept(percept);
				results.add(base.delete(formula));
			} catch (final MSTTranslationException e) {
				throw new MSTQueryException("unable to process percept percept '" + percept + "'.", e);
			}
		}
		return results;
	}

	@Override
	public Set<DatabaseFormula> getKnowledge() throws MSTDatabaseException, MSTQueryException {
		final SwiPrologMentalBase base = (SwiPrologMentalBase) getOwnModel().getBase(BASETYPE.KNOWLEDGEBASE);
		return base.getDatabase().getTheory().getFormulas();
	}

	private Set<File> getKnowledgeFiles() { // TODO: for future implementation
		return getOwner().getAllKnowledgeFiles();
	}

	@Override
	public Set<DatabaseFormula> getBeliefs(final AgentId... agent) throws MSTDatabaseException, MSTQueryException {
		final AgentId id = (agent.length == 0) ? this.agentId : agent[0];
		final SwiPrologMentalBase base = (SwiPrologMentalBase) getModel(id).getBase(BASETYPE.BELIEFBASE);
		return base.getDatabase().getTheory().getFormulas();
	}

	@Override
	public int getBeliefCount() {
		int count = 0;
		for (final MentalModel model : getActiveModels()) {
			final SwiPrologMentalBase base = (SwiPrologMentalBase) model.getBase(BASETYPE.BELIEFBASE);
			count += base.getDatabase().getTheory().getFormulas().size();
		}
		return count;
	}

	@Override
	public Set<Percept> getPercepts(final AgentId... agent) throws MSTDatabaseException, MSTQueryException {
		final AgentId id = (agent.length == 0) ? this.agentId : agent[0];
		final SwiPrologMentalBase base = (SwiPrologMentalBase) getModel(id).getBase(BASETYPE.PERCEPTBASE);
		final Set<DatabaseFormula> formulas = base.getDatabase().getTheory().getFormulas();
		final Set<Percept> returned = new LinkedHashSet<>(formulas.size());
		for (final DatabaseFormula formula : formulas) {
			try {
				final Percept percept = this.translator.convertPercept(formula);
				returned.add(percept);
			} catch (final MSTTranslationException e) {
				throw new MSTQueryException("unable to process percept formula '" + formula + "'.", e);
			}
		}
		return returned;
	}

	@Override
	public int getPerceptCount() {
		int count = 0;
		for (final MentalModel model : getActiveModels()) {
			final SwiPrologMentalBase base = (SwiPrologMentalBase) model.getBase(BASETYPE.PERCEPTBASE);
			count += base.getDatabase().getTheory().getFormulas().size();
		}
		return count;
	}

	@Override
	public Set<Message> getMessages() throws MSTDatabaseException, MSTQueryException {
		final SwiPrologMentalBase base = (SwiPrologMentalBase) getOwnModel().getBase(BASETYPE.MESSAGEBASE);
		final Set<DatabaseFormula> formulas = base.getDatabase().getTheory().getFormulas();
		final Set<Message> returned = new LinkedHashSet<>(formulas.size());
		for (final DatabaseFormula formula : formulas) {
			try {
				final Message message = this.translator.convertMessage(formula);
				returned.add(message);
			} catch (final MSTTranslationException e) {
				throw new MSTQueryException("unable to process message formula '" + formula + "'.", e);
			}
		}
		return returned;
	}

	@Override
	public int getMessageCount() {
		final SwiPrologMentalBase base = (SwiPrologMentalBase) getOwnModel().getBase(BASETYPE.MESSAGEBASE);
		return base.getDatabase().getTheory().getFormulas().size();
	}
}
