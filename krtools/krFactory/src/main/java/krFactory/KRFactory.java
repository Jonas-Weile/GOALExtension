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

package krFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import krTools.KRInterface;
import krTools.exceptions.KRInitFailedException;
import krTools.exceptions.KRInterfaceNotSupportedException;
import swiprolog.SwiPrologInterface;

/**
 * Factory of KR Interfaces.
 * <li>Getting a KR interface by its name specified.</li>
 * <li>Getting the name of an interface implementation.</li>
 * <li>Getting the set of supported KRs by their names. Currently, the factory
 * supports:
 * <ul>
 * <li>SWI Prolog v6.0.2</li>
 * <li>OWL - API v4.0</li>
 * <li>Jason v???</li>
 * <li>TU Prolog v2.9.2</li>
 * </ul>
 * </li>
 */
public class KRFactory {
	// The names of the supported KRIs
	public static String SWI_PROLOG = "SWI Prolog";
	// public static String OWL_REPO = "OWL Repo";
	// public static String JASON = "Jason";
	// public static String TU_PROLOG = "TU Prolog";

	/**
	 * A map of names to the {@link KRInterface}s that are supported.
	 */
	private static Map<String, Class<? extends KRInterface>> kr = new HashMap<>();

	static {
		kr.put(SWI_PROLOG, SwiPrologInterface.class);
		// kr.put(OWL_REPO, OWLRepoKRInterface.class);
		// kr.put(JASON, JasonInterface.class);
		// kr.put(TU_PROLOG, TuPrologInterface.class);
	}

	/**
	 * Utility class; constructor is hidden.
	 */
	private KRFactory() {
	}

	/**
	 * Provides an available knowledge representation technology. See the
	 * getSupportedKRs method for the set of supported KRTs.
	 *
	 * @param name The name of the KRT.
	 * @return A KR interface implementation.
	 * @throws KRInterfaceNotSupportedException If the name does not map to a known
	 *                                          interface.
	 * @throws KRInitFailedException            If the creation of the requested
	 *                                          implementation failed.
	 */
	public static KRInterface getKR(final String name) throws KRInterfaceNotSupportedException, KRInitFailedException {
		try {
			final KRInterface krInterface = kr.containsKey(name) ? kr.get(name).getConstructor().newInstance() : null;
			if (krInterface == null) {
				throw new KRInterfaceNotSupportedException(
						"could not find KRT '" + name + "' as only these are available: " + kr.keySet() + ".");
			} else {
				return krInterface;
			}
		} catch (ReflectiveOperationException | SecurityException e) {
			throw new KRInitFailedException("failed to initialize KRT '" + name + "'.", e);
		}
	}

	/**
	 * Checks if the interface is defined, and returns its name.
	 *
	 * @param kri the interface whose name is needed
	 * @return the name as a string or null if the interface is not defined
	 */
	public static String getName(final KRInterface kri) {
		if (kri != null) {
			for (final String name : kr.keySet()) {
				final Class<? extends KRInterface> defined = kr.get(name);
				if (defined.equals(kri.getClass())) {
					return name;
				}
			}
		}
		return "undetermined";
	}

	/**
	 * Get the set of supported KR's names.
	 *
	 * @return a set of strings representing the supported KRs
	 */
	public static Set<String> getSupportedKRs() {
		return kr.keySet();
	}
}