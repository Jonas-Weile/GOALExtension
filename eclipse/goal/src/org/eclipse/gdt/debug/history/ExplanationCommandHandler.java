package org.eclipse.gdt.debug.history;

import org.eclipse.debug.core.commands.IDebugCommandHandler;
import org.eclipse.debug.ui.actions.DebugCommandHandler;

public class ExplanationCommandHandler extends DebugCommandHandler {
	@Override
	protected Class<IExplanationHandler> getCommandType() {
		return IExplanationHandler.class;
	}

	public interface IExplanationHandler extends IDebugCommandHandler {
	}
}