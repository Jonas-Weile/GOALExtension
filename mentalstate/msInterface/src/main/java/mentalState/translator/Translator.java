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

package mentalState.translator;

import java.util.List;

import eis.iilang.Action;
import eis.iilang.Parameter;
import eis.iilang.Percept;
import krTools.language.DatabaseFormula;
import krTools.language.Term;
import krTools.language.Update;
import languageTools.program.actionspec.UserSpecAction;
import languageTools.program.agent.AgentId;
import languageTools.program.agent.msg.Message;
import mentalState.error.MSTTranslationException;

/**
 * Utility functions to convert between EIS {@link Parameter}s, KRLanguage
 * {@link Term}s and {@link Message} objects. Implementations must be stateless
 * as a single translator instance will be used everywhere.
 *
 */
public interface Translator {
	/**
	 * Converts a parameter from the EIS language <i>iilang</i> used for
	 * environment interaction to a term in the knowledge representation
	 * language used to represent the mental state.
	 *
	 * @param parameter
	 *            A parameter from the EIS language <i>iilang</i>.
	 * @return A translation of the parameter into a term in the knowledge
	 *         representation language.
	 * @throws MSTTranslationException
	 *             When the translation was not possible.
	 */
	Term convert(Parameter parameter) throws MSTTranslationException;

	/**
	 * Converts a term in the knowledge representation language used to
	 * represent the mental state to a parameter from the EIS language
	 * <i>iilang</i> used for environment interaction. This should be the exact
	 * reverse of convert(Parameter).
	 *
	 * @param term
	 *            A term in the knowledge representation language.
	 * @return A translation of the term into a parameter from the EIS language
	 *         <i>iilang</i>.
	 * @throws MSTTranslationException
	 *             When the translation was not possible.
	 */
	Parameter convert(Term term) throws MSTTranslationException;

	/**
	 * Converts a user-specified action to an EIS action that can be sent to an
	 * environment.
	 *
	 * @param action
	 *            A user-specified action.
	 * @return An EIS action.
	 * @throws MSTTranslationException
	 *             When the translation was not possible.
	 */
	Action convert(UserSpecAction action) throws MSTTranslationException;

	/**
	 * Converts a percept from EIS to a term in the knowledge representation
	 * language used to represent the mental state.
	 *
	 * @param percept
	 *            A percept from EIS language.
	 * @return A translation of the percept into a formula in the knowledge
	 *         representation language.
	 * @throws MSTTranslationException
	 *             When the translation was not possible.
	 */
	DatabaseFormula convertPercept(Percept percept) throws MSTTranslationException;

	/**
	 * Converts a term in the knowledge representation language used to
	 * represent the mental state to a percept from EIS. This should be the
	 * exact reverse of convert(Percept).
	 *
	 * @param formula
	 *            A formula in the knowledge representation language
	 *            representing a percept.
	 * @return A translation of the percept into the EIS language.
	 * @throws MSTTranslationException
	 *             When the translation was not possible.
	 */
	Percept convertPercept(DatabaseFormula formula) throws MSTTranslationException;

	/**
	 * Converts a Message from GOAL to a term in the knowledge representation
	 * language used to represent the mental state.
	 *
	 * @param message
	 *            A GOAL Message.
	 * @return A translation of the message into a formula in the knowledge
	 *         representation language.
	 * @throws MSTTranslationException
	 *             When the translation was not possible.
	 */
	DatabaseFormula convertMessage(Message message) throws MSTTranslationException;

	/**
	 * Converts a term in the knowledge representation language used to
	 * represent the mental state to a GOAL Message. This should be the exact
	 * reverse of convert(Message).
	 *
	 * @param formula
	 *            A formula in the knowledge representation language
	 *            representing a message.
	 * @return A translation into a GOAL Message.
	 * @throws MSTTranslationException
	 *             When the translation was not possible.
	 */
	Message convertMessage(DatabaseFormula formula) throws MSTTranslationException;

	/**
	 * Converts an agent identifier in something the knowledge repesentation
	 * language used to represent the mental state can understand.
	 *
	 * @param id
	 *            An agent identifier
	 * @return A translation of the agent identifier into a term in the
	 *         knowledge representation language.
	 * @throws MSTTranslationException
	 *             When the translation was not possible.
	 */
	Term convert(AgentId id) throws MSTTranslationException;

	/**
	 * Creates a Term containing an (ordered) list of Terms.
	 *
	 * @param termList
	 *            The list of Terms to convert to a single Term.
	 * @return A Term containing the given Terms as a list.
	 * @throws MSTTranslationException
	 *             When the translation was not possible.
	 */
	Term makeList(List<Term> termList) throws MSTTranslationException;

	/**
	 * gets the elements of a list, or the term itself if the term is not a
	 * list.
	 *
	 * @param term
	 *            A term that is potentially a list
	 * @return If term is a list, this returns a list with each of the subterms
	 *         of the list. Otherwise, returns a list with just the term.
	 * @throws MSTTranslationException
	 *             When the translation was not possible. Normally this should
	 *             not happen because if the term is not a proper list, the term
	 *             itself is to be returned.
	 */
	List<Term> unpackTerm(Term term) throws MSTTranslationException;

	Update makeUpdate(List<DatabaseFormula> formula) throws MSTTranslationException;
}