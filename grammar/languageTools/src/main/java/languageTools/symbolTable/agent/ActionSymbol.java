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
import languageTools.program.actionspec.UserSpecAction;
import languageTools.symbolTable.Symbol;

public class ActionSymbol extends Symbol {

	private final UserSpecAction spec;

	public ActionSymbol(String signature, UserSpecAction spec, SourceInfo info) {
		super(signature, info);
		this.spec = spec;
	}

	/**
	 * @return The action specification associated with this symbol.
	 */
	public UserSpecAction getActionSpecification() {
		return this.spec;
	}

	/**
	 * @return String representation of this {@link #ActionSymbol(String)}.
	 */
	@Override
	public String toString() {
		return "<ActionSymbol: " + this.spec + ">";
	}

}
