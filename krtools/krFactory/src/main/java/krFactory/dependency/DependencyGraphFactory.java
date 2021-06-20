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

package krFactory.dependency;

import java.util.HashMap;
import java.util.Map;

import krFactory.KRFactory;
import krTools.KRInterface;
import krTools.dependency.DependencyGraph;
import krTools.exceptions.KRInitFailedException;
import krTools.exceptions.KRInterfaceNotSupportedException;
import swiprolog.SwiPrologInterface;
import swiprolog.dependency.PrologDependencyGraph;

/**
 * Factory of Dependency Graphs for a specific KR Interface.
 */
public class DependencyGraphFactory {
	/**
	 * A mapping of {@link KRInterface}s to {@link DependencyGraph}s.
	 */
	private static Map<Class<? extends KRInterface>, Class<? extends DependencyGraph<?>>> graphs = new HashMap<>();

	static {
		graphs.put(SwiPrologInterface.class, PrologDependencyGraph.class);
		// graphs.put(OWLRepoKRInterface.class); TODO
	}

	/**
	 * Utility class; constructor is hidden.
	 */
	private DependencyGraphFactory() {
	}

	/**
	 * Provides a dependency graph for a certain knowledge representation.
	 *
	 * @param kri The knowledge representation interface.
	 * @return A DependencyGraph implementation.
	 * @throws KRInterfaceNotSupportedException If the interface does not map to a
	 *                                          known implementation.
	 * @throws KRInitFailedException            If the creation of the requested
	 *                                          implementation failed.
	 */
	public static DependencyGraph<?> getDependencyGraph(final KRInterface kri)
			throws KRInterfaceNotSupportedException, KRInitFailedException {
		try {
			final DependencyGraph<?> graph = graphs.containsKey(kri.getClass())
					? graphs.get(kri.getClass()).getConstructor().newInstance()
					: null;
			if (graph == null) {
				throw new KRInterfaceNotSupportedException("could not find a dependency graph implementation for '"
						+ KRFactory.getName(kri) + "' as only these are available: " + graphs.keySet() + ".");
			} else {
				return graph;
			}
		} catch (ReflectiveOperationException | SecurityException e) {
			throw new KRInitFailedException(
					"failed to initialize a dependency graph for '" + KRFactory.getName(kri) + "'.", e);
		}
	}
}