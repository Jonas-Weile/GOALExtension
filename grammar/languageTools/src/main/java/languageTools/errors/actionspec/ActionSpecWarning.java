package languageTools.errors.actionspec;

import java.util.MissingFormatArgumentException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import languageTools.errors.ValidatorWarning.ValidatorWarningType;

public enum ActionSpecWarning implements ValidatorWarningType {
	/**
	 * A variable is unused.
	 */
	VARIABLE_UNUSED;

	private static final ResourceBundle BUNDLE = ResourceBundle
			.getBundle("languageTools.messages.ActionSpecWarningMessages");

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