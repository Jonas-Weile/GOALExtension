package org.eclipse.gdt.ui.wizard;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.ui.wizards.NewElementWizard;
import org.eclipse.gdt.completion.GoalTemplateAccess;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;

public abstract class NewGoalProjectFileWizard extends NewElementWizard {
	// Basic, generic page for creating new GOAL project files
	protected NewGoalProjectFileWizardPage wizardPage;
	protected String pageName;
	protected String title;
	protected String description;
	protected String template;
	protected String fileExtension;
	protected IModelElement originator;

	/**
	 * Creates new GOAL project file wizard.
	 */
	public NewGoalProjectFileWizard() {
		setForcePreviousAndNextButtons(false);
	}

	public void setPageName(final String pageName) {
		this.pageName = pageName;
	}

	public void setTitle(final String title) {
		this.title = title;
	}

	public void setDescription(final String description) {
		this.description = description;
	}

	public void setTemplate(final String template) {
		this.template = template;
	}

	public void setFileExtension(final String fileExtension) {
		this.fileExtension = fileExtension;
	}

	@Override
	public void addPages() {
		IPath path = null;
		Object firstElement = getSelection().getFirstElement();
		if (firstElement instanceof IModelElement) {
			this.originator = (IModelElement) firstElement;
			while (firstElement instanceof ISourceModule) {
				firstElement = ((IModelElement) firstElement).getParent();
			}
			path = ((IModelElement) firstElement).getPath();

		} else if (firstElement instanceof IResource) {
			path = ((IResource) firstElement).getProject().getFullPath();
		}
		if (path != null) {
			super.addPages();
			this.wizardPage = new NewGoalProjectFileWizardPage(this.pageName, this.title, this.description);
			this.wizardPage.setCurrentPath(path);
			this.wizardPage.setFileExtension(this.fileExtension);
			addPage(this.wizardPage);
		} else {
			try {
				super.getContainer().getShell().close();
			} catch (final Exception ignore) {
			}
			return;
		}
	}

	@Override
	public boolean performFinish() {
		boolean res = super.performFinish();
		if (res) {
			final IModelElement newElement = getCreatedElement();
			if (newElement != null) {
				try {
					newElement.getScriptProject().getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
				} catch (final Exception e) {
					res = false;
					DLTKCore.error(e);
				}
			}
			final IFile newFile = this.wizardPage.getSelectedFile();
			if (newFile != null) {
				try {
					selectAndReveal(newFile);
					IDE.openEditor(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage(), newFile);
				} catch (final Exception e) {
					res = false;
					DLTKCore.error(e);
				}
			}
		}
		return res;
	}

	@Override
	protected void finishPage(final IProgressMonitor monitor) throws InterruptedException, CoreException {
	}

	@Override
	public IModelElement getCreatedElement() {
		// Add file template to currently selected location
		final IFile goalProjectFile = this.wizardPage.getSelectedFile();
		final TemplateStore templateStore = GoalTemplateAccess.getInstance().getTemplateStore();
		final Template template = templateStore.findTemplateById(this.template);
		final InputStream is = new ByteArrayInputStream(template.getPattern().getBytes());

		try {
			goalProjectFile.create(is, false, null);
		} catch (final Exception e) {
			MessageDialog.openError(getShell(), "Error", e.getMessage());
			super.performCancel();
		}

		return DLTKCore.create(goalProjectFile);
	}
}