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

package languageTools.symbolTable.agent;

import krTools.parser.SourceInfo;
import languageTools.program.agent.Module;
import languageTools.symbolTable.Symbol;

public class ModuleSymbol extends Symbol {

	private final Module module;

	/**
	 * @param name
	 *            Signature of module.
	 * @param module
	 *            A module.
	 * @param info
	 *            Source info object for module.
	 */
	public ModuleSymbol(String name, Module module, SourceInfo info) {
		super(name, info);
		this.module = module;
	}

	/**
	 * @return The module associated with this symbol.
	 */
	public Module getModule() {
		return this.module;
	}

	/**
	 * @return String representation of this {@link #ModuleSymbol(String)}.
	 */
	@Override
	public String toString() {
		return "<ModuleSymbol: " + this.module.getName() + ">";
	}

}
