
package tuPrologMentalState;

import eis.iilang.Percept;
import krTools.KRInterface;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Update;
import languageTools.program.agent.AgentId;
import languageTools.program.mas.AgentDefinition;
import mentalState.MentalState;
import mentalState.MentalStateTest;
import tuprolog.TuPrologInterface;
import tuprolog.language.JPLUtils;
import tuprolog.language.PrologDBFormula;
import tuprolog.language.PrologQuery;
import tuprolog.language.PrologTerm;
import tuprolog.language.PrologUpdate;

public class TuPrologMentalStateTest extends MentalStateTest {
	@Override
	protected KRInterface getKRI() throws Exception {
		return new TuPrologInterface();
	}

	@Override
	protected MentalState getMentalState(AgentDefinition owner, AgentId agentId, boolean addAgentModel)
			throws Exception {
		return new TuPrologMentalState(owner, agentId, addAgentModel);
	}

	@Override
	protected DatabaseFormula getDBFormula(String content) throws Exception {
		return new PrologDBFormula(new alice.tuprolog.Struct(content), null);
	}

	@Override
	protected Update getUpdate(String content) throws Exception {
		return new PrologUpdate(new alice.tuprolog.Struct(content), null);
	}

	@Override
	protected Query getQuery(String content, int... args) throws Exception {
		alice.tuprolog.Term[] terms = new alice.tuprolog.Term[args.length];
		for (int i = 0; i < args.length; i++) {
			terms[i] = new alice.tuprolog.Int(args[i]);
		}
		return new PrologQuery(new alice.tuprolog.Struct(content, terms), null);
	}

	@Override
	protected Percept getPercept(Query query) throws Exception {
		alice.tuprolog.Struct compound = (alice.tuprolog.Struct) ((PrologQuery) query).getTerm();
		return new Percept(compound.getName(),
				this.mentalState.getTranslator().convert(new PrologTerm(compound.getArg(0), null)));
	}

	@Override
	protected DatabaseFormula getMessage(String sender, String content) throws Exception {
		alice.tuprolog.Term senderTerm = new alice.tuprolog.Struct(sender);
		alice.tuprolog.Term contentTerm = new alice.tuprolog.Struct(content);
		return new PrologDBFormula(JPLUtils.createCompound("received", senderTerm, contentTerm), null);
	}
}
