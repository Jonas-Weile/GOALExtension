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

package languageTools.analyzer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cognitiveKr.CognitiveKR;
import cognitiveKrFactory.CognitiveKRFactory;
import cognitiveKrFactory.InstantiationFailedException;
import krFactory.KRFactory;
import krTools.KRInterface;
import krTools.exceptions.ParserException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Update;
import krTools.language.Var;
import krTools.parser.SourceInfo;
import languageTools.Analyzer;
import languageTools.analyzer.test.TestValidator;
import languageTools.errors.ParserError.SyntaxError;
import languageTools.errors.module.ModuleError;
import languageTools.errors.module.ModuleWarning;
import languageTools.program.Program;
import languageTools.program.actionspec.UserSpecAction;
import languageTools.program.agent.Module;
import languageTools.program.agent.actions.Action;
import languageTools.program.agent.actions.ActionCombo;
import languageTools.program.agent.actions.AdoptAction;
import languageTools.program.agent.actions.DeleteAction;
import languageTools.program.agent.actions.DropAction;
import languageTools.program.agent.actions.InsertAction;
import languageTools.program.agent.actions.ModuleCallAction;
import languageTools.program.agent.actions.SendAction;
import languageTools.program.agent.actions.UserSpecCallAction;
import languageTools.program.agent.actions.UserSpecOrModuleCall;
import languageTools.program.agent.msc.AGoalLiteral;
import languageTools.program.agent.msc.BelLiteral;
import languageTools.program.agent.msc.GoalALiteral;
import languageTools.program.agent.msc.GoalLiteral;
import languageTools.program.agent.msc.Macro;
import languageTools.program.agent.msc.MentalFormula;
import languageTools.program.agent.msc.MentalLiteral;
import languageTools.program.agent.msc.MentalStateCondition;
import languageTools.program.agent.msc.PerceptLiteral;
import languageTools.program.agent.msg.SentenceMood;
import languageTools.program.agent.rules.ListallDoRule;
import languageTools.program.agent.rules.Rule;
import languageTools.program.kr.KRProgram;
import languageTools.program.mas.UseClause;
import languageTools.program.mas.UseClause.UseCase;
import languageTools.symbolTable.Symbol;
import languageTools.symbolTable.SymbolTable;
import languageTools.symbolTable.agent.ActionSymbol;
import languageTools.symbolTable.agent.MacroSymbol;
import languageTools.symbolTable.agent.ModuleSymbol;

public abstract class ValidatorSecondPass {
	/**
	 * First pass over parse tree.
	 */
	private final Validator<?, ?, ?, ?> firstPass;
	/**
	 * The program constructed during the first pass.
	 */
	private Program program;
	/**
	 * Symbol tables built during first pass.
	 */
	protected SymbolTable actionSymbols = new SymbolTable();
	protected SymbolTable macroSymbols = new SymbolTable();
	protected final Set<String> macroLabelsUsed = new LinkedHashSet<>();
	/**
	 * Action and macro labels used.
	 */
	protected final Set<String> actionsDefined = new LinkedHashSet<>();
	protected final Set<String> actionLabelsUsed = new LinkedHashSet<>();
	/**
	 * All belief updates used.
	 */
	protected final Set<Update> beliefUpdates = new LinkedHashSet<>();
	/**
	 * The knowledge specified in the module.
	 */
	protected final Set<DatabaseFormula> knowledge = new LinkedHashSet<>();
	/**
	 * The beliefs specified in the module.
	 */
	protected final Set<DatabaseFormula> beliefs = new LinkedHashSet<>();
	/**
	 * The queries on the belief and knowledge base that occur in the module,
	 * including belief, a-goal, and goal-a literals as well as preconditions.
	 */
	protected final Set<Query> beliefQueries = new LinkedHashSet<>();
	/**
	 * Goals specified in the module and the goals that are adopted by means of
	 * adopt actions. Both sets below include the same goals, but the first
	 * represents goals as queries and the second as database formulas.
	 */
	protected final Set<Query> goals = new LinkedHashSet<>();
	protected final Set<DatabaseFormula> goalDbfs = new LinkedHashSet<>();
	/**
	 * The queries on the goal base that occur in the module, including goal,
	 * a-goal, and goal-a literals.
	 */
	protected final Set<Query> goalQueries = new LinkedHashSet<>();
	/**
	 * The queries on the percept base that occur in the module.
	 */
	protected final Set<Query> perceptQueries = new LinkedHashSet<>();
	// the updates from insert actions
	private final Set<Update> insertUpdates = new LinkedHashSet<>();
	// the updates from delete actions
	private final Set<Update> deleteUpdates = new LinkedHashSet<>();
	// updates from adopt actions
	private final Set<Update> adoptUpdates = new LinkedHashSet<>();
	// updates from drop actions
	private final Set<Update> dropUpdates = new LinkedHashSet<>();

	/**
	 * In the second pass, references in the given program are resolved and semantic
	 * checks are performed.
	 *
	 * <p>
	 * Assumes that the first pass has been performed.
	 * </p>
	 * <p>
	 * Reports validation errors and warnings.
	 * </p>
	 *
	 * @param firstPass The validator object that executed the first pass.
	 */
	protected ValidatorSecondPass(Validator<?, ?, ?, ?> firstPass) {
		this.firstPass = firstPass;
		this.program = firstPass.getProgram();
	}

	public Validator<?, ?, ?, ?> getFirstPass() {
		return this.firstPass;
	}

	/**
	 * Performs the validation and resolution of references by a walk over the
	 * program structure.
	 */
	public abstract void validate();

	/**
	 * Processes referenced files in use clauses.
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
	public void preProcess() {
		for (UseClause useClause : this.firstPass.getProgram().getUseClauses()) {
			for (File source : useClause.getResolvedReference()) {
				if (this.firstPass.getRegistry().needsProcessing(source)) {
					switch (useClause.getUseCase()) {
					case KNOWLEDGE:
					case BELIEFS:
						parseKRfile(source, UseCase.KNOWLEDGE, useClause.getSourceInfo());
						break;
					case GOALS:
						parseKRfile(source, UseCase.GOALS, useClause.getSourceInfo());
						break;
					default:
						// File should be processed by validator (ACTIONSPEC:,
						// EVENT:, INIT:, MAIN:, MODULE:, PLANNER:)
						try {
							Analyzer.processFile(source, this.firstPass.getRegistry());
						} catch (IOException e) {
							this.firstPass.reportParsingException(new ParserException(e.getMessage(), source));
						}
						// No need to register in file registry as validator takes
						// care of this.
						break;
					}
				}
			}
		}
	}

	/**
	 * Check whether a (single) KR language is used in all referenced files,
	 * including module and action specification files. If not, report first
	 * mismatch and abort. Also abort if KR interface could not be resolved for this
	 * module.
	 *
	 * @return {@code true} if one and the same KR language is used.
	 */
	public boolean checkKRIuse() {
		KRInterface kri = this.program.getKRInterface();
		if (kri == null) {
			return false;
		} else {
			for (UseClause useClause : this.program.getUseClauses()) {
				if (!useClause.hasKRUseCase()) {
					for (File file : useClause.getResolvedReference()) {
						Program program = this.firstPass.getRegistry().getProgram(file);
						if (program != null && program.getKRInterface() != kri) {
							this.firstPass.reportError(ModuleError.KR_USE_OF_DIFFERENT_KRIS, useClause.getSourceInfo(),
									KRFactory.getName(kri), useClause.getResolvedReference().toString(),
									KRFactory.getName(program.getKRInterface()));
							return false;
						}
					}
				}
			}
			return true;
		}
	}

	/**
	 * Extracts literals that query the belief base from a mental state condition.
	 * Included belief, a-goal, and goal-a literals; also extracts literals from
	 * macros.
	 *
	 * @param msc A mental state condition.
	 * @return Literals that query the belief base.
	 */
	protected Set<MentalLiteral> getBeliefLiterals(MentalStateCondition msc) {
		Set<MentalLiteral> literals = new LinkedHashSet<>();
		for (MentalFormula formula : msc.getSubFormulas()) {
			if (formula instanceof Macro && ((Macro) formula).getDefinition() != null) {
				literals.addAll(getBeliefLiterals(((Macro) formula).getDefinition()));
			} else if (formula instanceof BelLiteral) {
				literals.add((MentalLiteral) formula);
			} else if (formula instanceof GoalALiteral) {
				literals.add((MentalLiteral) formula);
			} else if (formula instanceof AGoalLiteral) {
				literals.add((MentalLiteral) formula);
			}
		}
		return literals;
	}

	/**
	 * Extracts goal literals from a mental state condition.
	 *
	 * @param msc A mental state condition.
	 * @return Literals extracted from condition.
	 */
	protected Set<MentalLiteral> getGoalLiterals(MentalStateCondition msc) {
		Set<MentalLiteral> literals = new LinkedHashSet<>();
		for (MentalFormula formula : msc.getSubFormulas()) {
			if (formula instanceof Macro && ((Macro) formula).getDefinition() != null) {
				literals.addAll(getGoalLiterals(((Macro) formula).getDefinition()));
			} else if (formula instanceof AGoalLiteral) {
				literals.add((MentalLiteral) formula);
			} else if (formula instanceof GoalALiteral) {
				literals.add((MentalLiteral) formula);
			} else if (formula instanceof GoalLiteral) {
				literals.add((MentalLiteral) formula);
			}
		}
		return literals;
	}

	/**
	 * Extracts percept literals from a mental state condition.
	 *
	 * @param msc A mental state condition.
	 * @return Literals extracted from condition.
	 */
	protected Set<MentalLiteral> getPerceptLiterals(MentalStateCondition msc) {
		Set<MentalLiteral> literals = new LinkedHashSet<>();
		for (MentalFormula formula : msc.getSubFormulas()) {
			if (formula instanceof Macro && ((Macro) formula).getDefinition() != null) {
				literals.addAll(getPerceptLiterals(((Macro) formula).getDefinition()));
			} else if (formula instanceof PerceptLiteral) {
				literals.add((MentalLiteral) formula);
			}
		}
		return literals;
	}

	/**
	 * Report unused KR expressions.
	 */
	public void validateKR() {
		// Collect the known database formulas.
		Set<DatabaseFormula> knownDbfs = new LinkedHashSet<>(getKnowledge());
		knownDbfs.addAll(getBeliefs());

		Set<Query> unachievableGoals = this.program.getKRInterface().getUndefined(knownDbfs, getGoals());

		// goals also define predicates (as dynamic)
		knownDbfs.addAll(getGoalDbfs());

		// Get knowledge defined in knowledge files but not used in them.
		Set<DatabaseFormula> knowledgeDfNotUsedInKB = this.program.getKRInterface().getUnused(getKnowledge(),
				new LinkedHashSet<Query>(0));

		Set<Query> tempQueries = new LinkedHashSet<>(getBeliefQueries());
		tempQueries.addAll(getGoals());

		Set<DatabaseFormula> unusedKnowledgeOrBeliefs = this.program.getKRInterface().getUnused(knownDbfs, tempQueries);

		Set<DatabaseFormula> knowledgeDfNotUsed = new LinkedHashSet<>(unusedKnowledgeOrBeliefs);
		knowledgeDfNotUsed.retainAll(knowledgeDfNotUsedInKB);

		Set<Query> undefinedBeliefQueries = this.program.getKRInterface().getUndefined(knownDbfs, getBeliefQueries());

		// Remove undefined knowledge and belief queries again
		unachievableGoals.removeAll(undefinedBeliefQueries);

		knownDbfs = new LinkedHashSet<>(getKnowledge());
		knownDbfs.addAll(getGoalDbfs());
		tempQueries = new LinkedHashSet<>(getGoalQueries());
		Set<DatabaseFormula> unusedGoals = this.program.getKRInterface().getUnused(knownDbfs, tempQueries);
		knowledgeDfNotUsed.retainAll(unusedGoals);
		// Remove knowledge definitions (are not considered to be goals)
		unusedGoals.removeAll(knowledgeDfNotUsedInKB);

		Set<Query> undefinedGoalQueries = this.program.getKRInterface().getUndefined(knownDbfs, getGoalQueries());
		// Remove queries that are undefined knowledge queries
		undefinedGoalQueries
				.removeAll(this.program.getKRInterface().getUndefined(getKnowledge(), new LinkedHashSet<Query>(0)));

		// Report undefined KR expressions.
		for (Query query : undefinedBeliefQueries) {
			this.firstPass.reportError(ModuleError.KR_BELIEF_QUERIED_NEVER_DEFINED, query.getSourceInfo(),
					query.toString());
		}
		for (Query query : undefinedGoalQueries) {
			this.firstPass.reportError(ModuleError.KR_GOAL_QUERIED_NEVER_DEFINED, query.getSourceInfo(),
					query.toString());
		}
		for (Query query : unachievableGoals) {
			this.firstPass.reportWarning(ModuleWarning.KR_GOAL_DOES_NOT_MATCH_BELIEF, query.getSourceInfo(),
					query.toString());
		}

		// TODO: do we also need to add checks for sent and percept literals?
	}

	/**
	 * Parses a KR file.
	 *
	 * @param source  A KR file.
	 * @param useCase Use case for file.
	 * @param info    Source info.
	 */
	private void parseKRfile(File source, UseCase useCase, SourceInfo info) {
		KRInterface kri = this.program.getKRInterface();
		if (kri != null) {
			try {
				CognitiveKR ckr = CognitiveKRFactory.getCognitiveKR(kri);
				List<DatabaseFormula> formulas;
				List<Query> queries;
				switch (useCase) {
				case KNOWLEDGE:
					formulas = ckr.visitKnowledgeFile(source);
					queries = new ArrayList<>(0);
					break;
				case BELIEFS:
					formulas = ckr.visitBeliefFile(source);
					queries = new ArrayList<>(0);
					break;
				case GOALS:
					formulas = new ArrayList<>(0);
					queries = ckr.visitGoalFile(source);
					break;
				default:
					throw new ParserException("use case '" + useCase + "' does not apply to a KR file.", info);
				}

				KRProgram program = new KRProgram(this.firstPass.getRegistry(), formulas, queries, info);
				this.firstPass.getRegistry().register(source, program);
				for (final SourceInfo error : ckr.getErrors()) {
					this.firstPass.reportError(SyntaxError.EMBEDDED_LANGUAGE_ERROR, error,
							KRFactory.getName(this.program.getKRInterface()), error.getMessage());
				}
				switch (useCase) {
				case KNOWLEDGE:
				case BELIEFS:
					if (formulas.isEmpty() && !program.getRegistry().hasSyntaxError()) {
						this.firstPass.reportWarning(ModuleWarning.EMPTY_FILE, info, source.toString());
					}
					break;
				case GOALS:
					if (queries.isEmpty() && !program.getRegistry().hasSyntaxError()) {
						this.firstPass.reportWarning(ModuleWarning.EMPTY_FILE, info, source.toString());
					}
					break;
				default:
					break;
				}
			} catch (ParserException e) {
				this.firstPass.reportParsingException(e);
			} catch (InstantiationFailedException e) {
				this.firstPass
						.reportParsingException(new ParserException("failed to parse '" + source + "'.", info, e));
			}
		}
	}

	/**
	 * Reports error if a variable in head of rule that should be bound is not.
	 */
	protected void reportUnboundVariables(Rule rule, Set<Var> bound) {
		Set<Var> boundedVars = new LinkedHashSet<>(bound);
		if (rule instanceof ListallDoRule) {
			boundedVars.add(((ListallDoRule) rule).getVariable());
		} else {
			boundedVars.addAll(rule.getCondition().getFreeVar());
		}
		// Check each action in the rule
		for (Action<?> action : rule.getAction().getActions()) {
			if (action == null) {
				continue;
			} else if (action instanceof ModuleCallAction && ((ModuleCallAction) action).getTarget().isAnonymous()) {
				// Only check nested rules, do not recursively enter other
				// modules!
				for (Rule ruleR : ((ModuleCallAction) action).getTarget().getRules()) {
					reportUnboundVariables(ruleR, boundedVars);
				}
			} else {
				Set<Var> newScope = new LinkedHashSet<>(boundedVars);
				Set<Var> freeActionParVars = new LinkedHashSet<>(action.getFreeVar());
				freeActionParVars.removeAll(newScope);
				if (!freeActionParVars.isEmpty()) {
					if (!(action instanceof SendAction)
							|| ((SendAction) action).getMood() != SentenceMood.INTERROGATIVE) {
						getFirstPass().reportError(ModuleError.ACTION_CALL_UNBOUND_VARIABLE, action.getSourceInfo(),
								getFirstPass().prettyPrintSet(freeActionParVars), action.getName());
					}
				}
			}
		}
	}

	protected void reportUnusedUndefined() {
		// Report unused macro definitions.
		Set<String> macrosDefined = new LinkedHashSet<>(this.macroSymbols.getNames());
		// Remove labels that are used.
		macrosDefined.removeAll(this.macroLabelsUsed);
		// Report unused.
		for (String df : macrosDefined) {
			Symbol symbol = this.macroSymbols.resolve(df);
			getFirstPass().reportWarning(ModuleWarning.MACRO_NEVER_USED, symbol.getSourceInfo(), df);
		}

		// Report undefined KR expressions.
		validateKR();
	}

	/**
	 * Get a substitution that we can apply to term, such that it will not use given
	 * varsInUse.
	 *
	 * @param varsToBeMadeUnique vars that have to be renamed if occurring iin the
	 *                           varsInUse list.
	 * @param varsInUse          set of {@link Var}s that are in use
	 * @return A substitution that will standardize apart variables.
	 */
	// TODO: Move this method to KR interface? #3430. This is quick fix.
	private Substitution makeTermVarsUnique(Set<Var> varsToBeMadeUnique, Set<Var> varsInUse) {
		Set<Var> varsToRename = getSharedVariables(varsToBeMadeUnique, varsInUse);
		Map<Var, Term> subst = new LinkedHashMap<>(varsToRename.size());
		for (Var var : varsToRename) {
			subst.put(var, var.getVariant(varsInUse));
		}
		return this.program.getKRInterface().getSubstitution(subst);
	}

	private Set<Var> getSharedVariables(Set<Var> t1, Set<Var> varsInUse) {
		Set<Var> sharedVars = new LinkedHashSet<>();
		sharedVars.addAll(t1);
		sharedVars.retainAll(varsInUse);
		return sharedVars;
	}

	/**
	 * Checks whether action label is defined in different references (source files)
	 * and reports error if that is the case.
	 *
	 * @param signature Signature of the action or module.
	 * @param symbol    Symbol that already was present in action symbol table.
	 */
	protected void reportDuplicateActionLabelDfs(String signature, Symbol symbol) {
		Symbol duplicate;
		String symbolSource, duplicateSource;
		// Check if duplicate in different source files.
		duplicate = this.actionSymbols.resolve(signature);
		symbolSource = symbol.getSourceInfo().getSource();
		duplicateSource = duplicate.getSourceInfo().getSource();
		if (!symbolSource.equals(duplicateSource)) {
			// TODO: should point to references in program file instead of
			// actual file names.
			getFirstPass().reportError(ModuleError.ACTION_LABEL_DEFINED_BY_MULTIPLE_REFERENCES,
					this.program.getSourceInfo(), signature, symbolSource, duplicateSource);
		}
	}

	/**
	 * Resolves references to macros in a rule condition and references to action
	 * specifications and/or modules in the head of a rule.
	 *
	 * @param rule     A decision rule.
	 * @param usedVars the vars that are already in use (parameters of the module).
	 *                 These are needed to rename vars inside macro calls.
	 */
	protected void resolveReferences(Rule rule, Set<Var> usedVars) {
		Set<Var> contextVars = new LinkedHashSet<>(usedVars);
		contextVars.addAll(rule.getCondition().getFreeVar());
		for (Action<?> action : rule.getAction()) {
			// include variables in potential sub-rules here too, otherwise
			// those might
			// still conflict with a substitution coming from a macro
			if (action instanceof ModuleCallAction && ((ModuleCallAction) action).getTarget().isAnonymous()) {
				for (Rule sub : ((ModuleCallAction) action).getTarget().getRules()) {
					contextVars.addAll(sub.getCondition().getFreeVar());
				}
			}
		}
		for (MentalFormula formula : rule.getCondition().getSubFormulas()) {
			if (formula instanceof Macro) {
				resolveMacroReference((Macro) formula, contextVars);
			}
		}
		resolveActionReferences(rule.getAction(), contextVars);
	}

	/**
	 * Resolves a macro reference with its definition.
	 *
	 * @param macroUse A macro call.
	 * @param usedVars the variables used in the context (rule + module vars).
	 */
	protected void resolveMacroReference(Macro macroUse, Set<Var> usedVars) {
		String signature = macroUse.getSignature();
		MacroSymbol symbol = (MacroSymbol) this.macroSymbols.resolve(signature);
		if (symbol == null) {
			// Report error if no definition could be found.
			getFirstPass().reportError(ModuleError.MACRO_NOT_DEFINED, macroUse.getSourceInfo(), signature);
		} else {
			// All formal parameters of definition are variables.
			// TODO: standardize variables in definition apart from
			// other variables that occur in rule condition.

			Macro macroDf = symbol.getMacro();
			Set<Var> macroVars = new LinkedHashSet<>(macroDf.getFreeVar());
			macroVars.addAll(macroDf.getDefinition().getFreeVar()); // workaround
																	// for #3833

			Substitution standardizeApart = makeTermVarsUnique(macroVars, usedVars);
			Macro renamedMacroDf = macroDf.applySubst(standardizeApart);

			Substitution unifier = renamedMacroDf.mgu(macroUse, this.program.getKRInterface());
			macroUse.setDefinition(renamedMacroDf.getDefinition().applySubst(unifier));

			// Add macro to set of macros used.
			this.macroLabelsUsed.add(signature);
		}
	}

	/**
	 * Resolves references to action specifications and/or modules.
	 *
	 * @param usedActions An action combo.
	 * @param usedVars    All variables used in the context of the call. needed to
	 *                    rename macro calls.
	 */
	protected void resolveActionReferences(ActionCombo usedActions, Set<Var> usedVars) {
		List<Action<?>> resolvedActions = new LinkedList<>();
		for (Action<?> action : usedActions.getActions()) {
			// Resolve reference to user specified action or module call.
			if (action instanceof UserSpecOrModuleCall) {
				Action<?> resolved = resolveActionReferences((UserSpecOrModuleCall) action);
				if (resolved != null) {
					resolvedActions.add(resolved);
				}
			} else if (action instanceof ModuleCallAction) {
				// Process nested rules.
				Module module = ((ModuleCallAction) action).getTarget();
				if (module.isAnonymous()) {
					for (Rule rule : module.getRules()) {
						resolveReferences(rule, usedVars);
					}
				}
				resolvedActions.add(action);
			} else {
				// Nothing to do.
				resolvedActions.add(action);
				// FIXME: correct?!
			}
		}

		// Substitute resolved references.
		usedActions.setActions(resolvedActions);
	}

	/**
	 * Resolve references in user-specified or module call action.
	 *
	 * @param usedAction A user-specified or module call action.
	 * @return Resolved action.
	 */
	private Action<?> resolveActionReferences(UserSpecOrModuleCall usedAction) {
		Action<?> resolvedAction = null;
		String signature = usedAction.getSignature();
		Symbol symbol = this.actionSymbols.resolve(signature);
		if (symbol instanceof ModuleSymbol) {
			Module target = ((ModuleSymbol) symbol).getModule();
			SourceInfo call = usedAction.getSourceInfo();
			if (getFirstPass() instanceof TestValidator) {
				call = target.getDefinition();
			}
			resolvedAction = new ModuleCallAction(target, usedAction.getParameters(), call);
		} else if (symbol instanceof ActionSymbol) {
			UserSpecAction target = ((ActionSymbol) symbol).getActionSpecification();
			resolvedAction = new UserSpecCallAction(target, usedAction.getParameters(), usedAction.getSourceInfo());
			if (target != null) {
				if (target.getPositivePostcondition() != null) {
					this.beliefUpdates.add(target.getPositivePostcondition().getPostCondition());
				}
				if (target.getNegativePostcondition() != null) {
					this.beliefUpdates.add(target.getNegativePostcondition().getPostCondition());
				}
			}
		} else {
			getFirstPass().reportError(ModuleError.ACTION_USED_NEVER_DEFINED, usedAction.getSourceInfo(), signature);
		}
		return resolvedAction;
	}

	protected void processInfoRule(Rule rule) {
		processInfoMsc(rule.getCondition());
		processInfoCombo(rule.getAction());
	}

	protected void processInfoMacro(Macro macro) {
		processInfoMsc(macro.getDefinition());
	}

	/**
	 * Extracts queries on the belief base (including belief, a-goal, and goal-a
	 * literals).
	 *
	 * @param msc A mental state condition.
	 */
	protected void processInfoMsc(MentalStateCondition msc) {
		// Add belief queries.
		for (MentalLiteral literal : getBeliefLiterals(msc)) {
			this.beliefQueries.add(literal.getFormula());
		}
		// Add goal queries.
		for (MentalLiteral literal : getGoalLiterals(msc)) {
			this.goalQueries.add(literal.getFormula());
		}
		// Add percept queries
		for (MentalLiteral literal : getPerceptLiterals(msc)) {
			this.perceptQueries.add(literal.getFormula());
		}
	}

	/**
	 * Extracts dynamically inserted beliefs and goals. Also records which action
	 * labels have been used.
	 *
	 * @param combo An action combo.
	 */
	protected void processInfoCombo(ActionCombo combo) {
		for (Action<?> action : combo) {
			if (action == null) {
				continue;
			}
			// Keep track of action labels that have been used.
			this.actionLabelsUsed.add(action.getSignature());
			// Get items inserted by insert action.
			if (action instanceof InsertAction) {
				Update update = ((InsertAction) action).getUpdate();
				this.beliefUpdates.add(update);
				this.beliefQueries.add(update.toQuery());
				this.insertUpdates.add(update);
			}
			// Get items inserted by delete action.
			if (action instanceof DeleteAction) {
				Update update = ((DeleteAction) action).getUpdate();
				this.beliefUpdates.add(update);
				this.beliefQueries.add(update.toQuery());
				this.deleteUpdates.add(update);

			}
			// Add updates in adopts.
			if (action instanceof AdoptAction) {
				this.goalQueries.add(((AdoptAction) action).getUpdate().toQuery());
				this.adoptUpdates.add(((AdoptAction) action).getUpdate());
			}
			// Add updates in drops
			if (action instanceof DropAction) {
				this.goalQueries.add(((DropAction) action).getUpdate().toQuery());
				this.dropUpdates.add(((DropAction) action).getUpdate());
			}

			// Process nested rules.
			if (action instanceof ModuleCallAction) {
				Module module = ((ModuleCallAction) action).getTarget();
				if (module.isAnonymous()) {
					for (Rule ruleR : module.getRules()) {
						processInfoRule(ruleR);
					}
				}
			}
		}
	}

	public Set<DatabaseFormula> getKnowledge() {
		return Collections.unmodifiableSet(this.knowledge);
	}

	public Set<DatabaseFormula> getBeliefs() {
		return Collections.unmodifiableSet(this.beliefs);
	}

	public Set<Query> getBeliefQueries() {
		return Collections.unmodifiableSet(this.beliefQueries);
	}

	public Set<Query> getGoals() {
		return Collections.unmodifiableSet(this.goals);
	}

	public Set<DatabaseFormula> getGoalDbfs() {
		return Collections.unmodifiableSet(this.goalDbfs);
	}

	public Set<Query> getGoalQueries() {
		return Collections.unmodifiableSet(this.goalQueries);
	}

	public Set<String> getActionsUsed() {
		return this.actionLabelsUsed;
	}

	/**
	 *
	 * @return The percept queries that occur in this module.
	 *
	 */
	public Set<Query> getPerceptQueries() {
		return Collections.unmodifiableSet(this.perceptQueries);
	}

	/**
	 *
	 * @return all updates done in insert actions
	 */
	public Set<Update> getInsertUpdates() {
		return Collections.unmodifiableSet(this.insertUpdates);
	}

	/**
	 *
	 * @return all updates done in delete actions
	 */
	public Set<Update> getDeleteUpdates() {
		return Collections.unmodifiableSet(this.deleteUpdates);
	}

	/**
	 *
	 * @return all updates done in insert actions
	 */
	public Set<Update> getAdoptUpdates() {
		return Collections.unmodifiableSet(this.adoptUpdates);
	}

	/**
	 *
	 * @return all updates done in insert actions
	 */
	public Set<Update> getDropUpdates() {
		return Collections.unmodifiableSet(this.dropUpdates);
	}

	/**
	 *
	 * @return all belief update actions found in the program. Both coming from
	 *         explicit module actions and from action specs.
	 */
	public Set<Update> getBeliefUpdates() {
		return Collections.unmodifiableSet(this.beliefUpdates);
	}
}
