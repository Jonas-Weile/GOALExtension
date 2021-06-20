package jasonMentalState;

import eis.iilang.Percept;
import jason.asSyntax.ASSyntax;
import jason.asSyntax.Literal;
import jason.asSyntax.LiteralImpl;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Structure;
import jasonkri.JasonInterface;
import jasonkri.Utils;
import jasonkri.language.JasonDatabaseFormula;
import jasonkri.language.JasonQuery;
import jasonkri.language.JasonUpdate;
import krTools.KRInterface;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Update;
import languageTools.program.agent.AgentId;
import languageTools.program.mas.AgentDefinition;
import mentalState.MentalState;
import mentalState.MentalStateTest;

public class JasonMentalStateTest extends MentalStateTest {
	@Override
	protected KRInterface getKRI() throws Exception {
		JasonInterface interf = new JasonInterface();
		interf.initialize(null);
		return interf;
	}

	@Override
	protected MentalState getMentalState(AgentDefinition owner, AgentId agentId, boolean addAgentModel)
			throws Exception {
		return new JasonMentalState(owner, agentId, addAgentModel);
	}

	@Override
	protected DatabaseFormula getDBFormula(String content) throws Exception {
		return new JasonDatabaseFormula(new LiteralImpl(content), null);
	}

	@Override
	protected Update getUpdate(String content) throws Exception {
		LiteralImpl pred = (LiteralImpl) ASSyntax.parseLiteral(content);
		return new JasonUpdate(pred, null);
	}

	@Override
	protected Query getQuery(String content, int... args) throws Exception {
		Literal literal = new LiteralImpl(content);
		for (int arg : args) {
			literal.addTerm(new NumberTermImpl(arg));
		}
		return new JasonQuery(literal, null);
	}

	@Override
	protected Percept getPercept(Query query) throws Exception {
		Structure compound = (Structure) ((JasonQuery) query).getJasonTerm();
		return new Percept(compound.getFunctor(), Converters.termToParameter(compound.getTerm(0)));
	}

	@Override
	protected DatabaseFormula getMessage(String sender, String content) throws Exception {
		LiteralImpl t = Utils.createPred("received", new LiteralImpl(sender), new LiteralImpl(content));
		return new JasonDatabaseFormula(t, null);
	}
}
