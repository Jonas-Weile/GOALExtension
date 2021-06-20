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
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;

import cognitiveKr.CognitiveKR;
import krTools.exceptions.KRInitFailedException;
import krTools.exceptions.ParserException;
import krTools.language.Query;
import krTools.language.Term;
import krTools.language.Update;
import krTools.language.Var;
import krTools.parser.SourceInfo;
import languageTools.analyzer.FileRegistry;
import languageTools.analyzer.Validator;
import languageTools.analyzer.ValidatorSecondPass;
import languageTools.errors.module.ModuleError;
import languageTools.errors.module.ModuleErrorStrategy;
import languageTools.errors.module.ModuleWarning;
import languageTools.parser.GOALLexer;
import languageTools.parser.InputStreamPosition;
import languageTools.parser.MOD2GParser;
import languageTools.parser.MOD2GParser.ActionContext;
import languageTools.parser.MOD2GParser.ActioncomboContext;
import languageTools.parser.MOD2GParser.ExitoptionContext;
import languageTools.parser.MOD2GParser.FocusoptionContext;
import languageTools.parser.MOD2GParser.GeneralactionContext;
import languageTools.parser.MOD2GParser.LearnclauseContext;
import languageTools.parser.MOD2GParser.MacroContext;
import languageTools.parser.MOD2GParser.MentalatomContext;
import languageTools.parser.MOD2GParser.MentalliteralContext;
import languageTools.parser.MOD2GParser.MentalopContext;
import languageTools.parser.MOD2GParser.ModuleContext;
import languageTools.parser.MOD2GParser.MscContext;
import languageTools.parser.MOD2GParser.OptionContext;
import languageTools.parser.MOD2GParser.OrderoptionContext;
import languageTools.parser.MOD2GParser.RefContext;
import languageTools.parser.MOD2GParser.RulesContext;
import languageTools.parser.MOD2GParser.SelectorContext;
import languageTools.parser.MOD2GParser.SelectoractionContext;
import languageTools.parser.MOD2GParser.SelectoropContext;
import languageTools.parser.MOD2GParser.StringContext;
import languageTools.parser.MOD2GParser.TimeoutoptionContext;
import languageTools.parser.MOD2GParser.UsecaseContext;
import languageTools.parser.MOD2GParser.UseclauseContext;
import languageTools.parser.MOD2GParserVisitor;
import languageTools.program.Program;
import languageTools.program.agent.Module;
import languageTools.program.agent.Module.ExitCondition;
import languageTools.program.agent.Module.FocusMethod;
import languageTools.program.agent.Module.RuleEvaluationOrder;
import languageTools.program.agent.actions.Action;
import languageTools.program.agent.actions.ActionCombo;
import languageTools.program.agent.actions.AdoptAction;
import languageTools.program.agent.actions.CancelTimerAction;
import languageTools.program.agent.actions.DeleteAction;
import languageTools.program.agent.actions.DropAction;
import languageTools.program.agent.actions.ExitModuleAction;
import languageTools.program.agent.actions.InsertAction;
import languageTools.program.agent.actions.LogAction;
import languageTools.program.agent.actions.ModuleCallAction;
import languageTools.program.agent.actions.PrintAction;
import languageTools.program.agent.actions.SendAction;
import languageTools.program.agent.actions.SleepAction;
import languageTools.program.agent.actions.StartTimerAction;
import languageTools.program.agent.actions.SubscribeAction;
import languageTools.program.agent.actions.UnsubscribeAction;
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
import languageTools.program.agent.msc.SentLiteral;
import languageTools.program.agent.msg.SentenceMood;
import languageTools.program.agent.rules.ForallDoRule;
import languageTools.program.agent.rules.IfThenRule;
import languageTools.program.agent.rules.ListallDoRule;
import languageTools.program.agent.rules.Rule;
import languageTools.program.agent.selector.Selector;
import languageTools.program.agent.selector.Selector.SelectorType;
import languageTools.program.mas.UseClause;
import languageTools.program.mas.UseClause.UseCase;
import languageTools.symbolTable.SymbolTable;
import languageTools.symbolTable.agent.MacroSymbol;
import languageTools.symbolTable.agent.ModuleSymbol;

/**
 * Validates a module file and constructs a module.
 */
public class ModuleValidator extends Validator<GOALLexer, MOD2GParser, ModuleErrorStrategy, Module>
		implements MOD2GParserVisitor<Object> {
	private MOD2GParser parser;
	private static ModuleErrorStrategy strategy = null;

	/**
	 * For agent validation, we use two symbol tables. The first is used for actions
	 * and modules as action labels and module names cannot have the same signature
	 * because a call cannot be resolved in that case. The second is used for
	 * macros.
	 */
	private final SymbolTable actionSymbols = new SymbolTable();
	private final SymbolTable macroSymbols = new SymbolTable();

	/**
	 * Creates validator for file with file name.
	 *
	 * @param filename Name of a file.
	 */
	public ModuleValidator(String filename, FileRegistry registry) {
		super(filename, registry);
	}

	@Override
	protected ParseTree startParser() {
		return this.parser.module();
	}

	@Override
	protected ModuleErrorStrategy getTheErrorStrategy() {
		if (strategy == null) {
			strategy = new ModuleErrorStrategy();
		}
		return strategy;
	}

	/**
	 * @return Symbol table with action and module symbols.
	 */
	public SymbolTable getActionSymbols() {
		return this.actionSymbols;
	}

	/**
	 * @return Symbol table with macro symbols.
	 */
	public SymbolTable getMacroSymbols() {
		return this.macroSymbols;
	}

	/**
	 * Validation of module that resolves references to action, macro, and module
	 * symbols, and checks whether all predicates used have been defined.
	 */
	@Override
	protected ValidatorSecondPass createSecondPass() {
		return new ModuleValidatorSecondPass(this);
	}

	@Override
	protected GOALLexer getNewLexer(CharStream stream) {
		return new GOALLexer(stream);
	}

	@Override
	protected MOD2GParser getNewParser(TokenStream stream) {
		this.parser = new MOD2GParser(stream);
		return this.parser;
	}

	@Override
	protected Module getNewProgram(File file) throws IOException {
		return new Module(this.registry, new InputStreamPosition(0, 0, 0, 0, file.getCanonicalPath()));
	}

	@Override
	public Module getProgram() {
		return (Module) super.getProgram();
	}

	/**
	 * Calls {@link ParseTree#accept} on the specified tree.
	 */
	@Override
	public Void visit(ParseTree tree) {
		tree.accept(this);
		return null; // Java says must return something even when Void
	}

	// -------------------------------------------------------------
	// Module
	// -------------------------------------------------------------

	@Override
	public Void visitModule(ModuleContext ctx) {
		Program program = getProgram();
		// Process use clauses.
		for (UseclauseContext useclausectx : ctx.useclause()) {
			visitUseclause(useclausectx);
		}

		// VALIDATE: Check which (single) KR language is used.
		// Based on KR file references only.
		boolean resolved = program.resolveKRInterface();
		// get list of uri-s from referenced kr stuff
		List<UseClause> krClauses = program.getKRUseClauses();
		List<URI> uris = new LinkedList<>();
		for (UseClause krClause : krClauses) {
			uris.addAll(krClause.getResolvedUriReference());
		}
		if (resolved) {
			try {
				// initialize it
				program.getKRInterface().initialize(uris);
			} catch (KRInitFailedException e) {
				reportError(ModuleError.KR_COULDNOT_INITIALIZE, ctx, e.getMessage());
			}
		} else {
			if (uris.isEmpty()) {
				reportError(ModuleError.KR_COULDNOT_RESOLVE_NO_KR_USECLAUSES, ctx);
			} else {
				reportError(ModuleError.KR_COULDNOT_RESOLVE_DIFFERENT_KRS_USED, ctx);
			}
			return null; // makes no sense to go on without a KRI
		}

		// Process belief/goal filters (for learning)
		for (LearnclauseContext learnctx : ctx.learnclause()) {
			visitLearnclause(learnctx);
		}

		// Process options: exit, focus, order.
		for (OptionContext optionctx : ctx.option()) {
			visitOption(optionctx);
		}

		// Process macros.
		for (MacroContext macroctx : ctx.macro()) {
			visitMacro(macroctx);
		}

		// Get module name.
		if (ctx.ID() != null) {
			String name = ctx.ID().getText();
			getProgram().setName(name);
			SourceInfo definition = getSourceInfo(ctx.ID());
			getProgram().setDefinition(definition);
			if (!FilenameUtils.getBaseName(definition.getSource()).equals(name)) {
				reportWarning(ModuleWarning.MODULE_NAME_MISMATCH, ctx.ID());
			}
		}

		// Get module parameters.
		if (ctx.PARLIST() != null) {
			List<Var> vars = visitVARLIST(ctx.PARLIST(), ctx);
			getProgram().setParameters(vars);
			// VALIDATE: Check for duplicate parameters.
			Set<Var> parameterCheck = new LinkedHashSet<>(vars.size());
			for (Var var : vars) {
				if (parameterCheck.contains(var)) {
					reportError(ModuleError.DUPLICATE_PARAMETER, ctx, var.toString());
				} else {
					parameterCheck.add(var);
				}
			}
		}

		// Process rules. Record variables that are used.
		Set<Var> used = new LinkedHashSet<>();
		for (RulesContext rulectx : ctx.rules()) {
			Rule rule = visitRules(rulectx);
			if (rule != null) {
				getProgram().addRule(rule);
				used.addAll(rule.getCondition().getFreeVar());
				for (Action<?> action : rule.getAction()) {
					used.addAll(action.getFreeVar());
				}
			}
		}

		// VALIDATE: Check for parameters that are never used.
		for (Var var : getProgram().getParameters()) {
			if (!used.contains(var)) {
				reportWarning(ModuleWarning.PARAMETER_NEVER_USED, ctx, var.toString());
			}
		}

		// Add module symbol to table (to allow for recursive calls).
		this.actionSymbols
				.define(new ModuleSymbol(getProgram().getSignature(), getProgram(), getProgram().getSourceInfo()));

		return null; // Java says must return something even when Void
	}

	@Override
	public String visitRef(RefContext ctx) {
		if (ctx == null) {
			return "";
		} else if (ctx.string() != null) {
			return visitString(ctx.string());
		} else {
			String path = "";
			for (String component : ctx.getText().split("\\.")) {
				path = FilenameUtils.concat(path, component);
			}
			return path;
		}
	}

	@Override
	public Void visitUseclause(UseclauseContext ctx) {
		// Get use case.
		UseCase useCase = UseCase.getUseCase(visitUsecase(ctx.usecase()));

		for (RefContext ref : ctx.ref()) {
			// Get reference.
			String reference = visitRef(ref);

			// Create use clause and resolve reference.
			UseClause useClause = new UseClause(reference, useCase, getPathRelativeToSourceFile(""),
					getSourceInfo(ref));
			List<URI> files = useClause.resolveReference();
			if (files.isEmpty()) {
				reportError(ModuleError.REFERENCE_COULDNOT_FIND, ref, reference);
			}

			// Add use clause to module.
			if (!getProgram().addUseClause(useClause)) {
				reportError(ModuleError.REFERENCE_DUPLICATE, ref, reference);
			}
		}

		return null; // Java says must return something even when Void
	}

	@Override
	public String visitUsecase(UsecaseContext ctx) {
		return (ctx == null) ? "" : ctx.getText();
	}

	@Override
	public Void visitLearnclause(LearnclauseContext ctx) {
		List<Term> terms = new LinkedList<>();
		if (ctx.PARLIST() != null) {
			for (Term term : visitPARLIST(ctx.PARLIST(), getSourceInfo(ctx.PARLIST()))) {
				terms.add(term);
			}
		}
		for (Term term : terms) {
			Set<String> sigs = new LinkedHashSet<>(0);
			try {
				CognitiveKR ckr = getCognitiveKR();
				sigs = ckr.getUsedSignatures(term);
				reportEmbeddedLanguageErrors(ckr);
			} catch (ParserException e) {
				reportParsingException(e);
			}
			for (String sig : sigs) {
				if (ctx.LEARNBEL() != null) {
					getProgram().addLearnedBelief(sig);
				} else { // learngoal
					getProgram().addLearnedGoal(sig);
				}
			}
		}

		return null;
	}

	@Override
	public Void visitOption(OptionContext ctx) {
		if (ctx.exitoption() != null) {
			visitExitoption(ctx.exitoption());
		} else if (ctx.focusoption() != null) {
			visitFocusoption(ctx.focusoption());
		} else if (ctx.orderoption() != null) {
			visitOrderoption(ctx.orderoption());
		}

		return null; // Java says must return something even when Void
	}

	@Override
	public Void visitExitoption(ExitoptionContext ctx) {
		if (ctx.value != null) {
			try {
				if (!getProgram().setExitCondition(ExitCondition.valueOf(ctx.value.getText().toUpperCase()))) {
					// Report warning if duplicate.
					reportWarning(ModuleWarning.DUPLICATE_OPTION, ctx, "exit");
				}
			} catch (IllegalArgumentException e) {
				// Simply ignore, parser will report problem.
			}
		}
		return null; // Java says must return something even when Void
	}

	@Override
	public Void visitFocusoption(FocusoptionContext ctx) {
		try {
			if (!getProgram().setFocusMethod(FocusMethod.valueOf(ctx.value.getText().toUpperCase()))) {
				// Report warning if duplicate.
				reportWarning(ModuleWarning.DUPLICATE_OPTION, ctx, "focus");
			}
		} catch (IllegalArgumentException e) {
			// simply ignore, parser will report problem
		}
		return null; // Java says must return something even when Void
	}

	@Override
	public Void visitOrderoption(OrderoptionContext ctx) {
		String order = (ctx == null || ctx.value == null) ? "" : ctx.value.getText().toUpperCase();
		try {
			if (getProgram().getRuleEvaluationOrder() == null) {
				getProgram().setRuleEvaluationOrder(RuleEvaluationOrder.valueOf(order));
			} else {
				reportWarning(ModuleWarning.DUPLICATE_OPTION, ctx, "order");
			}
		} catch (IllegalArgumentException e) {
			// simply ignore, parser will report problem
		}
		return null; // Java says must return something even when Void
	}

	@Override
	public Void visitMacro(MacroContext ctx) {
		// Get macro name.
		String name = null;
		if (ctx.ID() != null) {
			name = ctx.ID().getText();
		}

		// Get macro parameters.
		List<Term> parameters = new LinkedList<>();
		if (ctx.PARLIST() != null) {
			for (Var var : visitVARLIST(ctx.PARLIST(), ctx)) {
				if (parameters.contains(var)) {
					reportError(ModuleError.DUPLICATE_PARAMETER, ctx, var.toString());
				}
				parameters.add(var);
			}
		}

		// Get macro definition.
		MentalStateCondition msc = null;
		if (ctx.msc() != null) {
			msc = visitMsc(ctx.msc());
		}

		if (name != null && msc != null) {
			Macro macro = new Macro(name, parameters, msc, getSourceInfo(ctx));
			getProgram().addMacro(macro);

			// Add macro to symbol table.
			if (!this.macroSymbols.define(new MacroSymbol(macro.getSignature(), macro, macro.getSourceInfo()))) {
				// report duplicate use of macro symbol
				reportError(ModuleError.MACRO_DUPLICATE_NAME, ctx, macro.getSignature());
			}

			// Check whether macro parameters have been used in definition.
			if (!msc.getFreeVar().containsAll(macro.getParameters())) {
				reportError(ModuleError.MACRO_PARAMETERS_NOT_IN_DEFINITION, macro.getSourceInfo(),
						prettyPrintSet(new LinkedHashSet<>(macro.getParameters())), msc.toString());
			}

			// check if macros are called from inside the macro (not allowed, as
			// we can not substitute variables in the sub-macro in preprocessor)
			for (MentalFormula formula : msc.getSubFormulas()) {
				if (formula instanceof Macro) {
					reportError(ModuleError.MACRO_FROM_MACRO_CALL, formula.getSourceInfo(), formula.toString(),
							macro.toString());
				}
			}

			// TODO: Check that same macro parameter is not used twice.
		}

		return null; // Java says must return something even when Void
	}

	@Override
	public Rule visitRules(RulesContext ctx) {
		// Get mental state condition.
		MentalStateCondition msc = visitMsc(ctx.msc());

		// VALIDATE: Check that variables used in selectors are bound.
		checkSelectorVarBound(msc, getProgram().getParameters());

		// Get action combo.
		ActionCombo combo = null;
		if (ctx.actioncombo() != null) {
			combo = visitActioncombo(ctx.actioncombo());
		}

		// Nested rules: create anonymous module.
		if (ctx.rules() != null && !ctx.rules().isEmpty()) {
			Module module = new Module(this.registry, getSourceInfo(ctx));
			// Inherit KR interface and rule evaluation order.
			module.setKRInterface(getProgram().getKRInterface());
			module.setRuleEvaluationOrder(getProgram().getRuleEvaluationOrder());
			// The module needs a name (for learning)
			module.setName(getProgram().getName() + "_" + module.getSourceInfo().getLineNumber());
			module.setAnonymous();

			for (RulesContext rulectx : ctx.rules()) {
				module.addRule(visitRules(rulectx));
			}
			getProgram().getMap().merge(module.getMap());

			ModuleCallAction action = new ModuleCallAction(module, new ArrayList<Term>(0), module.getSourceInfo());
			combo = new ActionCombo(action.getSourceInfo());
			combo.addAction(action);
		}

		// Construct rule and add to module.
		Rule rule = null;
		if (msc != null && combo != null) {
			if (ctx.IF() != null) {
				rule = new IfThenRule(msc, combo, getSourceInfo(ctx));
			} else if (ctx.FORALL() != null) {
				rule = new ForallDoRule(msc, combo, getSourceInfo(ctx));
			} else if (ctx.LISTALL() != null) {
				// Get var.
				Var var = null;
				// Check if there is a parser problem with variable; if so,
				// don't do anything (parser will report error).
				if (ctx.VAR() != null) {
					try {
						CognitiveKR ckr = getCognitiveKR();
						var = ckr.visitListallVar(ctx.VAR().getText(), getSourceInfo(ctx.VAR()));
						reportEmbeddedLanguageErrors(ckr);
					} catch (ParserException e) {
						reportParsingException(e);
					}
				}
				rule = new ListallDoRule(msc, var, combo, getSourceInfo(ctx));
			}
		}

		return rule;
	}

	@Override
	public MentalStateCondition visitMsc(MscContext ctx) {
		List<MentalFormula> formulas = new LinkedList<>();
		for (MentalliteralContext literalctx : ctx.mentalliteral()) {
			MentalFormula formula = visitMentalliteral(literalctx);
			if (formula == null) {
				if (literalctx.TRUE() == null) {
					reportError(ModuleError.CONDITION_INVALID, ctx, literalctx.getText());
				}
			} else {
				formulas.add(formula);
			}
		}
		return new MentalStateCondition(formulas, getSourceInfo(ctx));
	}

	@Override
	public MentalFormula visitMentalliteral(MentalliteralContext ctx) {
		if (ctx.ID() != null && ctx.PARLIST() != null) {
			try {
				CognitiveKR ckr = getCognitiveKR();
				List<Term> args = ckr.visitArguments(ctx.PARLIST().getText(), getSourceInfo(ctx.PARLIST()));
				reportEmbeddedLanguageErrors(ckr);
				return new Macro(ctx.ID().getText(), args, null, getSourceInfo(ctx));
			} catch (ParserException e) {
				reportParsingException(e);
				return null;
			}
		} else if (ctx.NOT() != null) {
			MentalLiteral atom = visitMentalatom(ctx.mentalatom());
			if (atom != null) {
				atom.setPolarity(false);
			}
			return atom;
		} else if (ctx.mentalatom() != null) {
			return visitMentalatom(ctx.mentalatom());
		} else {
			return null;
		}
	}

	@Override
	public MentalLiteral visitMentalatom(MentalatomContext ctx) {
		// Get selector.
		Selector selector = visitSelector(ctx.selector());

		// Get KR.
		String krFragment = (ctx.PARLIST() == null) ? "" : removeLeadTrailCharacters(ctx.PARLIST().getText());
		String op = visitMentalop(ctx.mentalop());
		if (krFragment.isEmpty() || op.isEmpty()) {
			return null;
		}
		SourceInfo info = getSourceInfo(ctx);
		SourceInfo krInfo = getSourceInfo(ctx.PARLIST());
		krInfo = (krInfo == null) ? null
				: new InputStreamPosition(krInfo.getLineNumber(), krInfo.getCharacterPosition() + 1,
						krInfo.getStartIndex() + 1, krInfo.getStopIndex() - 1, krInfo.getSource());

		// Construct literal.
		try {
			MentalLiteral returned = null;
			CognitiveKR ckr = getCognitiveKR();
			if (getTokenName(MOD2GParser.BELIEF_OP).equals(op)) {
				Query bel = ckr.visitBeliefQuery(krFragment, krInfo);
				if (bel != null) {
					returned = new BelLiteral(true, selector, bel, ckr.getUsedSignatures(bel), info);
				}
			} else if (getTokenName(MOD2GParser.GOAL_OP).equals(op)) {
				Query goal = ckr.visitGoalQuery(krFragment, krInfo);
				if (goal != null) {
					returned = new GoalLiteral(true, selector, goal, ckr.getUsedSignatures(goal), info);
				}
			} else if (getTokenName(MOD2GParser.AGOAL_OP).equals(op)) {
				Query agoal = ckr.visitGoalQuery(krFragment, krInfo);
				if (agoal != null) {
					returned = new AGoalLiteral(true, selector, agoal, ckr.getUsedSignatures(agoal), info);
				}
			} else if (getTokenName(MOD2GParser.GOALA_OP).equals(op)) {
				Query goala = ckr.visitGoalQuery(krFragment, krInfo);
				if (goala != null) {
					returned = new GoalALiteral(true, selector, goala, ckr.getUsedSignatures(goala), info);
				}
			} else if (getTokenName(MOD2GParser.PERCEPT_OP).equals(op)) {
				Query percept = ckr.visitPercept(krFragment, krInfo);
				if (percept != null) {
					returned = new PerceptLiteral(true, selector, percept, ckr.getUsedSignatures(percept), info);
				}
			} else if (getTokenName(MOD2GParser.SENT_OP).equals(op)
					|| getTokenName(MOD2GParser.SENT_IND_OP).equals(op)) {
				Query sent1 = ckr.visitSent(krFragment, krInfo);
				if (sent1 != null) {
					returned = new SentLiteral(true, selector, sent1, SentenceMood.INDICATIVE,
							ckr.getUsedSignatures(sent1), info);
				}
			} else if (getTokenName(MOD2GParser.SENT_INT_OP).equals(op)) {
				Query sent2 = ckr.visitSent(krFragment, krInfo);
				if (sent2 != null) {
					returned = new SentLiteral(true, selector, sent2, SentenceMood.INTERROGATIVE,
							ckr.getUsedSignatures(sent2), info);
				}
			} else if (getTokenName(MOD2GParser.SENT_IMP_OP).equals(op)) {
				Query sent3 = ckr.visitSent(krFragment, krInfo);
				if (sent3 != null) {
					returned = new SentLiteral(true, selector, sent3, SentenceMood.IMPERATIVE,
							ckr.getUsedSignatures(sent3), info);
				}
			}
			reportEmbeddedLanguageErrors(ckr);
			return returned;
		} catch (ParserException e) {
			reportParsingException(e);
			return null;
		}
	}

	@Override
	public String visitMentalop(MentalopContext ctx) {
		return (ctx == null) ? "" : ctx.getText();
	}

	@Override
	public ActionCombo visitActioncombo(ActioncomboContext ctx) {
		ActionCombo combo = new ActionCombo(getSourceInfo(ctx));
		for (ActionContext actionCtx : ctx.action()) {
			Action<?> action = visitAction(actionCtx);
			if (action == null) {
				reportError(ModuleError.ACTION_INVALID, ctx, actionCtx.getText());
			} else {
				combo.addAction(action);
				// VALIDATE: Check whether action can be reached.
				if (combo.size() > 1 && combo.getActions().get(combo.size() - 2) instanceof ExitModuleAction) {
					reportWarning(ModuleWarning.EXITMODULE_CANNOT_REACH, actionCtx, action.toString());
				}
			}
		}
		return combo;
	}

	@Override
	public Action<?> visitAction(ActionContext ctx) {
		if (ctx.ID() != null) {
			// User-defined or module call action.
			List<Term> parameters = visitPARLIST(ctx.PARLIST(), getSourceInfo(ctx.PARLIST()));
			return new UserSpecOrModuleCall(ctx.ID().getText(), parameters, getSourceInfo(ctx));
		} else if (ctx.selectoraction() != null) {
			return visitSelectoraction(ctx.selectoraction());
		} else if (ctx.generalaction() != null) {
			return visitGeneralaction(ctx.generalaction());
		} else {
			return null;
		}
	}

	@Override
	public Action<?> visitSelectoraction(SelectoractionContext ctx) {
		// Get selector.
		Selector selector = visitSelector(ctx.selector());

		// Get KR content.
		String krFragment = (ctx.PARLIST() == null) ? "" : removeLeadTrailCharacters(ctx.PARLIST().getText());
		if (!krFragment.isEmpty() && ctx.op != null) {
			// Get action operator
			SourceInfo info = getSourceInfo(ctx);
			SourceInfo krInfo = getSourceInfo(ctx.PARLIST());
			krInfo = (krInfo == null) ? null
					: new InputStreamPosition(krInfo.getLineNumber(), krInfo.getCharacterPosition() + 1,
							krInfo.getStartIndex() + 1, krInfo.getStopIndex() - 1, krInfo.getSource());
			try {
				Action<?> returned = null;
				CognitiveKR ckr = getCognitiveKR();
				switch (ctx.op.getType()) {
				case MOD2GParser.INSERT:
					Update insert = ckr.visitBeliefInsert(krFragment, krInfo);
					if (insert != null) {
						returned = new InsertAction(selector, insert, info);
					}
					break;
				case MOD2GParser.DELETE:
					Update delete = ckr.visitBeliefDelete(krFragment, krInfo);
					if (delete != null) {
						returned = new DeleteAction(selector, delete, info);
					}
					break;
				case MOD2GParser.ADOPT:
					Update adopt = ckr.visitGoalAdopt(krFragment, krInfo);
					if (adopt != null) {
						returned = new AdoptAction(selector, adopt, info);
					}
					break;
				case MOD2GParser.DROP:
					Update drop = ckr.visitGoalDrop(krFragment, krInfo);
					if (drop != null) {
						returned = new DropAction(selector, drop, info);
					}
					break;
				case MOD2GParser.SEND:
				case MOD2GParser.SEND_IND:
				case MOD2GParser.SEND_INT:
				case MOD2GParser.SEND_IMP:
					if (selector.getType() == SelectorType.THIS) {
						reportError(ModuleError.SEND_INVALID_SELECTOR, info, selector.toString());
					}
					Update send = ckr.visitSend(krFragment, krInfo);
					if (send != null) {
						returned = new SendAction(ctx.op.getType(), selector, send, info);
					}
					break;
				default:
					break;
				}
				reportEmbeddedLanguageErrors(ckr);
				return returned;
			} catch (ParserException e) {
				reportParsingException(e);
				return null;
			}
		} else {
			return null;
		}
	}

	@Override
	public Action<?> visitGeneralaction(GeneralactionContext ctx) {
		// Get parameters.
		List<Term> parameters = null;
		if (ctx.PARLIST() != null) {
			try {
				CognitiveKR ckr = getCognitiveKR();
				parameters = ckr.visitArguments(ctx.PARLIST().getText(), getSourceInfo(ctx.PARLIST()));
				reportEmbeddedLanguageErrors(ckr);
			} catch (ParserException e) {
				reportParsingException(e);
			}
		}

		Action<?> returned = null;
		if (ctx.op != null) {
			SourceInfo info = getSourceInfo(ctx);
			switch (ctx.op.getType()) {
			case MOD2GParser.EXITMODULE:
				returned = new ExitModuleAction(info);
				break;
			case MOD2GParser.LOG:
				if (parameters != null && !parameters.isEmpty()) {
					returned = new LogAction(parameters, info);
				}
				break;
			case MOD2GParser.PRINT:
				if (parameters != null && !parameters.isEmpty()) {
					returned = new PrintAction(parameters, info);
				}
				break;
			case MOD2GParser.SLEEP:
				if (parameters != null && parameters.size() == 1) {
					returned = new SleepAction(parameters.get(0), info);
				}
				break;
			case MOD2GParser.SUBSCRIBE:
				if (parameters != null && !parameters.isEmpty()) {
					returned = new SubscribeAction(parameters, info);
				}
				break;
			case MOD2GParser.UNSUBSCRIBE:
				if (parameters != null && !parameters.isEmpty()) {
					returned = new UnsubscribeAction(parameters, info);
				}
				break;
			case MOD2GParser.STARTTIMER:
				if (parameters != null && parameters.size() == 3) {
					returned = new StartTimerAction(parameters, info);
				}
				break;
			case MOD2GParser.CANCELTIMER:
				if (parameters != null && parameters.size() == 1) {
					returned = new CancelTimerAction(parameters, info);
				}
				break;
			default:
				break;
			}
		}

		return returned;
	}

	@Override
	public Selector visitSelector(SelectorContext ctx) {
		if (ctx == null) {
			// return default
			return Selector.getDefault(getSourceInfo(ctx));
		} else if (ctx.PARLIST() != null) {
			List<Term> terms = visitPARLIST(ctx.PARLIST(), getSourceInfo(ctx.PARLIST()));
			return new Selector(terms, getSourceInfo(ctx));
		} else {
			return new Selector(Selector.SelectorType.valueOf(visitSelectorop(ctx.selectorop()).toUpperCase()),
					getSourceInfo(ctx));
		}
	}

	@Override
	public String visitSelectorop(SelectoropContext ctx) {
		return (ctx == null) ? "" : ctx.getText();
	}

	@Override
	public String visitString(StringContext ctx) {
		String str = "";
		if (ctx.StringLiteral() != null) {
			for (TerminalNode literal : ctx.StringLiteral()) {
				String[] parts = literal.getText().split("(?<!\\\\)\"", 0);
				if (parts.length > 1) {
					str += parts[1].replace("\\\"", "\"");
				}
			}
		}
		if (ctx.SingleQuotedStringLiteral() != null) {
			for (TerminalNode literal : ctx.SingleQuotedStringLiteral()) {
				String[] parts = literal.getText().split("(?<!\\\\)'", 0);
				if (parts.length > 1) {
					str += parts[1].replace("\\'", "'");
				}
			}
		}
		return str;
	}

	// -------------------------------------------------------------
	// Helper methods
	// -------------------------------------------------------------

	/**
	 * @param token A token index (can be found in GOAL grammar)
	 * @return The name of the token.
	 */
	public static String getTokenName(int token) {
		return StringUtils.remove(MOD2GParser.VOCABULARY.getDisplayName(token), '\'');
	}

	/**
	 * Checks whether all variables used in selectors in the list of mental formulas
	 * are bound. Variables in the given set of variables are considered bound.
	 * Reports variable(s) that are not bound as a validation error.
	 *
	 * @param formulas  A list of formulas to check.
	 * @param boundVars A set of variables that may bound variables in selectors in
	 *                  the formulas.
	 * @return {@code true} if all variables that occur in selectors are bound,
	 *         {@code false} otherwise.
	 */
	private boolean checkSelectorVarBound(MentalStateCondition msc, List<Var> variables) {
		List<Var> boundedVars = new ArrayList<>(variables);
		boolean bound = true;
		for (MentalFormula formula : msc.getSubFormulas()) {
			if (formula instanceof MentalLiteral) {
				// Add variables bound by this sub-formula to the set of bounded
				// vars.
				boundedVars.addAll(formula.getFreeVar());
			} else if (formula instanceof Macro) {
				// Recursively check whether variables in selectors in macro
				// definition are bound.
				MentalStateCondition macroDf = ((Macro) formula).getDefinition();
				if (macroDf != null) {
					bound &= checkSelectorVarBound(macroDf, boundedVars);
					// Add formal parameters of macro to set of bounded vars.
					for (Term term : ((Macro) formula).getParameters()) {
						if (term.isVar()) {
							boundedVars.add((Var) term);
						}
					}
				}
			}
		}
		return bound;
	}

	@Override
	public Void visitTimeoutoption(TimeoutoptionContext ctx) {
		// Used in test2g only...
		return null;
	}
}
