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

package msFactory;

import java.util.HashMap;
import java.util.Map;

import krFactory.KRFactory;
import krTools.KRInterface;
import languageTools.program.agent.AgentId;
import languageTools.program.mas.AgentDefinition;
import mentalState.MentalState;
import mentalState.MentalStateWithEvents;
import swiPrologMentalState.SwiPrologMentalState;
import swiprolog.SwiPrologInterface;

/**
 * Factory of Mental States for a specific KRI.
 */
public class MentalStateFactory {
	/**
	 * A mapping of {@link KRInterface}s to {@link MentalState}s.
	 */
	private static Map<Class<? extends KRInterface>, Class<? extends MentalState>> states = new HashMap<>();

	static {
		// states.put(JasonInterface.class, JasonMentalState.class);
		// states.put(OWLRepoKRInterface.class, OwlMentalState.class);
		states.put(SwiPrologInterface.class, SwiPrologMentalState.class);
		// states.put(TuPrologInterface.class, TuPrologMentalState.class);
	}

	/**
	 * Utility class; constructor is hidden.
	 */
	private MentalStateFactory() {
	}

	/**
	 * Provides a mental state for a certain knowledge representation.
	 *
	 * @param owner   The agent that requests a mental state.
	 * @param agentId The (running) name of the agent.
	 * @return A MentalState implementation.
	 * @throws InstantiationFailedException If the creation of the requested
	 *                                      implementation failed.
	 */
	public static MentalStateWithEvents getMentalState(final AgentDefinition owner, final AgentId agentId)
			throws InstantiationFailedException {
		return new MentalStateWithEvents(getMentalStateInternal(owner, agentId));
	}

	private static MentalState getMentalStateInternal(final AgentDefinition owner, final AgentId agentId)
			throws InstantiationFailedException {
		try {
			final KRInterface kri = owner.getKRInterface();
			final MentalState state = states.containsKey(kri.getClass()) ? states.get(kri.getClass())
					.getDeclaredConstructor(AgentDefinition.class, AgentId.class).newInstance(owner, agentId) : null;
			if (state == null) {
				throw new InstantiationFailedException("could not find a mental state implementation for '"
						+ KRFactory.getName(kri) + "' as only these are available: " + states.keySet() + ".");
			} else {
				return state;
			}
		} catch (ReflectiveOperationException | SecurityException e) {
			throw new InstantiationFailedException("failed to initialize a mental state for agent '" + agentId + "'.",
					e);
		}
	}
}