
package org.eclipse.gdt.debug.history;

import org.eclipse.debug.internal.ui.DebugPluginImages;
import org.eclipse.debug.internal.ui.IInternalDebugUIConstants;
import org.eclipse.debug.ui.actions.DebugCommandAction;
import org.eclipse.gdt.debug.history.ExplanationCommandHandler.IExplanationHandler;
import org.eclipse.jface.resource.ImageDescriptor;

@SuppressWarnings("restriction")
public class ExplanationCommandAction extends DebugCommandAction {
	public ExplanationCommandAction() {
		setActionDefinitionId("org.eclipse.gdt.debug.history.Explanation");
	}

	@Override
	public String getText() {
		return "History Explanation";
	}

	@Override
	public String getHelpContextId() {
		return null;
	}

	@Override
	public String getId() {
		return "org.eclipse.gdt.debug.history.explanation";
	}

	@Override
	public String getToolTipText() {
		return "GOAL History Explanation";
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
	protected Class<IExplanationHandler> getCommandType() {
		return IExplanationHandler.class;
	}
}
