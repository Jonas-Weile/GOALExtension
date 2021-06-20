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
package goal.tools.test.result;

import java.util.Map.Entry;

import goal.tools.test.exceptions.ConditionFailed;
import goal.tools.test.exceptions.EvaluationFailed;
import goal.tools.test.exceptions.TestActionFailed;
import languageTools.program.agent.AgentId;

public class TestResultFormatter implements ResultFormatter<String> {
	private String indent(int level) {
		String ret = "";
		for (int i = 0; i < level; i++) {
			ret += "  ";
		}
		return ret;
	}

	private String indent(int level, String text) {
		String ident = indent(level);
		return ident + text.replaceAll("\n", "\n" + ident).trim();
	}

	private String indent(String text) {
		return indent(1, text);
	}

	/**
	 * Formats a {@link TestProgramResult} by listing all agents that their test
	 * status. If an agent failed its tests, more details are printed.
	 *
	 * @param testRunResult
	 * @return
	 */
	@Override
	public String visit(TestProgramResult testProgramResult) {
		String ret;
		if (testProgramResult.isPassed()) {
			ret = "\ntest passed:\n";
		} else {
			ret = "\ntest failed:\n";
		}
		ret += indent("test: " + testProgramResult.getUnitTestFile()) + "\n";
		for (Entry<AgentId, TestInterpreterResult> tr : testProgramResult.getResults().entrySet()) {
			ret += indent(formatGroup(tr.getKey().toString(), tr.getValue())) + "\n";
		}
		return ret;
	}

	/**
	 * Formats a list of {@link AgentTestResult}s. If the list is empty the test did
	 * not run and a message if printed. If the test contains one result the list is
	 * printed concisely. Otherwise the full list is printed.
	 *
	 * @param groupName
	 * @param results
	 * @param indent
	 * @return
	 */
	private String formatGroup(String groupName, TestInterpreterResult results) {
		String ret = groupName + ":\n";
		ret += indent(results.accept(this)) + "\n";
		return ret;
	}

	/**
	 * Formats a {@link AgentTestResult}. If passed the result is formatted as a
	 * single line. If the test failed all {@link AssertTestResult}s are printed.
	 *
	 * @param result
	 * @return
	 */
	@Override
	public String visit(TestInterpreterResult result) {
		String ret = "";
		if (result.getUncaughtThrowable() != null) {
			ret += getCause(result.getUncaughtThrowable()) + "\n";
		}
		if (result.hasTimedOut()) {
			ret += "the test timeout was reached!\n";
		}
		if (result.getResult() != null) {
			ret += result.getResult().accept(this);
		}
		return ret;
	}

	@Override
	public String visit(AgentTestResult result) {
		String ret = "";
		for (ModuleTestResult test : result.getTestResults()) {
			ret += test.accept(this);
		}
		if (ret.trim().isEmpty()) {
			return "all conditions were evaluated succesfully.";
		} else {
			return ret + "\n";
		}
	}

	@Override
	public String visit(ModuleTestResult result) {
		String ret = "";
		for (TestConditionResult sub : result.getResults()) {
			ret += sub.accept(this);
		}
		if (result.getException() != null) {
			ret += "\n\texception: " + result.getException().accept(this);
		}
		return ret;
	}

	@Override
	public String visit(TestConditionResult result) {
		if (result.isPassed()) {
			return "";
		} else {
			String ret = "";
			if (result.getFailure() == null) {
				ret += "unknown failure";
			} else {
				ret += result.getFailure().getMessage();
			}
			return ret + "\n";
		}
	}

	@Override
	public String visit(TestActionFailed taf) {
		String ret = taf.getMessage();
		ret += getCause(taf.getCause());
		return ret;
	}

	@Override
	public String visit(EvaluationFailed ef) {
		String ret = ef.getMessage();
		ret += getCause(ef.getCause());
		return ret;
	}

	@Override
	public String visit(ConditionFailed cf) {
		String ret = cf.getMessage();
		ret += getCause(cf.getCause());
		return ret;
	}

	private String getCause(Throwable cause) {
		String ret = "";
		while (cause != null) {
			if (cause.getMessage() == null) {
				cause.printStackTrace();
			} else {
				ret += "\n\tbecause: " + cause.getMessage();
			}
			cause = cause.getCause();
		}
		return ret;
	}
}
