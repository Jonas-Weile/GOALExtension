
package org.eclipse.gdt.debug.history;

import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.actions.DebugCommandAction;
import org.eclipse.gdt.debug.history.ForwardCommandHandler.IForwardHandler;
import org.eclipse.jface.resource.ImageDescriptor;

@SuppressWarnings("restriction")
public class ForwardCommandAction extends DebugCommandAction {
	public ForwardCommandAction() {
		setActionDefinitionId("org.eclipse.gdt.debug.history.Forward");
	}

	@Override
	public String getText() {
		return "History Forward";
	}

	@Override
	public String getHelpContextId() {
		return null;
	}

	@Override
	public String getId() {
		return "org.eclipse.gdt.debug.history.stepForward";
	}

	@Override
	public String getToolTipText() {
		return "GOAL History Forward";
	}

	@Override
	public ImageDescriptor getDisabledImageDescriptor() {
		return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_DLCL_STEP_INTO);
	}

	@Override
	public ImageDescriptor getHoverImageDescriptor() {
		return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_STEP_INTO);
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return DebugPluginImages.getImageDescriptor(IInternalDebugUIConstants.IMG_ELCL_STEP_INTO);
	}

	@Override
	protected Class<IForwardHandler> getCommandType() {
		return IForwardHandler.class;
	}
}
