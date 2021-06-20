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

package tuPrologMentalState.translator;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import eis.iilang.Action;
import eis.iilang.Function;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Parameter;
import eis.iilang.ParameterList;
import eis.iilang.Percept;
import eis.iilang.TruthValue;
import krTools.language.Term;
import krTools.language.Update;
import krTools.parser.SourceInfo;
import languageTools.program.actionspec.UserSpecAction;
import languageTools.program.agent.AgentId;
import mentalState.error.MSTTranslationException;
import mentalState.translator.Translator;
import tuprolog.language.JPLUtils;
import tuprolog.language.PrologTerm;
import tuprolog.language.PrologUpdate;

public class TuPrologTranslator implements Translator {
	@Override
	public Term convert(Parameter parameter) throws MSTTranslationException {
		if (parameter instanceof Identifier) {
			// do not do quoting of the term, that is only for printing.
			return new PrologTerm(new alice.tuprolog.Struct(((Identifier) parameter).getValue()), null);
		} else if (parameter instanceof Numeral) {
			// check if parameter that is passed is a float.
			// note that LONG numbers are converted to float
			Number number = ((Numeral) parameter).getValue();
			if (number instanceof Double) {
				return new PrologTerm(new alice.tuprolog.Double(number.doubleValue()), null);
			} else if (number instanceof Float) {
				return new PrologTerm(new alice.tuprolog.Float(number.floatValue()), null);
			} else if (number instanceof Long) {
				return new PrologTerm(new alice.tuprolog.Long(number.longValue()), null);
			} else {
				return new PrologTerm(JPLUtils.createIntegerNumber(number.longValue()), null);
			}
		} else if (parameter instanceof Function) {
			Function f = (Function) parameter;
			List<alice.tuprolog.Term> terms = new ArrayList<>(f.getParameters().size());
			for (Parameter p : f.getParameters()) {
				PrologTerm t = (PrologTerm) convert(p);
				terms.add(t.getTerm());
			}
			return new PrologTerm(
					new alice.tuprolog.Struct(f.getName(), terms.toArray(new alice.tuprolog.Term[terms.size()])), null);
		} else if (parameter instanceof ParameterList) {
			ParameterList pl = (ParameterList) parameter;
			List<alice.tuprolog.Term> terms = new ArrayList<>(pl.size());
			for (Parameter p : pl) {
				PrologTerm t = (PrologTerm) convert(p);
				terms.add(t.getTerm());
			}
			return new PrologTerm(JPLUtils.termsToList(terms), null);
		} else if (parameter instanceof TruthValue) {
			return new PrologTerm(new alice.tuprolog.Struct(((TruthValue) parameter).getValue()), null);
		} else {
			throw new MSTTranslationException("encountered EIS parameter '" + parameter + "' of unsupported type '"
					+ parameter.getClass().getCanonicalName() + "'.");
		}
	}

	@Override
	public Parameter convert(Term term1) throws MSTTranslationException {
		if (!(term1 instanceof PrologTerm)) {
			throw new MSTTranslationException("term '" + term1 + "' is not a TU prolog term.");
		}
		alice.tuprolog.Term term = ((PrologTerm) term1).getTerm();
		if (term instanceof alice.tuprolog.Int) {
			return new Numeral(((alice.tuprolog.Int) term).intValue());
		} else if (term instanceof alice.tuprolog.Long) {
			return new Numeral(((alice.tuprolog.Long) term).longValue());
		} else if (term instanceof alice.tuprolog.Float) {
			return new Numeral(((alice.tuprolog.Float) term).floatValue());
		} else if (term instanceof alice.tuprolog.Double) {
			return new Numeral(((alice.tuprolog.Double) term).doubleValue());
		} else if (term.isAtom()) {
			return new Identifier(JPLUtils.toString(term));
		} else if (term instanceof alice.tuprolog.Var) {
			throw new MSTTranslationException("conversion of the variable '" + term
					+ "' to an EIS parameter is not possible: EIS does not support variables.");
		} else if (term instanceof alice.tuprolog.Struct) {
			LinkedList<Parameter> parameters = new LinkedList<>();
			// Check whether we're dealing with a list or other operator.
			String name = ((alice.tuprolog.Struct) term).getName();
			if (name.equals(".")) {
				for (alice.tuprolog.Term arg : JPLUtils.getOperands(".", term)) {
					parameters.add(convert(new PrologTerm(arg, null)));
				}
				// Remove the empty list.
				parameters.removeLast();
				return new ParameterList(parameters);
			} else {
				for (int i = 0; i < ((alice.tuprolog.Struct) term).getArity(); i++) {
					parameters.add(convert(new PrologTerm(((alice.tuprolog.Struct) term).getArg(i), null)));
				}
				return new Function(name, parameters);
			}
		} else {
			throw new MSTTranslationException("conversion of the term '" + term + "' of type '"
					+ term.getClass().getCanonicalName() + "' to an EIS parameter is not supported.");
		}
	}

	@Override
	public Action convert(UserSpecAction action) throws MSTTranslationException {
		LinkedList<Parameter> parameters = new LinkedList<>();
		for (Term term : action.getParameters()) {
			parameters.add(convert(term));
		}
		return new Action(action.getName(), parameters);
	}

	@Override
	public Update convert(Percept percept) throws MSTTranslationException {
		// Get main operator name and parameters of the percept.
		String name = percept.getName();
		List<Parameter> parameters = percept.getParameters();
		// Construct a JPL term from the percept operator and parameters.
		alice.tuprolog.Term term;
		if (parameters.size() == 0) {
			term = new alice.tuprolog.Struct(name);
		} else {
			List<alice.tuprolog.Term> terms = new ArrayList<>(parameters.size());
			for (Parameter parameter : parameters) {
				PrologTerm add = (PrologTerm) convert(parameter);
				terms.add(add.getTerm());
			}
			term = new alice.tuprolog.Struct(name, terms.toArray(new alice.tuprolog.Term[terms.size()]));
		}
		return new PrologUpdate(term, null);
	}

	@Override
	public Percept convert(Update update) throws MSTTranslationException {
		alice.tuprolog.Term percept = ((PrologUpdate) update).getTerm();
		if (!(percept instanceof alice.tuprolog.Struct)) {
			throw new MSTTranslationException("'" + percept + "' is not a struct.");
		}
		String name = ((alice.tuprolog.Struct) percept).getName();
		LinkedList<Parameter> parameters = new LinkedList<>();
		for (int i = 0; i < ((alice.tuprolog.Struct) percept).getArity(); i++) {
			parameters.add(convert(new PrologTerm(((alice.tuprolog.Struct) percept).getArg(i), null)));
		}
		return new Percept(name, parameters);
	}

	@Override
	public Term convert(AgentId id) throws MSTTranslationException {
		// TODO: can we use this method in general?
		return convert(new Identifier(id.getName()));
	}

	@Override
	public Term makeList(List<Term> termList) throws MSTTranslationException {
		SourceInfo source = null;
		if (!termList.isEmpty()) {
			source = termList.get(0).getSourceInfo();
		}
		List<alice.tuprolog.Term> terms = new ArrayList<>(termList.size());
		for (Term t : termList) {
			terms.add(((PrologTerm) t).getTerm());
		}
		return new PrologTerm(JPLUtils.termsToList(terms), source);
	}
}
