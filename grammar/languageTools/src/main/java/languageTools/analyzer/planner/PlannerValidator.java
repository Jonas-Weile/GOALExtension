package languageTools.analyzer.planner;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.ParserRuleContext;
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
import languageTools.errors.planner.PlannerError;
import languageTools.errors.planner.PlannerErrorStrategy;
import languageTools.errors.planner.PlannerWarning;
import languageTools.parser.GOALLexer;
import languageTools.parser.InputStreamPosition;
import languageTools.parser.PLAN2GParser;
import languageTools.parser.PLAN2GParser.DecompositionsContext;
import languageTools.parser.PLAN2GParser.FocusoptionContext;
import languageTools.parser.PLAN2GParser.MainTaskContext;
import languageTools.parser.PLAN2GParser.MentalatomContext;
import languageTools.parser.PLAN2GParser.MentalopContext;
import languageTools.parser.PLAN2GParser.MethodContext;
import languageTools.parser.PLAN2GParser.OperatorContext;
import languageTools.parser.PLAN2GParser.PlanContext;
import languageTools.parser.PLAN2GParser.PostContext;
import languageTools.parser.PLAN2GParser.PostaddContext;
import languageTools.parser.PLAN2GParser.PostdelContext;
import languageTools.parser.PLAN2GParser.PreContext;
import languageTools.parser.PLAN2GParser.RefContext;
import languageTools.parser.PLAN2GParser.StringContext;
import languageTools.parser.PLAN2GParser.SubtasksContext;
import languageTools.parser.PLAN2GParser.TaskContext;
import languageTools.parser.PLAN2GParser.UsecaseContext;
import languageTools.parser.PLAN2GParser.UseclauseContext;
import languageTools.parser.PLAN2GParserVisitor;
import languageTools.program.planner.PlanningMethod;
import languageTools.program.planner.Decomposition;
import languageTools.program.planner.PlanningModule;
import languageTools.program.planner.PlanningOperator;
import languageTools.program.planner.PlanningTask;
import languageTools.program.Program;
import languageTools.program.actionspec.ActionPostCondition;
import languageTools.program.actionspec.ActionPreCondition;
import languageTools.program.agent.Module.FocusMethod;
import languageTools.program.agent.msc.AGoalLiteral;
import languageTools.program.agent.msc.MentalLiteral;
import languageTools.program.agent.selector.Selector;
import languageTools.program.mas.UseClause;
import languageTools.program.mas.UseClause.UseCase;
import languageTools.symbolTable.SymbolTable;
import languageTools.symbolTable.planner.PlanningMethodSymbol;
import languageTools.symbolTable.planner.PlanningOperatorSymbol;
import languageTools.symbolTable.planner.PlanningTaskSymbol;


/**
 * Validates an planner file and constructs a planner
 * program. FIXME: Explain what is validated. How to use. What happens if
 * filename does not validate.
 */
public class PlannerValidator extends Validator<GOALLexer, PLAN2GParser, PlannerErrorStrategy, PlanningModule>
		implements PLAN2GParserVisitor<Object> {
	private PLAN2GParser parser;
	private static PlannerErrorStrategy strategy = null;
	
	/**
	 * We use three symbol tables.
	 * The first is for user specified actions taken from the actionspec file.
	 * The second is for methods and operators - operators must have the same name as an action or task, but cannot have a method name.
	 * The third table is for tasks and subtasks. These cannot have the same names, but a task must have the same name as either a method or an operator.
	 */
	private final SymbolTable actionSymbols = new SymbolTable();
	private final SymbolTable operatorSymbols = new SymbolTable();
	private final SymbolTable taskSymbols = new SymbolTable();

	public PlannerValidator(String filename, FileRegistry registry) {
		super(filename, registry);
	}

	@Override
	protected ParseTree startParser() {
		return this.parser.plan();
	}

	@Override
	protected PlannerErrorStrategy getTheErrorStrategy() {
		if (strategy == null) {
			strategy = new PlannerErrorStrategy();
		}
		return strategy;
	}

	public SymbolTable getActionSymbols() {
		return this.actionSymbols;
	}
	
	public SymbolTable getOperatorSymbols() {
		return this.operatorSymbols;
	}
	
	public SymbolTable getTaskSymbols() {
		return this.taskSymbols;
	}
	
	/**
	 * Validation of agent program resolves references to action, macro, and module
	 * symbols, and checks whether all predicates used have been defined.
	 */
	@Override
	protected ValidatorSecondPass createSecondPass() {
		return new PlannerValidatorSecondPass(this);
	}

	@Override
	protected GOALLexer getNewLexer(CharStream stream) {
		return new GOALLexer(stream);
	}

	@Override
	protected PLAN2GParser getNewParser(TokenStream stream) {
		this.parser = new PLAN2GParser(stream);
		return this.parser;
	}

	@Override
	protected PlanningModule getNewProgram(File file) throws IOException {
		return new PlanningModule(this.registry, new InputStreamPosition(0, 0, 0, 0, file.getCanonicalPath()));
	}

	@Override
	public PlanningModule getProgram() {
		return (PlanningModule) super.getProgram();
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
	// Planning Module
	// -------------------------------------------------------------

	@Override
	public Object visitPlan(PlanContext ctx) {
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
				reportError(PlannerError.KR_COULDNOT_INITIALIZE, ctx, e.getMessage());
			}
		} else {
			if (uris.isEmpty()) {
				reportError(PlannerError.KR_COULDNOT_RESOLVE_NO_KR_USECLAUSES, ctx);
			} else {
				reportError(PlannerError.KR_COULDNOT_RESOLVE_DIFFERENT_KRS_USED, ctx);
			}
			return null; // makes no sense to go on without a KRI
		}
		
		
		// Process options: exit, focus, order.
		if (ctx.focusoption() != null) {
			visitFocusoption(ctx.focusoption());
		}
		
		// Get Planning module name.
		if (ctx.ID() != null) {
			String name = ctx.ID().getText();
			getProgram().setName(name);
			SourceInfo definition = getSourceInfo(ctx.ID());
			getProgram().setDefinition(definition);
			if (!FilenameUtils.getBaseName(definition.getSource()).equals(name)) {
				reportWarning(PlannerWarning.MODULE_NAME_MISMATCH, ctx.ID());
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
					reportError(PlannerError.DUPLICATE_PARAMETER, ctx);
				} else {
					parameterCheck.add(var);
				}
			}
		}
		
		// Process the main task
		if (ctx.mainTask() != null) {
			
			PlanningTask task = visitMainTask(ctx.mainTask());
			if (task != null) {
				getProgram().setTask(task);
				// Ensure the variables in the task are bound					
				if (!getProgram().getParameters().containsAll(task.getFreeVar())) {
					Set<Var> unboundVars = new LinkedHashSet<>();
					unboundVars.addAll(task.getFreeVar());
					unboundVars.removeAll(getProgram().getParameters());
					reportError(PlannerError.TASK_UNBOUND_VARIABLE, ctx, prettyPrintSet(unboundVars));
				}
			}
		} else {
			reportError(PlannerError.NO_MAIN_TASK, ctx);
		}
		
		// Process methods
		for (MethodContext methodCtx : ctx.method()) {
			PlanningMethod method = visitMethod(methodCtx);
			if (method != null) {
				getProgram().addMethod(method);
			}
		}
		
		// Process operators.
		for (OperatorContext operatorCtx : ctx.operator()) {
			PlanningOperator operator = visitOperator(operatorCtx);
			if (operator != null) {
				getProgram().addOperator(operator);
			}
		}
		
		return null;
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
				reportError(PlannerError.REFERENCE_COULDNOT_FIND, ref, reference);
			}

			// Add use clause to module.
			if (!getProgram().addUseClause(useClause)) {
				reportError(PlannerError.REFERENCE_DUPLICATE, ref, reference);
			}
		}

		return null; // Java says must return something even when Void
	}
	
	@Override
	public String visitUsecase(UsecaseContext ctx) {
		return (ctx == null) ? "" : ctx.getText();
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
	public Void visitFocusoption(FocusoptionContext ctx) {
		try {
			if (!getProgram().setFocusMethod(FocusMethod.valueOf(ctx.value.getText().toUpperCase()))) {
				// Report warning if duplicate.
				reportWarning(PlannerWarning.DUPLICATE_OPTION, ctx, "focus");
			}
		} catch (IllegalArgumentException e) {
			// simply ignore, parser will report problem
		}
		return null; // Java says must return something even when Void
	}
	
	
	@Override
	public MentalLiteral visitMentalatom(MentalatomContext ctx) {
		SourceInfo info = getSourceInfo(ctx);
		
		// Get selector.
		Selector selector = Selector.getDefault(info);

		// Get KR.
		String krFragment = (ctx.PARLIST() == null) ? "" : removeLeadTrailCharacters(ctx.PARLIST().getText());
		String op = visitMentalop(ctx.mentalop());
		if (krFragment.isEmpty() || op.isEmpty()) {
			return null;
		}
		SourceInfo krInfo = getSourceInfo(ctx.PARLIST());
		krInfo = (krInfo == null) ? null
				: new InputStreamPosition(krInfo.getLineNumber(), krInfo.getCharacterPosition() + 1,
						krInfo.getStartIndex() + 1, krInfo.getStopIndex() - 1, krInfo.getSource());

		// Construct literal.
		try {
			MentalLiteral returned = null;
			CognitiveKR ckr = getCognitiveKR();
			if (getTokenName(PLAN2GParser.AGOAL_OP).equals(op)) {
				Query agoal = ckr.visitGoalQuery(krFragment, krInfo);
				if (agoal != null) {
					returned = new AGoalLiteral(true, selector, agoal, ckr.getUsedSignatures(agoal), info);
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
	public PlanningTask visitMainTask(MainTaskContext ctx) {
		if (ctx.task() != null) {
			return visitTask(ctx.task());
		}
		return null;
	}
	
	
	@Override
	public PlanningTask visitTask(TaskContext ctx) {
		List<Term> parameters = new LinkedList<>();
		
		if (ctx.ID() != null) {
			if (ctx.PARLIST() != null) {
				for (Term par : visitPARLIST(ctx.PARLIST(), getSourceInfo(ctx))) {
					if (parameters.contains(par)) {
						reportError(PlannerError.DUPLICATE_PARAMETER, ctx, par.toString());
					}
					parameters.add(par);
				}
			}
			
			PlanningTask planningTask = new PlanningTask(ctx.ID().getText(), parameters, getSourceInfo(ctx));
			
			// Add subtask to task-symbols - do not throw error if it is a duplicate - tasks are not unique!
			this.taskSymbols.define(new PlanningTaskSymbol(planningTask.getSignature(), planningTask, planningTask.getSourceInfo()));
			return planningTask;
		}
		return null;
	}
	
	
	
	@Override
	public PlanningMethod visitMethod(MethodContext ctx) {
		if (ctx.ID() != null) {
			
			List<Term> parameters = new LinkedList<>();
			if (ctx.PARLIST() != null) {
				for (Var var : visitVARLIST(ctx.PARLIST(), ctx)) {
					if (parameters.contains(var)) {
						reportError(PlannerError.DUPLICATE_PARAMETER, ctx, var.toString());
					}
					parameters.add(var);
				}
			}
			
			// Parse decompositions and collect free variables at the same time
			Set<Var> freeVariables = new HashSet<>();
			Set<Decomposition> decompositions = new LinkedHashSet<Decomposition>();
			for (DecompositionsContext decompositionCtx : ctx.decompositions()) {
				Decomposition decomposition = visitDecompositions(decompositionCtx);
				if (decomposition != null) {
					if (!decompositions.add(decomposition)) {
						reportError(PlannerError.DECOMPOSITION_DUPLICATE, ctx, decomposition.toString());
					}
					freeVariables.addAll(decomposition.getFreeVar());
				}
			}
			List<Decomposition> decompositionsList = new ArrayList<Decomposition>(decompositions);
			PlanningMethod planningMethod = new PlanningMethod(ctx.ID().getText(), parameters, decompositionsList, getSourceInfo(ctx));
			
			// Add planningMethod to symbol table - methods must be unique!
			if (!this.operatorSymbols.define(new PlanningMethodSymbol(planningMethod.getSignature(), planningMethod, planningMethod.getSourceInfo()))) {
				reportError(PlannerError.METHOD_DUPLICATE_NAME, ctx, planningMethod.getSignature());
			}
			
			// Ensure there are no free variables in the subtasks
			if (!planningMethod.getFreeVar().containsAll(freeVariables)) {
				Set<Term> unboundVars = new LinkedHashSet<>();
				unboundVars.addAll(planningMethod.getParameters());
				unboundVars.removeAll(freeVariables);
				reportError(PlannerError.METHOD_UNBOUND_VARIABLE, ctx, planningMethod.getSignature(), prettyPrintSet(unboundVars));
			}
			
			return planningMethod;
		}
		return null;
	}
	
	
	@Override
	public Decomposition visitDecompositions(DecompositionsContext ctx) {
		AGoalLiteral aGoalLiteral = (ctx.mentalatom() == null) ? null : (AGoalLiteral) visitMentalatom(ctx.mentalatom());
		ActionPreCondition precondition = visitPre(ctx.pre());
		Deque<PlanningTask> subtasks = visitSubtasks(ctx.subtasks());
		return new Decomposition(aGoalLiteral, precondition, subtasks);
	}
	
	
	@Override
	public Deque<PlanningTask> visitSubtasks(SubtasksContext ctx) {
		Deque<PlanningTask> subtasks = new ArrayDeque<>();
		PlanningTask planningSubtask;
		for (TaskContext taskCtx : ctx.task()) {
			planningSubtask = visitTask(taskCtx);
			
			if (planningSubtask != null) {
				// Add subtask to list
				subtasks.add(planningSubtask);
			}
		}
		return subtasks;
	}
	
	

	@Override
	public PlanningOperator visitOperator(OperatorContext ctx) {
		// Get action name.
		String name = "";
		if (ctx.ID() != null) {
			name = ctx.ID().getText();
		}

		// Get action parameters.
		List<Term> parameters = new LinkedList<>();
		if (ctx.PARLIST() != null) {
			for (Term term : visitPARLIST(ctx.PARLIST(), getSourceInfo(ctx))) {
				if (parameters.contains(term)) {
					reportError(PlannerError.DUPLICATE_PARAMETER, ctx, term.toString());
				}
				parameters.add(term);
			}
		}

		// Get pre- and post-condition
		ActionPreCondition precondition = (ctx.pre() == null) ? null : visitPre(ctx.pre());
		ActionPostCondition positivePostcondition = (ctx.post() == null) ? null : visitPost(ctx.post());
		ActionPostCondition negativePostcondition = null;
		if (positivePostcondition == null) {
			positivePostcondition = (ctx.postadd() == null) ? null : visitPostadd(ctx.postadd());
			negativePostcondition = (ctx.postdel() == null) ? null : visitPostdel(ctx.postdel());
		}

		// Create action specification and store it.
		PlanningOperator operator = new PlanningOperator(name, parameters, precondition, positivePostcondition, negativePostcondition, getSourceInfo(ctx));

		// VALIDATE: Check whether operation has already been defined.
		if (!this.operatorSymbols.define(new PlanningOperatorSymbol(operator.getSignature(), operator, operator.getSourceInfo()))) {
			reportError(PlannerError.OPERATOR_DUPLICATE_NAME, ctx, operator.getSignature());
		}

		// VALIDATE: Check whether variables in post-condition are bound.
		Set<Var> postvars = new LinkedHashSet<>();
		if (positivePostcondition != null && positivePostcondition.getPostCondition() != null) {
			postvars.addAll(positivePostcondition.getPostCondition().getFreeVar());
		}
		if (negativePostcondition != null && negativePostcondition.getPostCondition() != null) {
			postvars.addAll(negativePostcondition.getPostCondition().getFreeVar());
		}
		if (precondition != null && precondition.getPreCondition() != null) {
			postvars.removeAll(precondition.getPreCondition().getFreeVar());
		}
		postvars.removeAll(parameters);
		if (!postvars.isEmpty()) {
			reportError(PlannerError.POSTCONDITION_UNBOUND_VARIABLE, ctx, prettyPrintSet(postvars));
		}

		return operator;
	}


	@Override
	public ActionPreCondition visitPre(PreContext ctx) {
		String krFragment = (ctx.KR_BLOCK() == null) ? "" : removeLeadTrailCharacters(ctx.KR_BLOCK().getText());
		Query pre = null;
		Set<String> signatures = null;
		try {
			CognitiveKR ckr = getCognitiveKR();
			SourceInfo info = getSourceInfo(ctx.KR_BLOCK());
			info = (info == null) ? null
					: new InputStreamPosition(info.getLineNumber(), info.getCharacterPosition() + 1,
							info.getStartIndex() + 1, info.getStopIndex() - 1, info.getSource());
			pre = ckr.visitBeliefQuery(krFragment, info);
			if (pre != null) {
				signatures = ckr.getUsedSignatures(pre);
			}
			reportEmbeddedLanguageErrors(ckr);
		} catch (ParserException e) {
			reportParsingException(e);
		}
		return (pre == null) ? null : new ActionPreCondition(pre, signatures, getSourceInfo(ctx));
	}

	
	@Override
	public ActionPostCondition visitPost(PostContext ctx) {
		String krFragment = (ctx.KR_BLOCK() == null) ? "" : removeLeadTrailCharacters(ctx.KR_BLOCK().getText());
		Update post = null;
		try {
			CognitiveKR ckr = getCognitiveKR();
			SourceInfo info = getSourceInfo(ctx.KR_BLOCK());
			info = (info == null) ? null
					: new InputStreamPosition(info.getLineNumber(), info.getCharacterPosition() + 1,
							info.getStartIndex() + 1, info.getStopIndex() - 1, info.getSource());
			post = ckr.visitBeliefInsert(krFragment, info);
			reportEmbeddedLanguageErrors(ckr);
		} catch (ParserException e) {
			reportParsingException(e);
		}
		return (post == null) ? null : new ActionPostCondition(post, false, getSourceInfo(ctx));
	}

	
	@Override
	public ActionPostCondition visitPostadd(PostaddContext ctx) {
		String krFragment = (ctx.KR_BLOCK() == null) ? "" : removeLeadTrailCharacters(ctx.KR_BLOCK().getText());
		Update post = null;
		try {
			CognitiveKR ckr = getCognitiveKR();
			SourceInfo info = getSourceInfo(ctx.KR_BLOCK());
			info = (info == null) ? null
					: new InputStreamPosition(info.getLineNumber(), info.getCharacterPosition() + 1,
							info.getStartIndex() + 1, info.getStopIndex() - 1, info.getSource());
			post = ckr.visitBeliefInsert(krFragment, info);
			reportEmbeddedLanguageErrors(ckr);
		} catch (ParserException e) {
			reportParsingException(e);
		}
		return (post == null) ? null : new ActionPostCondition(post, false, getSourceInfo(ctx));
	}

	
	@Override
	public ActionPostCondition visitPostdel(PostdelContext ctx) {
		String krFragment = (ctx.KR_BLOCK() == null) ? "" : removeLeadTrailCharacters(ctx.KR_BLOCK().getText());
		Update post = null;
		try {
			CognitiveKR ckr = getCognitiveKR();
			SourceInfo info = getSourceInfo(ctx.KR_BLOCK());
			info = (info == null) ? null
					: new InputStreamPosition(info.getLineNumber(), info.getCharacterPosition() + 1,
							info.getStartIndex() + 1, info.getStopIndex() - 1, info.getSource());
			post = ckr.visitBeliefDelete(krFragment, info);
			reportEmbeddedLanguageErrors(ckr);
		} catch (ParserException e) {
			reportParsingException(e);
		}
		return (post == null) ? null : new ActionPostCondition(post, true, getSourceInfo(ctx));
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
	
	
	public static String getTokenName(int token) {
		return StringUtils.remove(PLAN2GParser.VOCABULARY.getDisplayName(token), '\'');
	}
	
	/**
	 * @param ctx An ANTLR {@link ParserRuleContext}.
	 * @return A source info object ({@link InputStreamPosition}) with information
	 *         extracted from the rule context.
	 */
	public SourceInfo getSourceInfo(ParserRuleContext ctx) {
		try {
			return (ctx == null) ? null
					: new InputStreamPosition(ctx.getStart(), ctx.getStop() == null ? ctx.getStart() : ctx.getStop(),
							this.source.getCanonicalPath());
		} catch (IOException e) {
			return null; // TODO
		}
	}
}