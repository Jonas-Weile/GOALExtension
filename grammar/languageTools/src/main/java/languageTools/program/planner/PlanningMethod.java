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

public class PlanningMethod extends GoalParsedObject {

	final String name;
	final List<Decomposition> decompositions;
	protected final List<Term> parameters = new LinkedList<>();
	protected final Set<Var> free = new LinkedHashSet<>();
	
	public String getName() {
		return this.name;
	}
	
	public List<Decomposition> getDecompositions() {
		return Collections.unmodifiableList(this.decompositions);
	}
	
	public List<PlanningTask> getAllSubtasks() {
		List<PlanningTask> subtasks = new LinkedList<>();
		
		for (Decomposition decomposition : this.decompositions) {
			subtasks.addAll(decomposition.getSubtasks());
		}
		
		return subtasks;
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
	
	public String getSignature() {
		return this.name + "/" + getParameters().size();
	}
	
	public PlanningMethod(String name, List<Term> parameters, List<Decomposition> decompositions, SourceInfo info) {
		super(info);
		this.name = name;
		
		for (Term parameter : parameters) {
			addParameter(parameter);
		}
		
		this.decompositions = decompositions;
	}
	
	
	public boolean isClosed() {
		return getFreeVar().isEmpty();
	}
	
	
	public PlanningMethod applySubst(Substitution substitution) {
		// Apply substitution to action parameters, pre- and post-condition.
		List<Term> parameters = new ArrayList<>(getParameters().size());
		for (Term parameter : getParameters()) {
			parameters.add(parameter.applySubst(substitution));
		}
		
		List<Decomposition> decompositions = new ArrayList<>(getDecompositions().size());
		for (Decomposition decomposition : getDecompositions()) {
			decompositions.add(decomposition.applySubst(substitution));
		}
				
		return new PlanningMethod(getName(), parameters, decompositions, getSourceInfo());
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
		PlanningMethod other = (PlanningMethod) obj;
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
