package org.eclipse.gdt.debug.history;

import org.eclipse.debug.core.commands.IDebugCommandHandler;
import org.eclipse.debug.ui.actions.DebugCommandHandler;

public class BackCommandHandler extends DebugCommandHandler {
	@Override
	protected Class<IBackHandler> getCommandType() {
		return IBackHandler.class;
	}

	public interface IBackHandler extends IDebugCommandHandler {
	}
}