package languageTools.analyzer.mas;

import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import krTools.parser.SourceInfo;
import languageTools.errors.ValidatorWarning;
import languageTools.errors.mas.MASWarning;
import languageTools.program.mas.Entity;
import languageTools.program.mas.LaunchInstruction;
import languageTools.program.mas.LaunchRule;

/**
 * Toolbox for semantic checks of MAS. Mainly used from MASValidator .
 */
public class MASValidatorTools {

	private static final String STAR = "*";

	/**
	 * Check that launch rules are not preceded by another launch rule that
	 * catches all entities of the same type. We do not know how entity names
	 * and entity types are related, so we can not check this if a launch rule
	 * asks for a specific name instead of type.
	 * 
	 * @param launchRules
	 *            the launch rules to check
	 * @return list of warning messages.
	 */
	public static List<ValidatorWarning> checkLaunchRulesReachable(List<LaunchRule> launchRules, SourceInfo info) {
		List<ValidatorWarning> warnings = new LinkedList<>();
		// a list of type names. If a type is in this list if we encountered a
		// rule that catches all of this type. "*" means all types are caught
		List<String> finishedTypes = new LinkedList<>();

		for (LaunchRule rule : launchRules) {
			if (finishedTypes.contains(STAR)) {
				// star-catchall catches everything so nothing can get after
				// it..
				warnings.add(
						new ValidatorWarning(MASWarning.PREV_LAUNCHRULE_CATCHALL_OF_TYPE, info, rule.toString(), STAR));
				continue;
			}
			if (!rule.isConditional())
				continue;
			Entity entity = rule.getEntity();
			if (entity.getName() == null) { // if name=null, this works by type
				String type = entity.getType();
				if (type == null)
					type = STAR;
				if (finishedTypes.contains(type)) {
					warnings.add(new ValidatorWarning(MASWarning.PREV_LAUNCHRULE_CATCHALL_OF_TYPE, info,
							rule.toString(), type));
				}
				if (rule.isCatchAll()) {
					// then this is the last reachable launch rule.
					finishedTypes.add(type);
				}
			}
		}
		return warnings;
	}

	/**
	 * Check that all agent definitions have been used
	 * 
	 * @param launchRules
	 *            the launch rules to check
	 * @param agentNames
	 *            all known agent names
	 * @return list of warning messages.
	 */
	public static List<ValidatorWarning> checkAgentsUsed(List<LaunchRule> launchRules, SourceInfo info,
			Set<String> agentNames) {
		List<ValidatorWarning> warnings = new LinkedList<>();
		List<String> agentsUsed = new LinkedList<>();
		for (LaunchRule rule : launchRules) {
			for (LaunchInstruction instruction : rule.getInstructions()) {
				agentsUsed.add(instruction.getAgentName());
			}
		}
		if (!agentsUsed.containsAll(agentNames)) {
			warnings.add(new ValidatorWarning(MASWarning.AGENT_UNUSED, info));
		}
		return warnings;
	}

	/**
	 * If environment is specified, launch policy section should have
	 * conditional launch rules to connect agents to it.
	 * 
	 * @param launchRules
	 *            the launch rules to check
	 * @return list of warning messages.
	 */
	public static List<ValidatorWarning> checkEnvironmentUsed(List<LaunchRule> launchRules, SourceInfo info) {
		List<ValidatorWarning> warnings = new LinkedList<>();
		boolean crule = false;
		for (LaunchRule rule : launchRules) {
			if (rule.isConditional()) {
				crule = true;
				break;
			}
		}
		if (!crule) {
			warnings.add(new ValidatorWarning(MASWarning.LAUNCH_NO_CONDITIONAL_RULES, info));
		}
		return warnings;
	}

}
