package jasonMentalState;

import jason.asSyntax.Atom;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.LiteralImpl;
import jason.asSyntax.NumberTermImpl;
import jason.asSyntax.Pred;
import jason.asSyntax.Structure;
import jason.asSyntax.Term;
import jasonkri.Utils;
import jasonkri.language.JasonUpdate;

import java.util.LinkedList;
import java.util.List;

import languageTools.program.agent.AgentId;
import languageTools.program.agent.msg.Message;
import languageTools.program.agent.msg.SentenceMood;
import mentalState.error.MSTQueryException;
import eis.iilang.Function;
import eis.iilang.Identifier;
import eis.iilang.Numeral;
import eis.iilang.Parameter;
import eis.iilang.ParameterList;
import eis.iilang.Percept;
import eis.iilang.TruthValue;

/**
 * Utility class with converters to EIS, message, etc. This class only contains
 * converters to and from Jason {@link Term}s (and not other GOAL related
 * objects like {@link krTools.language.Term}s)
 * 
 * @author W.Pasman 11jun15
 *
 */
public class Converters {

	/**
	 * Update of message is special, it does contain an extra wrapper.
	 * 
	 * @param message
	 * @return
	 * @throws MSTQueryException
	 */
	public static JasonUpdate messageToUpdate(Message message)
			throws MSTQueryException {
		Structure content = message2Term(
				((JasonUpdate) message.getContent()).getJasonStructure(),
				message.getMood());
		Atom sender = new Atom(message.getSender().getName());
		return new JasonUpdate(Utils.createPred("received", sender, content),
				null);
	}

	/**
	 * Convert a {@link Message} to a {@link Structure}. Does NOT wrap the
	 * message in send or received wrapper. See also
	 * {@link #messageToUpdate(Message)}
	 * 
	 * @param message
	 *            message to convert
	 * @return {@link Structure} with message, if necessary wrapped in "imp" or
	 *         "int"
	 */
	private static Structure message2Term(Structure content, SentenceMood mood) {
		switch (mood) {
		case IMPERATIVE:
			return Utils.createPred("imp", content);
		case INDICATIVE:
			return content;
		case INTERROGATIVE:
			return Utils.createPred("int", content);
		}
		throw new IllegalArgumentException("unknown mood" + mood);
	}

	/**
	 * Converts {@link Literal} into a message.
	 * 
	 * @param message
	 *            the {@link Literal} to convert. This must be a predicate with
	 *            2 args: the sender and the content. Both must be
	 *            LogicalFormulas. For indicative and imperative messages, the
	 *            content must be of the form int(content) or imp(content).
	 * @return the converted term
	 */
	public static Message termToMessage(Literal message) {
		Literal sender = (Literal) message.getTerm(0);
		Term content = message.getTerm(1);

		SentenceMood mood = SentenceMood.INDICATIVE;
		if (content instanceof Literal && ((Literal) content).getArity() == 1) {
			Literal contentLit = (Literal) content;
			switch (contentLit.getFunctor()) {
			case "imp":
				mood = SentenceMood.IMPERATIVE;
				content = contentLit.getTerm(0);
				break;
			case "int":
				mood = SentenceMood.INDICATIVE;
				content = contentLit.getTerm(0);
				break;
			default:
				break;
			}
		}

		// FIXME get the source info from message.
		Message returned = new Message(new JasonUpdate((Structure) content,
				null), mood);
		returned.setSender(new AgentId(sender.getFunctor()));
		return returned;
	}

	/**
	 * Converts a percept into a JasonUpdate
	 * 
	 * @param percept
	 *            percept to convert
	 * @return {@link JasonUpdate}
	 */
	public static JasonUpdate perceptToUpdate(Percept percept) {
		return new JasonUpdate(Utils.createPred("percept",
				perceptToTerm(percept)), null);
	}

	/**
	 * Convert {@link Percept} to {@link LiteralImpl}. We convert to
	 * {@link LiteralImpl} because that's the minimum for database inserts in
	 * Jason. This does NOT wrap the term into the obligatory "percept" functor.
	 * See also {@link #perceptToUpdate(Percept)}
	 * 
	 * @param percept
	 *            the {@link Percept} to convert
	 * @return the {@link Term}
	 */
	private static LiteralImpl perceptToTerm(Percept percept) {
		// Get main operator name and parameters of the percept.
		LiteralImpl term = new LiteralImpl(percept.getName());
		for (Parameter param : percept.getParameters()) {
			term.addTerm(parameterToTerm(param));
		}
		return term;
	}

	/**
	 * Converts a {@link Term} into a {@link Percept}. Inverse of
	 * {@link #perceptToTerm(Percept)}
	 * 
	 * @param term
	 *            the term to convert. Must be a Pred containing DOC
	 * @return
	 */
	public static Percept predToPercept(Pred pred) {
		LinkedList<Parameter> params = new LinkedList<Parameter>();
		for (Term term : pred.getTerms()) {
			params.add(termToParameter(term));
		}
		return new Percept(pred.getFunctor(), params);
	}

	/**
	 * Convert a {@link Parameter} to a {@link Term}.
	 * 
	 * @param parameter
	 *            the {@link Parameter} to convert
	 * @return {@link Term}
	 * @throws IllegalArgumentException
	 *             if the given argument can not be converted.
	 */
	public static Term parameterToTerm(Parameter parameter) {
		if (parameter instanceof Identifier) {
			return new Atom(((Identifier) parameter).getValue());
		} else if (parameter instanceof Numeral) {
			// JASON only has DOUBLE numbers, no integer handling.
			Number number = ((Numeral) parameter).getValue();
			return new NumberTermImpl(number.doubleValue());
		} else if (parameter instanceof Function) {
			Function f = (Function) parameter;
			Pred term = new Pred(f.getName());
			for (Parameter p : f.getParameters()) {
				term.addTerm(parameterToTerm(p));
			}
			return term;
		} else if (parameter instanceof ParameterList) {
			ListTermImpl list = new ListTermImpl();
			for (Parameter p : (ParameterList) parameter) {
				list.add(parameterToTerm(p));
			}
			return list;
		} else if (parameter instanceof TruthValue) {
			String value = ((TruthValue) parameter).getValue();
			if (value.equalsIgnoreCase("true")) {
				return Literal.LTrue;
			}
			if (value.equalsIgnoreCase("false")) {
				return Literal.LFalse;
			}
			throw new IllegalArgumentException(
					"Jason can not handle truth value " + value);
		} else {
			throw new IllegalArgumentException("Encountered EIS parameter "
					+ parameter + " of unsupported type "
					+ parameter.getClass().getCanonicalName());
		}
	}

	/**
	 * Convert {@link Term} into a {@link Parameter}
	 * 
	 * @param jasonTerm
	 *            term to convert
	 * @return {@link Parameter}
	 */
	public static Parameter termToParameter(Term term) {
		if (term instanceof NumberTermImpl) {
			return new Numeral(((NumberTermImpl) term).solve());
		}

		if (term.isStructure()) {
			/** these can be converted to Function */
			LinkedList<Parameter> params = new LinkedList<Parameter>();
			for (Term t : ((Structure) term).getTerms()) {
				params.add(termToParameter(t));
			}
			return new Function(((Structure) term).getFunctor(), params);
		}

		return new Identifier(term.toString());

	}

	/**
	 * Returns the operands of a (repeatedly used) right associative binary
	 * operator.
	 * <p>
	 * Can be used, for example, to get the conjuncts of a conjunction or the
	 * elements of a list. Note that the <i>second</i> conjunct or element in a
	 * list concatenation can be a conjunct or list itself again.
	 * </p>
	 * <p>
	 * A list (term) of the form '.'(a,'.'(b,'.'(c, []))), for example, returns
	 * the elements a, b, c, <i>and</i> the empty list []. A conjunction of the
	 * form ','(e0,','(e1,','(e2...((...,en)))...) returns the list of conjuncts
	 * e0, e1, e2, etc.
	 * </p>
	 *
	 * @param operator
	 *            The binary operator.
	 * @param term
	 *            The term to be unraveled.
	 * @return A list of operands.
	 */
	public static List<Term> getOperands(String operator, Term term) {
		List<Term> list = new LinkedList<>();

		if (term.isPred() && ((Pred) term).getFunctor().equals(operator)
				&& ((Pred) term).getArity() == 2) {
			list.add(((Pred) term).getTerm(0));
			list.addAll(getOperands(operator, ((Pred) term).getTerm(1)));
		} else {
			list.add(term);
		}
		return list;
	}

}
