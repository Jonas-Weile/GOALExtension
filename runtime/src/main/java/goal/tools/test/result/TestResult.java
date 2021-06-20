package goal.tools.test.result;

public interface TestResult {
	<T> T accept(ResultFormatter<T> formatter);
}
