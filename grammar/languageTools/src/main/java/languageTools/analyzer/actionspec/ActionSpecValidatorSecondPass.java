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

package languageTools.analyzer.actionspec;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import cognitiveKr.CognitiveKR;
import krTools.exceptions.ParserException;
import krTools.language.Var;
import languageTools.analyzer.ValidatorSecondPass;
import languageTools.errors.actionspec.ActionSpecWarning;
import languageTools.program.actionspec.ActionSpecProgram;
import languageTools.program.actionspec.UserSpecAction;
import languageTools.program.agent.msc.MentalLiteral;

/**
 * Implements second pass for validating an action specification file.
 */
public class ActionSpecValidatorSecondPass extends ValidatorSecondPass {
	/**
	 * Program that is outcome of first pass.
	 */
	private final ActionSpecProgram program;

	/**
	 * In the second pass, references in the given action specification file are
	 * resolved and related semantic checks are performed.
	 *
	 * <p>
	 * Assumes that the first pass has been performed and the resulting action
	 * specification program does not contain any {@code null} references.
	 * </p>
	 * <p>
	 * Any validation errors or warnings are reported.
	 * </p>
	 *
	 * @param firstPass The validator object that executed the first pass.
	 */
	public ActionSpecValidatorSecondPass(ActionSpecValidator firstPass) {
		super(firstPass);
		this.program = firstPass.getProgram();
	}

	/**
	 * Performs the validation and resolution of references by a walk over the
	 * program structure.
	 */
	@Override
	public void validate() {
		preProcess();

		if (!checkKRIuse() || this.program.getRegistry().hasAnyError()) {
			return;
		}

		processInfo();
		validateKR();

		reportUnusedVariables();
	}

	protected void reportUnusedVariables() {
		for (UserSpecAction spec : this.program.getActionSpecifications()) {
			List<Var> vars = new LinkedList<>();
			try {
				CognitiveKR ckr = getFirstPass().getCognitiveKR();
				if (spec.getPrecondition() != null) {
					for (MentalLiteral literal : spec.getPrecondition().getAllLiterals()) {
						vars.addAll(ckr.getAllVariables(literal.getFormula()));
					}
				}
				if (spec.getNegativePostcondition() != null) {
					vars.addAll(ckr.getAllVariables(spec.getNegativePostcondition().getPostCondition()));
				}
				if (spec.getPositivePostcondition() != null) {
					vars.addAll(ckr.getAllVariables(spec.getPositivePostcondition().getPostCondition()));
				}
				Set<Var> unique = new LinkedHashSet<>(vars);
				unique.removeAll(spec.getParameters());
				for (Var var : unique) {
					int occurences = Collections.frequency(vars, var);
					if (occurences < 2) {
						getFirstPass().reportWarning(ActionSpecWarning.VARIABLE_UNUSED, var.getSourceInfo(),
								var.toString());
					}
				}
			} catch (ParserException e) {
				getFirstPass().reportParsingException(e);
			}
		}
	}

	/**
	 * Extracts relevant info for validation.
	 */
	private void processInfo() {
		// Extract relevant info from referenced files.
		this.knowledge.addAll(this.program.getKnowledge());
		this.beliefs.addAll(this.program.getBeliefs());

		// Extract relevant info from action specifications.
		for (UserSpecAction action : this.program.getActionSpecifications()) {
			for (MentalLiteral literal : getBeliefLiterals(action.getPrecondition())) {
				this.beliefQueries.add(literal.getFormula());
			}
			if (action.getPositivePostcondition() != null) {
				this.beliefQueries.add(action.getPositivePostcondition().getPostCondition().toQuery());
			}
			if (action.getNegativePostcondition() != null) {
				this.beliefQueries.add(action.getNegativePostcondition().getPostCondition().toQuery());
			}
		}
	}
}
