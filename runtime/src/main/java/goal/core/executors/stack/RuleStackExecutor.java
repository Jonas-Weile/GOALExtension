package goal.core.executors.stack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import events.Channel;
import events.ExecutionEventGeneratorInterface;
import goal.core.runtime.service.agent.Result;
import goal.core.runtime.service.agent.RunState;
import goal.tools.errorhandling.exceptions.GOALActionFailedException;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;
import languageTools.program.agent.Module;
import languageTools.program.agent.Module.RuleEvaluationOrder;
import languageTools.program.agent.msc.MentalStateCondition;
import languageTools.program.agent.rules.ForallDoRule;
import languageTools.program.agent.rules.ListallDoRule;
import languageTools.program.agent.rules.Rule;
import mentalState.MSCResult;
import mentalState.MentalStateWithEvents;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTQueryException;
import mentalState.error.MSTTranslationException;
import mentalState.executors.MentalStateConditionExecutor;
import mentalState.translator.Translator;
import msFactory.InstantiationFailedException;
import msFactory.translator.TranslatorFactory;

/**
 * Executor for a {@link Rule}. Keeps its own stack of
 * {@link ActionComboStackExecutor}s as determined by {@link #pushed()}, which
 * evaluates the rule's condition. Not all possible actioncombo instantiations
 * that result from the initialization will be pushed to the {@link CallStack}
 * (and thus executed), for example when executing an if-then rule.
 */
public class RuleStackExecutor extends StackExecutor {
	/**
	 * The rule to be evaluated and, if applicable, to be applied.
	 */
	private final Rule rule;
	/**
	 * Substitution to be used for instantiating variables in the rule.
	 */
	private final Substitution substitution;
	/**
	 * The module that we're executing the rule in. CHECK can we remove this? We
	 * now only need it to get KR Language
	 */
	private Module context;
	/**
	 * The actions to execute as initially determined by {@link #pushed()}
	 */
	private Deque<ActionComboStackExecutor> actions;
	/**
	 * The last result of an execute-call
	 */
	private Result result;
	/**
	 * A possible exception (instead of a result)
	 */
	private GOALActionFailedException failure;

	/**
	 * Create an executor for a {@link Rule}.
	 *
	 * @param parent
	 *            The {@link CallStack} that we are working in.
	 * @param runstate
	 *            The {@link RunState} (i.e. agent) that we are working for.
	 * @param rule
	 *            The {@link Rule} that is to be evaluated and, if applicable,
	 *            applied.
	 * @param substitution
	 *            The {@link Substitution} that is to be used for instantiating
	 *            variables in the rule.
	 */
	public RuleStackExecutor(CallStack parent, RunState runstate, Rule rule, Substitution substitution) {
		super(parent, runstate);
		this.rule = rule;
		this.substitution = substitution;
	}

	/**
	 * @param context
	 *            The {@link Module} that we are executing the rule in.
	 */
	public void setContext(Module context) {
		this.context = context;
	}

	@Override
	public void popped() {
		if (this.failure != null) {
			return;
		}

		ExecutionEventGeneratorInterface generator = this.runstate.getEventGenerator();
		if (this.actions == null) {
			generator.event(Channel.RULE_START, this.rule, this.rule.getCondition().getSourceInfo(), "evaluating '%s'.",
					this.rule);

			this.result = Result.START;
			try {
				this.actions = generateExecutors();
			} catch (GOALActionFailedException e) {
				this.failure = e;
			}
		}

		if (this.failure != null) {
			return;
		}

		try {
			// If we have just finished executing an actioncombo, we use its
			// result as our
			// result. This assumes the strict order of elements on the {@link
			// CallStack}.
			ActionComboStackExecutor previous = (getPrevious() instanceof ActionComboStackExecutor)
					? (ActionComboStackExecutor) getPrevious() : null;
			if (previous != null) {
				this.result = this.result.merge(previous.getResult());
			}
			// Check if we need to stop executing the rule.
			boolean all = (this.rule instanceof ForallDoRule);
			boolean exit = (this.actions.isEmpty()) || (!all && this.result.justPerformedAction());
			if (exit) {
				this.actions.clear();
				generator.event(Channel.RULE_EXIT, this.rule, this.rule.getCondition().getSourceInfo(),
						"finished rule '%s'.", this.rule);
			} else {
				// Put the rule itself back on the stack,
				// and add the next action to execute to it.
				select(this);
				select(this.actions.remove());
			}
		} catch (GOALActionFailedException e) {
			this.failure = e;
		}

	}

	@Override
	public Result getResult() throws GOALActionFailedException {
		if (this.failure == null) {
			return this.result;
		} else {
			throw this.failure;
		}
	}

	/**
	 * Generates action executors from evaluation results. Only generates
	 * executors for actions whose precondition holds.
	 *
	 * @return A list of action executors.
	 *
	 * @throws GOALActionFailedException
	 *             If the evaluation of the rule condition failed.
	 */
	public Deque<ActionComboStackExecutor> generateExecutors() throws GOALActionFailedException {
		LinkedList<ActionComboStackExecutor> executors = new LinkedList<>();
		try {
			MSCResult result = evaluateRule();
			if (result.holds()) {
				for (MentalStateCondition goal : result.getFocusedGoals()) {
					Set<Substitution> answers = result.getFocusedResults(goal);
					for (Substitution substitution : getVarSubstitution(answers)) {
						ActionComboStackExecutor executor = (ActionComboStackExecutor) getExecutor(
								this.rule.getAction(), substitution);
						if (result.focus()) {
							executor.setFocus(goal.applySubst(substitution));
						}
						executors.add(executor);
					}
				}
				// use the runtime rule order of the module we're in
				RuleEvaluationOrder order = (this.parent.getParentModuleExecutor() == null)
						? this.context.getRuleEvaluationOrder() : this.parent.getParentModuleExecutor().getRuleOrder();
				if (order == RuleEvaluationOrder.LINEARRANDOM || order == RuleEvaluationOrder.LINEARALLRANDOM
						|| order == RuleEvaluationOrder.RANDOM || order == RuleEvaluationOrder.RANDOMALL) {
					Collections.shuffle(executors);
				}
			}
			return executors;
		} catch (MSTQueryException | MSTDatabaseException e) {
			throw new GOALActionFailedException(
					"failed to evaluate condition of '" + this.rule.applySubst(this.substitution) + "'", e);
		} finally {
			this.runstate.getEventGenerator().event(Channel.RULE_EVAL_CONDITION_DONE, this.rule,
					this.rule.getCondition().getSourceInfo(), "evaluated '%s'", this.rule);
		}
	}

	/**
	 * @return the results of evaluating the rule's {@link MentalStateCondition}
	 * @throws MSTDatabaseException
	 * @throws MSTQueryException
	 */
	private MSCResult evaluateRule() throws MSTQueryException, MSTDatabaseException {
		MentalStateWithEvents mst = this.runstate.getMentalState();
		ExecutionEventGeneratorInterface generator = this.runstate.getEventGenerator();
		Rule instantiatedRule = this.rule.applySubst(this.substitution);

		generator.event(Channel.RULE_CONDITIONAL_VIEW, instantiatedRule.getCondition(),
				instantiatedRule.getCondition().getSourceInfo(), "evaluating condition of '%s'.", instantiatedRule);
		MentalStateConditionExecutor msce = mst.getConditionExecutor(this.rule.getCondition(), this.substitution);
		MSCResult mscresult = msce.evaluate(mst, instantiatedRule.getFocusMethod(), this.runstate.getEventGenerator());
		// Report on results and generate the list of actioncombo
		// executors for this rule (if the condition applies).
		if (mscresult.holds()) {
			generator.event(Channel.HIDDEN_RULE_CONDITION_EVALUATION, instantiatedRule.getAction(),
					instantiatedRule.getCondition().getSourceInfo(), "condition of '%s' holds.", instantiatedRule);
			generator.event(Channel.RULE_CONDITION_EVALUATION, instantiatedRule.getCondition(),
					instantiatedRule.getCondition().getSourceInfo(), "condition of '%s' holds for: %s.",
					instantiatedRule, mscresult.getAnswers());
		} else {
			generator.event(Channel.RULE_CONDITION_EVALUATION, instantiatedRule.getCondition(),
					instantiatedRule.getCondition().getSourceInfo(), "condition of '%s' does not hold.",
					instantiatedRule);
		}
		return mscresult;
	}

	/**
	 * Creates a set with a single substitution that assigns the (parameter)
	 * solutions to the variable of this rule.
	 *
	 * @param solutions
	 *            The set of solutions to process.
	 * @return A singleton set with a substitution that binds the variable of
	 *         this rule with all solution substitutions provided as parameter.
	 * @throws GOALActionFailedException
	 *             If variable of listall rule could not be instantiated.
	 */
	private Set<Substitution> getVarSubstitution(Set<Substitution> solutions) throws GOALActionFailedException {
		// If the solution set is empty, then the variable of this rule should
		// not be instantiated and we simply return the empty set.
		if (solutions.isEmpty() || !(this.rule instanceof ListallDoRule)) {
			return solutions;
		} else {
			// Get the variable of the list-all rule.
			Var var = ((ListallDoRule) this.rule).getVariable();

			// Create the substitution for the variable of this list-all rule.
			Term term = substitutionsToTerm(solutions);
			Substitution varSubst = this.runstate.getKRI().getSubstitution(null);
			varSubst.addBinding(var, term);

			// Combine again with any parameters that were passed on to this
			// rule.
			Substitution temp = this.substitution.clone();
			temp.remove(var);
			varSubst = varSubst.combine(temp);

			// Add the substitution to a set and return it.
			Set<Substitution> result = new LinkedHashSet<>(1);
			result.add(varSubst);
			return result;
		}
	}

	/**
	 * Combines all given substitutions into a single {@link Term}. Exclusively
	 * used for {@link ListallDoRule}.
	 *
	 * @param substitutions
	 *            A set of substitutions to be mapped onto a single term.
	 * @return A new term for the {@link #variable}. The substitution will be a
	 *         list of all values for that var in the given set of
	 *         {@link Substitution}s.
	 * @throws GOALActionFailedException
	 *             If variable of listall rule could not be instantiated.
	 */
	private Term substitutionsToTerm(Set<Substitution> substitutions) throws GOALActionFailedException {
		try {
			Translator translator = TranslatorFactory.getTranslator(this.context.getKRInterface());
			// First make single terms from each substitution.
			List<Term> substsAsTerms = new ArrayList<>(substitutions.size());
			// Get the variables from the condition of the rule; bindings for
			// those variables will be turned into a list.
			Set<Var> boundVar = this.rule.getCondition().getFreeVar();
			for (Substitution substitution : substitutions) {
				List<Term> subTerms = new ArrayList<>(boundVar.size());
				for (Var v : boundVar) {
					Term t = substitution.get(v);
					if (t != null) { // #3616 can happen for anonymous vars
						subTerms.add(t);
					}
				}
				// if there is only one bound var, we shouldn't make lists of
				// them. the end result should simply be a list of values
				// instead of a list of singleton lists.
				if (subTerms.size() == 1) {
					substsAsTerms.add(subTerms.get(0));
				} else if (subTerms.size() > 1) {
					substsAsTerms.add(translator.makeList(subTerms));
				}
				// if empty, do not add anything.
				// it means there is no substitution, so we want the end result
				// to be '[]' (and not '[[]]')
			}
			return translator.makeList(substsAsTerms);
		} catch (MSTTranslationException | InstantiationFailedException e) {
			throw new GOALActionFailedException("could not instantiate listall rule", e);
		}
	}

	@Override
	public String toString() {
		return "RuleStackExecutor for " + this.rule.getCondition() + " with " + this.substitution;
	}
}