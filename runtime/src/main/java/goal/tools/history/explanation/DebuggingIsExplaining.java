
package goal.tools.history.explanation;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import goal.tools.history.EventStorage;
import goal.tools.history.events.AbstractEvent;
import goal.tools.history.events.ActionEvent;
import goal.tools.history.events.CallEvent;
import goal.tools.history.events.InspectionEvent;
import goal.tools.history.events.ModificationAction;
import goal.tools.history.events.ModificationEvent;
import goal.tools.history.explanation.reasons.ActionPreCondition;
import goal.tools.history.explanation.reasons.ActionReason;
import goal.tools.history.explanation.reasons.ActionRuleCondition;
import goal.tools.history.explanation.reasons.NoActionNeverApplied;
import goal.tools.history.explanation.reasons.NoActionNeverEvaluated;
import goal.tools.history.explanation.reasons.NoActionNeverSatisfied;
import goal.tools.history.explanation.reasons.NoActionReason;
import goal.tools.history.explanation.reasons.Reason;
import krTools.KRInterface;
import krTools.language.DatabaseFormula;
import krTools.language.Substitution;
import krTools.language.Update;
import krTools.parser.ParsedObject;
import krTools.parser.SourceInfo;
import languageTools.program.ProgramMap;
import languageTools.program.agent.Module;
import languageTools.program.agent.actions.Action;
import languageTools.program.agent.msc.AGoalLiteral;
import languageTools.program.agent.msc.BelLiteral;
import languageTools.program.agent.msc.GoalALiteral;
import languageTools.program.agent.msc.GoalLiteral;
import languageTools.program.agent.msc.MentalLiteral;
import languageTools.program.agent.msc.MentalStateCondition;
import languageTools.program.agent.rules.Rule;

/**
 * Supports generating explanations ({@link Reason}s) from an agent trace based
 * on the 'Debugging is Explaining' paper (Hindriks 2012).
 */
public class DebuggingIsExplaining {
	private final EventStorage trace;
	private final ProgramMap map;
	private Set<DatabaseFormula> beliefs;
	private Set<DatabaseFormula> goals;
	private Set<Action<?>> actions;

	public DebuggingIsExplaining(final EventStorage trace, final ProgramMap map) {
		this.trace = trace;
		this.map = map;
	}

	/**
	 * Call this to update the info returned by {@link #getAllActions()},
	 * {@link #getAllBeliefs()}, and {@link #getAllGoals()}.
	 */
	public void process() {
		this.beliefs = new LinkedHashSet<>();
		this.goals = new LinkedHashSet<>();
		this.actions = new LinkedHashSet<>();
		if (this.trace == null) {
			return;
		}
		for (final AbstractEvent event : this.trace.getAll()) {
			if (event instanceof InspectionEvent) {
				final InspectionEvent inspection = (InspectionEvent) event;
				for (final MentalLiteral literal : inspection.getMentalStateCondition(this.map).getAllLiterals()) {
					if (literal instanceof BelLiteral) {
						final Update query = literal.getFormula().toUpdate();
						this.beliefs.addAll(query.getAddList());
						this.beliefs.addAll(query.getDeleteList());
					} else if (literal instanceof GoalLiteral || literal instanceof GoalALiteral
							|| literal instanceof AGoalLiteral) {
						final Update query = literal.getFormula().toUpdate();
						this.goals.addAll(query.getAddList());
						this.goals.addAll(query.getDeleteList());
					}
				}
			} else if (event instanceof ModificationEvent) {
				final ModificationAction modification = ((ModificationEvent) event).getUpdate();
				switch (modification.getBase()) {
				case BELIEFBASE:
					this.beliefs.addAll(modification.getAdded());
					this.beliefs.addAll(modification.getRemoved());
					break;
				case GOALBASE:
					this.goals.addAll(modification.getAdded());
					this.goals.addAll(modification.getRemoved());
					break;
				default:
					break;
				}
			} else if (event instanceof CallEvent) {
				final Action<?> called = ((CallEvent) event).getAction(this.map);
				if (called != null) {
					this.actions.add(called);
				}
			} else if (event instanceof ActionEvent) {
				final Action<?> executed = ((ActionEvent) event).getAction(this.map);
				if (executed != null) {
					this.actions.add(executed);
				}
			}
		}
	}

	/**
	 * @return All beliefs (instantiated or not) that can be found in the trace,
	 *         either queried or actually believed at some point. Call
	 *         {@link #process()} first to update the returned beliefs.
	 */
	public Set<DatabaseFormula> getAllBeliefs() {
		return Collections.unmodifiableSet(this.beliefs);
	}

	/**
	 * @return All goals (instantiated or not) that can be found in the trace,
	 *         either queried or actually to be achieved at some point. Call
	 *         {@link #process()} first to update the returned goals.
	 */
	public Set<DatabaseFormula> getAllGoals() {
		return Collections.unmodifiableSet(this.goals);
	}

	/**
	 * @return All actions (instantiated or not) that can be found in the trace,
	 *         either tried or actually executed at some point. Call
	 *         {@link #process()} first to update the returned actions.
	 */
	public Set<Action<?>> getAllActions() {
		return Collections.unmodifiableSet(this.actions);
	}

	/**
	 * @param action
	 *            An action from which we want to know why it was excuted.
	 * @return One or more {@link ActionReason}s (if the action was actually
	 *         executed) explaining why this action was executed (each entry
	 *         corresponds to one successful execution).
	 */
	public List<Reason> whyAction(final Action<?> action, final KRInterface kri) {
		final List<Reason> reasons = new LinkedList<>();
		if (this.trace == null) {
			return reasons;
		}
		Action<?> executed = null;
		ActionReason current = null;
		Substitution subst = null;
		for (int i = (this.trace.getMax() - 1); i >= 0; --i) {
			final AbstractEvent event = this.trace.getAll().get(i);
			if (event instanceof ActionEvent) {
				executed = ((ActionEvent) event).getAction(this.map);
			} else if (executed != null && event instanceof CallEvent) {
				final CallEvent call = (CallEvent) event;
				subst = call.getSubstitution();
				executed = executed.applySubst(subst);
				if (action.mgu(executed, kri) != null) {
					current = new ActionReason(executed, i);
				}
				executed = null;
			} else if (current != null && event instanceof InspectionEvent) {
				final InspectionEvent inspection = (InspectionEvent) event;
				final MentalStateCondition msc = inspection.getMentalStateCondition(this.map);
				if (current.hasPreConditionReason()) {
					final ActionRuleCondition pre = new ActionRuleCondition(msc, subst, i);
					current.setRuleCondition(pre);
					reasons.add(current);
					current = null;
				} else {
					final ActionPreCondition pre = new ActionPreCondition(msc, i);
					current.setPreCondition(pre);
				}
			}
		}
		return reasons;
	}

	/**
	 * @param action
	 *            An action from which we want to know why it was excuted.
	 * @return A list containing a single {@link NoActionReason} (if the action
	 *         was never actually executed; one or more {@link ActionReason}s
	 *         otherwise, see {@link DebuggingIsExplaining#whyAction(Action)})
	 *         explaining why this action was not executed.
	 */
	public List<Reason> whyNotAction(final Action<?> action, final KRInterface kri) {
		final List<Reason> reasons = whyAction(action, kri);
		if (reasons.isEmpty() && this.trace != null) {
			final Set<SourceInfo> related = new LinkedHashSet<>();
			for (final ParsedObject object : this.map.getAll()) {
				if (object instanceof Module) {
					Module module = (Module) object;
					for (final Rule rule : module.getRules()) {
						for (final Action<?> call : rule.getAction()) {
							if (call.getSignature().equals(action.getSignature())) {
								related.add(rule.getCondition().getSourceInfo());
								break;
							}
						}
					}
				}
			}
			final Set<Action<?>> otherInstances = new LinkedHashSet<>();
			boolean preEvaluated = false, ruleEvaluated = false;
			for (final AbstractEvent event : this.trace.getAll()) {
				if (event instanceof CallEvent) {
					final CallEvent call = (CallEvent) event;
					final Action<?> called = (call.getAction(this.map) == null) ? null
							: call.getAction(this.map).applySubst(call.getSubstitution());
					if (action.mgu(called, kri) != null) {
						preEvaluated = true;
						break;
					} else if (called != null && action.getSignature().equals(called.getSignature())) {
						otherInstances.add(called);
					}
				} else if (!ruleEvaluated && event instanceof InspectionEvent) {
					final MentalStateCondition condition = ((InspectionEvent) event).getMentalStateCondition(this.map);
					if (related.contains(condition.getSourceInfo())) {
						ruleEvaluated = true;
					}
				}
			}
			NoActionReason noAction = null;
			if (preEvaluated) {
				final NoActionNeverSatisfied neverSatisfied = new NoActionNeverSatisfied(action);
				neverSatisfied.setRuleSatisfied();
				noAction = neverSatisfied;
			} else if (otherInstances.size() > 0) {
				final NoActionNeverApplied neverApplied = new NoActionNeverApplied(action);
				neverApplied.setOtherInstances(otherInstances);
				noAction = neverApplied;
			} else if (ruleEvaluated) {
				final NoActionNeverSatisfied neverSatisfied = new NoActionNeverSatisfied(action);
				noAction = neverSatisfied;
			} else {
				final NoActionNeverEvaluated neverEvaluated = new NoActionNeverEvaluated(action);
				neverEvaluated.setRelatedRules(related);
				noAction = neverEvaluated;
			}
			reasons.add(noAction);
		}
		return reasons;
	}
}
