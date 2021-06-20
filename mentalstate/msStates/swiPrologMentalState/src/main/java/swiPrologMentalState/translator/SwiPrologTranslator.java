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

package swiPrologMentalState.translator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.jpl7.JPL;

import eis.iilang.Action;
import eis.iilang.Function;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Parameter;
import eis.iilang.ParameterList;
import eis.iilang.Percept;
import eis.iilang.TruthValue;
import krTools.language.DatabaseFormula;
import krTools.language.Term;
import krTools.language.Update;
import krTools.parser.SourceInfo;
import languageTools.program.actionspec.UserSpecAction;
import languageTools.program.agent.AgentId;
import languageTools.program.agent.msg.Message;
import languageTools.program.agent.msg.SentenceMood;
import mentalState.error.MSTTranslationException;
import mentalState.translator.Translator;
import swiprolog.language.PrologCompound;
import swiprolog.language.PrologDBFormula;
import swiprolog.language.PrologQuery;
import swiprolog.language.PrologTerm;
import swiprolog.language.impl.PrologImplFactory;

public class SwiPrologTranslator implements Translator {
	private final static Term LIST_NIL = PrologImplFactory.getAtom(JPL.LIST_NIL.name(), null);

	@Override
	public Term convert(final Parameter parameter) throws MSTTranslationException {
		if (parameter instanceof Identifier) {
			// do not do quoting of the term, that is only for printing.
			return PrologImplFactory.getAtom(((Identifier) parameter).getValue(), null);
		} else if (parameter instanceof Numeral) {
			// check if parameter that is passed is a float.
			// note that LONG numbers are converted to float
			final Number number = ((Numeral) parameter).getValue();
			if (number instanceof Double || number instanceof Float || number.longValue() < Integer.MIN_VALUE
					|| number.longValue() > Integer.MAX_VALUE) {
				return PrologImplFactory.getNumber(number.doubleValue(), null);
			} else {
				return PrologImplFactory.getNumber(number.longValue(), null);
			}
		} else if (parameter instanceof Function) {
			final Function f = (Function) parameter;
			final List<Term> terms = new ArrayList<>(f.getParameters().size());
			for (final Parameter p : f.getParameters()) {
				terms.add(convert(p));
			}
			return PrologImplFactory.getCompound(f.getName(), terms.toArray(new Term[terms.size()]), null);
		} else if (parameter instanceof ParameterList) {
			final ParameterList pl = (ParameterList) parameter;
			final List<Term> terms = new ArrayList<>(pl.size());
			for (final Parameter p : pl) {
				terms.add(convert(p));
			}
			return makeList(terms);
		} else if (parameter instanceof TruthValue) {
			return PrologImplFactory.getAtom(((TruthValue) parameter).getValue(), null);
		} else {
			throw new MSTTranslationException("encountered EIS parameter '" + parameter + "' of unsupported type '"
					+ parameter.getClass().getCanonicalName() + "'.");
		}
	}

	@Override
	public Parameter convert(final Term term) throws MSTTranslationException {
		if (term instanceof PrologCompound) {
			final PrologCompound compound = (PrologCompound) term;
			final LinkedList<Parameter> parameters = new LinkedList<>();
			// Check whether we're dealing with a list or other operator.
			if (JPL.LIST_PAIR.equals(compound.getName()) && compound.getArity() == 2) {
				for (final Term arg : compound.getOperands(JPL.LIST_PAIR)) {
					parameters.add(convert(arg));
				}
				// Remove the empty list.
				parameters.removeLast();
				return new ParameterList(parameters);
			} else if (compound.getArity() == 0) {
				return new Identifier(compound.getName());
			} else {
				for (final Term arg : compound) {
					parameters.add(convert(arg));
				}
				return new Function(compound.getName(), parameters);
			}
		} else if (term instanceof PrologTerm && ((PrologTerm) term).isNumeric()) {
			return new Numeral(NumberUtils.createNumber(((PrologTerm) term).toString()));
		} else {
			throw new MSTTranslationException("conversion of the term '" + term + "' of type '"
					+ term.getClass().getCanonicalName() + "' to an EIS parameter is not supported.");
		}
	}

	@Override
	public Action convert(final UserSpecAction action) throws MSTTranslationException {
		final LinkedList<Parameter> parameters = new LinkedList<>();
		for (final Term term : action.getParameters()) {
			parameters.add(convert(term));
		}
		return new Action(action.getName(), parameters);
	}

	@Override
	public DatabaseFormula convertPercept(final Percept percept) throws MSTTranslationException {
		// Get main operator name and parameters of the percept.
		final String name = percept.getName();
		final List<Parameter> parameters = percept.getParameters();
		// Construct a compound from the percept operator and parameters.
		final List<Term> terms = new ArrayList<>(parameters.size());
		for (final Parameter parameter : parameters) {
			terms.add(convert(parameter));
		}
		final PrologCompound compound1 = PrologImplFactory.getCompound(name, terms.toArray(new Term[terms.size()]),
				null);
		final PrologCompound compound2 = PrologImplFactory.getCompound("percept", new Term[] { compound1 }, null);
		return PrologImplFactory.getDBFormula(compound2);
	}

	@Override
	public Percept convertPercept(final DatabaseFormula dbf) throws MSTTranslationException {
		final PrologCompound content = ((PrologDBFormula) dbf).getCompound();
		final PrologCompound percept = (PrologCompound) content.getArg(0);
		final LinkedList<Parameter> parameters = new LinkedList<>();
		for (final Term parameter : percept) {
			parameters.add(convert(parameter));
		}
		return new Percept(percept.getName(), parameters);
	}

	@Override
	public DatabaseFormula convertMessage(final Message message) throws MSTTranslationException {
		PrologCompound content = ((PrologQuery) message.getContent().toQuery()).getCompound();
		switch (message.getMood()) {
		case IMPERATIVE:
			content = PrologImplFactory.getCompound("imp", new Term[] { content }, content.getSourceInfo());
			break;
		case INTERROGATIVE:
			content = PrologImplFactory.getCompound("int", new Term[] { content }, content.getSourceInfo());
			break;
		default:
		}
		final Term sender = convert(message.getSender());
		final PrologCompound compound = PrologImplFactory.getCompound("received", new Term[] { sender, content },
				content.getSourceInfo());
		return PrologImplFactory.getDBFormula(compound);
	}

	@Override
	public Message convertMessage(final DatabaseFormula formula) throws MSTTranslationException {
		final PrologCompound message = ((PrologDBFormula) formula).getCompound();
		final PrologCompound sender = (PrologCompound) message.getArg(0);
		PrologCompound content = (PrologCompound) message.getArg(1);

		SentenceMood mood = SentenceMood.INDICATIVE;
		if (content.getArity() == 1) {
			switch (content.getName()) {
			case "imp":
				mood = SentenceMood.IMPERATIVE;
				content = (PrologCompound) content.getArg(0);
				break;
			case "int":
				mood = SentenceMood.INDICATIVE;
				content = (PrologCompound) content.getArg(0);
				break;
			default:
				break;
			}
		}

		final Message returned = new Message(PrologImplFactory.getUpdate(content), mood);
		returned.setSender(new AgentId(sender.getName()));
		return returned;
	}

	@Override
	public Term convert(final AgentId id) throws MSTTranslationException {
		// TODO: can we use this method in general?
		return convert(new Identifier(id.toString()));
	}

	@Override
	public Term makeList(final List<Term> terms) throws MSTTranslationException {
		SourceInfo source = null;
		if (!terms.isEmpty()) {
			source = terms.get(0).getSourceInfo();
		}
		// Start with element in list, since innermost term of Prolog list is
		// the last term.
		Term list = LIST_NIL;
		for (int i = terms.size() - 1; i >= 0; i--) {
			list = PrologImplFactory.getCompound(JPL.LIST_PAIR, new Term[] { terms.get(i), list }, source);
		}
		return list;
	}

	@Override
	public List<Term> unpackTerm(final Term term) throws MSTTranslationException {
		List<Term> unpacked = new LinkedList<>();
		if (LIST_NIL.equals(term)) {
			return unpacked;
		}
		if (term instanceof PrologCompound) {
			final PrologCompound comp = ((PrologCompound) term);
			if (JPL.LIST_PAIR.equals(comp.getName()) && comp.getArity() == 2) {
				unpacked = unpackTerm(comp.getArg(1));
				unpacked.add(0, comp.getArg(0));
				return unpacked;
			}
		}
		unpacked.add(term);
		return unpacked;
	}

	@Override
	public Update makeUpdate(final List<DatabaseFormula> formulas) throws MSTTranslationException {
		final List<Term> terms = new ArrayList<>(formulas.size());
		for (final DatabaseFormula add : formulas) {
			terms.add(((PrologDBFormula) add).getCompound());
		}
		return PrologImplFactory.getUpdate(termsToConjunct(terms, null));
	}

	/**
	 * Returns a (possibly empty) conjunct containing the given terms.
	 *
	 * @param terms
	 * @param info  source info
	 * @return possibly empty conjunct containing the given terms
	 */
	public static PrologCompound termsToConjunct(final List<Term> terms, final SourceInfo info) {
		if (terms.isEmpty()) {
			return PrologImplFactory.getAtom("true", info);
		} else {
			// build up list last to first.
			PrologCompound list = (PrologCompound) terms.get(terms.size() - 1); // last
			for (int i = terms.size() - 2; i >= 0; i--) {
				list = PrologImplFactory.getCompound(",", new Term[] { terms.get(i), list }, info);
			}
			return list;
		}
	}
}
