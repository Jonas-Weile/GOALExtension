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

package languageTools.symbolTable;

public interface Scope {
	/**
	 * @return The name of the scope, if any; otherwise the empty string.
	 */
	public String getScopeName();

	/**
	 * @return The scope this scope is part of (directly enclosed in).
	 */
	public Scope getEnclosingScope();

	/**
	 * @param name
	 *            Name of the new scope.
	 * @return New scope.
	 */
	public Scope getNewScope(String name);

	/**
	 * Adds a new definition of a symbol to the scope.
	 *
	 * @param sym
	 *            The name of the symbol that is added to the scope.
	 * @return {@code true} if symbol was added; {@code false} if symbol is
	 *         duplicate already present in table.
	 */
	public boolean define(Symbol sym);

	/**
	 * Resolves a reference (use) of a name within the scope and connects it to
	 * its definition.
	 *
	 * @param name
	 *            The name that is referenced and needs to be resolved.
	 * @return The symbol that provides a definition for the name.
	 */
	public Symbol resolve(String name);
}