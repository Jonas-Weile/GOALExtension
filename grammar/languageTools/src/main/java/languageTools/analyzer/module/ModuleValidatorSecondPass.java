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
package languageTools.analyzer.module;

import java.io.File;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import cognitiveKr.CognitiveKR;
import krTools.exceptions.ParserException;
import krTools.language.DatabaseFormula;
import krTools.language.Expression;
import krTools.language.Query;
import krTools.language.Term;
import krTools.language.Var;
import languageTools.analyzer.ValidatorSecondPass;
import languageTools.errors.module.ModuleWarning;
import languageTools.program.actionspec.UserSpecAction;
import languageTools.program.agent.Module;
import languageTools.program.agent.actions.Action;
import languageTools.program.agent.actions.MentalAction;
import languageTools.program.agent.actions.ModuleCallAction;
import languageTools.program.agent.msc.Macro;
import languageTools.program.agent.msc.MentalLiteral;
import languageTools.program.agent.rules.ListallDoRule;
import languageTools.program.agent.rules.Rule;
import languageTools.program.kr.KRProgram;
import languageTools.program.mas.UseClause;
import languageTools.symbolTable.Symbol;
import languageTools.symbolTable.agent.ActionSymbol;
import languageTools.symbolTable.agent.ModuleSymbol;

/**
 * Implements second pass for validating a module.
 */
public class ModuleValidatorSecondPass extends ValidatorSecondPass {
	/**
	 * Program that is outcome of first pass.
	 */
	private final Module program;

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
	protected ModuleValidatorSecondPass(ModuleValidator firstPass) {
		super(firstPass);
		this.program = firstPass.getProgram();
		this.actionSymbols = firstPass.getActionSymbols();
		this.macroSymbols = firstPass.getMacroSymbols();
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

		// Report variables that should be bound but are not.
		reportUnboundVariables();

		// Report unused items.
		reportUnusedUndefined();
		reportUnusedImports();
		reportUnusedVariables();
	}

	/**
	 * Reports any import ('use ...') that is not defining predicates used in the
	 * module.
	 */
	private void reportUnusedImports() {
		for (UseClause useClause : getFirstPass().getProgram().getUseClauses()) {
			for (File file : useClause.getResolvedReference()) {
				switch (useClause.getUseCase()) {
				case BELIEFS:
				case KNOWLEDGE:
				case GOALS:
					KRProgram program = (KRProgram) getFirstPass().getRegistry().getProgram(file);
					reportIfUnusedDatabase(useClause, program.getDBFormulas());
					break;
				default:
					// modules, MAS and actionspec do not contain predicate
					// definitions.
					break;
				}
			}
		}
	}

	/**
	 * Check if the given database formulas from the useClause contains at least one
	 * useful definition
	 *
	 * @param useClause  the reference to the imported file.
	 * @param dbFormulas the {@link DatabaseFormula}s in the file. If null, the
	 *                   check is skipped.
	 */
	private void reportIfUnusedDatabase(UseClause useClause, List<DatabaseFormula> dbFormulas) {
		if (dbFormulas == null) {
			return;
		}
		// for (DatabaseFormula formula : dbFormulas) TODO
	}

	/**
	 * Reports any variables that should have been bound but are not.
	 */
	protected void reportUnboundVariables() {
		// Report variables in rule heads that should have been bound.
		for (Rule rule : this.program.getRules()) {
			reportUnboundVariables(rule, new LinkedHashSet<>(this.program.getParameters()));
		}
	}

	protected void reportUnusedVariables() {
		for (Rule rule : this.program.getRules()) {
			reportUnusedVariables(rule, this.program.getParameters());
		}
	}

	private void reportUnusedVariables(Rule rule, List<Var> bound) {
		try {
			CognitiveKR ckr = getFirstPass().getCognitiveKR();
			List<Var> vars = getAllRuleVariables(rule, ckr);
			Set<Var> unique = new LinkedHashSet<>(vars);
			unique.removeAll(bound);
			for (Var var : unique) {
				int occurences = Collections.frequency(vars, var);
				if (occurences < 2) {
					getFirstPass().reportWarning(ModuleWarning.VARIABLE_UNUSED, var.getSourceInfo(), var.toString());
				}
			}
			for (Action<?> action : rule.getAction().getActions()) {
				if (action instanceof ModuleCallAction && ((ModuleCallAction) action).getTarget().isAnonymous()) {
					for (Rule ruleR : ((ModuleCallAction) action).getTarget().getRules()) {
						List<Var> boundR = new LinkedList<>();
						boundR.addAll(bound);
						for (MentalLiteral literal : rule.getCondition().getAllLiterals()) {
							for (Term term : literal.getSelector().getParameters()) {
								boundR.addAll(ckr.getAllVariables(term));
							}
							boundR.addAll(ckr.getAllVariables(literal.getFormula()));
						}
						reportUnusedVariables(ruleR, boundR);
					}
				}
			}
		} catch (ParserException e) {
			getFirstPass().reportParsingException(e);
		}
	}

	private static List<Var> getAllRuleVariables(Rule rule, CognitiveKR ckr) throws ParserException {
		List<Var> vars = new LinkedList<>();
		if (rule.getCondition() != null) {
			for (MentalLiteral literal : rule.getCondition().getAllLiterals()) {
				for (Term term : literal.getSelector().getParameters()) {
					vars.addAll(ckr.getAllVariables(term));
				}
				vars.addAll(ckr.getAllVariables(literal.getFormula()));
			}
		}
		if (rule.getAction() != null) {
			for (Action<? extends Expression> action : rule.getAction()) {
				if (action instanceof MentalAction) {
					for (Term term : ((MentalAction) action).getSelector().getParameters()) {
						vars.addAll(ckr.getAllVariables(term));
					}
				} else if (action instanceof ModuleCallAction) {
					Module anon = ((ModuleCallAction) action).getTarget();
					if (anon != null && anon.isAnonymous()) {
						for (Rule subrule : anon.getRules()) {
							vars.addAll(getAllRuleVariables(subrule, ckr));
						}
					}
				}
				for (Expression param : action.getParameters()) {
					vars.addAll(ckr.getAllVariables(param));
				}
			}
		}
		if (rule instanceof ListallDoRule) {
			vars.add(((ListallDoRule) rule).getVariable());
		}
		return vars;
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
		Symbol symbol;
		String signature;
		for (UserSpecAction actionspec : this.program.getActionSpecifications()) {
			signature = actionspec.getSignature();
			symbol = new ActionSymbol(signature, actionspec, actionspec.getSourceInfo());
			if (!this.actionSymbols.define(symbol)) {
				reportDuplicateActionLabelDfs(signature, symbol);
			}
			this.actionsDefined.add(actionspec.getSignature());
		}
		// Fill action symbol table: Add referenced modules.
		for (Module module : this.program.getUsedModules()) {
			signature = module.getSignature();
			symbol = new ModuleSymbol(signature, module, module.getSourceInfo());
			if (!this.actionSymbols.define(symbol)) {
				reportDuplicateActionLabelDfs(signature, symbol);
			}
		}
	}

	/**
	 * Resolves references in module.
	 */
	private void resolveReferences() {
		Set<Var> moduleVars = new LinkedHashSet<>(this.program.getParameters());
		for (Rule rule : this.program.getRules()) {
			resolveReferences(rule, moduleVars);
		}
	}

	/**
	 * Extracts relevant info for validation.
	 */
	private void processInfo() {
		// Extract relevant info from referenced files.
		this.knowledge.addAll(this.program.getKnowledge());
		this.beliefs.addAll(this.program.getBeliefs());
		this.goals.addAll(this.program.getGoals());

		// Extract relevant info from macros.
		for (Macro macro : this.program.getMacros()) {
			processInfoMacro(macro);
		}

		// Extract relevant info from rules.
		for (Rule rule : this.program.getRules()) {
			processInfoRule(rule);
		}

		// Add goals in referenced files as goal definitions.
		for (Query query : this.program.getGoals()) {
			this.goalDbfs.addAll(query.toUpdate().getAddList());
		}
	}
}