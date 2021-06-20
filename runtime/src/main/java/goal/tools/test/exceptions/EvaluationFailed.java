package goal.tools.test.exceptions;

import goal.tools.test.result.ResultFormatter;
import goal.tools.test.result.TestResult;

public class EvaluationFailed extends Exception implements TestResult {
	/** Generated serialVersionUID */
	private static final long serialVersionUID = -5176959367012313106L;

	public EvaluationFailed(String message, Exception cause) {
		super(message, cause);
	}

	@Override
	public <T> T accept(ResultFormatter<T> formatter) {
		return formatter.visit(this);
	}
}
