package languageTools.program.test.testcondition;

import languageTools.program.test.TestMentalStateCondition;

public class Always extends TestCondition {
	/**
	 * Constructs a new Always operator
	 *
	 * @param query
	 *            to evaluate at each state change
	 */
	public Always(TestMentalStateCondition query, String original) {
		super(query, original);
	}
}
