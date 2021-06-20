package swiprolog.errors;

import krTools.KRInterface;
import krTools.exceptions.KRException;
import krTools.exceptions.KRQueryFailedException;
import krTools.language.Term;
import swiprolog.language.PrologCompound;
import swiprolog.language.impl.PrologImplFactory;

/**
 * A wrapper for {@link org.jpl7.PrologException}s.
 *
 * reasons for having this:
 * <ul>
 * <li>a {@link KRInterface} can only throw {@link KRException}s
 * <li>We need to do pretty printing, {@link org.jpl7.PrologException} toString
 * is unreadable for most humans
 * </ul>
 *
 * <p>
 * The error contents were determined with reverse engineering. We most likely
 * have not covered all possible cases. We try to give more general errors in
 * unknown cases. But including the original {@link org.jpl7.PrologException}
 * still is necessary.
 *
 */
public class PrologError extends KRQueryFailedException {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Known {@link org.jpl7.PrologException}s
	 */
	enum ErrorType {
		/**
		 * type_error(+Type, +Term) Tell the user that Term is not of the expected Type
		 */
		TYPE_ERROR(2),
		/**
		 * domain_error(+Type, +Term). The argument is of the proper type, but has a
		 * value that is outside the supported values
		 */
		DOMAIN_ERROR(2),
		/**
		 * existence_error(+Type, +Term). Term is of the correct type and correct
		 * domain, but there is no existing (external) resource that is represented by
		 * it.
		 */
		EXISTENCE_ERROR(2),
		/**
		 * permission_error(+Action, +Type, +Term): It is not allowed to perform Action
		 * on the object Term that is of the given Type.
		 */
		PERMISSION_ERROR(3),
		/**
		 * instantiation_error(+Term). An argument is under-instantiated. I.e. it is not
		 * acceptable as it is, but if some variables are bound to appropriate values it
		 * would be acceptable.
		 */
		INSTANTIATION_ERROR(1),
		/**
		 * uninstantiation_error(+Term): An argument is over-instantiated.
		 */

		UNINSTANTIATION_ERROR(1),
		/**
		 * representation_error(+Reason). A representation error indicates a limitation
		 * of the implementation.
		 */
		REPRESENTATION_ERROR(1),

		/**
		 * syntax_error(+Culprit): A text has invalid syntax.
		 */
		SYNTAX_ERROR(1),

		/**
		 * evaliation_error(Cause): some math evaluation failed. Not documented by SWI
		 * so reverse engineered.
		 */
		EVALUATION_ERROR(1);

		private int arity;

		ErrorType(final int arity) {
			this.arity = arity;
		}

		public int getArity() {
			return this.arity;
		}
	}

	/**
	 * Subtype, often passed as first argument of the error. This indicates to which
	 * type of object the error is referring. This also influences the format and
	 * type of the other arguments of the error.
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
	}

	/**
	 * Creates prolog error from given error term.
	 *
	 * @param err must be a {@link org.jpl7.PrologException} (not null) containing a
	 *            compound term which in turn containing the error details term. The
	 *            name of error details should be one of the known
	 *            {@link ErrorType}s. Unknown error types can not be handled
	 *            properly
	 */
	public PrologError(final org.jpl7.PrologException exc) {
		// we can't know message before processing exc.. as workaround we
		// override #getMessage()
		super("", exc);
	}

	@Override
	public String getMessage() {
		String mess = "swi prolog says the query failed";
		final String detail = extractDetailMessage();
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
		final org.jpl7.PrologException cause = (org.jpl7.PrologException) getCause();
		final Term term = fromJpl(cause.term());
		final PrologCompound compound = (term instanceof PrologCompound) ? (PrologCompound) term : null;
		// we are expecting error(specific error, context), so something
		// like this:
		// error(existence_error(procedure,
		// :('towerbuilder:main:on(a,b) , on(b,c) , on(c,table) , on(d,e) ,
		// on(e,f) , on(f,table) , maintain', /(target, 2))),
		// context(:(system, /(',', 2)), _36))
		// we will try to translate the contained specific error.
		if (compound != null && compound.getArity() > 0 && compound.getName().equals("error")) {
			return makeDetailMessage((PrologCompound) compound.getArg(0));
		} else {
			return null;
		}
	}

	/**
	 * @param error a Compound term like existence_error(..,..).
	 * @return human readable error message, or null if unknown term.
	 */
	private String makeDetailMessage(final PrologCompound error) {
		ErrorType type;
		try {
			type = ErrorType.valueOf(error.getName().toUpperCase());
		} catch (final IllegalArgumentException e) {
			return null;
		}

		final String defaultmessage = "because of a general " + type.toString().toLowerCase();

		if (error.getArity() != type.getArity()) {
			// sometimes SWI gives wrong error objects to us with no details.
			// Still trying to be helpful somehow...
			return defaultmessage;
		}

		switch (type) {
		case EXISTENCE_ERROR:
			ErrorSubType subtype;
			try {
				subtype = ErrorSubType.valueOf(((PrologCompound) error.getArg(0)).getName().toUpperCase());
			} catch (final IllegalArgumentException e) {
				return defaultmessage;
			}
			switch (subtype) {
			case PROCEDURE:
				final PrologCompound t = (PrologCompound) error.getArg(1);
				if (t.getArity() == 2) {
					return "the term " + t + " is undefined";
				} else {
					return defaultmessage;
				}
			case SOURCE_SINK:
				return "the file " + error.getArg(1) + " is unaccesable";
			default:
				return defaultmessage;
			}
		case INSTANTIATION_ERROR:
			return "the term " + error.getArg(0) + " is under-instantiated";
		case DOMAIN_ERROR:
			return "The term " + error.getArg(1) + " has a value that is outside the supported values";
		case UNINSTANTIATION_ERROR:
			return "the term " + error.getArg(0) + " is over-instantiated";
		case PERMISSION_ERROR:
			return "it is not allowed to perform " + error.getArg(0) + " on the object " + error.getArg(1)
					+ " that is of type " + error.getArg(2);
		case TYPE_ERROR:
			return "the term " + error.getArg(1) + " is not of the expected " + error.getArg(0) + " type";
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

	// used only here and in (deprecated) tests...
	public static Term fromJpl(final org.jpl7.Term term) {
		if (term.isAtom()) {
			final org.jpl7.Atom atom = (org.jpl7.Atom) term;
			return PrologImplFactory.getAtom(atom.name(), null);
		} else if (term.isCompound()) {
			final org.jpl7.Compound compound = (org.jpl7.Compound) term;
			final Term[] args = new Term[compound.arity()];
			for (int i = 1; i <= compound.arity(); ++i) {
				args[i - 1] = fromJpl(compound.arg(i));
			}
			return PrologImplFactory.getCompound(compound.name(), args, null);
		} else if (term.isFloat()) {
			final org.jpl7.Float flot = (org.jpl7.Float) term;
			return PrologImplFactory.getNumber(flot.doubleValue(), null);
		} else if (term.isInteger()) {
			final org.jpl7.Integer integer = (org.jpl7.Integer) term;
			return PrologImplFactory.getNumber(integer.longValue(), null);
		} else if (term.isVariable()) {
			final org.jpl7.Variable var = (org.jpl7.Variable) term;
			return PrologImplFactory.getVar(var.name(), null);
		} else {
			return null;
		}
	}
}
