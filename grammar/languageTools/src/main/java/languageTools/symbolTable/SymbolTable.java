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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

public class SymbolTable implements Scope {
	private final Map<String, Symbol> symbols = new LinkedHashMap<>();
	private final String scopeName = "global";
	private SymbolTable enclosingScope = null;

	/**
	 * Creates global scope.
	 */
	public SymbolTable() {

	}

	/**
	 * Creates new scope. Use {@link #getNewScope(String)} to create new scope.
	 *
	 * @param name
	 *            Name of the new scope (refer to program element).
	 * @param scope
	 *            Previous scope.
	 */
	private SymbolTable(String name, SymbolTable scope) {
		this.enclosingScope = scope;
	}

	@Override
	public String getScopeName() {
		return this.scopeName;
	}

	/**
	 * @return enclosing scope.
	 */
	@Override
	public Scope getEnclosingScope() {
		return this.enclosingScope;
	}

	/**
	 * Creates a new scope, with a reference to this scope.
	 *
	 * @return New symbol table that defines new scope.
	 */
	@Override
	public Scope getNewScope(String name) {
		return new SymbolTable(name, this);
	}

	@Override
	public boolean define(Symbol sym) {
		if (this.symbols.containsKey(sym.getName())) {
			return false;
		}
		this.symbols.put(sym.getName(), sym);
		return true;
	}

	@Override
	public Symbol resolve(String name) {
		if (this.symbols.containsKey(name)) {
			return this.symbols.get(name);
		} else if (this.enclosingScope != null) {
			return getEnclosingScope().resolve(name);
		}
		return null;
	}

	/**
	 * @return Names defined in this {@link SymbolTable}.
	 */
	public Set<String> getNames() {
		return Collections.unmodifiableSet(this.symbols.keySet());
	}

	@Override
	public String toString() {
		return getScopeName() + ":" + this.symbols;
	}
}
