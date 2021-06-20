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

import java.util.HashSet;
import java.util.List;

import events.Channel;
import events.ExecutionEventGeneratorInterface;
import goal.core.runtime.service.agent.Result;
import goal.core.runtime.service.agent.RunState;
import goal.tools.errorhandling.Warning;
import goal.tools.errorhandling.exceptions.GOALActionFailedException;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;
import languageTools.program.actionspec.ActionPostCondition;
import languageTools.program.agent.actions.UserSpecCallAction;
import mentalState.MSCResult;
import mentalState.MentalStateWithEvents;
import mentalState.error.MSTException;

/**
 * Executor for a user-specified action.
 */
public class UserSpecActionExecutor extends ActionExecutor {
	/**
	 * The substitution to use in the original actionspecification.
	 */
	private Substitution targetSubst;
	/**
	 * The result of the last call to evaluatePrecondition, which is assumed to
	 * have happened before execute is called.
	 */
	private Substitution preSubst;

	/**
	 * Executor for the user-specified action.
	 *
	 * @param action
	 *            A {@link UserSpecCallAction}. e.g. putDown(X). The actual
	 *            userspecaction putDown(Y) is already resolved.
	 * @param substitution
	 *            Substitution for instantiating parameters of the
	 *            user-specified action, as on the caller's side. e.g.
	 *            <code>[X/1, Y/2]</code>. But there may be additional variables
	 *            in this substi that should not be used here.
	 */
	UserSpecActionExecutor(UserSpecCallAction action, Substitution substitution) {
		super(action, substitution);
		this.targetSubst = createTargetSubstitution(action.getSpecification().getParameters(), action.getParameters(),
				substitution);
	}

	@Override
	public Substitution getTargetSubstitution() {
		return this.targetSubst;
	}

	/**
	 * Given a
	 *
	 * @param specParams
	 *            the parameters as specified in the target/definition (module
	 *            head, action parameters)
	 * @param callerParams
	 *            the parameters on the caller side
	 * @param substitution
	 *            the substitution as on the caller side
	 * @return substitution as to be used on the target side
	 */
	private Substitution createTargetSubstitution(List<Term> specParams, List<Term> callerParams,
			Substitution substitution) {
		// create empty subst
		Substitution subst = substitution.clone();
		subst.retainAll(new HashSet<Var>(0));
		for (int i = 0; i < specParams.size(); i++) {
			subst.addBinding((Var) specParams.get(i), callerParams.get(i).applySubst(substitution));
		}
		return subst;
	}

	@Override
	public MSCResult evaluatePrecondition(RunState runState) throws GOALActionFailedException {
		MSCResult result = super.evaluatePrecondition(runState);
		UserSpecCallAction action = (UserSpecCallAction) getAction();
		// Generate the appropriate event
		ExecutionEventGeneratorInterface generator = runState.getEventGenerator();
		if (result.holds()) {
			this.preSubst = result.getAnswers().iterator().next();
			generator.event(Channel.ACTION_PRECOND_EVALUATION,
					action.getPrecondition().applySubst(getTargetSubstitution()),
					action.getFullPreCondition().getSourceInfo(), "pre-condition of '%s' holds for %s.",
					action.applySubst(getSourceSubstitution()), result.getAnswers());
		} else {
			this.preSubst = null;
			generator.event(Channel.ACTION_PRECOND_EVALUATION,
					action.getPrecondition().applySubst(getTargetSubstitution()),
					action.getFullPreCondition().getSourceInfo(), "pre-condition of '%s' failed.",
					action.applySubst(getSourceSubstitution()));
		}
		// Issue warning if more than one answer is returned.
		if (result.nrOfAnswers() > 1) {
			Warning warning = new Warning(
					"evaluating the pre-condition of '" + action.applySubst(getSourceSubstitution())
							+ "' yields multiple answers; only using the first one.");
			runState.getEventGenerator().event(Channel.WARNING, warning, action.getPrecondition().getSourceInfo());
		}
		return result;
	}

	@Override
	public Result execute(RunState runState) throws GOALActionFailedException {
		MentalStateWithEvents mentalState = runState.getMentalState();
		ExecutionEventGeneratorInterface generator = runState.getEventGenerator();
		UserSpecCallAction action = ((UserSpecCallAction) getAction());

		// Send the action to the environment if it is an external action.
		runState.doPerformAction(action.getSpecification().applySubst(getTargetSubstitution()));
		try {
			// Apply the action's negative postcondition (if any).
			if (action.getSpecification().getNegativePostcondition() != null) {
				ActionPostCondition postcondition = action.getSpecification().getNegativePostcondition()
						.applySubst(this.preSubst);
				// Breakpoint
				generator.event(Channel.ACTION_POSTCOND_EVALUATION, postcondition, postcondition.getSourceInfo(),
						"processing negative post-condition '%s' of action '%s' with %s.", postcondition,
						action.applySubst(getSourceSubstitution()), this.preSubst);
				mentalState.Result deletedBeliefs = mentalState.delete(postcondition.getPostCondition(), generator)
						.get(0);
				generator.event(Channel.BB_UPDATES, deletedBeliefs, postcondition.getSourceInfo());
			}
			// Apply the action's positive postcondition (if any).
			if (action.getSpecification().getPositivePostcondition() != null) {
				ActionPostCondition postcondition = action.getSpecification().getPositivePostcondition()
						.applySubst(this.preSubst);
				// Breakpoint
				generator.event(Channel.ACTION_POSTCOND_EVALUATION, postcondition, postcondition.getSourceInfo(),
						"processing positive post-condition of action '%s', '%s', with %s.",
						action.applySubst(getSourceSubstitution()), postcondition, this.preSubst);
				mentalState.Result insertedBeliefs = mentalState.insert(postcondition.getPostCondition(), generator)
						.get(0);
				generator.event(Channel.BB_UPDATES, insertedBeliefs, postcondition.getSourceInfo());
			}
			// Check if goals have been achieved and, if so, update goal base.
			updateGoalState(runState);
			return new Result(getAction());
		} catch (MSTException e) {
			throw new GOALActionFailedException("post-condition of action '" + action + "' cannot be processed.", e);
		}
	}
}
