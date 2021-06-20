package goal.core.executors.modules;

import java.util.List;

import events.Channel;
import events.ExecutionEventGeneratorInterface;
import goal.core.executors.stack.CallStack;
import goal.core.executors.stack.StackExecutor;
import goal.core.runtime.service.agent.Result;
import goal.core.runtime.service.agent.Result.RunStatus;
import goal.core.runtime.service.agent.RunState;
import goal.tools.errorhandling.exceptions.GOALActionFailedException;
import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Substitution;
import languageTools.program.agent.Module;
import languageTools.program.agent.Module.RuleEvaluationOrder;
import languageTools.program.agent.msc.MentalStateCondition;
import languageTools.program.planner.PlanningModule;
import mentalState.MentalStateWithEvents;
import mentalState.error.MSTDatabaseException;
import mentalState.error.MSTException;
import mentalState.error.MSTQueryException;
import mentalState.translator.Translator;
import msFactory.InstantiationFailedException;
import msFactory.translator.TranslatorFactory;

/**
 * Abstract class for module executors, and also factory for module executors.
 *
 * <p>
 * An action executor determines how to execute a {@link Module}.
 * <p>
 */
public abstract class ModuleExecutor extends StackExecutor {
	/**
	 * The module to be executed.
	 */
	private final Module module;
	/**
	 * Substitution to be used for instantiating parameters of the module.
	 */
	private final Substitution substitution;
	/**
	 * The goal to be focused on (if any).
	 */
	private MentalStateCondition focus;
	/**
	 * Have we prepared the mental state for the module yet? A call to
	 * {@link #prepareMentalState()} will put this to true.
	 */
	private boolean prepared;
	/**
	 * the order in which the rules in this module are to be evaluated if the
	 * module didn't specify an order
	 */
	private RuleEvaluationOrder ruleOrder;
	/**
	 * The last result of an execute-call.
	 */
	protected Result result;
	/**
	 * A possible exception (instead of a result)
	 */
	protected GOALActionFailedException failure;

	/**
	 * Create an executor for a {@link Module}.
	 *
	 * @param parent
	 *            The {@link CallStack} that we are working in.
	 * @param runstate
	 *            The {@link RunState} (i.e. agent) that we are working for.
	 * @param module
	 *            The {@link Module} that is to be executed.
	 * @param substitution
	 *            The {@link Substitution} to be used for instantiating
	 *            parameters of the module.
	 * @param ruleOrder
	 *            the default rule order, to be used if the module does not
	 *            specify a rule order. Must never be null.
	 */
	ModuleExecutor(CallStack parent, RunState runstate, Module module, Substitution substitution,
			RuleEvaluationOrder defaultRuleOrder) {
		super(parent, runstate);
		this.module = module;
		this.substitution = substitution;
		this.ruleOrder = module.getRuleEvaluationOrder();
		if (this.ruleOrder == null) {
			this.ruleOrder = defaultRuleOrder;
		}
	}

	/**
	 * @param focus
	 *            The goal to focus on (if any, can be null)
	 */
	public void setFocus(MentalStateCondition focus) {
		this.focus = focus;
	}

	/**
	 * @return The goal to focus on (if any, can be null)
	 */
	public MentalStateCondition getFocus() {
		return this.focus;
	}

	/**
	 * @return The module to be executed.
	 */
	public Module getModule() {
		return this.module;
	}

	/**
	 * @return The substitution that ...
	 */
	public Substitution getSubstitution() {
		return this.substitution;
	}

	/**
	 * @return True iff prepareMentalState has been executed successfully
	 */
	protected boolean hasPreparedMentalState() {
		return this.prepared;
	}

	/**
	 * If the module has a focus option, then create a new attention set in line
	 * with the option specified. Then add beliefs and goals from module's
	 * beliefs and goals section to the agent's mental state.
	 *
	 * @param runState
	 *            The run state used to prepare the module's execution.
	 * @throws GOALActionFailedException
	 *             If inserting a belief or adopting a goal failed.
	 */
	protected void prepareMentalState() throws GOALActionFailedException {
		// Push (non-anonymous) modules that were just entered onto
		// stack that keeps track of modules that have been entered but
		// not yet exited again and initialize the mental state for the
		// module, i.e. initial beliefs/goals and possible focus.
		if (!this.module.isAnonymous()) {
			this.runstate.getEventGenerator().event(Channel.MODULE_ENTRY, this.module, this.module.getDefinition(),
					"entered '%s' with %s.", this.module, getSubstitution());
		}
		this.runstate.enterModule(this.module);

		// Create new attention set if module uses focus option.
		Module module = getModule();
		this.runstate.setFocus(module, getFocus());

		// Process goals and beliefs use cases (if any).
		List<DatabaseFormula> beliefs = module.getBeliefs();
		List<Query> goals = module.getGoals();
		if (!beliefs.isEmpty() || !goals.isEmpty()) {
			try {
				MentalStateWithEvents mentalState = this.runstate.getMentalState();
				ExecutionEventGeneratorInterface generator = this.runstate.getEventGenerator();
				if (!beliefs.isEmpty()) {
					Translator translator = TranslatorFactory.getTranslator(this.runstate.getKRI());
					List<mentalState.Result> allInsertedBeliefs = mentalState.insert(translator.makeUpdate(beliefs),
							generator, this.runstate.getId());
					for (mentalState.Result insertedBeliefs : allInsertedBeliefs) {
						generator.event(Channel.BB_UPDATES, insertedBeliefs, module.getDefinition());
					}
				}
				for (Query goal : goals) {
					List<mentalState.Result> allInsertedGoals = mentalState.adopt(goal.toUpdate(), true, generator,
							this.runstate.getId());
					for (mentalState.Result insertedGoals : allInsertedGoals) {
						generator.event(Channel.GB_UPDATES, insertedGoals, module.getDefinition());
					}
				}
			} catch (MSTException | InstantiationFailedException e) {
				throw new GOALActionFailedException("execution of module '" + module + "' failed.", e);
			}
		}

		this.prepared = true;
	}

	/**
	 * Check whether we need to start a new cycle. We do so if we do NOT exit
	 * this module and are running within the main module's context (never start
	 * a new cycle when running the init/event or a module called from either of
	 * these modules).
	 *
	 * @return true if the event module executor has been pushed onto the call
	 *         stack. If this happens, the caller should probably return too, to
	 *         let the handling of the stack proceed.
	 */
	protected boolean doEvent(boolean reset, Result previousResult) throws GOALActionFailedException {
		if (reset && this.runstate.isMainModuleRunning()) {
			this.runstate.startCycle(previousResult.justPerformedRealAction());
			Module event = this.runstate.getEventModule();
			if (event != null) {
				ModuleExecutor exec = ModuleExecutor.getModuleExecutor(this.parent, this.runstate, event,
						event.getKRInterface().getSubstitution(null), RuleEvaluationOrder.LINEARALL);
				select(this);
				select(exec);
				return true;
			}
		}
		return false;
	}

	/**
	 * Evaluates whether the module should be terminated.
	 *
	 * @param runState
	 *            The run state used for evaluating the termination conditions.
	 * @return {@code true} if the module needs to be terminated; {@code false}
	 *         otherwise.
	 * @throws GOALActionFailedException
	 */
	protected boolean isModuleTerminated(boolean noMoreRules) throws GOALActionFailedException {
		// Set exit flag if {@link ExitModuleAction} has been performed.
		boolean exit = this.result.getStatus() != RunStatus.RUNNING;
		// Evaluate module's exit condition.
		if (noMoreRules) {
			switch (getModule().getExitCondition()) {
			case NOGOALS:
				try {
					exit |= !this.runstate.getMentalState().hasGoals();
				} catch (MSTDatabaseException | MSTQueryException e) {
					throw new GOALActionFailedException(
							"could not verify whether agent '" + this.runstate.getId() + "' has goals.", e);
				}
				break;
			case NOACTION:
				exit |= !this.result.justPerformedAction();
				break;
			case ALWAYS:
				exit |= true;
				break;
			default:
			case NEVER:
				// exit whenever module has been terminated (see above)
				break;
			}
		}
		exit |= this.runstate.getParent().isTerminated();
		return exit;
	}

	/**
	 * Takes all the necessary actions to properly exit a module.
	 */
	protected void terminateModule() throws GOALActionFailedException {
		// Remove focus on attention set if we exit module again.
		this.runstate.removeFocus(this.module);
		// Remove the module from the tack of modules that have been
		// entered and possibly update top level context in which we run
		this.runstate.exitModule(this.module);
		// Report the module exit on the module's debug channel.
		if (!this.module.isAnonymous()) {
			this.result = this.result.merge(Result.SOFTSTOP);
			this.runstate.getEventGenerator().event(Channel.MODULE_EXIT, this.module, this.module.getDefinition(),
					"exited '%s'.", this.module);
		}
	}

	/**
	 * @return the {@link RuleEvaluationOrder} of this module executor.
	 */
	public RuleEvaluationOrder getRuleOrder() {
		return this.ruleOrder;
	}

	@Override
	public String toString() {
		return getClass().getSimpleName() + " for " + getModule() + " with " + getSubstitution();
	}

	/**
	 * ModuleExecutor factory. Creates a module executor for the module.
	 *
	 * @param parent
	 *            The {@link CallStack} that we are working in.
	 * @param runstate
	 *            The {@link RunState} (i.e. agent) that we are working for.
	 * @param module
	 *            The {@link Module} to create an executor for.
	 * @param substitution
	 *            The {@link Substitution} to be used for instantiating
	 *            parameters of the module.
	 * @param defaultRuleOrder
	 *            the order in which the rules in this module are to be
	 *            evaluated if the module didn't specify an order.
	 * @return A module executor for the module. If the module requests specific
	 *         rule order, that order is used; otherwise the default rule order
	 *         is used.
	 */
	public static ModuleExecutor getModuleExecutor(CallStack parent, RunState runstate, Module module,
			Substitution substitution, RuleEvaluationOrder defaultRuleOrder) {
		RuleEvaluationOrder order = module.getRuleEvaluationOrder();
		if (order == null) {
			order = defaultRuleOrder;
		}
		
		if (module instanceof PlanningModule) {
			return new PlanningModuleExecutor(parent, runstate, (PlanningModule) module, substitution, order);
		}
		
		if (module.isAdaptive()) {
			return new AdaptiveModuleExecutor(parent, runstate, module, substitution, order);
		} else {
			return new LinearModuleExecutor(parent, runstate, module, substitution, order);
		}
	}
}
