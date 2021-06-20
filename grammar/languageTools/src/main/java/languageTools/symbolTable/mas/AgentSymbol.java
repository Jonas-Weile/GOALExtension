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

package languageTools.symbolTable.mas;

import java.io.File;

import krTools.parser.SourceInfo;
import languageTools.program.mas.AgentDefinition;
import languageTools.symbolTable.Symbol;

/**
 * An agent symbol associates an agent definition with the agent name in an
 * agent definition section in a MAS file.
 */
public class AgentSymbol extends Symbol {

	private AgentDefinition agentDf;

	public AgentSymbol(String name, AgentDefinition agentDf, SourceInfo info) {
		super(name, info);
		this.agentDf = agentDf;
	}

	/**
	 * @return The agent definition associated with this agent symbol.
	 */
	public AgentDefinition getAgentDf() {
		return this.agentDf;
	}

	/**
	 * @return String representation of this {@link #AgentSymbol(String, File)}.
	 */
	@Override
	public String toString() {
		return "<AgentSymbol: " + getName() + ">";
	}

}
