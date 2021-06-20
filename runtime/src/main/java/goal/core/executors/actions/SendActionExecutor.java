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

import com.google.common.collect.ImmutableSet;

import goal.core.runtime.service.agent.Result;
import goal.core.runtime.service.agent.RunState;
import goal.tools.errorhandling.exceptions.GOALActionFailedException;
import krTools.language.Substitution;
import languageTools.program.agent.AgentId;
import languageTools.program.agent.actions.SendAction;
import languageTools.program.agent.msg.Message;
import languageTools.program.agent.msg.SentenceMood;

/**
 * Executor for the send action.
 */
public class SendActionExecutor extends ActionExecutor {
	/**
	 * Executor for the send action.
	 *
	 * @param action       A send action.
	 * @param substitution Substitution for instantiating parameters of the send
	 *                     action.
	 */
	SendActionExecutor(SendAction action, Substitution substitution) {
		super(action, substitution);
	}

	@Override
	public Result execute(RunState runState) throws GOALActionFailedException {
		SendAction send = (SendAction) getAction();
		List<AgentId> receivers = resolveSelector(runState);

		Message message = send.getMessage().applySubst(getSourceSubstitution());
		message.setReceivers(ImmutableSet.copyOf(receivers));

		runState.send(send, message);

		return new Result(getAction());
	}

	/**
	 * @return {@code true} if action is closed or the message to be sent has
	 *         interrogative mood.
	 */
	@Override
	public boolean canBeExecuted() {
		return ((SendAction) getAction()).getMood() == SentenceMood.INTERROGATIVE
				|| getAction().applySubst(getSourceSubstitution()).isClosed();
	}
}