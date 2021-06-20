package languageTools.errors;

import krTools.parser.SourceInfo;

/**
 *
 *
 */
public class ValidatorWarning extends Message {

	/**
	 *
	 */
	private static final long serialVersionUID = 852843283922639179L;

	public interface ValidatorWarningType extends ValidatorMessageType {
	}

	public ValidatorWarning(ValidatorWarningType type, SourceInfo source, String... args) {
		super(type, source, args);
	}

	@Override
	public ValidatorWarningType getType() {
		return (ValidatorWarningType) this.type;
	}

	@Override
	public String toString() {
		if (getSource() != null) {
			return "Warning at " + getSource() + ": " + toShortString();
		} else {
			return "Warning: " + toShortString();
		}
	}

}
