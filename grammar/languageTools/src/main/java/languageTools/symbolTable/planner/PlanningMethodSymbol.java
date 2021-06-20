package languageTools.symbolTable.planner;

import krTools.parser.SourceInfo;
import languageTools.program.planner.PlanningMethod;
import languageTools.symbolTable.Symbol;

public class PlanningMethodSymbol extends Symbol {

	private final PlanningMethod planningMethod;

	public PlanningMethodSymbol(String signature, PlanningMethod planningMethod, SourceInfo info) {
		super(signature, info);
		this.planningMethod = planningMethod;
	}

	/**
	 * @return The action specification associated with this symbol.
	 */
	public PlanningMethod getPlanningMethod() {
		return this.planningMethod;
	}

	/**
	 * @return String representation of this {@link #PlanningMethodSymbol(String)}.
	 */
	@Override
	public String toString() {
		return "<PlanningMethodSymbol: " + this.planningMethod + ">";
	}

}
