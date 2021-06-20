package languageTools.program.planner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Update;
import krTools.language.Var;
import krTools.parser.SourceInfo;
import languageTools.program.GoalParsedObject;
import languageTools.program.actionspec.ActionPostCondition;
import languageTools.program.actionspec.ActionPreCondition;
import languageTools.program.agent.actions.Action;
import languageTools.program.agent.msc.BelLiteral;
import languageTools.program.agent.msc.MentalFormula;
import languageTools.program.agent.msc.MentalStateCondition;
import languageTools.program.agent.selector.Selector;
import languageTools.program.agent.selector.Selector.SelectorType;

public class PlanningOperator extends GoalParsedObject {
		
	final String name;
	private Action<?> action;
	protected final List<Term> parameters = new LinkedList<>();
	protected final Set<Var> free = new LinkedHashSet<>();
	private final ActionPreCondition precondition;
	private final ActionPostCondition positivePostcondition;
	private final ActionPostCondition negativePostcondition;
		
	public String getName() {
		return this.name;
	}
	
	public Action<?> getAction() {
		return this.action;
	}
	
	public void setAction(Action<?> action) {
		this.action = action;
	}
	
	
	public List<Term> getParameters() {
		return Collections.unmodifiableList(this.parameters);
	}
	
	public void addParameter(Term parameter) {
		this.parameters.add(parameter);
		this.free.addAll(parameter.getFreeVar());
	}
	
	public Set<Var> getFreeVar() {
		return Collections.unmodifiableSet(this.free);
	}
	
	public ActionPreCondition getFullPreCondition() {
		return this.precondition;
	}

	public ActionPostCondition getPositivePostcondition() {
		return this.positivePostcondition;
	}

	public ActionPostCondition getNegativePostcondition() {
		return this.negativePostcondition;
	}
	
	public String getSignature() {
		return this.name + "/" + getParameters().size();
	}
	
	
	public PlanningOperator(String name, List<Term> parameters, ActionPreCondition precondition, ActionPostCondition positivePostcondition, ActionPostCondition negativePostcondition, SourceInfo info) {
		super(info);
		this.name = name;
		for (Term parameter : parameters) {
			addParameter(parameter);
		}
		this.precondition = precondition;
		this.positivePostcondition = positivePostcondition;
		this.negativePostcondition = negativePostcondition;
	}
	
	
	
	
	public Substitution createSubstitution(Substitution substitution) {

		Substitution translatedSubstitution = substitution.clone();
		translatedSubstitution.retainAll(new HashSet<Var>(0));
		
		for (int i = 0; i < getParameters().size(); i++) {
			translatedSubstitution.addBinding((Var) getAction().getParameters().get(i), getParameters().get(i).applySubst(substitution));
		}
		
		return translatedSubstitution;
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
	
	public PlanningOperator applySubst(Substitution substitution) {
		// Apply substitution to parameters, pre- and post-condition.
		List<Term> parameters = new ArrayList<>(getParameters().size());
		for (Term parameter : getParameters()) {
			parameters.add(parameter.applySubst(substitution));
		}
		Action<?> action = (getAction() == null) ? null 
				: getAction();
		ActionPreCondition precondition = (getFullPreCondition() == null) ? null
				: getFullPreCondition().applySubst(substitution);
		ActionPostCondition positivePostcondition = (getPositivePostcondition() == null) ? null
				: getPositivePostcondition().applySubst(substitution);
		ActionPostCondition negativePostcondition = (getNegativePostcondition() == null) ? null
				: getNegativePostcondition().applySubst(substitution);
		
		return new PlanningOperator(getName(), parameters, precondition, positivePostcondition, negativePostcondition, getSourceInfo()).withAction(action);
	}
	
	// Helper method
	private PlanningOperator withAction(Action<?> action) {
		this.action = action;
		return this;
	}
		
	
	/**
	 * Default implementations of string, hashcode and equals	 
	 * 
	 */
	@Override
	public String toString() {
		String str = this.name;
		if (!getParameters().isEmpty()) {
			str += "(";
			for (int i = 0; i < getParameters().size(); i++) {
				str += getParameters().get(i);
				str += (i < getParameters().size() - 1 ? ", " : "");
			}
			str += ")";
		}
		return str;
	}

	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = getClass().hashCode();
		result = prime * result + ((this.name == null) ? 0 : this.name.hashCode());
		result = prime * result + ((this.parameters == null) ? 0 : this.parameters.hashCode());
		return result;
	}

	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null || getClass() != obj.getClass()) {
			return false;
		}
		PlanningOperator other = (PlanningOperator) obj;
		if (this.name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!this.name.equals(other.name)) {
			return false;
		}
		if (this.parameters == null) {
			if (other.parameters != null) {
				return false;
			}
		} else if (!this.parameters.equals(other.parameters)) {
			return false;
		}
		return true;
	}

}
