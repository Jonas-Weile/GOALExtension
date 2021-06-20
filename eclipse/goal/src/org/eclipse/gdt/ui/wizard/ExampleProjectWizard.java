package org.eclipse.gdt.ui.wizard;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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
import org.eclipse.gdt.Activator;
import org.eclipse.gdt.GoalNature;
import org.eclipse.gdt.Messages;
import org.eclipse.jface.dialogs.MessageDialog;

public class ExampleProjectWizard extends NewElementWizard {
	private ExampleProjectWizardPage wizardPage;

	public ExampleProjectWizard() {
		setForcePreviousAndNextButtons(false);
		setWindowTitle(Messages.ExampleProjectWizard_Title);
	}

	@Override
	public void addPages() {
		super.addPages();
		this.wizardPage = new ExampleProjectWizardPage(Messages.ExampleProjectWizard_1);
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

		final String example = this.wizardPage.getExampleHandle();
		if (example.isEmpty()) {
			MessageDialog.openError(getShell(), "Error", "Please select an example project in the list");
			super.performCancel();
		}

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
		final File dest = scriptProject.getResource().getLocation().toFile();
		try {
			final BufferedInputStream bis = new BufferedInputStream(
					new FileInputStream(new File(Activator.getDefault().getAgentPath())));
			final ZipInputStream zis = new ZipInputStream(bis);
			ZipEntry entry = null;
			final byte[] buffer = new byte[2048];
			while ((entry = zis.getNextEntry()) != null) {
				if (entry.getName().startsWith(example + "/")) {
					final File fileInDir = new File(dest, entry.getName().replace(example + "/", ""));
					if (entry.isDirectory()) {
						fileInDir.mkdir();
					} else {
						final FileOutputStream fOutput = new FileOutputStream(fileInDir);
						int count = 0;
						while ((count = zis.read(buffer)) > 0) {
							fOutput.write(buffer, 0, count);
						}
						fOutput.close();
					}
				}
				zis.closeEntry();
			}
			zis.close();
			bis.close();
		} catch (final Exception e) {
			MessageDialog.openError(getShell(), "Error", e.getMessage());
			super.performCancel();
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