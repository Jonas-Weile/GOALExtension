/**
 * The GOAL Mental State. Copyright (C) 2014 Koen Hindriks.
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

package swiprolog.database;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

import krTools.language.DatabaseFormula;

/**
 * Represents a set of formulas that have been inserted into a corresponding
 * database. A theory is associated with a corresponding database. A theory thus
 * is the equivalent at the GOAL level of a database created by some kr
 * technology. A theory is used to represent the belief base, percept base,
 * mailbox, and goal base (which consists of multiple databases each of which
 * have an associated theory).
 *
 * Formulas in a theory need to be {@link DatabaseFormula}, i.e. formulas that
 * can be inserted into a database created by the associated KR technology.
 *
 * Summarizing, the motivation for introducing the Theory class are that it
 * allows to: - represent the content of belief and goal bases, as well as
 * databases in general. - avoid the need for (complex) access methods to
 * databases maintained by the underlying knowledge technology such as Prolog. -
 * avoid duplication of clauses or facts in a database (a Theory itself is a
 * set).
 */
public class Theory {
	/**
	 * The formulas in this {@link Theory}.
	 */
	private final Set<DatabaseFormula> content;

	/**
	 * Creates a theory and adds all given formulas to it.
	 *
	 * @param formulas A collection of formulas to set the theory's initial content.
	 */
	public Theory() {
		this.content = new LinkedHashSet<>();
	}

	/**
	 * Returns the set of all formulas that are part of this theory.
	 *
	 * @return the {@link DatabaseFormula}s in this theory.
	 */
	public Set<DatabaseFormula> getFormulas() {
		return Collections.unmodifiableSet(this.content);
	}

	// *************** insertion methods *************/

	/**
	 * Adds a formula to this {@link Theory}, if it not already occurs in the
	 * theory.
	 *
	 * @param formula The formula.
	 *
	 * @return <code>true</code> if the theory changed; <code>false</code>
	 *         otherwise.
	 */
	synchronized boolean add(final DatabaseFormula formula) {
		return this.content.add(formula);
	}

	/**
	 * Adds all formulas in theory literally to this theory (but does not introduce
	 * duplicates).
	 *
	 * @param content The content.
	 *
	 * @return {@code true} if theory changed; {@code false} otherwise.
	 */
	synchronized boolean add(final Collection<DatabaseFormula> content) {
		return this.content.addAll(content);
	}

	// *************** deletion methods *************/

	/**
	 * Removes a formula from the theory if it occurs literally as element in
	 * theory.
	 *
	 * @param formula formula to be removed from theory.
	 * @return true if the theory contained formula as element and it has been
	 *         successfully removed.
	 */
	synchronized boolean remove(final DatabaseFormula formula) {
		return this.content.remove(formula);
	}

	/**
	 * Erases all content in the theory.
	 */
	synchronized void eraseContent() {
		this.content.clear();
	}

	/**
	 * Generates string with all formulas in the theory on separate lines.
	 */
	@Override
	public String toString() {
		String text = "";
		for (final DatabaseFormula formula : this.content) {
			text += formula.toString() + ".\n";
		}
		return text;
	}

	@Override
	public int hashCode() {
		return this.content.hashCode();
	}

	@Override
	public boolean equals(final Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || !(obj instanceof Theory)) {
			return false;
		}
		final Theory other = (Theory) obj;
		if (this.content == null) {
			if (other.content != null) {
				return false;
			}
		} else if (!this.content.equals(other.content)) {
			return false;
		}
		return true;
	}
}
