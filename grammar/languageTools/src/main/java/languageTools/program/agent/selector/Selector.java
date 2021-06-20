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

package languageTools.program.agent.selector;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;
import krTools.parser.SourceInfo;
import languageTools.program.GoalParsedObject;

/**
 * A {@link Selector} is used for indicating the mental model(s) that a mental
 * literal should be evaluated on or an action should be applied to. A selector
 * is a list of select expressions, which need to be resolved into agent names
 * at runtime.
 *
 * <p>
 * Default selector has select expression with type SelectorType.THIS. See
 * {@link #getDefault()}.
 * </p>
 */
public class Selector extends GoalParsedObject {
	/**
	 * Selector types represent different categories of selector mechanisms that can
	 * be used to prefix mental atoms and actions.
	 */
	public enum SelectorType {
		SELF, ALL, ALLOTHER, SOME, SOMEOTHER, THIS, PARAMETERLIST, VARIABLE;
	}

	/**
	 * Type of this {@link Selector}.
	 */
	private final SelectorType type;
	/**
	 * The parameters that are part of this {@link Selector}. Should only be
	 * non-empty list in case the selector type of this selector is
	 * {@link SelectorType#PARAMETERLIST} or {@link SelectorType#VARIABLE}
	 */
	private final List<Term> parameters;
	/**
	 * A cache of free variables in the selector parameters.
	 */
	private final Set<Var> free;

	/**
	 * Creates a {@link Selector} of a particular {@link SelectorType} without
	 * parameters.
	 *
	 * @param type
	 *            The selector type of this {@link Selector}. Should not be
	 *            {@link SelectorType#PARAMETERLIST} or
	 *            {@link SelectorType#VARIABLE}
	 */
	public Selector(SelectorType type, SourceInfo info) {
		super(info);
		this.type = type;
		if (type == SelectorType.PARAMETERLIST || type == SelectorType.VARIABLE) {
			throw new IllegalArgumentException("cannot have parameterlist or variable selector with empty content");
		}
		// selector has no parameters
		this.parameters = new ArrayList<>(0);
		this.free = new LinkedHashSet<>(0);
	}

	/**
	 * Creates a {@link Selector} using the given parameters and sets type of
	 * selector to {@link SelectorType#PARAMETERLIST} or
	 * {@link SelectorType#VARIABLE}
	 *
	 * @param parameters
	 *            List of {@link Term}s.
	 */
	public Selector(List<Term> parameters, SourceInfo info) {
		super(info);
		this.parameters = parameters;
		if (parameters == null || parameters.isEmpty()) {
			throw new IllegalArgumentException("cannot have parameterlist selector with empty content");
		}
		if (parameters.size() == 1 && parameters.get(0).isVar()) {
			this.type = SelectorType.VARIABLE;
		} else {
			this.type = SelectorType.PARAMETERLIST;
		}
		// cache
		this.free = new LinkedHashSet<>();
		for (Term term : parameters) {
			this.free.addAll(term.getFreeVar());
		}
	}

	/**
	 * @return The {@link SelectorType} of this {@link Selector}.
	 */
	public SelectorType getType() {
		return this.type;
	}

	/**
	 * @return The parameters of this {@link Selector}.
	 */
	public List<Term> getParameters() {
		return Collections.unmodifiableList(this.parameters);
	}

	public Var getVariable() {
		return (Var) this.parameters.get(0);
	}

	/**
	 * @return The default selector, use this ;-) if no selector is specified.
	 */
	public static Selector getDefault(SourceInfo info) {
		return new Selector(SelectorType.THIS, info);
	}

	/**
	 * Applies a {@link Substitution} to this {@link Selector} and returns a new
	 * instantiated selector.
	 *
	 * @param substitution
	 *            A substitution.
	 * @return Selector with same type as this one where parameters have been
	 *         instantiated by applying the substitution. No effect if selector has
	 *         no parameters.
	 */
	public Selector applySubst(Substitution substitution) {
		if (getType() == SelectorType.PARAMETERLIST || getType() == SelectorType.VARIABLE) {
			List<Term> terms = new ArrayList<>(getParameters().size());
			for (Term term : getParameters()) {
				terms.add(term.applySubst(substitution));
			}
			return new Selector(terms, getSourceInfo());
		} else {
			return this;
		}
	}

	/**
	 * Returns the set of free variables that occur in this {@link Selector}.
	 *
	 * @return the set of free variables that occur in this selector.
	 */
	public Set<Var> getFreeVar() {
		return Collections.unmodifiableSet(this.free);
	}

	/**
	 * Checks whether this {@link Selector} is closed, i.e. does not contain any
	 * occurrences of free variables.
	 *
	 * @return {@code true} if this selector is closed; {@code false} otherwise.
	 */
	public boolean isClosed() {
		return getFreeVar().isEmpty();
	}

	/**
	 * @return A string with type or list of parameters.
	 */
	@Override
	public String toString() {
		switch (this.type) {
		case SELF:
		case ALL:
		case ALLOTHER:
		case SOME:
		case SOMEOTHER:
		case THIS:
			return this.type.toString().toLowerCase();
		case PARAMETERLIST:
		case VARIABLE:
			StringBuilder str = new StringBuilder();
			str.append("(");
			for (int i = 0; i < this.parameters.size(); i++) {
				str.append(this.parameters.get(i).toString());
				str.append((i < this.parameters.size() - 1 ? ", " : ""));
			}
			str.append(")");
			return str.toString();
		}

		return null;
	}

	/**
	 * Returns string that can be used as prefix for mental actions and mental
	 * literals.
	 *
	 * @return String with selector name followed by dot or the empty string if
	 *         selector is default selector.
	 */
	public String toPrefixString() {
		return equals(getDefault(getSourceInfo())) ? "" : (this + ".");
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((this.type == null) ? 0 : this.type.hashCode());
		result = prime * result + ((this.parameters == null) ? 0 : this.parameters.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || !(obj instanceof Selector)) {
			return false;
		}
		Selector other = (Selector) obj;
		if (this.type != other.type) {
			return false;
		}
		if (this.parameters == null) {
			if (other.parameters != null) {
				return false;
			}
		} else if (!this.parameters.equals(other.parameters)) {
			return false;
		}
		return true;
	}
}
