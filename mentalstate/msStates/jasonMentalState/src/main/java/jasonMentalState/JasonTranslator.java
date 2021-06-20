package jasonMentalState;

import jason.asSyntax.Atom;
import jason.asSyntax.Pred;
import jasonkri.Utils;
import jasonkri.language.JasonTerm;
import jasonkri.language.JasonUpdate;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import krTools.language.Term;
import krTools.language.Update;
import languageTools.program.actionspec.UserSpecAction;
import languageTools.program.agent.AgentId;
import mentalState.error.MSTTranslationException;
import eis.iilang.Action;
import eis.iilang.Parameter;
import eis.iilang.Percept;

public class JasonTranslator implements mentalState.translator.Translator {

	@Override
	public Term convert(Parameter parameter) throws MSTTranslationException {
		return new JasonTerm(Converters.parameterToTerm(parameter), null);
	}

	@Override
	public Parameter convert(Term term) throws MSTTranslationException {
		return Converters.termToParameter(((JasonTerm) term).getJasonTerm());
	}

	@Override
	public Action convert(UserSpecAction action) throws MSTTranslationException {
		LinkedList<Parameter> actions = new LinkedList<Parameter>();
		for (Term param : action.getParameters()) {
			actions.add(convert(param));
		}
		return new eis.iilang.Action(action.getName(), actions);
	}

	@Override
	public Update convert(Percept percept) throws MSTTranslationException {
		return Converters.perceptToUpdate(percept);
	}

	@Override
	public Percept convert(Update update) throws MSTTranslationException {
		return Converters.predToPercept((Pred) ((JasonUpdate) update)
				.getJasonTerm());
	}

	@Override
	public Term convert(AgentId id) throws MSTTranslationException {
		return new JasonTerm(new Atom(id.getName()), null);
	}

	@Override
	public Term makeList(List<Term> termList) throws MSTTranslationException {
		List<jason.asSyntax.Term> list = new ArrayList<jason.asSyntax.Term>();
		for (Term term : termList) {
			list.add(((JasonTerm) term).getJasonTerm());
		}
		return new JasonTerm(Utils.makeList(list), null);
	}

}
