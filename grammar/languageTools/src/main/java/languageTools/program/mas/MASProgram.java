/**
 * The GOAL Grammar Tools. Copyright (C) 2014 Koen Hindriks.
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

package languageTools.program.mas;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import krTools.parser.SourceInfo;
import languageTools.analyzer.FileRegistry;
import languageTools.program.Program;

/**
 * A MAS program consists of:
 * <ul>
 * <li>An (optional) <i>environment section</i>, including:</li>
 * <ul>
 * <li>a reference to an environment interface file</li>
 * <li>an optional list of key-value pairs for initializing the environment.
 * </li>
 * </ul>
 * <li>An <i>agent files section</i> with a non-empty list of references to GOAL
 * agent programs.</li>
 * <li>A <i>launch policy section</i> with a non-empty list of launch rules.
 * </li>
 * </ul>
 */
public class MASProgram extends Program {
	/**
	 * The environment interface file.
	 */
	private File environmentfile = null;
	/**
	 * The environment initialization parameters.
	 */
	private final Map<String, Object> parameters = new LinkedHashMap<>();
	/**
	 * The agent file references.
	 */
	private final Map<String, AgentDefinition> agentDfs = new LinkedHashMap<>();
	/**
	 * An ordered list of launch rules.
	 */
	private final List<LaunchRule> launchRules = new LinkedList<>();

	/**
	 * Creates a new (empty) MAS program.
	 *
	 * @param masfile
	 *            The source file used to construct this MAS program.
	 */
	public MASProgram(FileRegistry registry, SourceInfo masfile) {
		super(registry, masfile);
	}

	/**
	 * @return The environment interface file.
	 */
	public File getEnvironmentfile() {
		return this.environmentfile;
	}

	/**
	 * Sets the environment interface file specified in the MAS file.
	 *
	 * @param environmentfile
	 *            The environment interface file.
	 */
	public void setEnvironmentfile(File environmentfile) {
		this.environmentfile = environmentfile;
	}

	/**
	 * @return {@code true} if environment file has been specified (correctly);
	 *         {@code false} otherwise.
	 */
	public boolean hasEnvironment() {
		return (this.environmentfile != null);
	}

	/**
	 * @return The environment initialization parameters.
	 */
	public Map<String, Object> getInitParameters() {
		return Collections.unmodifiableMap(this.parameters);
	}

	/**
	 * Clears all currently set init parameters.
	 */
	public void resetInitParameters() {
		this.parameters.clear();
	}

	/**
	 * Adds a key-value pair to the map of environment initialization parameters.
	 *
	 * @param key
	 *            The key of the parameter.
	 * @param value
	 *            The value of the initialization parameter.
	 */
	public void addInitParameter(String key, Object value) {
		this.parameters.put(key, value);
	}

	/**
	 * @return List of agent definitions defined in MAS file.
	 */
	public Set<String> getAgentNames() {
		return Collections.unmodifiableSet(this.agentDfs.keySet());
	}

	/**
	 * Adds an agent definition to this MAS program.
	 *
	 * @param agentDf
	 *            An agent definition.
	 */
	public void addAgentDefinition(AgentDefinition agentDf) {
		this.agentDfs.put(agentDf.getName(), agentDf);
	}

	/**
	 * @param name
	 *            The name of the definition that is requested.
	 * @return The definition for the given agent.
	 */
	public AgentDefinition getAgentDefinition(String name) {
		return this.agentDfs.get(name);
	}

	/**
	 * @return The launch rules.
	 */
	public List<LaunchRule> getLaunchRules() {
		return Collections.unmodifiableList(this.launchRules);
	}

	/**
	 * Adds a launch rule to this MAS program.
	 *
	 * @param rule
	 *            The launch rule that is added.
	 */
	public void addLaunchRule(LaunchRule rule) {
		this.launchRules.add(rule);
	}

	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();

		// MAS file name
		str.append("<MAS file: " + getSourceFile() + ",\n");

		// Environment section
		if (hasEnvironment()) {
			str.append("<environment interface: " + this.environmentfile.getName() + ">,\n");
		}
		if (!this.parameters.isEmpty()) {
			str.append("<environment initialization parameters: " + this.parameters + ">,\n");
		}

		// Agent definition section
		for (AgentDefinition agentDf : this.agentDfs.values()) {
			str.append("<agent definition " + agentDf.getName() + ">\n");
			// TODO: add use clauses
		}

		// Launch policy section
		for (LaunchRule rule : this.launchRules) {
			str.append("<launch rule: " + rule + ">,");
		}

		str.append(">");

		return str.toString();
	}

}
