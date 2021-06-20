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

package languageTools.dependency;

import krTools.dependency.DependencyGraph;
import krTools.exceptions.KRException;
import krTools.language.DatabaseFormula;
import languageTools.program.actionspec.UserSpecAction;
import languageTools.program.agent.msc.MentalLiteral;

/**
 * {@link DependencyGraphGenerator} for {@link ActionSpecification}s. Adds the
 * declarative content of the {@link ActionSpecification} to an
 * {@link DependencyGraph}.
 *
 */
public class ActionSpecificationGraphGenerator extends DependencyGraphGenerator<UserSpecAction> {

	/**
	 * Fills the given expression graph with expressions found in this
	 * {@link ActionSpecification}.
	 */
	@Override
	protected void doCreateGraph(UserSpecAction subject) {
		// The precondition is queried.
		for (MentalLiteral formula : subject.getPrecondition().getAllLiterals()) {
			try {
				super.getGraph().add(formula.getFormula());
			} catch (KRException error) {
				report(error);
			}
		}
		// The add list of the positive postcondition consists of definitions.
		if (subject.getPositivePostcondition() != null) {
			for (DatabaseFormula formula : subject.getPositivePostcondition().getPostCondition().getAddList()) {
				try {
					super.getGraph().add(formula, true, false);
				} catch (KRException error) {
					report(error);
				}
			}
			// The delete list of the positive postcondition consists of
			// queries.
			for (DatabaseFormula formula : subject.getPositivePostcondition().getPostCondition().getDeleteList()) {
				try {
					super.getGraph().add(formula, false, true);
				} catch (KRException error) {
					report(error);
				}
			}
		}
		if (subject.getNegativePostcondition() != null) {
			// The delete list of the negative postcondition consists of
			// definitions.
			for (DatabaseFormula formula : subject.getNegativePostcondition().getPostCondition().getDeleteList()) {
				try {
					super.getGraph().add(formula, true, false);
				} catch (KRException error) {
					report(error);
				}
			}
			// The add list of the negative postcondition consists of queries.
			for (DatabaseFormula formula : subject.getNegativePostcondition().getPostCondition().getAddList()) {
				try {
					super.getGraph().add(formula, false, true);
				} catch (KRException error) {
					report(error);
				}
			}
		}
	}
}
