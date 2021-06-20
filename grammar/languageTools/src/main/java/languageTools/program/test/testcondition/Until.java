package languageTools.program.test.testcondition;

import languageTools.program.test.TestMentalStateCondition;

public class Until extends TestCondition {
	/**
	 * Constructs a new Until operator
	 *
	 * @param query
	 *            to evaluate
	 */
	public Until(TestMentalStateCondition query, String original) {
		super(query, original);
	}

	@Override
	public void setNestedCondition(TestCondition nested) {
		throw new IllegalArgumentException("an until-condition cannot have a nested condition.");
	}
}
