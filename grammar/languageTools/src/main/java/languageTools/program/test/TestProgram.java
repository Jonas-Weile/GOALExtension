package languageTools.program.test;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import krTools.parser.SourceInfo;
import languageTools.analyzer.FileRegistry;
import languageTools.program.Program;
import languageTools.program.mas.MASProgram;

/**
 * UnitTest for GOAL. A unit test consists of a list of tests.
 */
public class TestProgram extends Program {
	private Map<String, ModuleTest> moduletests;
	private Map<String, AgentTest> agenttests;
	private MASProgram mas;
	private long timeout;

	/**
	 * Constructs a new unit test.
	 */
	public TestProgram(FileRegistry registry, SourceInfo info) {
		super(registry, info);
		this.moduletests = new LinkedHashMap<>();
		this.agenttests = new LinkedHashMap<>();
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	public void setMAS(MASProgram mas) {
		this.mas = mas;
		if (mas != null) {
			setKRInterface(mas.getKRInterface());
		}
	}

	public MASProgram getMAS() {
		return this.mas;
	}

	public boolean addModuleTest(ModuleTest test) {
		return (this.moduletests.put(test.getModuleSignature(), test) == null);
	}

	public boolean addAgentTest(AgentTest test) {
		return (this.agenttests.put(test.getAgentName(), test) == null);
	}

	public Collection<ModuleTest> getModuleTests() {
		return this.moduletests.values();
	}

	public Collection<AgentTest> getAgentTests() {
		return this.agenttests.values();
	}

	/**
	 * Returns a test for the module with the given name or null when the module has
	 * no test associated with it.
	 *
	 * @param moduleName
	 *            to find test for
	 * @return a test or null
	 */
	public ModuleTest getModuleTest(String moduleName) {
		return this.moduletests.get(moduleName);
	}

	/**
	 * Returns a test for the agent with the given base name or null when the agent
	 * has no test associated with it.
	 *
	 * @param agentName
	 *            to find test for
	 * @return a test or null
	 */
	public AgentTest getAgentTest(String agentName) {
		return this.agenttests.get(agentName);
	}

	/**
	 * @return The number of seconds before the test should time out (if any; 0
	 *         otherwise, meaning that the test should not time out).
	 */
	public long getTimeout() {
		return this.timeout;
	}

	@Override
	public String toString() {
		return "TestProgram [moduletests=" + this.moduletests + ", agenttests=" + this.agenttests + ", timeout="
				+ this.timeout + "]";
	}
}
