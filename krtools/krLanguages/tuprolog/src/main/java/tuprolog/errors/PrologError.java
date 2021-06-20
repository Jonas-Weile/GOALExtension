package tuprolog.errors;

import alice.tuprolog.PrologException;
import alice.tuprolog.Term;
import krTools.KRInterface;
import krTools.exceptions.KRException;
import krTools.exceptions.KRQueryFailedException;
import tuprolog.language.JPLUtils;

/**
 * A wrapper for {@link PrologException}s.
 *
 * reasons for having this:
 * <ul>
 * <li>a {@link KRInterface} can only throw {@link KRException}s
 * <li>We need to do pretty printing, {@link PrologException} toString is
 * unreadable for most humans
 * </ul>
 *
 * <p>
 * The error contents were determined with reverse engineering. We most likely
 * have not covered all possible cases. We try to give more general errors in
 * unknown cases. But including the original {@link PrologException} still is
 * necessary.
 *
 */
public class PrologError extends KRQueryFailedException {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Known {@link PrologException}s
	 */
	enum ErrorType {
		/**
		 * type_error(+Type, +Term) Tell the user that Term is not of the
		 * expected Type
		 */
		TYPE_ERROR(2),
		/**
		 * domain_error(+Type, +Term). The argument is of the proper type, but
		 * has a value that is outside the supported values
		 */
		DOMAIN_ERROR(2),
		/**
		 * existence_error(+Type, +Term). Term is of the correct type and
		 * correct domain, but there is no existing (external) resource that is
		 * represented by it.
		 */
		EXISTENCE_ERROR(2),
		/**
		 * permission_error(+Action, +Type, +Term): It is not allowed to perform
		 * Action on the object Term that is of the given Type.
		 */
		PERMISSION_ERROR(3),
		/**
		 * instantiation_error(+Term). An argument is under-instantiated. I.e.
		 * it is not acceptable as it is, but if some variables are bound to
		 * appropriate values it would be acceptable.
		 */
		INSTANTIATION_ERROR(1),
		/**
		 * uninstantiation_error(+Term): An argument is over-instantiated.
		 */

		UNINSTANTIATION_ERROR(1),
		/**
		 * representation_error(+Reason). A representation error indicates a
		 * limitation of the implementation.
		 */
		REPRESENTATION_ERROR(1),

		/**
		 * syntax_error(+Culprit): A text has invalid syntax.
		 */
		SYNTAX_ERROR(1),

		/**
		 * evaliation_error(Cause): some math evaluation failed. Not documented
		 * by TU so reverse engineered.
		 */
		EVALUATION_ERROR(1);

		private int arity;

		ErrorType(int arity) {
			this.arity = arity;
		}

		public int getArity() {
			return this.arity;
		}
	};

	/**
	 * Subtype, often passed as first argument of the error. This indicates to
	 * which type of object the error is referring. This also influences the
	 * format and type of the other arguments of the error.
	 *
	 */
	enum ErrorSubType {
		/**
		 * predicate/procedure type.
		 */
		PROCEDURE,
		/**
		 * files type
		 */
		SOURCE_SINK
	};

	/**
	 * Creates prolog error from given error term.
	 *
	 * @param err
	 *            must be a {@link PrologException} (not null) containing a
	 *            compound term which in turn containing the error details term.
	 *            The name of error details should be one of the known
	 *            {@link ErrorType}s. Unknown error types can not be handled
	 *            properly
	 */
	public PrologError(PrologException exc) {
		// we can't know message before processing exc.. as workaround we
		// override #getMessage()
		super("", exc);
	}

	@Override
	public String getMessage() {
		String mess = "tu prolog says the query failed";
		String detail = extractDetailMessage();
		if (detail != null) {
			mess = mess + " because " + detail;
		}
		return mess;
	}

	/**
	 * Try to extract a detailed message
	 *
	 * @return message, or null if we fail to create such a message.
	 */
	private String extractDetailMessage() {
		PrologException cause = (PrologException) getCause();
		Term term = null; // cause.term(); FIXME
		// we are expecting error(specific error, context), so something
		// like this:
		// error(existence_error(procedure,
		// :('towerbuilder:main:on(a,b) , on(b,c) , on(c,table) , on(d,e) ,
		// on(e,f) , on(f,table) , maintain', /(target, 2))),
		// context(:(system, /(',', 2)), _36))
		// we will try to translate the contained specific error.
		if (!(term instanceof alice.tuprolog.Struct)) {
			return null;
		}
		alice.tuprolog.Struct errterm = (alice.tuprolog.Struct) term;
		if ((!errterm.getName().equals("error")) || errterm.getArity() == 0
				|| !(errterm.getArg(0) instanceof alice.tuprolog.Struct)) {
			return null;
		}
		return makeDetailMessage((alice.tuprolog.Struct) errterm.getArg(0));

	}

	/**
	 * @param error
	 *            a Compound term like existence_error(..,..).
	 * @return human readable error message, or null if unknown term.
	 */
	private String makeDetailMessage(alice.tuprolog.Struct error) {
		ErrorType type;
		try {
			type = ErrorType.valueOf(error.getName().toUpperCase());
		} catch (IllegalArgumentException e) {
			return null;
		}

		String defaultmessage = "because of a general " + type.toString().toLowerCase();

		if (error.getArity() != type.getArity()) {
			// sometimes TU gives wrong error objects to us with no details.
			// Still trying to be helpful somehow...
			return defaultmessage;
		}

		switch (type) {
		case EXISTENCE_ERROR:
			ErrorSubType subtype;
			try {
				subtype = ErrorSubType.valueOf(((alice.tuprolog.Struct) error.getArg(0)).getName().toUpperCase());
			} catch (IllegalArgumentException e) {
				return defaultmessage;
			}
			switch (subtype) {
			case PROCEDURE:
				Term t = error.getArg(1);
				if (!(t instanceof alice.tuprolog.Struct)) {
					return defaultmessage;
				}
				alice.tuprolog.Struct explanation = (alice.tuprolog.Struct) t;
				if (explanation.getArity() != 2) {
					return defaultmessage;
				}

				return "the term " + JPLUtils.toString(explanation.getArg(1)) + " is undefined";
			case SOURCE_SINK:
				return "the file " + JPLUtils.toString(error.getArg(1)) + " is unaccesable";
			default:
				return defaultmessage;
			}

		case INSTANTIATION_ERROR:
			return "the term " + JPLUtils.toString(error.getArg(0)) + " is under-instantiated";
		case DOMAIN_ERROR:
			return "The term " + JPLUtils.toString(error.getArg(1))
					+ " has a value that is outside the supported values";
		case UNINSTANTIATION_ERROR:
			return "the term " + JPLUtils.toString(error.getArg(0)) + " is over-instantiated";
		case PERMISSION_ERROR:
			return "it is not allowed to perform " + JPLUtils.toString(error.getArg(0)) + " on the object "
					+ JPLUtils.toString(error.getArg(1)) + " that is of type " + error.getArg(2);
		case TYPE_ERROR:
			return "the term " + JPLUtils.toString(error.getArg(1)) + " is not of the expected " + error.getArg(0)
					+ " type";
		case REPRESENTATION_ERROR:
			return "implementation limits exceeded:" + error.getArg(0);
		case SYNTAX_ERROR:
			return "text has invalid syntax:" + error.getArg(0);
		case EVALUATION_ERROR:
			return "computation failed:" + error.getArg(0);
		default:
			return defaultmessage;
		}
	}
}
