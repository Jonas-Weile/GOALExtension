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

package languageTools.analyzer.test;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import cognitiveKr.CognitiveKR;
import krTools.exceptions.ParserException;
import krTools.language.Expression;
import krTools.language.Term;
import krTools.language.Var;
import languageTools.analyzer.ValidatorSecondPass;
import languageTools.errors.test.TestWarning;
import languageTools.program.actionspec.ActionSpecProgram;
import languageTools.program.actionspec.UserSpecAction;
import languageTools.program.agent.Module;
import languageTools.program.agent.actions.Action;
import languageTools.program.agent.actions.ActionCombo;
import languageTools.program.agent.actions.MentalAction;
import languageTools.program.agent.actions.UserSpecOrModuleCall;
import languageTools.program.agent.msc.Macro;
import languageTools.program.agent.msc.MentalFormula;
import languageTools.program.agent.msc.MentalLiteral;
import languageTools.program.agent.msc.MentalStateCondition;
import languageTools.program.agent.rules.Rule;
import languageTools.program.kr.KRProgram;
import languageTools.program.mas.UseClause;
import languageTools.program.test.AgentTest;
import languageTools.program.test.ModuleTest;
import languageTools.program.test.TestAction;
import languageTools.program.test.TestMentalStateCondition;
import languageTools.program.test.TestProgram;
import languageTools.program.test.testcondition.TestCondition;
import languageTools.symbolTable.Symbol;
import languageTools.symbolTable.agent.ActionSymbol;
import languageTools.symbolTable.agent.ModuleSymbol;

public class TestValidatorSecondPass extends ValidatorSecondPass {
	/**
	 * Program that is outcome of first pass.
	 */
	private final TestProgram program;

	/**
	 * In the second pass, references in the given agent program are resolved and
	 * related semantic checks are performed.
	 *
	 * <p>
	 * Assumes that the first pass has been performed and the resulting agent
	 * program does not contain any {@code null} references.
	 * </p>
	 * <p>
	 * Any validation errors or warnings are reported.
	 * </p>
	 *
	 * @param firstPass The validator object that executed the first pass.
	 */
	public TestValidatorSecondPass(TestValidator firstPass) {
		super(firstPass);
		this.program = firstPass.getProgram();
	}

	/**
	 * Performs the validation and resolution of references by a walk over the
	 * program structure.
	 */
	@Override
	public void validate() {
		// Process use clause references.
		preProcess();
		preProcessSymbolTables();

		// VALIDATE: Check whether a (single) KR language is used in all
		// referenced files, including module and action specification files.
		// Abort if that is not the case.
		if (!checkKRIuse() || this.program.getRegistry().hasSyntaxError()) { // Abort.
			return;
		}

		// Resolve references (do this after initializing action symbol table).
		resolveReferences(); // FIXME: move into validator itself (first pass)

		// Collect all info needed for validation.
		processInfo();

		// Report unused items.
		reportUnusedUndefined();
		reportUnusedVariables();
	}

	/**
	 * Processes referenced files in use clauses. Also initializes action symbol
	 * table.
	 * <p>
	 * Parses referenced KR files, and validates referenced module and action
	 * specification files, if these have not been processed yet. We need the
	 * contents of these files to be able to:
	 * <ul>
	 * <li>Resolve user-specified actions (add pre- and post-conditions);</li>
	 * <li>Resolve module calls (add targeted modules);</li>
	 * <li>Verify that predicates used have also been defined.</li>
	 * </ul>
	 * </p>
	 */
	private void preProcessSymbolTables() {
		// Fill action symbol table: Add action specifications. Check for
		// duplicates.
		for (UseClause clause : this.program.getUseClauses()) {
			for (File file : clause.getResolvedReference()) {
				switch (clause.getUseCase()) {
				case ACTIONSPEC:
					ActionSpecProgram actionspecs = (ActionSpecProgram) this.program.getRegistry().getProgram(file);
					for (UserSpecAction actionspec : actionspecs.getActionSpecifications()) {
						String signature = actionspec.getSignature();
						Symbol symbol = new ActionSymbol(signature, actionspec, actionspec.getSourceInfo());
						if (!this.actionSymbols.define(symbol)) {
							reportDuplicateActionLabelDfs(signature, symbol);
						}
						this.actionsDefined.add(actionspec.getSignature());
					}
					break;
				case MODULE:
					// Fill action symbol table: Add referenced modules.
					Module module = (Module) this.program.getRegistry().getProgram(file);
					String signature = module.getSignature();
					Symbol symbol = new ModuleSymbol(signature, module, module.getSourceInfo());
					if (!this.actionSymbols.define(symbol)) {
						reportDuplicateActionLabelDfs(signature, symbol);
					}
					break;
				default:
					break;
				}
			}
		}
	}

	/**
	 * Resolves references in module.
	 */
	private void resolveReferences() {
		List<ActionCombo> actions = new LinkedList<>();
		List<MentalStateCondition> conditions = new LinkedList<>();
		for (AgentTest agent : this.program.getAgentTests()) {
			for (TestAction action : agent.getActions()) {
				actions.add(action.getAction());
				if (action.getCondition() != null && action.getCondition().getQuery() != null
						&& action.getCondition().getQuery().getCondition() != null) {
					conditions.add(action.getCondition().getQuery().getCondition());
				}
			}
		}
		for (ModuleTest module : this.program.getModuleTests()) {
			ActionCombo stub = new ActionCombo(module.getSourceInfo());
			stub.addAction(new UserSpecOrModuleCall(module.getModuleName(), module.getModuleArguments(),
					module.getSourceInfo()));
			actions.add(stub);
			if (module.getPre() != null && module.getPre().getAction() != null) {
				actions.add(module.getPre().getAction().getAction());
			}
			if (module.getPre() != null && module.getPre().getCondition() != null) {
				conditions.add(module.getPre().getCondition());
			}
			for (TestCondition maintest : module.getIn()) {
				TestCondition test = maintest;
				while (test != null) {
					TestMentalStateCondition condition = (test == null) ? null : test.getQuery();
					if (condition != null && condition.getAction() != null) {
						actions.add(condition.getAction().getAction());
					}
					if (condition != null && condition.getCondition() != null) {
						conditions.add(condition.getCondition());
					}
					test = test.getNestedCondition();
				}
			}
			if (module.getPost() != null && module.getPost().getAction() != null) {
				actions.add(module.getPost().getAction().getAction());
			}
			if (module.getPost() != null && module.getPost().getCondition() != null) {
				conditions.add(module.getPost().getCondition());
			}
		}

		Set<Var> contextVars = new LinkedHashSet<>(0);
		for (ActionCombo action : actions) {
			resolveActionReferences(action, contextVars);
			processInfoCombo(action);
		}
		for (MentalStateCondition condition : conditions) {
			for (MentalFormula formula : condition.getSubFormulas()) {
				if (formula instanceof Macro) {
					resolveMacroReference((Macro) formula, condition.getFreeVar());
				}
			}
			processInfoMsc(condition);
		}
	}

	protected void reportUnusedVariables() {
		for (ModuleTest test : this.program.getModuleTests()) {
			try {
				CognitiveKR ckr = getFirstPass().getCognitiveKR();
				if (test.getPre() != null) {
					List<Var> vars = getVariablesInTestMSC(test.getPre(), ckr);
					Set<Var> unique = new LinkedHashSet<>(vars);
					unique.removeAll(test.getModuleArguments());
					for (Var var : unique) {
						int occurences = Collections.frequency(vars, var);
						if (occurences < 2) {
							getFirstPass().reportWarning(TestWarning.VARIABLE_UNUSED, var.getSourceInfo(),
									var.toString());
						}
					}
				}
				if (test.getPost() != null) {
					List<Var> vars = getVariablesInTestMSC(test.getPost(), ckr);
					Set<Var> unique = new LinkedHashSet<>(vars);
					unique.removeAll(test.getModuleArguments());
					for (Var var : unique) {
						int occurences = Collections.frequency(vars, var);
						if (occurences < 2) {
							getFirstPass().reportWarning(TestWarning.VARIABLE_UNUSED, var.getSourceInfo(),
									var.toString());
						}
					}
				}
				for (TestCondition condition : test.getIn()) {
					List<Var> vars = getVariablesInTestCondition(condition, ckr);
					Set<Var> unique = new LinkedHashSet<>(vars);
					unique.removeAll(test.getModuleArguments());
					for (Var var : unique) {
						int occurences = Collections.frequency(vars, var);
						if (occurences < 2) {
							getFirstPass().reportWarning(TestWarning.VARIABLE_UNUSED, var.getSourceInfo(),
									var.toString());
						}
					}
				}
			} catch (ParserException e) {
				getFirstPass().reportParsingException(e);
			}
		}
	}

	private static List<Var> getVariablesInTestCondition(TestCondition condition, CognitiveKR ckr)
			throws ParserException {
		List<Var> vars = getVariablesInTestMSC(condition.getQuery(), ckr);
		if (condition.hasNestedCondition()) {
			vars.addAll(getVariablesInTestCondition(condition.getNestedCondition(), ckr));
		}
		return vars;
	}

	private static List<Var> getVariablesInTestMSC(TestMentalStateCondition condition, CognitiveKR ckr)
			throws ParserException {
		List<Var> vars = new LinkedList<>();
		if (condition.getCondition() != null) {
			for (MentalLiteral literal : condition.getCondition().getAllLiterals()) {
				for (Term term : literal.getSelector().getParameters()) {
					vars.addAll(ckr.getAllVariables(term));
				}
				vars.addAll(ckr.getAllVariables(literal.getFormula()));
			}
		}
		if (condition.getAction() != null) {
			for (Action<? extends Expression> action : condition.getAction().getAction()) {
				if (action instanceof MentalAction) {
					for (Term term : ((MentalAction) action).getSelector().getParameters()) {
						vars.addAll(ckr.getAllVariables(term));
					}
				}
				for (Expression param : action.getParameters()) {
					vars.addAll(ckr.getAllVariables(param));
				}
			}
		}
		return vars;
	}

	/**
	 * Extracts relevant info for validation.
	 */
	private void processInfo() {
		for (UseClause clause : this.program.getUseClauses()) {
			for (File file : clause.getResolvedReference()) {
				switch (clause.getUseCase()) {
				case BELIEFS:
					KRProgram beliefsKr = (KRProgram) this.program.getRegistry().getProgram(file);
					this.beliefs.addAll(beliefsKr.getDBFormulas());
					break;
				case GOALS:
					KRProgram goalsKr = (KRProgram) this.program.getRegistry().getProgram(file);
					this.goalDbfs.addAll(goalsKr.getDBFormulas());
					break;
				case KNOWLEDGE:
					KRProgram knowledgeKr = (KRProgram) this.program.getRegistry().getProgram(file);
					this.knowledge.addAll(knowledgeKr.getDBFormulas());
					break;
				default:
					break;
				}
			}
		}
	}

	@Override
	protected void processInfoRule(Rule rule) {
		processInfoCombo(rule.getAction());
	}
}
