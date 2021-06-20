package languageTools.program.test.testcondition;

import languageTools.program.test.TestMentalStateCondition;

public class Eventually extends TestCondition {
	/**
	 * Constructs a new Eventually operator
	 *
	 * @param query
	 *            to evaluate at the end
	 */
	public Eventually(TestMentalStateCondition query, String original) {
		super(query, original);
	}
}
