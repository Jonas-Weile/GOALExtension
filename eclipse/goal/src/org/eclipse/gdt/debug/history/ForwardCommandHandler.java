package org.eclipse.gdt.debug.history;

import org.eclipse.debug.core.commands.IDebugCommandHandler;
import org.eclipse.debug.ui.actions.DebugCommandHandler;

public class ForwardCommandHandler extends DebugCommandHandler {
	@Override
	protected Class<IForwardHandler> getCommandType() {
		return IForwardHandler.class;
	}

	public interface IForwardHandler extends IDebugCommandHandler {
	}
}