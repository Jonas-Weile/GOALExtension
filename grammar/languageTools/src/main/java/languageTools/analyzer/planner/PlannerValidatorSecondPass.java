package languageTools.analyzer.planner;

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
import languageTools.errors.module.ModuleError;
import languageTools.errors.module.ModuleWarning;
import languageTools.errors.planner.PlannerError;
import languageTools.errors.planner.PlannerWarning;
import languageTools.program.planner.Decomposition;
import languageTools.program.planner.PlanningMethod;
import languageTools.program.planner.PlanningModule;
import languageTools.program.planner.PlanningOperator;
import languageTools.program.planner.PlanningTask;
import languageTools.symbolTable.Symbol;
import languageTools.symbolTable.SymbolTable;
import languageTools.symbolTable.agent.ActionSymbol;
import languageTools.symbolTable.planner.PlanningMethodSymbol;
import languageTools.symbolTable.planner.PlanningOperatorSymbol;
import languageTools.program.actionspec.UserSpecAction;
import languageTools.program.agent.actions.Action;
import languageTools.program.agent.actions.UserSpecCallAction;
import languageTools.program.agent.msc.MentalLiteral;

/**
 * Implements second pass for validating a planning module file.
 */
public class PlannerValidatorSecondPass extends ValidatorSecondPass {
	/**
	 * Program that is outcome of first pass.
	 */
	private final PlanningModule program;
	private final SymbolTable actionSymbols; 
	private final SymbolTable operatorSymbols;
	private final SymbolTable taskSymbols;

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
	public PlannerValidatorSecondPass(PlannerValidator firstPass) {
		super(firstPass);
		this.program = firstPass.getProgram();
		this.actionSymbols = firstPass.getActionSymbols();
		this.operatorSymbols = firstPass.getOperatorSymbols();
		this.taskSymbols = firstPass.getTaskSymbols();
	}

	/**
	 * Performs the validation and resolution of references by a walk over the
	 * program structure.
	 */
	@Override
	public void validate() {
		preProcess();
		preProcessSymbolTables();

		if (!checkKRIuse() || this.program.getRegistry().hasAnyError()) {
			return;
		}
		
		// Ensure all methods and operators refer to existing tasks and actions, and that all tasks have corresponding methods or operators.
		resolveReferences();
		
		processInfo();
		validateKR();
		reportUnusedVariables();			
	}

	
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
	}
	
	private void resolveReferences() {

		PlanningTask task = this.program.getTask();
		resolveTask(task);
		
		for (PlanningMethod method : this.program.getAllMethods()) {
			resolveReferences(method);
		}
		
		for (PlanningOperator operator : this.program.getAllOperators()) {
			resolveReferences(operator);
		}
		
	}
	
	
	private void resolveTask(PlanningTask task) {
		Symbol symbol = this.operatorSymbols.resolve(task.getSignature());
		
		if (symbol instanceof PlanningMethodSymbol) {
			task.markAsCompound();
		} else if (symbol instanceof PlanningOperatorSymbol) {
			task.markAsPrimitive();
		} else {
			getFirstPass().reportError(PlannerError.TASK_CANNOT_BE_SOLVED,
					task.getSourceInfo(), task.getSignature());
		}
			
	}

	protected void resolveReferences(PlanningMethod method) {
		// Ensure the method corresponds to some task
		if (this.taskSymbols.resolve(method.getSignature()) == null) {
			getFirstPass().reportWarning(PlannerWarning.METHOD_UNUSED,
					method.getSourceInfo(), method.getSignature());
		}
		
		// Ensure all subtasks have corresponding operators
		for (PlanningTask subtask : method.getAllSubtasks()) {
			resolveTask(subtask);
		}
	}
	
	protected void resolveReferences(PlanningOperator operator) {
		Action<?> resolvedAction = null;
		String signature = operator.getSignature();
		Symbol symbol = this.actionSymbols.resolve(signature);
		
		if (symbol instanceof ActionSymbol) {
			UserSpecAction target = ((ActionSymbol) symbol).getActionSpecification();
			resolvedAction = new UserSpecCallAction(target, operator.getParameters(), operator.getSourceInfo());
			operator.setAction(resolvedAction);
			if (target != null) {
				if (target.getPositivePostcondition() != null) {
					this.beliefUpdates.add(target.getPositivePostcondition().getPostCondition());
				}
				if (target.getNegativePostcondition() != null) {
					this.beliefUpdates.add(target.getNegativePostcondition().getPostCondition());
				}
			}
		} else {
			getFirstPass().reportError(PlannerError.OPERATOR_HAS_NO_ACTION, operator.getSourceInfo(), signature);
		}
	}
	

	//
	// MODIFY THIS
	//
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
						getFirstPass().reportWarning(PlannerWarning.VARIABLE_UNUSED, var.getSourceInfo(),
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
		
		for (PlanningMethod method : this.program.getAllMethods()) {
			for (Decomposition decomposition : method.getDecompositions()) {
				for (MentalLiteral literal : getBeliefLiterals(decomposition.getPrecondition())) {
					this.beliefQueries.add(literal.getFormula());
				}
				if (decomposition.getAGoalLiteral() != null) {
					this.goalQueries.add(decomposition.getAGoalLiteral().getFormula());
				}
			}
		}
		
		for (PlanningOperator operator : this.program.getAllOperators()) {
			for (MentalLiteral literal : getBeliefLiterals(operator.getPrecondition())) {
				this.beliefQueries.add(literal.getFormula());
			}
			if (operator.getPositivePostcondition() != null) {
				this.beliefQueries.add(operator.getPositivePostcondition().getPostCondition().toQuery());
			}
			if (operator.getNegativePostcondition() != null) {
				this.beliefQueries.add(operator.getNegativePostcondition().getPostCondition().toQuery());
			}
		}
	}
}
