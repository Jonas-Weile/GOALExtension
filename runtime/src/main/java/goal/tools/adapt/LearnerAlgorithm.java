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

import java.io.Serializable;
import java.util.Hashtable;

import mentalState.MentalState;
import mentalState.converter.GOALMentalStateConverter;

/**
 * General interface through which specific learner algorithms can be hooked
 * into the {@link FileLearner}.
 * 
 * *
 * <h1>Overview of Learning</h1>
 * <p>
 * The learning is based on Reinforcement Learning.
 * 
 * The basic reinforcement learning model consists of:
 * <ol>
 * <li>a set of environment states S (the beliefs and goals; the current
 * module?);
 * <li>a set of actions A (the actions in the rules);
 * <li>rules of transitioning between states (the rules);
 * <li>rules that determine the scalar immediate reward of a transition (the
 * environment reward indicator or an estimate based on the number of remaining
 * goals )
 * <li>rules that describe what the agent observes (beliefs and goals. Percepts
 * and messages are ignored)
 * </ol>
 * 
 * At each time t, the agent receives an observation o<sub>t</sub> , which
 * includes the reward r<sub>t</sub>. It then chooses an action a<sub>t</sub>
 * from the set of actions available, which is subsequently sent to the
 * environment. The environment moves to a new state s<sub>t+1</sub> and the
 * reward r<sub>t+1</sub> associated with the transition ( s<sub>t</sub> ,
 * a<sub>t</sub> , s<sub>t+1</sub> ) is determined. The goal of a reinforcement
 * learning agent is to collect as much reward as possible.
 * <p>
 * An example. Say the agent has just started and moved a block in blocksworld.
 * It should receive a reward for it. The reward in this case is likely 0,
 * unless it reached the desired blocks configurable with that last move in
 * which case it gets a reward of 1. So while the agent did not receive a
 * meaningful reward until the end, it did in fact "learn" at every step. This
 * becomes more evident over many runs as the "value" of each state+action pair
 * starts to become non-zero, and then the agent starts to make more meaningful
 * decisions about which action to take in a given state (based on which action
 * has the most known value so far in that state).
 * </p>
 * <h1>Rewarding an action</h1>
 * <p>
 * The reward is what guides the Learner, as it strives to gain the highest
 * reward. Not all environments support the notion of a reward. If the
 * environment has a reward value, that value can be used right away. Otherwise,
 * reward can be estimated from the number of remaining goals. Even if the
 * reward only comes at the end, reinforcement learning back propagates the
 * rewards to ensure that states that lead to the reward also get rewarded.
 * </p>
 */
public interface LearnerAlgorithm extends Serializable {

	/**
	 * Called at the start of each episode
	 */
	void start();

	/**
	 * ask the learner which action to execute next. Learner should return
	 * action or null. MUST be called as it works in tandem with
	 * {@link #update(double, Integer)}.
	 *
	 * @param state
	 *            is current state number that we are in now. See
	 *            {@link FileLearner#stateid} and
	 *            {@link GOALMentalStateConverter}
	 * @param actions
	 *            is an array with the possible actions at this point. see
	 *            {@link FileLearner#actionid}. All integers in actions must be
	 *            not null
	 * @return returns the suggested action to be executed. null if no action
	 *         suggestion is available.
	 */
	Integer nextAction(Integer state, Integer[] actions);

	/**
	 * Indicates that the execution of the suggested
	 * {@link #nextAction(Integer, Integer[])} results in the indicated reward
	 * and that the agent got into the given newstate. Should is called after
	 * each action of the agent. This function does not change the current state
	 *
	 * @param reward
	 *            The reward is as the reward for going from current to new
	 *            state. See also {@link MentalState#getReward()}.
	 * @param newstate
	 *            the new state number. See {@link GOALMentalStateConverter}
	 */
	void update(double reward, Integer newstate);

	/**
	 * Returns the learnt action values for a given state: the values for each
	 * of the actions in a given state. The keys of the returned table are the
	 * action numbers.
	 *
	 * @param state
	 *            the state number. See {@link GOALMentalStateConverter}
	 * 
	 * @return
	 */
	Hashtable<Integer, Double> actionValues(Integer state);

	/**
	 * Called at the end of each episode.
	 *
	 * @param reward
	 *            The reward is as indicated by {@link MentalState#getReward()}
	 */
	void finish(double reward);

}
