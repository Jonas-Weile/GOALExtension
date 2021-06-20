package languageTools.errors;

import java.util.MissingFormatArgumentException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import krTools.parser.SourceInfo;

public class ParserError extends Message {

	/**
	 *
	 */
	private static final long serialVersionUID = -4097859122421338841L;

	public interface SyntaxErrorType extends ValidatorMessageType {
	}

	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("languageTools.messages.ParserErrorMessages");

	/**
	 * Syntax errors to distinguish lexer (token recognition errors) from parser
	 * errors.
	 */
	public enum SyntaxError implements SyntaxErrorType {
		/**
		 * Any error received from a parser that parsed embedded language
		 * fragments.
		 */
		EMBEDDED_LANGUAGE_ERROR,
		/**
		 * Parsing and validation should not produce fatal exceptions.
		 */
		FATAL,
		/**
		 * It should be possible to read the program file.
		 */
		FILE_COULDNOT_OPEN,
		/**
		 * A formal parameter should be a variable (applies to module, action
		 * specification, macro definition).
		 */
		PARAMETER_NOT_A_VARIABLE,
		/**
		 * Character streams should comply with lexer token definitions.
		 */
		TOKENRECOGNITIONERROR,
		/**
		 * User should be informed about missing double quote.
		 */
		UNTERMINATEDSTRINGLITERAL,
		/**
		 * User should be informed about missing single quote.
		 */
		UNTERMINATEDSINGLEQUOTEDSTRINGLITERAL,
		/**
		 * All parser errors.
		 */
		NOVIABLEALTERNATIVE, INPUTMISMATCH, FAILEDPREDICATE, UNWANTEDTOKEN, MISSINGTOKEN, UNEXPECTEDINPUT;

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

	public ParserError(SyntaxErrorType type, SourceInfo source, String... args) {
		super(type, source, args);
	}

	@Override
	public SyntaxErrorType getType() {
		return (SyntaxErrorType) this.type;
	}

	@Override
	public String toString() {
		if (getSource() != null) {
			return "Syntax error at " + getSource() + ": " + toShortString();
		} else {
			return "Syntax error: " + toShortString();
		}
	}

}