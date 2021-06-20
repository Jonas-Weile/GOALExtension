package languageTools.errors.mas;

import java.util.MissingFormatArgumentException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import languageTools.errors.ValidatorWarning.ValidatorWarningType;

public enum MASWarning implements ValidatorWarningType {
	/**
	 * Agent definitions should be used in launch rules.
	 */
	AGENT_UNUSED,
	/**
	 * Any type of entity constraint should be specified only once in a launch
	 * rule.
	 */
	CONSTRAINT_DUPLICATE,
	/**
	 * Same key should not be used more than once.
	 */
	INIT_DUPLICATE_KEY,
	/**
	 * If environment is specified, launch policy section should have
	 * conditional rules to connect agents to it.
	 */
	LAUNCH_NO_CONDITIONAL_RULES, LAUNCH_CONDITIONAL_RULE,
	/**
	 * Unused stuff
	 */
	PREDICATE_UNUSED, ACTION_UNUSED, MODULE_UNUSED, VARIABLE_UNUSED, PREV_LAUNCHRULE_CATCHALL_OF_TYPE;

	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("languageTools.messages.MASWarningMessages");

	@Override
	public String toReadableString(String... args) {
		try {
			return String.format(BUNDLE.getString(name()), (Object[]) args);
		} catch (MissingResourceException e1) {
			if (args.length > 0) {
				return args[0];
			} else {
				return name();
			}
		} catch (MissingFormatArgumentException e2) {
			return BUNDLE.getString(name());
		}
	}
}
