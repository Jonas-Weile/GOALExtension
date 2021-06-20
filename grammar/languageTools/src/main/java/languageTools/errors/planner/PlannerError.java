package languageTools.errors.planner;

import java.util.MissingFormatArgumentException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import languageTools.errors.ValidatorError.ValidatorErrorType;

public enum PlannerError implements ValidatorErrorType {
	ACTION_INVALID, CONDITION_INVALID,
	/**
	 * The same action signature should not be defined in two different
	 * references files, whether action specification or module files.
	 */
	ACTION_LABEL_DEFINED_BY_MULTIPLE_REFERENCES,
	/**
	 * A user-defined action that is used in a rule should be specified in an
	 * action specification file, or as a module.
	 */
	OPERATOR_USED_NEVER_DEFINED,
	/**
	 * The task has no corresponding method nor operator
	 */
	UNBOUND_TASK,
	/**
	 * Module parameter should not be duplicated.
	 */
	DUPLICATE_PARAMETER,
	/**
	 * A KR expression that is used as a belief query should be defined in a KR
	 * file.
	 */
	KR_BELIEF_QUERIED_NEVER_DEFINED,
	/**
	 * It should be possible to infer the KR interface used in the module from
	 * its use clauses.
	 */
	KR_COULDNOT_RESOLVE_NO_KR_USECLAUSES, KR_COULDNOT_RESOLVE_DIFFERENT_KRS_USED,
	/**
	 * The resolved KR interface should be initialized correctly.
	 */
	KR_COULDNOT_INITIALIZE,
	/**
	 * A KR expression used as a goal query should be defined in a KR file.
	 */
	KR_GOAL_QUERIED_NEVER_DEFINED,
	/**
	 * Prolog update of adopt action cannot contain negative facts, e.g.
	 * adopt(on(a,b), not(on(b,c))) is not allowed.
	 */
	KR_PROLOG_ADOPT_UPDATE_NEGATIVE,
	/**
	 * A formal parameter of a module or macro cannot be an anonymous variable.
	 */
	KR_PROLOG_ANONYMOUS_VARIABLE,
	/**
	 * Listall parameter cannot be a Prolog anonymous variable.
	 */
	KR_PROLOG_LISTALL_ANONYMOUS_VARIABLE,
	/**
	 * Mental literals of type a-goal and goal-a should not contain Prolog
	 * anonymous variables.
	 */
	KR_PROLOG_MENTAL_LITERAL_ANONYMOUS_VARIABLE,
	/**
	 * Cannot provide an update with an empty content (e.g. insert() or drop()).
	 */
	KR_USE_OF_DIFFERENT_KRIS,
	/**
	 * Referenced file could not be found.
	 */
	REFERENCE_COULDNOT_FIND,
	/**
	 * Referenced file is a duplicate.
	 */
	REFERENCE_DUPLICATE, TASK_UNBOUND_VARIABLE,
	
	NO_MAIN_TASK, 
	
	DECOMPOSITION_DUPLICATE, 
	
	METHOD_DUPLICATE_NAME, OPERATOR_DUPLICATE_NAME,
	
	METHOD_UNBOUND_VARIABLE, 
	
	POSTCONDITION_UNBOUND_VARIABLE,
	
	OPERATOR_HAS_NO_ACTION, 
	
	TASK_CANNOT_BE_SOLVED;

	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("languageTools.messages.PlannerErrorMessages");

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
