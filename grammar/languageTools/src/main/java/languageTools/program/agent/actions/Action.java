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

package languageTools.program.agent.actions;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import krTools.KRInterface;
import krTools.language.Expression;
import krTools.language.Substitution;
import krTools.language.Var;
import krTools.parser.SourceInfo;
import languageTools.program.GoalParsedObject;
import languageTools.program.agent.msc.MentalStateCondition;

/**
 * An action that an agent can perform.
 * <p>
 * Instances of this primarily contain the action as called in the program; but
 * they can also contain pointers to specifications on how to execute the
 * action. For example, it can be putDown(X). When it is figured out this is a
 * user spec action, an additional link to the definition of the action can be
 * included in the object, eg. a ref to user spec action putDown(Y) with
 * pre/post conditions.
 *
 * <p>
 * There are two types of actions: so-called <i>built-in</i> (also called
 * reserved) actions and so-called <i>user-specified</i> actions. Adopting and
 * dropping a goal, inserting and deleting beliefs, and sending a message are
 * examples of built-in actions.
 * </p>
 * <p>
 * By default, whenever an agent is connected to an environment, a
 * user-specified action is sent to that environment for execution. A programmer
 * can indicate using the "@int" option (inserted directly after the action name
 * and parameters in the action specification) that an action should NOT be sent
 * to an environment.
 * </p>
 * <p>
 * Every action has a precondition and a postcondition. A precondition specifies
 * the conditions that need to hold to be able to perform the action
 * (successfully); a postcondition specifies the (expected) effects of the
 * action. Only user-specified actions may have <i>multiple</i> action
 * specifications, i.e., preconditions and corresponding postconditions.
 * </p>
 */
public abstract class Action<Parameter extends Expression> extends GoalParsedObject {
	/**
	 * The name of the action.
	 */
	protected final String name;
	/**
	 * The parameters of the action.
	 */
	protected final List<Parameter> parameters = new LinkedList<>();
	/**
	 * A cache of free variables in the parameters of the action.
	 */
	protected final Set<Var> free = new LinkedHashSet<>();

	/**
	 * Creates an action (without instantiating its parameters, if any).
	 *
	 * @param name
	 *            The name of the action.
	 * @param info
	 *            the source info of this parsed object.
	 */
	public Action(String name, SourceInfo info) {
		super(info);
		this.name = name;
	}

	/**
	 * Returns the name of this {@link Action}.
	 *
	 * @return The name of the action.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Returns the parameters of this {@link Action}.
	 *
	 * @return The parameters of the action.
	 */
	public List<Parameter> getParameters() {
		return Collections.unmodifiableList(this.parameters);
	}

	/**
	 * Adds a parameter of the action.
	 *
	 * @param parameter
	 *            The parameter to be added.
	 */
	public void addParameter(Parameter parameter) {
		this.parameters.add(parameter);
		this.free.addAll(parameter.getFreeVar());
	}

	/**
	 * Returns the precondition for this {@link Action}.
	 *
	 * <p>
	 * The precondition of an action should provide the conditions for successfully
	 * performing the action. That is, in principle, if the precondition holds, one
	 * should reasonably be able to expect the action to succeed.
	 * </p>
	 *
	 * <p>
	 * The precondition is a mental state condition because the built-in actions for
	 * adopting a goal include conditions on the agent's goal base.
	 * </p>
	 *
	 * <p>
	 * This is a default implementation of the method that assumes the action can
	 * always be performed, i.e., its precondition is true (represented by an empty
	 * mental state condition).
	 *
	 * @return A {@link MentalStateCondition} that represents the action's
	 *         precondition.
	 */
	public MentalStateCondition getPrecondition() {
		return new MentalStateCondition(null, this.info);
	}

	public String getSignature() {
		return this.name + "/" + getParameters().size();
	}

	public Set<Var> getFreeVar() {
		return Collections.unmodifiableSet(this.free);
	}

	public boolean isClosed() {
		return getFreeVar().isEmpty();
	}

	public abstract Action<?> applySubst(Substitution substitution);

	// TODO: move this functionality to KR interface...
	public Substitution mgu(Action<?> other, KRInterface kri) {
		if (other == null || !getSignature().equals(other.getSignature())
				|| getParameters().size() != other.getParameters().size()) {
			return null;
		} else if (getParameters().isEmpty()) {
			return kri.getSubstitution(null);
		} else {
			// Get mgu for first parameter
			Substitution substitution = getParameters().get(0).mgu(other.getParameters().get(0));
			// Get mgu's for remaining parameters
			for (int i = 1; i < getParameters().size() && substitution != null; i++) {
				Substitution mgu = getParameters().get(i).mgu(other.getParameters().get(i));
				substitution = substitution.combine(mgu);
			}
			return substitution;
		}
	}

	/**
	 * Default implementation of string representation for an action.
	 */
	@Override
	public String toString() {
		String str = this.name;
		if (!getParameters().isEmpty()) {
			str += "(";
			for (int i = 0; i < getParameters().size(); i++) {
				str += getParameters().get(i);
				str += (i < getParameters().size() - 1 ? ", " : "");
			}
			str += ")";
		}
		return str;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = getClass().hashCode();
		result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
		result = prime * result + ((this.parameters == null) ? 0 : this.parameters.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		Action<?> other = (Action<?>) obj;
		if (this.name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!this.name.equals(other.name)) {
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
