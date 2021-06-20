package goal.tools.adapt;

public class QLearnerTest extends LearnerAlgorithmTest {
	@Override
	public LearnerAlgorithm getLearner() {
		double sarsa_alpha = 0.9;
		double sarsa_gamma = 0.9;
		double sarsa_epsilon = 0.1;
		double sarsa_epsilon_decay = 0.0;

		return new QLearner(sarsa_alpha, sarsa_epsilon, sarsa_epsilon_decay, sarsa_gamma);
	}
}
