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

import krTools.language.Update;
import krTools.parser.SourceInfo;
import languageTools.program.agent.selector.Selector;

/**
 * {@inheritDoc}
 *
 * Parent class for all actions that *only* modify the mental state of an agent
 * and do *not* have external side effects outside the multi-agent system
 * itself.
 * <p>
 * A mental action has a {@link Selector} that indicates the agent whose mental
 * model is affected, or the agent(s) to whom a message should be sent.
 * </p>
 * <p>
 * The mental actions include:
 * <ul>
 * <li>The adopt action {@link AdoptAction}</li>
 * <li>The adoptone action {@link AdoptOneAction}</li>
 * <li>The drop action {@link DropAction}</li>
 * <li>The insert action {@link InsertAction}</li>
 * <li>The delete action {@link DeleteAction}</li>
 * <li>The send action {@link SendAction}</li>
 * <li>The sendonce action {@link SendOnceAction}</li>
 * </ul>
 * </p>
 */
public abstract class MentalAction extends Action<Update> {
	/**
	 * The selector indicating on which mental model this action should be executed.
	 */
	private final Selector selector;

	/**
	 * Creates a new {@link MentalAction}.
	 *
	 * @param selector
	 *            The selector indicating on which mental model(s) this action
	 *            should be executed.
	 */
	protected MentalAction(String name, Selector selector, SourceInfo info) {
		super(name, info);
		this.selector = selector;
		this.free.addAll(this.selector.getFreeVar());
	}

	/**
	 * Returns the {@link Selector} for this {@link MentalAction}.
	 *
	 * @return The selector for this action.
	 */
	public Selector getSelector() {
		return this.selector;
	}

	@Override
	public String toString() {
		return this.selector.toPrefixString() + super.toString();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((this.selector == null) ? 0 : this.selector.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (!super.equals(obj)) {
			return false;
		}
		MentalAction other = (MentalAction) obj;
		if (this.selector == null) {
			if (other.selector != null) {
				return false;
			}
		} else if (!this.selector.equals(other.selector)) {
			return false;
		}
		return true;
	}
}
