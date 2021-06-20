package languageTools.program.planner;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import krTools.language.Substitution;
import krTools.language.Var;
import languageTools.program.actionspec.ActionPreCondition;
import languageTools.program.agent.msc.AGoalLiteral;
import languageTools.program.agent.msc.BelLiteral;
import languageTools.program.agent.msc.MentalFormula;
import languageTools.program.agent.msc.MentalStateCondition;
import languageTools.program.agent.selector.Selector;
import languageTools.program.agent.selector.Selector.SelectorType;

public class Decomposition {
	
	final AGoalLiteral aGoalLiteral;
	final ActionPreCondition precondition;
	final Deque<PlanningTask> subtasks;
	private final Set<Var> free = new LinkedHashSet<>();
	
	public AGoalLiteral getAGoalLiteral() {
		return this.aGoalLiteral;
	}
	
	public ActionPreCondition getFullPreCondition() {
		return this.precondition;
	}
	
	public Deque<PlanningTask> getSubtasks() {
		return this.subtasks;
	}
	
	public Decomposition(AGoalLiteral aGoalLiteral, ActionPreCondition precondition, Deque<PlanningTask> subtasks) {
		this.aGoalLiteral = aGoalLiteral;
		this.precondition = precondition;
		this.subtasks = subtasks;
		
		for (PlanningTask subtask : subtasks) {
			this.free.addAll(subtask.getFreeVar());
		}
		
		if (aGoalLiteral != null) {
			this.free.removeAll(aGoalLiteral.getFreeVar());
		}
		this.free.removeAll(precondition.getPreCondition().getFreeVar());
	}
	
	public MentalStateCondition getPrecondition() {
		// Create mental state condition of the form "self.bel(precondition)".
		if (this.precondition == null) {
			return new MentalStateCondition(null, null);
		} else {
			List<MentalFormula> formulalist = new ArrayList<>(1);
			formulalist.add(new BelLiteral(true, new Selector(SelectorType.SELF, this.precondition.getSourceInfo()),
					this.precondition.getPreCondition(), this.precondition.getSignatures(),
					this.precondition.getSourceInfo()));
			return new MentalStateCondition(formulalist, this.precondition.getSourceInfo());
		}
	}
	
	public Decomposition applySubst(Substitution substitution) {
		AGoalLiteral aGoalLiteral = (getAGoalLiteral() == null) ? null
				: getAGoalLiteral().applySubst(substitution);
		
		ActionPreCondition precondition = (getFullPreCondition() == null) ? null
				: getFullPreCondition().applySubst(substitution);
				
		Deque<PlanningTask> subtasks = new ArrayDeque<>(getSubtasks().size());
		for (PlanningTask subtask : getSubtasks()) {
			subtasks.add(subtask.applySubst(substitution));
		}
		
		return new Decomposition(aGoalLiteral, precondition, subtasks);
	}

	public Set<Var> getFreeVar() {
		return Collections.unmodifiableSet(this.free);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		Decomposition other = (Decomposition) obj;
		
		if (this.subtasks == null) {
			if (other.getSubtasks() != null) {
				return false;
			}
		} else if (!this.subtasks.equals(other.getSubtasks())) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		
		if (this.aGoalLiteral != null) {
			sb.append("a-goal literal: ");
			sb.append(this.aGoalLiteral.toString());
			sb.append(", ");
		}
		
		sb.append("precondition: ");
		sb.append(this.precondition.toString());
		sb.append(", subtasks: [");
		
		for (PlanningTask subtask : this.subtasks) {
			sb.append(subtask.toString());
			sb.append(", ");
		}
		sb.delete(sb.length()-2, sb.length());
		sb.append("]");
		
		return sb.toString();
	}
}
