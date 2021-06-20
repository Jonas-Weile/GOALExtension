package org.eclipse.gdt.ui.wizard;

import java.util.Observable;

import org.eclipse.core.resources.IFile;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

@SuppressWarnings({ "restriction", "deprecation" })
public class NewGoalProjectFileWizardPage extends GoalWizardPage {
	// Section for retrieving project name from user.
	private NameGroup fileNameGroup;
	// Checks whether a valid instance of a file has been created by user.
	private Validator filenameValidator;

	protected NewGoalProjectFileWizardPage(final String pageName, final String title, final String description) {
		super(pageName, title, description);
	}

	@Override
	public void createControl(final Composite parent) {
		initializeDialogUnits(parent);
		final Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		composite.setLayout(initGridLayout(new GridLayout(1, false), true));
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

		// create UI elements
		this.fileNameGroup = new NameGroup(composite, new String());

		// create and connect validator
		this.filenameValidator = new Validator(this.fileNameGroup.getField());
		this.fileNameGroup.addObserver(this.filenameValidator);

		setControl(composite);
		Dialog.applyDialogFont(composite);

		// initialize elements
		this.fileNameGroup.dialogFieldChanged(null);
	}

	/**
	 * Requests a name. Fires an event whenever the text field is changed.
	 */
	private final class NameGroup extends Observable implements IDialogFieldListener {
		private final StringDialogField nameField;

		protected NameGroup(final Composite composite, final String initialName) {
			final Composite nameComposite = new Composite(composite, SWT.NONE);
			nameComposite.setFont(composite.getFont());
			nameComposite.setLayout(initGridLayout(new GridLayout(2, false), false));
			nameComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			// text field for project name
			this.nameField = new StringDialogField();
			this.nameField.setLabelText("File name");
			this.nameField.setDialogFieldListener(this);
			setName(initialName);
			this.nameField.doFillIntoGrid(nameComposite, 2);
			LayoutUtil.setHorizontalGrabbing(this.nameField.getTextControl(null));
		}

		public StringDialogField getField() {
			return this.nameField;
		}

		public String getName() {
			return this.nameField.getText().trim();
		}

		public void setName(final String name) {
			this.nameField.setText(name);
		}

		@Override
		public void dialogFieldChanged(final DialogField field) {
			setChanged();
			notifyObservers();
		}
	}

	public IFile getSelectedFile() {
		return this.getSelectedFile(this.fileNameGroup.getName());
	}
}