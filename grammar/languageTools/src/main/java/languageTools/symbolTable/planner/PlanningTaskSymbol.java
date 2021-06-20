package languageTools.symbolTable.planner;

import krTools.parser.SourceInfo;
import languageTools.program.planner.PlanningTask;
import languageTools.symbolTable.Symbol;

public class PlanningTaskSymbol extends Symbol {

	private final PlanningTask planningTask;

	public PlanningTaskSymbol(String signature, PlanningTask planningTask, SourceInfo info) {
		super(signature, info);
		this.planningTask = planningTask;
	}

	/**
	 * @return The action specification associated with this symbol.
	 */
	public PlanningTask getPlanningTask() {
		return this.planningTask;
	}

	/**
	 * @return String representation of this {@link #PlanningTaskSymbol(String)}.
	 */
	@Override
	public String toString() {
		return "<PlanningTaskSymbol: " + this.planningTask + ">";
	}

}
