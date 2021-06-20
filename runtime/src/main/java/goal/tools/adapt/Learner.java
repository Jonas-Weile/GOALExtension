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
package goal.tools.adapt;

import java.util.List;

import goal.core.agent.Agent;
import goal.core.executors.modules.AdaptiveModuleExecutor;
import goal.core.executors.stack.ActionComboStackExecutor;
import goal.tools.errorhandling.exceptions.GOALRunFailedException;
import languageTools.program.agent.Module;
import mentalState.MentalState;
import mentalState.MentalStateWithEvents;
import mentalState.converter.GOALMentalStateConverter;

/**
 * To make decisions in adaptive sections of the GOAL program the interpreter
 * consults a Learner. A Learner can make decisions based on past decisions and
 * rewards received from these decisions.
 * 
 * Learner connects GOAL {@link Module}s to {@link LearnerAlgorithm}s.
 * 
 * To do this, the learner has to
 * <ul>
 * <li>map the current GOAL state (the current goals, beliefs, and focus) into a
 * state number (an Integer). This is supported by the
 * {@link GOALMentalStateConverter}
 * <li>Keep for each {@link Module} an instance of {@link LearnerAlgorithm} and
 * update it with every step that the {@link Agent} does. The actual coupling
 * from the agent happens in {@link AdaptiveModuleExecutor}.
 * <li>Save the learned information to a file till the agent is run again.
 * <ul>
 * 
 * 
 * <h1>Saving the learned information</h1>
 * <p>
 * When the agent is died and {@link Learner#terminate(MentalStateWithEvents,
 * Double)()} is called, The learner then updates a <code>.lrn</code> file, a
 * <code>.adaptive.out</code> and a <code>.lrn.txt</code> file. the
 * <code>.lrn</code> file holds the saved learning from each run and should get
 * updated on each run. So at any time, if you killed the agent, you would have
 * saved the learning so far. We also allowed for the agent to start up with the
 * specified <code>.lrn</code> file (i.e., with prior learning). The
 * <code>.lrn.txt</code> file is more for users (students) so that they can get
 * some feedback on what was being learned (when coding, debugging). This file
 * could be shared/compared between students. The <code>.adaptive.out</code> is
 * the log of all states and actions and rewards from memory.
 */
public interface Learner {

	/**
	 * Selects an action from the list of options. The Learner can make this
	 * choice based on the current module, mental state and prior experiences.
	 *
	 * @param module
	 *            the current {@link ModuleID}.
	 * @param ms
	 *            the current mental state.
	 * @param options
	 *            the enabled actions from which the learner can choose an
	 *            action executor. This list should contain at least 1 element.
	 * @return the selected action from the options. (null is NOT allowed).
	 */
	public abstract ActionComboStackExecutor act(ModuleID module, MentalStateWithEvents ms,
			List<ActionComboStackExecutor> options);

	/**
	 * Update the reward based on the last action taken in the previous state.
	 * Note that this makes the Learner stateful: you can not learn just
	 * arbitrary actions. This must be called after every
	 * {@link #act(String, MentalStateWithEvents, List)} call, even if the
	 * environment can not give a proper reward value
	 *
	 * @param module
	 *            the current {@link ModuleID}.
	 * 
	 * @param ms
	 *            the current mental state
	 * @param reward
	 *            the reward for executing the last action selected by
	 *            {@link #act(String, MentalState, List)}.
	 */

	public abstract void update(ModuleID module, MentalStateWithEvents ms, double reward);

	/**
	 * Terminate all learning.
	 *
	 * @param ms
	 * @param envReward
	 * @throws GOALRunFailedException
	 */
	public abstract void terminate(MentalStateWithEvents ms, Double envReward) throws GOALRunFailedException;

}