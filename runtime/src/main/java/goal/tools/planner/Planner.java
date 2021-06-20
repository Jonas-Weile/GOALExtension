package goal.tools.planner;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import events.Channel;
import events.ExecutionEventGeneratorInterface;
import goal.tools.adapt.ModuleID;
import krTools.KRInterface;
import krTools.exceptions.KRDatabaseException;
import krTools.exceptions.KRQueryFailedException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import krTools.language.Update;
import languageTools.program.actionspec.ActionPostCondition;
import languageTools.program.agent.msc.AGoalLiteral;
import languageTools.program.agent.msc.BelLiteral;
import languageTools.program.agent.msc.MentalLiteral;
import languageTools.program.agent.msc.MentalStateCondition;
import languageTools.program.mas.AgentDefinition;
import languageTools.program.planner.Decomposition;
import languageTools.program.planner.PlanningModule;
import languageTools.program.planner.PlanningOperator;
import languageTools.program.planner.PlanningTask;
import mentalState.BASETYPE;
import mentalState.GoalBase;
import mentalState.MentalStateWithEvents;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;
import swiprolog.database.PrologDatabase;

public abstract class Planner implements PlanningAlgorithm {
	
	private ExecutionEventGeneratorInterface generator;
	
	public void setGenerator(ExecutionEventGeneratorInterface generator) {
		this.generator = generator;
	}
	protected ExecutionEventGeneratorInterface getGenerator() {
		return this.generator;
	}
	
	

	private MentalStateWithEvents mentalState;
	private PrologDatabase beliefBase;
	private GoalBase goalBase;
	private PlanningModule module;
	private Substitution substitution;
	
	protected PlanningModule getModule() {
		return this.module;
	}
	
	protected Substitution getSubstitution() {
		return this.substitution;
	}
	
	
	public Planner(PlanningModule module, MentalStateWithEvents mentalState, Substitution substitution) throws KRDatabaseException, MSTDatabaseException, MSTQueryException {
		this.module = module;
		this.mentalState = mentalState;
		this.substitution = substitution;
		setupPlanner();
	}

	public void reset(MentalStateWithEvents mentalState, Substitution substitution) {
		this.mentalState = mentalState;
		this.substitution = substitution;
		setGoalBase();
		
	}
	
	private void setupPlanner() throws KRDatabaseException, MSTDatabaseException, MSTQueryException {
		instatiateNewBeliefBase();
		setGoalBase();
		return;
	}
	
	private void setGoalBase() {
		this.goalBase = mentalState.getOwnModel().getAttentionSet(false);		
	}
	
	private void instatiateNewBeliefBase() throws KRDatabaseException, MSTDatabaseException, MSTQueryException {
		PrologDatabase beliefBase = createBeliefBase();
		addKnowledgeToBeliefBase(beliefBase);
		this.beliefBase = beliefBase;
		return;
	}

	private PrologDatabase createBeliefBase() throws KRDatabaseException {
		AgentDefinition thisAgent = mentalState.getOwner();
		KRInterface kri = thisAgent.getKRInterface();
		String id = new ModuleID(module.getSignature()).toString();
		PrologDatabase beliefBase = (PrologDatabase) kri.getDatabase(thisAgent.toString(), id);
		return beliefBase;
	}

	private void addKnowledgeToBeliefBase(PrologDatabase beliefBase) throws MSTDatabaseException, MSTQueryException, KRDatabaseException {
		final Set<DatabaseFormula> knowledge = this.mentalState.getKnowledge();
		beliefBase.addKnowledge(knowledge);
	}

	public final Plan createNewPlan() throws MSTDatabaseException, MSTQueryException, KRDatabaseException, KRQueryFailedException {
		initialize();
		Plan plan = doPlanning();
		clean();
		return plan;
	}

	private void initialize() throws MSTDatabaseException, MSTQueryException, KRDatabaseException {
		Set<DatabaseFormula> beliefs = this.mentalState.getBeliefs();
		for (DatabaseFormula formula : beliefs) {
			this.beliefBase.insert(formula);
		}
		return;
	}
	
	private final void clean() throws KRDatabaseException {
		// Due to shortcomings of JPL, we remove all inserted beliefs (NOT KNOWLEDGDE) manually.
		List<DatabaseFormula> beliefs = new ArrayList<DatabaseFormula>(this.beliefBase.getTheory().getFormulas());
		for (DatabaseFormula formula : beliefs) {
			this.beliefBase.delete(formula);
		}
		return;
	}
	
	
	
	
	
	

	//*******************************************************************************************************************************//
	//*********************************                   Evaluation methods                   **************************************//
	//*******************************************************************************************************************************//
	
	protected Set<Substitution> getPossibleSubstitutionsForDecomposition(Decomposition decomposition) throws MSTQueryException, KRQueryFailedException {
		Set<Substitution> result;	
		result = evaluateDecomposition(decomposition);
		return result;
	}
	
	protected Deque<PlanningTask> applyDecomposition(Decomposition decomposition, Substitution substitution) throws MSTQueryException, KRQueryFailedException {
		return decomposition.applySubst(substitution).getSubtasks();
	}
		
	
	protected Set<Substitution> getPossibleSubstitutionsForOperator(PlanningOperator operator) throws MSTQueryException, KRQueryFailedException {
		Set<Substitution> result = new LinkedHashSet<>();
		result.add(this.substitution);
		result = evaluatePrecondition(operator.getPrecondition(), result);
		return result;
	}
	
	
	protected void executeOperator(PlanningOperator operator, Substitution substitution) throws MSTQueryException {
		applyPostcondition(operator.getPositivePostcondition(), operator.getNegativePostcondition(), substitution);
	}
	
	protected void executeOperatorReversed(PlanningOperator operator, Substitution substitution) throws MSTQueryException {
		applyPostcondition(operator.getNegativePostcondition(), operator.getPositivePostcondition(), substitution);
	}
	
	
	
	private Set<Substitution> evaluateDecomposition(Decomposition decomposition) throws MSTQueryException, KRQueryFailedException {
		Set<Substitution> result = new LinkedHashSet<>();
		result.add(this.substitution);
		
		AGoalLiteral aGoalLiteral = decomposition.getAGoalLiteral();
		MentalStateCondition precondition = decomposition.getPrecondition();
		
		if (aGoalLiteral != null) {
			result = evaluateLiteral(aGoalLiteral, result);
		}
		result = evaluatePrecondition(precondition, result);
		
		return result;
	}

	
	private Set<Substitution> evaluateLiteral(MentalLiteral literal, Set<Substitution> substitutions) throws MSTQueryException, KRQueryFailedException {
		Set<Substitution> answers, result = new LinkedHashSet<>();
		for (Substitution currentSubstitution : substitutions) {
			answers = query(literal.applySubst(currentSubstitution));
			for (Substitution answer : answers) {
				result.add(currentSubstitution.combine(answer));
			}
		}
		// Update results found so far.
		return result;
		
	}
	
	
	private Set<Substitution> evaluatePrecondition(MentalStateCondition msc, Set<Substitution> substitutions) throws MSTQueryException, KRQueryFailedException {
		Set<Substitution> result = substitutions;
		
		for (MentalLiteral literal : msc.getAllLiterals()) {
			result = evaluateLiteral(literal, result);
		}
		
		return result;
	}	
	
	
	private Set<Substitution> query(MentalLiteral literal) throws MSTQueryException, KRQueryFailedException {
		Query formula = literal.getFormula();
		Set<Substitution> result = null;
		
		if (literal instanceof BelLiteral) {
			result = beliefQuery(formula);
		} else if (literal instanceof AGoalLiteral) {
			result = agoalQuery(formula);
		} 

		if (result == null) {
			throw new MSTQueryException("unknown literal '" + literal + "'.");
		} else {
			return result;
		}
	}

	private Set<Substitution> beliefQuery(Query query) throws KRQueryFailedException {
		return this.beliefBase.query(query);
	}
	
	
	private Set<Substitution> agoalQuery(Query formula) throws MSTQueryException, KRQueryFailedException {
		// First, check whether query follows from goal base.
		Set<Substitution> lSubstSet = this.goalBase.query(formula);
		
		// Second, remove all substitutions for which query after applying
		// that substitution to it also follows from the belief base.
		Set<Substitution> removeSubstSet = new LinkedHashSet<>();
		Query instantiatedQuery;
		for (Substitution lSubst : lSubstSet) {
			instantiatedQuery = formula.applySubst(lSubst);
			if (!instantiatedQuery.isClosed()) { // should be closed; see TRAC
				// #174.
				throw new MSTQueryException("goal query '" + formula
						+ "' did not result in a closed formula but returned '" + instantiatedQuery + "'.");
			}
			if (!beliefQuery(instantiatedQuery).isEmpty()) {
				removeSubstSet.add(lSubst);
			}
		}

		lSubstSet.removeAll(removeSubstSet);
		return lSubstSet;
	}
	
	private void applyPostcondition(ActionPostCondition positivePostCondition, ActionPostCondition negativePostCondition, Substitution substitution) throws MSTQueryException {		
		// Apply the action's positive postcondition (if any).
		if (positivePostCondition != null) {
			ActionPostCondition postcondition = positivePostCondition.applySubst(substitution);
			mentalState.Result insertedBeliefs = insert(postcondition.getPostCondition());
			

			generator.event(Channel.BB_UPDATES, insertedBeliefs, postcondition.getSourceInfo());
		}
		// Apply the negative postcondition, if there is one.
		if (negativePostCondition != null) {
			ActionPostCondition postcondition = negativePostCondition.applySubst(substitution);
			mentalState.Result deletedBeliefs = delete(postcondition.getPostCondition());
			

			generator.event(Channel.BB_UPDATES, deletedBeliefs, postcondition.getSourceInfo());
		}	
	}

	
	private mentalState.Result insert(final Update update) throws MSTQueryException {
		return update(update.getAddList(), update.getDeleteList());
	}
	
	
	private mentalState.Result delete(final Update update) throws MSTQueryException {
		return update(update.getDeleteList(), update.getAddList());
	}

	
	private mentalState.Result update(final List<DatabaseFormula> addList, final List<DatabaseFormula> deleteList) throws MSTQueryException {
		final mentalState.Result result = this.mentalState.createResult(BASETYPE.BELIEFBASE, mentalState.getOwner().toString());
		for (final DatabaseFormula formula : deleteList) {
			result.merge(delete(formula));
		}
		for (final DatabaseFormula formula : addList) {
			result.merge(insert(formula));
		}
		return result;
	}
		
		
	private mentalState.Result insert(final DatabaseFormula formula) throws MSTQueryException {
		try {
			final mentalState.Result result = this.mentalState.createResult(BASETYPE.BELIEFBASE, mentalState.getOwner().toString());
			if (this.beliefBase.insert(formula)) {
				result.added(formula);
			}
			return result;
		} catch (final KRDatabaseException e) {
			throw new MSTQueryException(
					String.format("failed to add '%s' to '%s'.", formula.toString(), this.beliefBase.getName()), e);
		}
	}	
	
	
	private mentalState.Result delete(final DatabaseFormula formula) throws MSTQueryException {
		try {
			final mentalState.Result result = this.mentalState.createResult(BASETYPE.BELIEFBASE, mentalState.getOwner().toString());
			if (this.beliefBase.delete(formula)) {
				result.removed(formula);
			}
			return result;
		} catch (final KRDatabaseException e) {
			throw new MSTQueryException(
					String.format("failed to delete '%s' from '%s'.", formula.toString(), this.beliefBase.getName()), e);
		}
	}
}
