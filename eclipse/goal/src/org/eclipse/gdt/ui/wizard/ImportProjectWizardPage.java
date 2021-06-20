package org.eclipse.gdt.ui.wizard;

import java.io.File;
import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.gdt.Messages;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FileFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class ImportProjectWizardPage extends GoalWizardPage {
	// Section for retrieving environment file from user
	private FileGroup masFileGroup;
	// Section for checking if we should copy
	private CopyGroup copyGroup;

	protected ImportProjectWizardPage(final String pageName) {
		super(pageName, "Import an existing Project", "Import an existing GOAL Project in the workspace");
	}

	@Override
	public void createControl(final Composite parent) {
		initializeDialogUnits(parent);
		final Composite composite = new Composite(parent, SWT.NULL);
		composite.setFont(parent.getFont());
		composite.setLayout(initGridLayout(new GridLayout(1, false), true));
		composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));

		// create UI elements
		this.masFileGroup = new FileGroup(composite);
		this.copyGroup = new CopyGroup(composite);

		setControl(composite);
		Dialog.applyDialogFont(composite);
	}

	/**
	 * Requests a mas2g file path
	 */
	private final class FileGroup {
		// field for mas2g path
		private final FileFieldEditor masField;

		protected FileGroup(final Composite composite) {
			final Composite envComposite = new Composite(composite, SWT.NONE);
			envComposite.setFont(composite.getFont());
			envComposite.setLayout(initGridLayout(new GridLayout(3, false), false));
			envComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			this.masField = new FileFieldEditor("envFile", "MAS2G to import", envComposite);
			this.masField.setEmptyStringAllowed(false);
			this.masField.setFileExtensions(new String[] { "*." + Messages.MASFileExtension });
			this.masField.fillIntoGrid(envComposite, 3);
		}

		public String getPath() {
			return this.masField.getStringValue().trim();
		}
	}

	/**
	 * Requests a mas2g file path
	 */
	private final class CopyGroup {
		// field for mas2g path
		private final BooleanFieldEditor copyField;

		protected CopyGroup(final Composite composite) {
			final Composite copyComposite = new Composite(composite, SWT.NONE);
			copyComposite.setFont(composite.getFont());
			copyComposite.setLayout(initGridLayout(new GridLayout(2, false), false));
			copyComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
			this.copyField = new BooleanFieldEditor("copyMAS2G", "Copy into workspace", copyComposite);
			this.copyField.fillIntoGrid(copyComposite, 2);
		}

		public boolean shouldCopy() {
			return this.copyField.getBooleanValue();
		}
	}

	/**
	 * Creates a project resource handle for the current project name field
	 * value.
	 * <p>
	 * This method does not create the project resource; this is the
	 * responsibility of <code>IProject::create</code> invoked by the new
	 * project resource wizard.
	 * </p>
	 *
	 * @return the new project resource handle
	 */
	public IProject getProjectHandle() {
		final File mas2g = new File(this.masFileGroup.getPath());
		if (mas2g.exists() && mas2g.isFile() && mas2g.getName().endsWith("." + Messages.MASFileExtension)) {
			setPageComplete(true);
			setErrorMessage(null);
			setMessage(null);
			final String name = mas2g.getName().replace("." + Messages.MASFileExtension, "");
			final IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
			final IStatus check = validateFilename(name, true);
			if (check != null && !check.isOK()) {
				setErrorMessage(check.getMessage());
				setPageComplete(false);
				return null;
			} else {
				return project;
			}
		} else {
			setErrorMessage("Invalid file selected");
			setPageComplete(false);
			return null;
		}
	}

	public boolean shouldCopy() {
		return this.copyGroup.shouldCopy();
	}

	public URI getLocationHandle() {
		final File mas2g = new File(this.masFileGroup.getPath());
		if (mas2g.exists() && mas2g.isFile()) {
			return mas2g.getParentFile().toURI();
		} else {
			return null;
		}
	}
}