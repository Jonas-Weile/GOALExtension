package languageTools.program.planner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;
import krTools.parser.SourceInfo;
import languageTools.program.GoalParsedObject;
import languageTools.program.agent.actions.Action;
import languageTools.program.agent.msc.AGoalLiteral;

public class PlanningTask extends GoalParsedObject {
	
	
	private final String name;
	protected final List<Term> parameters = new LinkedList<>();
	protected final Set<Var> free = new LinkedHashSet<>();
	private boolean primitive;
	
	
	public String getName() {
		return this.name;
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
	
	public void markAsPrimitive() {
		this.primitive = true;
	}
	
	public void markAsCompound() {
		this.primitive = false;
	}
	
	public boolean isPrimitive() {
		return this.primitive;
	}
	
	public PlanningTask(String name, List<Term> parameters, SourceInfo info) {
		super(info);

		this.name = name;
		
		for (Term parameter : parameters) {
			addParameter(parameter);
		}
	}
	
	public String getSignature() {
		return this.name + "/" + getParameters().size();
	}

	public boolean isClosed() {
		return getFreeVar().isEmpty();
	}
	
	public PlanningTask applySubst(Substitution substitution) {
		// Apply substitution to action parameters, pre- and post-condition.
		List<Term> parameters = new ArrayList<>(getParameters().size());
		for (Term parameter : getParameters()) {
			parameters.add(parameter.applySubst(substitution));
		}
		
		return new PlanningTask(getName(), parameters, getSourceInfo()).withPrimitive(this.primitive);
	}

	
	// Helper method
	private PlanningTask withPrimitive(Boolean primitive) {
		this.primitive = primitive;
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
		PlanningTask other = (PlanningTask) obj;
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
