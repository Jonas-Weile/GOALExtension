package languageTools.symbolTable.planner;

import krTools.parser.SourceInfo;
import languageTools.program.planner.PlanningOperator;
import languageTools.symbolTable.Symbol;

public class PlanningOperatorSymbol extends Symbol {

	private final PlanningOperator planningOperator;

	public PlanningOperatorSymbol(String signature, PlanningOperator planningOperator, SourceInfo info) {
		super(signature, info);
		this.planningOperator = planningOperator;
	}

	/**
	 * @return The action specification associated with this symbol.
	 */
	public PlanningOperator getPlanningTask() {
		return this.planningOperator;
	}

	/**
	 * @return String representation of this {@link #PlanningTaskSymbol(String)}.
	 */
	@Override
	public String toString() {
		return "<PlanningOperatorSymbol: " + this.planningOperator + ">";
	}

}