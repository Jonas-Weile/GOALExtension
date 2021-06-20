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
import languageTools.program.agent.actions.DeleteAction;
import mentalState.MentalStateWithEvents;
import mentalState.error.MSTException;

/**
 * Executor for the delete action.
 */
public class DeleteActionExecutor extends ActionExecutor {
	/**
	 * Executor for the delete action.
	 *
	 * @param action
	 *            A delete action.
	 * @param substitution
	 *            Substitution for instantiating parameters of the delete action.
	 */
	DeleteActionExecutor(DeleteAction action, Substitution substitution) {
		super(action, substitution);
	}

	@Override
	public Result execute(RunState runState) throws GOALActionFailedException {
		DeleteAction delete = (DeleteAction) getAction();
		Update update = delete.getUpdate().applySubst(getSourceSubstitution());
		List<AgentId> selectors = resolveSelector(runState);

		MentalStateWithEvents mentalState = runState.getMentalState();
		ExecutionEventGeneratorInterface generator = runState.getEventGenerator();
		try {
			List<mentalState.Result> allRemovedBeliefs = mentalState.delete(update, generator,
					selectors.toArray(new AgentId[selectors.size()]));
			for (mentalState.Result removedBeliefs : allRemovedBeliefs) {
				generator.event(Channel.BB_UPDATES, removedBeliefs, delete.getSourceInfo());
			}
			if (runState.getParent().isRunning()) {
				updateGoalState(runState);
			}
			return new Result(getAction());
		} catch (MSTException e) {
			throw new GOALActionFailedException(
					"failed to execute '" + delete + "' with " + getSourceSubstitution() + ".", e);
		}
	}
}
