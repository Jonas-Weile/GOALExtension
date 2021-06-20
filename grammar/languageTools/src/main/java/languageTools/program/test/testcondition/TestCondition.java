package languageTools.program.test.testcondition;

import krTools.parser.SourceInfo;
import languageTools.program.test.TestMentalStateCondition;

/**
 * Abstract base for any test condition. Test conditions are evaluated in the
 * context of a running agent and need to provide an evaluator that can do so.
 */
public abstract class TestCondition {
	/**
	 * What the original test statement looks like
	 */
	private final String original;
	/**
	 * The mental state condition of the query
	 */
	protected final TestMentalStateCondition query;
	/**
	 * An optional nested condition (... -> ...)
	 */
	protected TestCondition nested;

	/**
	 * @return the mental state condition of the query
	 */
	public TestMentalStateCondition getQuery() {
		return this.query;
	}

	/**
	 * @return the nested condition (... -> ...) if it is present (null
	 *         otherwise)
	 */
	public TestCondition getNestedCondition() {
		return this.nested;
	}

	/**
	 * @return true when a nested condition is present
	 */
	public boolean hasNestedCondition() {
		return (this.nested != null);
	}

	/**
	 * Creates a {@link TestCondition} using the mental state condition.
	 *
	 * @param query
	 *            A mental state condition.
	 * @param original
	 *            A string representing the original test condition (what it
	 *            looks like in the file)
	 */
	public TestCondition(TestMentalStateCondition query, String original) {
		this.query = query;
		this.original = original;
	}

	/**
	 * Defines a nested condition (when ... -> ...)
	 *
	 * @param nested
	 *            The nested TestCondition.
	 */
	public void setNestedCondition(TestCondition nested) {
		this.nested = nested;
	}

	public SourceInfo getSourceInfo() {
		if (getQuery().getCondition() != null) {
			return getQuery().getCondition().getSourceInfo();
		} else if (getQuery().getAction() != null) {
			return getQuery().getAction().getAction().getSourceInfo();
		} else {
			return null;
		}
	}

	@Override
	public String toString() {
		return this.original;
	}

	@Override
	public int hashCode() {
		return this.original.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		TestCondition other = (TestCondition) obj;
		return this.original.equals(other.original);
	}
}
