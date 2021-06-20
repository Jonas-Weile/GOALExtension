package languageTools.program.planner;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import krTools.language.Substitution;
import krTools.language.Term;
import krTools.language.Var;
import krTools.parser.SourceInfo;
import languageTools.analyzer.FileRegistry;
import languageTools.program.agent.Module;

/**
 * Skeleton for planner module
 */
public class PlanningModule extends Module {
	
	private PlanningTask task;
	private Map<String, PlanningMethod> methods = new HashMap<>();
	private Map<String, PlanningOperator> operators = new HashMap<>();

	
	public PlanningTask getTask() {
		return this.task;
	}
	
	public PlanningTask getSubstitutedTask(Substitution substitution) {
		return this.task.applySubst(substitution);
	}
	
	public void setTask(PlanningTask task) {
		this.task = task;
	}
	
	public PlanningMethod getMethod(String signature) {
		return this.methods.get(signature);
	}
	
	public PlanningMethod getSubstitutedMethod(String signature, List<Term> callerParameters, Substitution substitution) {
		PlanningMethod method = this.methods.get(signature);
		if (method != null) {
			List<Term> methodParameters = method.getParameters();
			
			Substitution translatedSubstitution = substitution.clone();
			translatedSubstitution.retainAll(new HashSet<Var>(0));
			
			for (int i = 0; i < methodParameters.size(); i++) {
				translatedSubstitution.addBinding((Var) methodParameters.get(i), callerParameters.get(i).applySubst(substitution));
			}
			
			return method.applySubst(translatedSubstitution);
		}
		return null;
	}
	
	public Collection<PlanningMethod> getAllMethods() {
		return this.methods.values();
	}
	
	public void addMethod(PlanningMethod method) {
		this.methods.put(method.getSignature(), method);
	}
	
	public PlanningOperator getOperator(String signature) {
		return this.operators.get(signature);
	}
	
	public PlanningOperator getSubstitutedOperator(String signature, List<Term> callerParameters, Substitution substitution) {
		PlanningOperator operator = this.operators.get(signature);
		
		if (operator != null) {
			List<Term> operatorParameters = operator.getParameters();
			
			Substitution translatedSubstitution = substitution.clone();
			translatedSubstitution.retainAll(new HashSet<Var>(0));
			
			for (int i = 0; i < operatorParameters.size(); i++) {
				translatedSubstitution.addBinding((Var) operatorParameters.get(i), callerParameters.get(i).applySubst(substitution));
			}
			
			return operator.applySubst(translatedSubstitution);
		}
		return null;
	}
	
	public Collection<PlanningOperator> getAllOperators() {
		return this.operators.values();
	}
	
	public void addOperator(PlanningOperator operator) {
		this.operators.put(operator.getSignature(), operator);
	}	
	
	
	/**
	 * Creates a new Planning Module program.
	 *
	 * @param info
	 *            Source info.
	 */
	public PlanningModule(FileRegistry registry, SourceInfo info) {
		super(registry, info);
	}	
}
