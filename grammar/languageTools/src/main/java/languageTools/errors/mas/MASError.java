package languageTools.errors.mas;

import java.util.MissingFormatArgumentException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import languageTools.errors.ValidatorError.ValidatorErrorType;

public enum MASError implements ValidatorErrorType {
	/**
	 * Name constraints in launch rules should not result in naming conflicts.
	 */
	AGENT_DUPLICATE_GIVENNAME,
	/**
	 * Same agent name should not be used more than once.
	 */
	AGENT_DUPLICATE_NAME,
	/**
	 * Should be able to locate environment file.
	 */
	ENVIRONMENT_COULDNOT_FIND,
	/**
	 * Environment interface file should be a jar file.
	 */
	ENVIRONMENT_NOT_JAR,
	/**
	 * Initialization parameter in environment section should be valid.
	 */
	INIT_UNRECOGNIZED_PARAMETER,
	/**
	 * Wild cards should not be used in unconditional launch rules.
	 */
	LAUNCH_INVALID_WILDCARD,
	/**
	 * Launch instruction refers to missing agent definition.
	 */
	LAUNCH_MISSING_AGENTDF,
	/**
	 * Referenced file could not be found.
	 */
	 REFERENCE_COULDNOT_FIND,
	/**
	 * Duplicate use clause for init, event, or main module.
	 */
	USECASE_DUPLICATE, USECASE_INVALID,
	/**
	 * Predicate already defined as knowledge.
	 */
	PREDICATE_ALREADY_KNOWLEDGE,
	/**
	 * Can't get signature.
	 */
	NO_SIGNATURE;

	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("languageTools.messages.MASErrorMessages");

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
