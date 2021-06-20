package org.eclipse.gdt.parser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Pattern;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.TokenStream;
import org.antlr.v4.runtime.tree.ErrorNodeImpl;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.dltk.ast.ASTListNode;
import org.eclipse.dltk.ast.declarations.Argument;
import org.eclipse.dltk.ast.declarations.MethodDeclaration;
import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
import org.eclipse.dltk.ast.declarations.TypeDeclaration;
import org.eclipse.dltk.ast.expressions.StringLiteral;
import org.eclipse.dltk.ast.references.SimpleReference;
import org.eclipse.dltk.ast.statements.Block;

import cognitiveKr.CognitiveKR;
import krTools.exceptions.ParserException;
import krTools.language.Query;
import krTools.language.Term;
import krTools.language.Var;
import languageTools.analyzer.FileRegistry;
import languageTools.analyzer.Validator;
import languageTools.analyzer.ValidatorSecondPass;
import languageTools.errors.module.ModuleErrorStrategy;
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
import languageTools.program.agent.Module;
import languageTools.program.agent.actions.UserSpecOrModuleCall;
import languageTools.program.agent.msc.Macro;
import languageTools.program.agent.selector.Selector;
import languageTools.program.mas.UseClause;
import languageTools.program.mas.UseClause.UseCase;
import swiprolog.language.PrologCompound;
import swiprolog.language.PrologTerm;

public class ModuleWalker extends Validator<GOALLexer, MOD2GParser, ModuleErrorStrategy, Module>
		implements MOD2GParserVisitor<Object> {
	private MOD2GParser parser;
	private static ModuleErrorStrategy strategy = null;
	private ModuleDeclaration dltk = new ModuleDeclaration(0);

	public ModuleWalker(String filename, FileRegistry registry) {
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

	@Override
	protected ValidatorSecondPass createSecondPass() {
		return null;
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

	public ModuleDeclaration getDeclaration() {
		return this.dltk;
	}

	@Override
	public Void visit(ParseTree tree) {
		tree.accept(this);
		return null;
	}

	// -------------------------------------------------------------
	// Module
	// -------------------------------------------------------------

	@Override
	public Void visitModule(ModuleContext ctx) {
		final ModuleDeclaration declaration = new ModuleDeclaration(ctx.getText().length());

		// Process use clauses.
		for (UseclauseContext useclausectx : ctx.useclause()) {
			SimpleReference useclause = visitUseclause(useclausectx);
			if (useclause != null) {
				declaration.addStatement(useclause);
			}
		}

		for (LearnclauseContext learnclausectx : ctx.learnclause()) {
			SimpleReference learnclause = visitLearnclause(learnclausectx);
			if (learnclause != null) {
				declaration.addStatement(learnclause);
			}
		}

		if (!getProgram().resolveKRInterface()) {
			this.dltk = declaration;
			return null;
		}

		// Process options: exit, focus, order.
		for (OptionContext optionctx : ctx.option()) {
			SimpleReference option = visitOption(optionctx);
			if (option != null) {
				declaration.addStatement(option);
			}
		}

		// Process macros.
		for (MacroContext macroctx : ctx.macro()) {
			TypeDeclaration macro = visitMacro(macroctx);
			if (macro != null) {
				declaration.addStatement(macro);
			}
		}

		// Get module name.
		StringLiteral moduleName = (ctx.ID() == null) ? null
				: new StringLiteral(ctx.ID().getSymbol().getStartIndex(), ctx.ID().getSymbol().getStopIndex() + 1,
						ctx.ID().getText());
		final MethodDeclaration module = new MethodDeclaration(moduleName.getValue(), moduleName.start(),
				moduleName.end(), ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex() + 1);
		// Get module parameters.
		if (ctx.PARLIST() != null && // need to check whether something has gone
										// seriously wrong here
				!(ctx.PARLIST() instanceof ErrorNodeImpl)) {
			List<Var> vars = visitVARLIST(ctx.PARLIST(), ctx);
			for (Var var : vars) {
				SimpleReference arg = new SimpleReference(var.getSourceInfo().getStartIndex(),
						var.getSourceInfo().getStopIndex() + 1, var.toString());
				module.addArgument(new Argument(arg, arg.start(), arg.end(), null, 0));
			}
		}

		// Process rules.
		final ASTListNode body = new ASTListNode(ctx.getStart().getStartIndex() + 1, ctx.getStop().getStopIndex());
		for (RulesContext rulectx : ctx.rules()) {
			TypeDeclaration rule = new TypeDeclaration("", rulectx.getStart().getStartIndex(),
					rulectx.getStop().getStopIndex() + 1, rulectx.getStart().getStartIndex(),
					rulectx.getStop().getStopIndex() + 1);
			Block ruleBody = visitRules(rulectx);
			if (ruleBody != null) {
				rule.setBody(ruleBody);
			}
			body.addNode(rule);
		}
		module.setBody(body);

		declaration.addStatement(module);
		this.dltk = declaration;
		return null;
	}

	@Override
	public SimpleReference visitUseclause(UseclauseContext ctx) {
		String useCase = (ctx.usecase() == null) ? "" : visitUsecase(ctx.usecase());
		String reference = "";
		for (RefContext ref : ctx.ref()) {
			if (!reference.isEmpty()) {
				reference += ",";
			}
			reference += visitRef(ref);
		}

		// Create use clause and resolve reference.
		UseClause useClause = new UseClause(reference, UseCase.getUseCase(useCase), getPathRelativeToSourceFile(""),
				getSourceInfo(ctx));
		useClause.resolveReference();
		getProgram().addUseClause(useClause);

		if (!useCase.isEmpty()) {
			reference += " [" + useCase + "]";
		}
		return new SimpleReference(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex() + 1, reference);
	}

	@Override
	public String visitUsecase(UsecaseContext ctx) {
		return (ctx == null) ? "" : ctx.getText();
	}

	@Override
	public SimpleReference visitLearnclause(LearnclauseContext ctx) {
		return new SimpleReference(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex() + 1, ctx.getText());
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
	public SimpleReference visitOption(OptionContext ctx) {
		String option = "";
		if (ctx.exitoption() != null) {
			option = visitExitoption(ctx.exitoption());
		} else if (ctx.focusoption() != null) {
			option = visitFocusoption(ctx.focusoption());
		} else if (ctx.orderoption() != null) {
			option = visitOrderoption(ctx.orderoption());
		}
		return new SimpleReference(ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex() + 1, option);
	}

	@Override
	public String visitExitoption(ExitoptionContext ctx) {
		return ctx.EXIT().getText() + "=" + (ctx.value == null ? "" : ctx.value.getText());
	}

	@Override
	public String visitFocusoption(FocusoptionContext ctx) {
		return ctx.FOCUS().getText() + "=" + (ctx.value == null ? "" : ctx.value.getText());
	}

	@Override
	public String visitOrderoption(OrderoptionContext ctx) {
		return ctx.ORDER().getText() + "=" + (ctx.value == null ? "" : ctx.value.getText());
	}

	@Override
	public TypeDeclaration visitMacro(MacroContext ctx) {
		// Get macro name.
		String name = null;
		if (ctx.ID() != null) {
			name = ctx.ID().getText();
		}

		// Get macro parameters.
		List<Term> parameters = new LinkedList<>();
		if (ctx.PARLIST() != null) {
			for (Var var : visitVARLIST(ctx.PARLIST(), ctx)) {
				parameters.add(var);
			}
		}

		if (name != null) {
			Macro macro = new Macro(name, parameters, null, getSourceInfo(ctx));
			return new TypeDeclaration(macro.toString(), ctx.getStart().getStartIndex(),
					ctx.getStop().getStopIndex() + 1, ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex() + 1);

		} else {
			return null;
		}
	}

	@Override
	public Block visitRules(RulesContext ctx) {
		final Block body = new Block(ctx.getStart().getStartIndex() + 1, ctx.getStop().getStopIndex());

		// Get mental state condition.
		if (ctx.msc() != null) {
			List<TypeDeclaration> conditions = visitMsc(ctx.msc());
			for (TypeDeclaration condition : conditions) {
				body.addStatement(condition);
			}
		}

		// Get action combo.
		if (ctx.actioncombo() != null) {
			List<TypeDeclaration> combo = visitActioncombo(ctx.actioncombo());
			for (TypeDeclaration action : combo) {
				body.addStatement(action);
			}
		}

		// Get nested rules.
		if (ctx.rules() != null) {
			for (RulesContext rulectx : ctx.rules()) {
				Block nested = visitRules(rulectx);
				if (nested != null) {
					body.addStatement(nested);
				}
			}
		}

		return body;
	}

	@Override
	public List<TypeDeclaration> visitMsc(MscContext ctx) {
		List<TypeDeclaration> formulas = new LinkedList<>();
		for (MentalliteralContext literalctx : ctx.mentalliteral()) {
			TypeDeclaration formula = visitMentalliteral(literalctx);
			if (formula != null) {
				formulas.add(formula);
			}
		}
		return formulas;
	}

	@Override
	public TypeDeclaration visitMentalliteral(MentalliteralContext ctx) {
		if (ctx.ID() != null && ctx.PARLIST() != null) {
			try {
				CognitiveKR ckr = getCognitiveKR();
				List<Term> args = ckr.visitArguments(ctx.PARLIST().getText(), getSourceInfo(ctx.PARLIST()));
				Macro macro = new Macro(ctx.ID().getText(), args, null, getSourceInfo(ctx));
				return new TypeDeclaration(macro.toString(), ctx.getStart().getStartIndex(),
						ctx.getStop().getStopIndex() + 1, ctx.getStart().getStartIndex(),
						ctx.getStop().getStopIndex() + 1);
			} catch (ParserException e) {
				return null; // TODO
			}
		} else if (ctx.NOT() != null) {
			TypeDeclaration atom = visitMentalatom(ctx.mentalatom());
			// TODO: handle the NOT
			return atom;
		} else if (ctx.mentalatom() != null) {
			return visitMentalatom(ctx.mentalatom());
		} else {
			return null;
		}
	}

	@Override
	public TypeDeclaration visitMentalatom(MentalatomContext ctx) {
		// Construct literal.
		String op = visitMentalop(ctx.mentalop());
		if (ctx.selector() != null) {
			// Get selector.
			Selector selector = visitSelector(ctx.selector());
			op = selector + "." + op;
		}

		final TypeDeclaration atom = new TypeDeclaration(op, ctx.getStart().getStartIndex(),
				ctx.getStart().getStartIndex() + op.length(), ctx.PARLIST().getSymbol().getStartIndex(),
				ctx.PARLIST().getSymbol().getStopIndex() + 1);
		final Block body = new Block(ctx.PARLIST().getSymbol().getStartIndex() + 1,
				ctx.PARLIST().getSymbol().getStopIndex());

		// Get KR query.
		Query query = null;
		if (ctx.PARLIST() != null) {
			String krFragment = removeLeadTrailCharacters(ctx.PARLIST().getText());
			try {
				CognitiveKR ckr = getCognitiveKR();
				query = ckr.visitBeliefQuery(krFragment, getSourceInfo(ctx.PARLIST()));
			} catch (ParserException e) {
				// TODO
			}
		}

		if (query != null) {
			final List<krTools.language.Expression> base = new ArrayList<>(1);
			base.add(query);
			addBaseToBody(ctx.PARLIST().getSymbol(), body, base);
		}

		atom.setBody(body);
		return atom;
	}

	@Override
	public String visitMentalop(MentalopContext ctx) {
		return (ctx == null) ? "" : ctx.getText();
	}

	@Override
	public List<TypeDeclaration> visitActioncombo(ActioncomboContext ctx) {
		List<TypeDeclaration> combo = new LinkedList<>();
		for (ActionContext actionCtx : ctx.action()) {
			TypeDeclaration action = visitAction(actionCtx);
			if (action != null) {
				combo.add(action);
			}
		}
		return combo;
	}

	@Override
	public TypeDeclaration visitAction(ActionContext ctx) {
		if (ctx.ID() != null) {
			// User-defined or module call action.
			List<Term> parameters = visitPARLIST(ctx.PARLIST(), getSourceInfo(ctx.PARLIST()));
			UserSpecOrModuleCall call = new UserSpecOrModuleCall(ctx.ID().getText(), parameters, getSourceInfo(ctx));
			return new TypeDeclaration(call.toString(), ctx.getStart().getStartIndex(),
					ctx.getStop().getStopIndex() + 1, ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex() + 1);
		} else if (ctx.selectoraction() != null) {
			return visitSelectoraction(ctx.selectoraction());
		} else if (ctx.generalaction() != null) {
			return visitGeneralaction(ctx.generalaction());
		} else {
			return null;
		}
	}

	@Override
	public TypeDeclaration visitSelectoraction(SelectoractionContext ctx) {
		// Construct action.
		String op = (ctx.op == null) ? "" : ctx.op.getText();
		if (ctx.selector() != null) {
			// Get selector.
			Selector selector = visitSelector(ctx.selector());
			op = selector + "." + op;
		}

		final TypeDeclaration action = new TypeDeclaration(op, ctx.getStart().getStartIndex(),
				ctx.getStart().getStartIndex() + op.length(), ctx.PARLIST().getSymbol().getStartIndex(),
				ctx.PARLIST().getSymbol().getStopIndex() + 1);
		final Block body = new Block(ctx.PARLIST().getSymbol().getStartIndex() + 1,
				ctx.PARLIST().getSymbol().getStopIndex());

		// Get KR content.
		List<Term> content = null;
		if (ctx.PARLIST() != null) {
			String krFragment = removeLeadTrailCharacters(ctx.PARLIST().getText());
			if (!krFragment.isEmpty()) {
				try {
					CognitiveKR ckr = getCognitiveKR();
					content = ckr.visitArguments(krFragment, getSourceInfo(ctx.PARLIST()));
				} catch (ParserException e) {
					// TODO
				}
			}
		}

		if (content != null) {
			final List<krTools.language.Expression> base = new ArrayList<>(content.size());
			base.addAll(content);
			addBaseToBody(ctx.PARLIST().getSymbol(), body, base);
		}

		action.setBody(body);
		return action;
	}

	@Override
	public TypeDeclaration visitGeneralaction(GeneralactionContext ctx) {
		// Construct action.
		String op = (ctx.op == null) ? "" : ctx.op.getText();
		if (ctx.PARLIST() != null) {
			final TypeDeclaration action = new TypeDeclaration(op, ctx.getStart().getStartIndex(),
					ctx.getStart().getStartIndex() + op.length(), ctx.PARLIST().getSymbol().getStartIndex(),
					ctx.PARLIST().getSymbol().getStopIndex() + 1);
			final Block body = new Block(ctx.PARLIST().getSymbol().getStartIndex() + 1,
					ctx.PARLIST().getSymbol().getStopIndex());

			// Get parameters.
			List<Term> parameters = null;
			String krFragment = removeLeadTrailCharacters(ctx.PARLIST().getText());
			if (!krFragment.isEmpty()) {
				try {
					CognitiveKR ckr = getCognitiveKR();
					parameters = ckr.visitArguments(krFragment, getSourceInfo(ctx.PARLIST()));
				} catch (ParserException e) {
					// TODO
				}
			}

			if (parameters != null) {
				final List<krTools.language.Expression> base = new ArrayList<>(parameters.size());
				base.addAll(parameters);
				addBaseToBody(ctx.PARLIST().getSymbol(), body, base);
			}

			action.setBody(body);
			return action;
		} else {
			return new TypeDeclaration(op, ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex() + 1,
					ctx.getStart().getStartIndex(), ctx.getStop().getStopIndex() + 1);
		}
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

	@Override
	public Void visitTimeoutoption(TimeoutoptionContext ctx) {
		// Used in test2g only...
		return null;
	}

	// -------------------------------------------------------------
	// Helpers
	// -------------------------------------------------------------

	private static List<String> fromGoalExpression(final krTools.language.Expression expression) {
		final List<String> result = new LinkedList<>();
		if (expression instanceof PrologTerm) {
			processIndidvidualTerms(result, (PrologTerm) expression);
		}
		return result;
	}

	private static void processIndidvidualTerms(final List<String> result, final PrologTerm term) {
		if (term instanceof PrologCompound) {
			PrologCompound compound = (PrologCompound) term;
			result.add(compound.getName());
			for (final Term arg : compound) {
				processIndidvidualTerms(result, (PrologTerm) arg);
			}
		} else {
			result.add(term.toString());
		}
	}

	private static void addBaseToBody(final Token start, final Block body,
			final List<krTools.language.Expression> base) {
		int pos = start.getStartIndex();
		final Pattern alphanum = Pattern.compile("[a-zA-Z0-9]");
		for (final krTools.language.Expression exp : base) {
			for (final String term : fromGoalExpression(exp)) {
				if (alphanum.matcher(term).find()) { // no symbols
					final TypeDeclaration declaration = new TypeDeclaration(term, pos, pos + term.length(), pos,
							pos + term.length());
					body.addStatement(declaration);
				}
				pos += term.length();
			}
		}
	}
}
