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

package languageTools.program.actionspec;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import krTools.language.DatabaseFormula;
import krTools.parser.SourceInfo;
import languageTools.analyzer.FileRegistry;
import languageTools.program.Program;
import languageTools.program.mas.UseClause.UseCase;

/**
 * Container for action specifications.
 */
public class ActionSpecProgram extends Program {

	// -------------------------------------------------------------
	// Action specifications in this action specification file.
	// -------------------------------------------------------------

	/**
	 * List of action specifications.
	 */
	private List<UserSpecAction> actions = new LinkedList<>();

	/**
	 * Creates a new action specification program.
	 *
	 * @param info
	 *            Source info.
	 */
	public ActionSpecProgram(FileRegistry registry, SourceInfo info) {
		super(registry, info);
	}

	/**
	 * Add an action specification to this action specification program.
	 *
	 * @param actionSpec
	 *            An action specification.
	 */
	public void addActionSpecification(UserSpecAction action) {
		register(action);
		if (action.getFullPreCondition() != null) {
			register(action.getPrecondition());
		}
		if (action.getPositivePostcondition() != null) {
			register(action.getPositivePostcondition());
		}
		if (action.getNegativePostcondition() != null) {
			register(action.getNegativePostcondition());
		}
		this.actions.add(action);
	}

	/**
	 * @return All action specifications of this program.
	 */
	public List<UserSpecAction> getActionSpecifications() {
		return Collections.unmodifiableList(this.actions);
	}

	@SuppressWarnings("unchecked")
	public List<DatabaseFormula> getKnowledge() {
		return ((List<DatabaseFormula>) getItems(UseCase.KNOWLEDGE));
	}

	@SuppressWarnings("unchecked")
	public List<DatabaseFormula> getBeliefs() {
		return (List<DatabaseFormula>) getItems(UseCase.BELIEFS);
	}

	@Override
	public String toString() {
		return this.actions.toString();
	}

	@Override
	public int hashCode() {
		return this.actions.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (!(obj instanceof ActionSpecProgram)) {
			return false;
		}
		ActionSpecProgram other = (ActionSpecProgram) obj;
		if (this.actions == null) {
			if (other.actions != null) {
				return false;
			}
		} else if (!this.actions.equals(other.actions)) {
			return false;
		}
		return true;
	}
}
