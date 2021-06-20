package languageTools.errors.planner;

import java.util.MissingFormatArgumentException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import languageTools.errors.ValidatorWarning.ValidatorWarningType;

public enum PlannerWarning implements ValidatorWarningType {

	
	/**
	 * Each type of option should be used only once.
	 */
	DUPLICATE_OPTION,
	/**
	 * There should be a matching belief for every goal (to support automated
	 * removal of goal when achieved).
	 */
	KR_GOAL_DOES_NOT_MATCH_BELIEF,
	
	/**
	 * Module parameter should be used.
	 */
	PARAMETER_NEVER_USED,
	/**
	 * The file is empty.
	 */
	EMPTY_FILE,
	/**
	 * The name of the module does not match the file name.
	 */
	MODULE_NAME_MISMATCH,
	/**
	 * A variable is unused.
	 */
	VARIABLE_UNUSED, METHOD_UNUSED;
	
	private static final ResourceBundle BUNDLE = ResourceBundle
			.getBundle("languageTools.messages.PlannerWarningMessages");

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
