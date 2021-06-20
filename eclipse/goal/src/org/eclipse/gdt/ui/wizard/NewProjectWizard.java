package org.eclipse.gdt.ui.wizard;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.environment.EnvironmentManager;
import org.eclipse.dltk.core.environment.IEnvironment;
import org.eclipse.dltk.ui.wizards.BuildpathsBlock;
import org.eclipse.dltk.ui.wizards.NewElementWizard;
import org.eclipse.dltk.utils.ResourceUtil;
import org.eclipse.gdt.GoalNature;
import org.eclipse.gdt.Messages;
import org.eclipse.gdt.completion.GoalTemplateAccess;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.persistence.TemplateStore;

public class NewProjectWizard extends NewElementWizard {
	private NewProjectWizardPage wizardPage;

	public NewProjectWizard() {
		setForcePreviousAndNextButtons(false);
		setWindowTitle(Messages.NewProjectWizard_Title);
	}

	@Override
	public void addPages() {
		super.addPages();
		this.wizardPage = new NewProjectWizardPage(Messages.NewProjectWizard_1);
		addPage(this.wizardPage);
	}

	@Override
	public boolean performFinish() {
		boolean res = super.performFinish();
		if (res) {
			final IScriptProject newElement = getCreatedElement();
			if (newElement != null) {
				selectAndReveal(newElement.getProject());
				try {
					newElement.getProject().refreshLocal(IResource.DEPTH_INFINITE, null);
				} catch (final Exception e) {
					res = false;
					DLTKCore.error(e);
				}
			} else {
				res = false;
			}
		}
		return res;
	}

	@Override
	protected void finishPage(final IProgressMonitor monitor) throws InterruptedException, CoreException {
	}

	@Override
	public IScriptProject getCreatedElement() {
		final NullProgressMonitor monitor = new NullProgressMonitor();
		final IProject project = this.wizardPage.getProjectHandle();

		// Create project
		try {
			createProject(project);
		} catch (final Exception e) {
			MessageDialog.openError(getShell(), "Error", e.getMessage());
			super.performCancel();
		}

		// Add nature to project
		try {
			ResourceUtil.addNature(project, monitor, GoalNature.GOAL_NATURE);
		} catch (final Exception e) {
			MessageDialog.openError(getShell(), "Error", e.getMessage());
			super.performCancel();
		}

		final IScriptProject scriptProject = DLTKCore.create(project);

		// Add MAS file template to project
		final TemplateStore templateStore = GoalTemplateAccess.getInstance().getTemplateStore();
		Template masTemplate = templateStore.findTemplateById(Messages.MASFileTemplateID);
		final File envFile = this.wizardPage.getEnvironmentHandle();
		if (envFile != null) {
			masTemplate = templateStore.findTemplateById(Messages.MASFileEnvTemplateID);
		}

		try {
			final IFile masFile = scriptProject.getProject()
					.getFile(project.getName() + "." + Messages.MASFileExtension);
			String template = masTemplate.getPattern();
			if (envFile != null) {
				template = String.format(template, envFile.getName());
			}
			final InputStream is1 = new ByteArrayInputStream(template.getBytes());
			masFile.create(is1, false, null);
		} catch (final Exception e) {
			MessageDialog.openError(getShell(), "Error", e.getMessage());
			super.performCancel();
		}

		if (envFile != null) {
			try {
				final IFile newEnvFile = scriptProject.getProject().getFile(envFile.getName());
				final InputStream is2 = new FileInputStream(envFile);
				newEnvFile.create(is2, false, null);
			} catch (final Exception e) {
				MessageDialog.openError(getShell(), "Error", e.getMessage());
				super.performCancel();
			}
		}

		return scriptProject;
	}

	/**
	 * Helper method to create and open a IProject. The project location is
	 * configured. No natures are added.
	 *
	 * @param project
	 *            The handle of the project to create.
	 * @param locationURI
	 *            The location of the project or <code>null</code> to create the
	 *            project in the workspace
	 * @param monitor
	 *            a progress monitor to report progress or <code>null</code> if
	 *            progress reporting is not desired
	 * @throws CoreException
	 *             if the project could not be created
	 * @see org.eclipse.core.resources.IProjectDescription#setLocationURI(java.net.URI)
	 */
	protected void createProject(final IProject project) throws CoreException {
		BuildpathsBlock.createProject(project, null, null);
		final IEnvironment environment = EnvironmentManager.getLocalEnvironment();
		final IEnvironment pEnv = EnvironmentManager.detectEnvironment(project);
		if (environment.equals(pEnv)) {
			EnvironmentManager.setEnvironmentId(project, null, false);
		} else {
			EnvironmentManager.setEnvironmentId(project, environment.getId(), false);
		}
	}
}