package languageTools.errors.actionspec;

import java.util.MissingFormatArgumentException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import languageTools.errors.ValidatorError.ValidatorErrorType;

public enum ActionSpecError implements ValidatorErrorType {

	/**
	 * The same action signature should not be defined twice.
	 */
	ACTION_LABEL_ALREADY_DEFINED,
	/**
	 * Action parameter should not be duplicated.
	 */
	DUPLICATE_PARAMETER,
	/**
	 * It should be possible to infer the KR interface used in the specification
	 * file from its use clauses.
	 */
	KR_COULDNOT_RESOLVE_NO_KR_USECLAUSES, KR_COULDNOT_RESOLVE_DIFFERENT_KRS_USED,
	/**
	 * A KR expression used as a pre - condition should be defined.
	 */
	KR_PRECONDITION_NEVER_DEFINED,
	/**
	 * A formal action parameter cannot be an anonymous variable.
	 */
	KR_PROLOG_ANONYMOUS_VARIABLE,
	/**
	 * Free variables in post-condition should be bound by formal action
	 * parameters or by precondition.
	 */
	POSTCONDITION_UNBOUND_VARIABLE,
	/**
	 * Referenced file could not be found.
	 */
	REFERENCE_COULDNOT_FIND,
	/**
	 * Referenced file is a duplicate.
	 */
	REFERENCE_DUPLICATE;

	private static final ResourceBundle BUNDLE = ResourceBundle
			.getBundle("languageTools.messages.ActionSpecErrorMessages");

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
