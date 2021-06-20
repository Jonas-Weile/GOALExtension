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

package msFactory.translator;

import java.util.HashMap;
import java.util.Map;

import krFactory.KRFactory;
import krTools.KRInterface;
import mentalState.translator.Translator;
import msFactory.InstantiationFailedException;
import swiPrologMentalState.translator.SwiPrologTranslator;
import swiprolog.SwiPrologInterface;

/**
 * Factory of Mental State Translators for a specific KRI.
 */
public class TranslatorFactory {
	/**
	 * A mapping of {@link KRInterface}s to {@link Translator}s.
	 */
	private static Map<Class<? extends KRInterface>, Translator> states = new HashMap<>();

	static {
		// register(JasonInterface.class, JasonTranslator.class);
		// register(OWLRepoKRInterface.class, OwlTranslator.class);
		register(SwiPrologInterface.class, new SwiPrologTranslator());
		// register(TuPrologInterface.class, TuPrologTranslator.class);
	}

	/**
	 * Utility class; constructor is hidden.
	 */
	private TranslatorFactory() {
	}

	/**
	 * register given translator for given kri.
	 * 
	 * @param kri
	 *            the KRI for which a translator is being registered.
	 * @param translator
	 *            instance of the proper translator for this kind of KRI. The
	 *            translator is shared by all users and therefore must be
	 *            stateless.
	 */
	public static void register(Class<? extends KRInterface> kri, Translator translator) {
		states.put(kri, translator);
	}

	/**
	 * Provides a mental state translator for a certain knowledge
	 * representation.
	 *
	 * @param kri
	 * @return A MentalState Translator implementation.
	 * @throws InstantiationFailedException
	 *             If the creation of the requested implementation failed.
	 */
	public static Translator getTranslator(KRInterface kri) throws InstantiationFailedException {

		Translator translator = states.get(kri.getClass());
		if (translator == null) {
			throw new InstantiationFailedException("could not find a mental state translator implementation for '"
					+ KRFactory.getName(kri) + "' as only these are available: " + states.keySet() + ".");
		}
		return translator;
	}
}