package languageTools.errors.test;

import java.util.MissingFormatArgumentException;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import languageTools.errors.ValidatorWarning.ValidatorWarningType;

public enum TestWarning implements ValidatorWarningType {
	/**
	 * Each agent name should be used only once.
	 */
	DUPLICATE_AGENT_TEST,
	/**
	 * Each module name should be used only once.
	 */
	DUPLICATE_MODULE_TEST,
	/**
	 * A variable is unused.
	 */
	VARIABLE_UNUSED;

	private static final ResourceBundle BUNDLE = ResourceBundle.getBundle("languageTools.messages.TestWarningMessages");

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
