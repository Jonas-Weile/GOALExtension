package languageTools.program.agent.actions;

import krTools.language.Term;
import krTools.parser.SourceInfo;
import languageTools.program.actionspec.UserSpecAction;

/**
 * Actions like {@link ExitModuleAction}, {@link LogAction},
 * {@link ModuleCallAction}, etc, but not including {@link UserSpecAction}.
 * 
 */

public abstract class NonMentalAction extends Action<Term> {
	public NonMentalAction(String name, SourceInfo info) {
		super(name, info);
	}
}
