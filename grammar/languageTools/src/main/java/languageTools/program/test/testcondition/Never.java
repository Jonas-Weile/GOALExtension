package languageTools.program.test.testcondition;

import languageTools.program.test.TestMentalStateCondition;

public class Never extends TestCondition {
	/**
	 * Constructs a new Never operator
	 *
	 * @param query
	 *            to evaluate at each state change
	 */
	public Never(TestMentalStateCondition query, String original) {
		super(query, original);
	}

	@Override
	public void setNestedCondition(TestCondition nested) {
		throw new IllegalArgumentException("a never-condition cannot have a nested condition.");
	}
}
