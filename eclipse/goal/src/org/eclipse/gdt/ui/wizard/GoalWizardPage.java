package org.eclipse.gdt.ui.wizard;

import java.util.Observable;
import java.util.Observer;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.ComboDialogField;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.DialogField;
import org.eclipse.dltk.internal.ui.wizards.dialogfields.StringDialogField;
import org.eclipse.dltk.ui.DLTKUIPlugin;
import org.eclipse.dltk.ui.dialogs.StatusInfo;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.layout.GridLayout;

@SuppressWarnings({ "restriction", "deprecation" })
public abstract class GoalWizardPage extends WizardPage {
	// Path to selected file
	private IPath path;
	// The required extension
	private String fileExtension;

	protected GoalWizardPage(final String pageName, final String title, final String description) {
		super(pageName);
		setTitle(title);
		setDescription(description);
	}

	public void setFileExtension(final String fileExtension) {
		this.fileExtension = fileExtension;
	}

	public void setCurrentPath(final IPath path) {
		this.path = path;
	}

	/**
	 * Initialize a grid layout with the default Dialog settings.
	 */
	protected GridLayout initGridLayout(final GridLayout layout, final boolean margins) {
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		if (margins) {
			layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
			layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		} else {
			layout.marginWidth = 0;
			layout.marginHeight = 0;
		}
		return layout;
	}

	/**
	 * Validate this page and show appropriate warnings and error messages.
	 */
	protected final class Validator implements Observer {
		private final DialogField field;
		private final boolean project;

		protected Validator(final DialogField forField) {
			this.field = forField;
			this.project = false;
		}

		protected Validator(final DialogField forField, final boolean project) {
			this.field = forField;
			this.project = project;
		}

		private void validate(final Observable o, final Object arg) {
			String text = "";
			if (this.field instanceof StringDialogField) {
				text = ((StringDialogField) this.field).getText();
			} else if (this.field instanceof ComboDialogField) {
				text = ((ComboDialogField) this.field).getText();
			}
			final IStatus filenameStatus = validateFilename(text.trim(), this.project);
			if (filenameStatus != null) {
				setPageComplete(false);
				switch (filenameStatus.getSeverity()) {
				case IStatus.ERROR:
					setErrorMessage(filenameStatus.getMessage());
					break;
				case IStatus.OK:
					if (text.trim().isEmpty()) {
						setErrorMessage(filenameStatus.getMessage());
					} else {
						setErrorMessage("A project with this name already exists");
					}
					break;
				default:
					setErrorMessage(null);
					setMessage(filenameStatus.getMessage());
					break;
				}
			} else {
				setPageComplete(true);
				setErrorMessage(null);
				setMessage(null);
			}
		}

		@Override
		public void update(final Observable o, final Object arg) {
			validate(o, arg);
		}
	}

	/**
	 * Validates project fields.
	 *
	 * @return Returns {@link IStatus} or, if the project is valid,
	 *         <code>null</code>.
	 */
	protected IStatus validateFilename(final String name, final boolean project) {
		// check whether the project name field is empty
		if (name.isEmpty()) {
			return new StatusInfo(IStatus.OK, "Enter a name.");
		}
		// check whether the file name is valid
		final IWorkspace workspace = DLTKUIPlugin.getWorkspace();
		final IStatus nameStatus = workspace.validateName(name, IResource.FILE);
		if (!nameStatus.isOK()) {
			return nameStatus;
		}

		// check whether file has expected extension
		final IFile handle = getSelectedFile(name);
		if (handle != null) {
			if (this.fileExtension != null && !this.fileExtension.equalsIgnoreCase(handle.getFileExtension())) {
				return new StatusInfo(IStatus.ERROR, "File must have " + this.fileExtension + " extension.");
			}
			// check whether file already exists
			if (handle.exists()) {
				return new StatusInfo(IStatus.ERROR, "A file with this name already exists.");
			}
		}
		IPath fileLocation = workspace.getRoot().getLocation().append(name);
		if (fileLocation.toFile().exists()) {
			try { // correct casing
				final String canonicalPath = fileLocation.toFile().getCanonicalPath();
				fileLocation = new Path(canonicalPath);
			} catch (final Exception e) {
				DLTKCore.error(e);
			}
			if (project) {
				return validateProject(name, fileLocation);
			}
		}
		return null;
	}

	protected IStatus validateProject(final String name, final IPath location) {
		final IProject check = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
		return DLTKUIPlugin.getWorkspace().validateProjectLocation(check, location);
	}

	protected IFile getSelectedFile(final String name) {
		if (this.path != null) {
			IPath filePath = this.path.addTrailingSeparator().append(name);
			final String extension = filePath.getFileExtension();
			if (extension == null && this.fileExtension != null) {
				filePath = filePath.addFileExtension(this.fileExtension);
			}
			return ResourcesPlugin.getWorkspace().getRoot().getFile(filePath);
		} else {
			return null;
		}
	}
}