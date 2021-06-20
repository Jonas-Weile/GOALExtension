package org.eclipse.gdt.ui.wizard;

import java.io.File;
import java.util.Observable;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.IDialogFieldListener;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.LayoutUtil;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

@SuppressWarnings({ "restriction", "deprecation" })
public class NewProjectWizardPage extends GoalWizardPage {
	// Section for retrieving project name from user
	private NameGroup projectNameGroup;
	// Section for retrieving environment file from user
	private FileGroup envFileGroup;
	// Checks whether a valid instance of a project has been created by user
	private Validator projectValidator;

	protected NewProjectWizardPage(final String pageName) {
		super(pageName, "Create new GOAL Project", "Create a new GOAL Project in the workspace");
	}

	@Override
	public void createControl(final Composite parent) {
		initializeDialogUnits(parent);
		final Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		composite.setLayout(initGridLayout(new GridLayout(1, false), true));
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

		// create UI elements
		this.projectNameGroup = new NameGroup(composite);
		this.envFileGroup = new FileGroup(composite);

		// create and connect validator
		this.projectValidator = new Validator(this.projectNameGroup.getField(), true);
		this.projectNameGroup.addObserver(this.projectValidator);

		setControl(composite);
		Dialog.applyDialogFont(composite);

		// initialize elements
		this.projectNameGroup.dialogFieldChanged(null);
	}

	/**
	 * Requests a name. Fires an event whenever the text field is changed.
	 */
	private final class NameGroup extends Observable implements IDialogFieldListener {
		// text field for project name
		private final StringDialogField nameField;

		protected NameGroup(final Composite composite) {
			final Composite nameComposite = new Composite(composite, SWT.NONE);
			nameComposite.setFont(composite.getFont());
			nameComposite.setLayout(initGridLayout(new GridLayout(2, false), false));
			nameComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			this.nameField = new StringDialogField();
			this.nameField.setLabelText("Project name");
			this.nameField.setDialogFieldListener(this);
			this.nameField.doFillIntoGrid(nameComposite, 2);
			LayoutUtil.setHorizontalGrabbing(this.nameField.getTextControl(null));
		}

		public StringDialogField getField() {
			return this.nameField;
		}

		public String getName() {
			return this.nameField.getText().trim();
		}

		@Override
		public void dialogFieldChanged(final DialogField field) {
			setChanged();
			notifyObservers();
		}
	}

	/**
	 * Requests an optional path
	 */
	private final class FileGroup {
		// field for environment file path
		private final FileFieldEditor envField;

		protected FileGroup(final Composite composite) {
			final Composite envComposite = new Composite(composite, SWT.NONE);
			envComposite.setFont(composite.getFont());
			envComposite.setLayout(initGridLayout(new GridLayout(3, false), false));
			envComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			this.envField = new FileFieldEditor("envFile", "Environment (optional)", envComposite);
			this.envField.fillIntoGrid(envComposite, 3);
		}

		public String getPath() {
			return this.envField.getStringValue().trim();
		}
	}

	/**
	 * Creates a project resource handle for the current project name field value.
	 * <p>
	 * This method does not create the project resource; this is the responsibility
	 * of <code>IProject::create</code> invoked by the new project resource wizard.
	 * </p>
	 *
	 * @return the new project resource handle
	 */
	public IProject getProjectHandle() {
		return ResourcesPlugin.getWorkspace().getRoot().getProject(this.projectNameGroup.getName());
	}

	public File getEnvironmentHandle() {
		final File returned = new File(this.envFileGroup.getPath());
		if (returned.exists() && returned.isFile()) {
			return returned;
		} else {
			return null;
		}
	}
}