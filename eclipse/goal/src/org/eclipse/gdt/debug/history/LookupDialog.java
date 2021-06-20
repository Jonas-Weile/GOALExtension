package org.eclipse.gdt.debug.history;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

public class LookupDialog extends ElementListSelectionDialog {
	public static final int ALL_OCCURENCES = 22;
	public static final int FIRST_OCCURENCE = 23;
	public static final int LAST_OCCURENCE = 24;

	public LookupDialog(final String[] elements) {
		super(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(), new LabelProvider());
		setBlockOnOpen(true);
		setMultipleSelection(true);
		setElements(elements);
		setTitle("Select one or more items to focus at");
	}

	@Override
	protected void createButtonsForButtonBar(final Composite parent) {
		createButton(parent, ALL_OCCURENCES, "Apply filter", true);
		createButton(parent, FIRST_OCCURENCE, "Go to first", false);
		createButton(parent, LAST_OCCURENCE, "Go to last", false);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected void buttonPressed(final int buttonId) {
		computeResult();
		setReturnCode(buttonId);
		close();
	}
}
