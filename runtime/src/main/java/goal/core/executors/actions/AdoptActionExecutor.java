/**
 * The GOAL Runtime Environment. Copyright (C) 2015 Koen Hindriks.
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

package goal.core.executors.actions;

import java.util.List;

import events.Channel;
import events.ExecutionEventGeneratorInterface;
import goal.core.runtime.service.agent.Result;
import goal.core.runtime.service.agent.RunState;
import goal.tools.errorhandling.exceptions.GOALActionFailedException;
import krTools.language.Substitution;
import krTools.language.Update;
import languageTools.program.agent.AgentId;
import languageTools.program.agent.actions.AdoptAction;
import languageTools.program.agent.selector.Selector.SelectorType;
import mentalState.MentalStateWithEvents;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;

/**
 * Executor for the adopt action.
 */
public class AdoptActionExecutor extends ActionExecutor {
	/**
	 * Executor for the adopt action.
	 *
	 * @param action
	 *            An adopt action.
	 * @param substitution
	 *            Substitution for instantiating parameters of the adopt action.
	 */
	AdoptActionExecutor(AdoptAction action, Substitution substitution) {
		super(action, substitution);
	}

	@Override
	public Result execute(RunState runState) throws GOALActionFailedException {
		AdoptAction adopt = (AdoptAction) getAction();
		Update upd = adopt.getUpdate().applySubst(getSourceSubstitution());
		List<AgentId> selectors = resolveSelector(runState);
		boolean topLevel = (adopt.getSelector().getType() == SelectorType.SELF);

		MentalStateWithEvents mentalState = runState.getMentalState();
		ExecutionEventGeneratorInterface generator = runState.getEventGenerator();
		try {
			List<mentalState.Result> allAdoptedGoals = mentalState.adopt(upd, !topLevel, generator,
					selectors.toArray(new AgentId[selectors.size()]));
			for (mentalState.Result adoptedGoals : allAdoptedGoals) {
				generator.event(Channel.GB_UPDATES, adoptedGoals, adopt.getSourceInfo());
			}
			return new Result(getAction());
		} catch (MSTQueryException | MSTDatabaseException e) {
			throw new GOALActionFailedException(
					"failed to execute '" + adopt + "' with " + getSourceSubstitution() + ".", e);
		}
	}
}