package goal.tools.profiler;

import events.Channel;

/**
 * The types of info that we derive from the incoming data. This is a kind of
 * categories of {@link Channel}.
 */
public enum InfoType {
	/** global info, not specific to one module */
	GLOBAL("totals for runtime, rounds, KR and module calls"),
	/** module info */
	MODULE("module"),
	/** per-rule info */
	RULE("rule"),
	/** Info for the condition parts of rules */
	RULE_CONDITION("rule condition"),
	/** Info about rule calls to actions */
	RULE_ACTION("rule action"),
	/** Info about KR calls made for specific rules or conditions */
	KR_CALL("rule KR calls");

	private String description;

	InfoType(String desc) {
		this.description = desc;
	}

	public String getDescription() {
		return this.description;
	}
}
