package org.eclipse.gdt.debug.history;

import org.eclipse.debug.core.commands.IDebugCommandHandler;
import org.eclipse.debug.ui.actions.DebugCommandHandler;

public class LookupCommandHandler extends DebugCommandHandler {
	@Override
	protected Class<ILookupHandler> getCommandType() {
		return ILookupHandler.class;
	}

	public interface ILookupHandler extends IDebugCommandHandler {
	}
}