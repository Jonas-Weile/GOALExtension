/**
 * Knowledge Representation Tools. Copyright (C) 2014 Koen Hindriks.
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

package cognitiveKrFactory;

import java.util.HashMap;
import java.util.Map;

import cognitiveKr.CognitiveKR;
import cognitiveSwiprolog.CognitiveSwiProlog;
import krFactory.KRFactory;
import krTools.KRInterface;
import swiprolog.SwiPrologInterface;

public class CognitiveKRFactory {
	/**
	 * A mapping of {@link KRInterface}s to {@link CognitiveKR}s.
	 */
	private static Map<Class<? extends KRInterface>, Class<? extends CognitiveKR>> states = new HashMap<>();

	static {
		// states.put(JasonInterface.class, CognitiveJason.class);
		// states.put(OWLRepoKRInterface.class, CognitiveOwl.class);
		states.put(SwiPrologInterface.class, CognitiveSwiProlog.class);
		// states.put(TuPrologInterface.class, CognitiveTuProlog.class);
	}

	/**
	 * Utility class; constructor is hidden.
	 */
	private CognitiveKRFactory() {
	}

	/**
	 * Provides a cognitive KR for a certain KR interface.
	 *
	 * @param kri
	 * @return A CognitiveKR implementation.
	 * @throws InstantiationFailedException If the creation of the requested
	 *                                      implementation failed.
	 */
	public static CognitiveKR getCognitiveKR(final KRInterface kri) throws InstantiationFailedException {
		try {
			final CognitiveKR ckr = states.containsKey(kri.getClass())
					? states.get(kri.getClass()).getDeclaredConstructor(KRInterface.class).newInstance(kri)
					: null;
			if (ckr == null) {
				throw new InstantiationFailedException("could not find a cognitive KR implementation for '"
						+ KRFactory.getName(kri) + "' as only these are available: " + states.keySet() + ".");
			} else {
				return ckr;
			}
		} catch (ReflectiveOperationException | SecurityException e) {
			throw new InstantiationFailedException(
					"failed to initialize a cognitive KR for '" + KRFactory.getName(kri) + "'.", e);
		}
	}
}