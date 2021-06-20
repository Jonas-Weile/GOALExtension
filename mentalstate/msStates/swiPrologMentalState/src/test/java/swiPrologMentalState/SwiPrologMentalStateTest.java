
package swiPrologMentalState;

import eis.iilang.Percept;
import krTools.KRInterface;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Term;
import krTools.language.Update;
import languageTools.program.agent.AgentId;
import languageTools.program.mas.AgentDefinition;
import mentalState.MentalState;
import mentalState.MentalStateTest;
import swiPrologMentalState.translator.SwiPrologTranslator;
import swiprolog.SwiPrologInterface;
import swiprolog.language.PrologCompound;
import swiprolog.language.PrologQuery;
import swiprolog.language.impl.PrologImplFactory;

public class SwiPrologMentalStateTest extends MentalStateTest {
	@Override
	protected KRInterface getKRI() throws Exception {
		return new SwiPrologInterface();
	}

	@Override
	protected MentalState getMentalState(AgentDefinition owner, AgentId agentId, boolean addAgentModel)
			throws Exception {
		return new SwiPrologMentalState(owner, agentId, addAgentModel);
	}

	@Override
	protected DatabaseFormula getDBFormula(char content) throws Exception {
		return PrologImplFactory.getDBFormula(PrologImplFactory.getAtom(String.valueOf(content), null));
	}

	@Override
	protected Update getUpdate(char content) throws Exception {
		return PrologImplFactory.getUpdate(PrologImplFactory.getAtom(String.valueOf(content), null));
	}

	@Override
	protected Query getQuery(char content, int arg) throws Exception {
		return PrologImplFactory.getQuery(PrologImplFactory.getCompound(String.valueOf(content),
				new Term[] { PrologImplFactory.getNumber(arg, null) }, null));
	}

	@Override
	protected Percept getPercept(Query query) throws Exception {
		PrologCompound compound = ((PrologQuery) query).getCompound();
		return new Percept(compound.getName(), new SwiPrologTranslator().convert(compound.getArg(0)));
	}

	@Override
	protected DatabaseFormula getMessage(char sender, char content) throws Exception {
		Term senderTerm = PrologImplFactory.getAtom(String.valueOf(sender), null);
		Term contentTerm = PrologImplFactory.getAtom(String.valueOf(content), null);
		return PrologImplFactory
				.getDBFormula(PrologImplFactory.getCompound("received", new Term[] { senderTerm, contentTerm }, null));
	}
}
