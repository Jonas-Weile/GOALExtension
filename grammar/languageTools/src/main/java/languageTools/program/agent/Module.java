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

package languageTools.program.agent;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import krTools.language.DatabaseFormula;
import krTools.language.Query;
import krTools.language.Var;
import krTools.parser.SourceInfo;
import languageTools.analyzer.FileRegistry;
import languageTools.program.Program;
import languageTools.program.actionspec.UserSpecAction;
import languageTools.program.agent.actions.Action;
import languageTools.program.agent.msc.AGoalLiteral;
import languageTools.program.agent.msc.GoalLiteral;
import languageTools.program.agent.msc.Macro;
import languageTools.program.agent.rules.Rule;
import languageTools.program.mas.UseClause.UseCase;
import languageTools.program.planner.PlanningModule;

/**
 * A module has a name and possibly parameters, module options, may define
 * macros, and should contain one or more rules.
 */
public class Module extends Program {
	/**
	 * Name of this module.
	 */
	private String name;
	/**
	 * Location of the module definition.
	 */
	private SourceInfo definition;
	/**
	 * The (possibly empty) list of parameters of this module.
	 *
	 * <p>
	 * For anonymous modules, the parameters are derived by the validator from the
	 * variables present in the rule that calls/triggers it.
	 * </p>
	 */
	private List<Var> parameters = new LinkedList<>();
	// LEARNING
	private Set<String> learnedBeliefs = new LinkedHashSet<>();
	private Set<String> learnedGoals = new LinkedHashSet<>();

	// -------------------------------------------------------------
	// Module options.
	// -------------------------------------------------------------

	/**
	 * The focus method that is used when entering the module. If no method has been
	 * set, {@link #getFocusMethod()} will return the default method.
	 */
	private FocusMethod focusMethod;
	/**
	 * The exit condition that is used to decide when to exit the module. If no
	 * condition has been specified, then {@link #getExitCondition()} will return
	 * the default condition.
	 */
	private ExitCondition exitCondition;
	/**
	 * The rule evaluation order that is used to evaluate the module's rules. If no
	 * order has been set, then {@link #getRuleEvaluationOrder()} will return the
	 * default order.
	 */
	private RuleEvaluationOrder order;

	// -------------------------------------------------------------
	// Macros and rules of this module.
	// -------------------------------------------------------------

	/**
	 * The macros specified in the module.
	 */
	private List<Macro> macros = new LinkedList<>();
	/**
	 * The rules specified in the module.
	 */
	private List<Rule> rules = new LinkedList<>();
	/**
	 * True iff this module is anonymous
	 */
	private boolean anonymous;

	/**
	 * Creates an (empty) module.
	 *
	 * @param info A source info object.
	 */
	public Module(FileRegistry registry, SourceInfo info) {
		super(registry, info);
	}

	/**
	 * @return The name of this module.
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * @param name A name for this module.
	 */
	public void setName(String name) {
		this.name = name;
	}

	public SourceInfo getDefinition() {
		return this.definition;
	}

	public void setDefinition(SourceInfo definition) {
		this.definition = definition;
		if (!isAnonymous()) {
			register(this);
		}
	}

	/**
	 * @return The list of formal parameters of this module.
	 */
	public List<Var> getParameters() {
		return Collections.unmodifiableList(this.parameters);
	}

	/**
	 * @param parameters The parameter list of the module.
	 */
	public void setParameters(List<Var> parameters) {
		this.parameters = parameters;
	}

	/**
	 * @return The signature of this {@link Module}, i.e., [name]/[nrOfPars]
	 */
	public String getSignature() {
		return this.name + "/" + this.parameters.size();
	}

	/**
	 * @return The focus method that is used when entering this module.
	 */
	public FocusMethod getFocusMethod() {
		if (this.focusMethod == null) {
			// return default
			return FocusMethod.NONE;
		} else {
			return this.focusMethod;
		}
	}

	/**
	 * Sets the focus method of this module, if option has not already been set.
	 *
	 * @param focusMethod A focus method.
	 * @return {@code true} if the condition was set, {@code false} if condition had
	 *         already been set.
	 */
	public boolean setFocusMethod(FocusMethod focusMethod) {
		if (this.focusMethod == null) {
			this.focusMethod = focusMethod;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @return The exit condition of this module.
	 */
	public ExitCondition getExitCondition() {
		if (this.exitCondition == null) {
			// return default
			return ExitCondition.ALWAYS;
		} else {
			return this.exitCondition;
		}
	}

	/**
	 * Sets exit condition of this module, if option has not already been set.
	 *
	 * @param exitCondition A type of exit condition.
	 * @return {@code true} if the condition was set, {@code false} if condition had
	 *         already been set.
	 */
	public boolean setExitCondition(ExitCondition exitCondition) {
		if (this.exitCondition == null) {
			this.exitCondition = exitCondition;
			return true;
		} else {
			return false;
		}
	}

	/**
	 * @return The rule evaluation order of this module as specified in the program.
	 *         Note that inherited rule order is handled at runtime, because modules
	 *         can be reused by different parent modules that have different rule
	 *         orders.
	 */
	public RuleEvaluationOrder getRuleEvaluationOrder() {
		return this.order;
	}

	/**
	 * Sets exit condition of this module as specified in the program. Normally only
	 * called in the parser.
	 *
	 * @param order The rule evaluation order of this module as specified in the
	 *              program. Null if the module does not specify a rule order.
	 */
	public void setRuleEvaluationOrder(RuleEvaluationOrder order) {
		this.order = order;
	}

	/**
	 * @return The macros specified in the module.
	 */
	public List<Macro> getMacros() {
		return Collections.unmodifiableList(this.macros);
	}

	/**
	 * @param macro A macro specified in the module.
	 */
	public void addMacro(Macro macro) {
		this.macros.add(macro);
	}

	/**
	 * @return The decision rules of this module.
	 */
	public List<Rule> getRules() {
		return Collections.unmodifiableList(this.rules);
	}

	/**
	 * @param rule A decision rule of this module.
	 */
	public void addRule(Rule rule) {
		register(rule.getCondition());
		for (Action<?> action : rule.getAction()) {
			register(action);
		}
		this.rules.add(rule);
	}

	/**
	 * Mark this module as anonymous.
	 */
	public void setAnonymous() {
		this.anonymous = true;

	}

	/**
	 * A module is <i>anonymous</i> if it has no name. In that case, the module is
	 * used to represent a set of nested rules of a rule.
	 *
	 * @return {@code true} if the module has no name, {@code false} otherwise.
	 */
	public boolean isAnonymous() {
		return this.anonymous;
	}

	/**
	 * A module is adaptive if the rule evaluation order is
	 * {@link RuleEvaluationOrder#ADAPTIVE}
	 *
	 * @return {@code true} if the module is adaptive, {@code false} otherwise.
	 */
	public boolean isAdaptive() {
		return getRuleEvaluationOrder() == RuleEvaluationOrder.ADAPTIVE;
	}

	public void addLearnedBelief(String signature) {
		this.learnedBeliefs.add(signature);
	}

	public void addLearnedGoal(String signature) {
		this.learnedGoals.add(signature);
	}

	public Set<String> getLearnedBeliefs() {
		return Collections.unmodifiableSet(this.learnedBeliefs);
	}

	public Set<String> getLearnedGoals() {
		return Collections.unmodifiableSet(this.learnedGoals);
	}

	@SuppressWarnings("unchecked")
	public List<DatabaseFormula> getKnowledge() {
		return ((List<DatabaseFormula>) getItems(UseCase.KNOWLEDGE));
	}

	@SuppressWarnings("unchecked")
	public List<DatabaseFormula> getBeliefs() {
		return (List<DatabaseFormula>) getItems(UseCase.BELIEFS);
	}

	@SuppressWarnings("unchecked")
	public List<Query> getGoals() {
		return (List<Query>) getItems(UseCase.GOALS);
	}

	@SuppressWarnings("unchecked")
	public List<UserSpecAction> getActionSpecifications() {
		return (List<UserSpecAction>) getItems(UseCase.ACTIONSPEC);
	}

	@SuppressWarnings("unchecked")
	public List<Module> getUsedModules() {
		List<Module> usedModules = (List<Module>) getItems(UseCase.MODULE);
		usedModules.addAll((List<Module>) getItems(UseCase.PLANNER));
		return usedModules;
	}

	/**
	 * Recursively searches for (implicitly) referenced modules. Terminates if no
	 * new modules are found.
	 *
	 * @return List of all (indirectly) referenced modules from initial list of
	 *         modules.
	 */
	public Set<Module> referencedModules(Set<Module> found) {
		found.add(this);
		for (Module module : getUsedModules()) {
			if (found.add(module)) {
				module.referencedModules(found);
			}
		}
		return found;
	}

	/**
	 * @return A string with the name and parameters of this module.
	 */
	@Override
	public String toString() {
		StringBuilder str = new StringBuilder();
		str.append(this.name);

		if (!this.parameters.isEmpty()) {
			str.append("(");
			Iterator<Var> pars = this.parameters.iterator();
			while (pars.hasNext()) {
				str.append(pars.next());
				str.append(pars.hasNext() ? ", " : "");
			}
			str.append(")");
		}

		return str.toString();
	}

	// ----------------------------------------------------------------------------
	// Enum classes for focus methods, exit conditions, and rule evaluation
	// orders
	// ----------------------------------------------------------------------------

	/**
	 * The available options for creating an attention set associated with a module.
	 *
	 * <p>
	 * The options are:
	 * <ul>
	 * <li>{@link #FILTER}: goal from rule condition that acts like filter is
	 * inserted into the module's attention set.</li>
	 * <li>{@link #SELECT}: one of the agent's current goals that satisfies the rule
	 * condition is inserted into the module's attention set.</li>
	 * <li>{@link #NONE}: no new attention set associated with the module is
	 * created.</li>
	 * <li>{@link #NEW}: creates a new and empty attention set.</li>
	 * </ul>
	 * </p>
	 */
	public enum FocusMethod {
		/**
		 * After focusing, the agents gets a single goal for each of the positive
		 * {@link GoalLiteral} and {@link AGoalLiteral}s in the instantiated
		 * precondition of the rule that focuses on the module.
		 */
		FILTER,
		/**
		 * After focusing, the agent gets a single goal from the current attention set,
		 * which validates the 'context' of the module.
		 */
		SELECT,
		/**
		 * After focusing, the agent will have the same attention set as before. The
		 * same goal base is re-used. Goals in the <code>goals { }</code> section are
		 * simply added to that attention set. This is the default value.
		 */
		NONE,
		/**
		 * After focusing, the agent will have no goals in its attention set, aside from
		 * those defined in the Module's <code>goals { }</code> -section.
		 */
		NEW;
	}

	/**
	 * The available options for the exit condition of a module.
	 *
	 * <p>
	 * This condition is evaluated each time that the rules of the module have been
	 * evaluated (according to the rule evaluation order associated with the
	 * module). That is, <i>after</i> the rules have been evaluated, it is checked
	 * whether the module should be exited.
	 * </p>
	 *
	 * <p>
	 * Note that modules can also be exited using the <code>exit-module</code>
	 * action.
	 * </p>
	 */
	public static enum ExitCondition {
		/**
		 * The module should be exited once there are no goals left in the current
		 * attention set at the end of an evaluation step.
		 */
		NOGOALS,
		/**
		 * The module should be exited once an evaluation step produced no executed
		 * actions.
		 */
		NOACTION,
		/**
		 * The module should always be exited after an evaluation step.<br>
		 * This is the default value.
		 */
		ALWAYS,
		/**
		 * The module never exits. This is the default for the main program. If the main
		 * program exits, the agent dies.
		 */
		NEVER;
	}

	/**
	 * The different orders in which rules can be evaluated.
	 */
	public enum RuleEvaluationOrder {
		/**
		 * The <i>first</i> applicable rule will be applied. If more than one
		 * instantiation of an if-then rule can be applied, only the <i>first applicable
		 * instantiation</i> will be applied.
		 */
		LINEAR,
		/**
		 * The <i>first</i> applicable rule will be applied. If more than one
		 * instantiation of an if-then rule can be applied, a <i>random applicable
		 * instantiation</i> will be applied.
		 */
		LINEARRANDOM,
		/**
		 * <i>All</i> applicable rules will be applied. If more than one instantiation
		 * of an if-then rule can be applied, only the <i>first</i> applicable
		 * instantiation will be applied.
		 */
		LINEARALL,
		/**
		 * <i>All</i> applicable rules will be applied. If more than one instantiation
		 * of an if-then rule can be applied, a <i>random applicable instantiation</i>
		 * will be applied.
		 */
		LINEARALLRANDOM,
		/**
		 * A <i>random</i> applicable rule will be applied. If the rule to be applied is
		 * an if-then rule and more than one instantiation of the rule can be applied, a
		 * <i>random applicable instantiation</i> will be applied.
		 */
		RANDOM,
		/**
		 * <i>All</i> applicable rules will be applied in <i>random order</i>. If more
		 * than one instantiation of an if-then rule can be applied, a <i>random
		 * applicable instantiation</i> will be applied.
		 */
		RANDOMALL,
		/**
		 * <i>All options</i> of all applicable rules will be generated (i.e. all
		 * applicable instantiations of each rule will be computed). A learning
		 * mechanism determines which option will be applied.
		 */
		ADAPTIVE;
		/**
		 * All options of only the <i>first</i> applicable rule are generated. A
		 * learning mechanism determines which option will be applied.
		 */
		// LINEARADAPTIVE; FIXME: not implemented atm.
	}
}