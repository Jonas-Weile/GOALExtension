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
package mentalState.executors;

import java.util.LinkedList;
import java.util.List;

import krTools.language.Term;
import languageTools.program.agent.AgentId;
import languageTools.program.agent.selector.Selector;
import mentalState.MentalState;
import mentalState.error.MSTQueryException;

/**
 * Executor for a selector.
 */
public class SelectorExecutor {
	/**
	 * The selector to be evaluated.
	 */
	private final Selector selector;

	/**
	 * Executor for a selector.
	 *
	 * @param selector
	 *            The selector to be evaluated.
	 */
	public SelectorExecutor(Selector selector) {
		this.selector = selector;
	}

	/**
	 * @return The selector to be executed (evaluated).
	 */
	public Selector getSelector() {
		return this.selector;
	}

	/**
	 * Resolves the references in the selector to agent names.
	 *
	 * @param ms
	 *            An agent's mental state; to evaluate the selector on.
	 * @return The set of agents that this selector refers to.
	 * @throws MSTQueryException
	 */
	public List<AgentId> evaluate(MentalState ms, boolean sent) throws MSTQueryException {
		List<AgentId> agents = new LinkedList<>();
		switch (this.selector.getType()) {
		case ALL:
		case SOME:
			agents.addAll(ms.getKnownAgents());
			break;
		case ALLOTHER:
		case SOMEOTHER:
			agents.addAll(ms.getKnownAgents());
			agents.remove(ms.getAgentId());
			break;
		case VARIABLE:
			if (sent) {
				return null;
			} else {
				agents.addAll(ms.getKnownAgents());
			}
			break;
		case PARAMETERLIST:
			for (Term term : this.selector.getParameters()) {
				agents.add(new AgentId(term.toString()));
			}
			break;
		default:
		case THIS:
		case SELF:
			agents.add(ms.getAgentId());
			break;
		}

		return agents;
	}
}