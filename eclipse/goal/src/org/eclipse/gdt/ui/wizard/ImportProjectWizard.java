package org.eclipse.gdt.ui.wizard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.util.LinkedHashSet;
import java.util.Set;

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
import org.eclipse.jface.dialogs.MessageDialog;

public class ImportProjectWizard extends NewElementWizard {
	private ImportProjectWizardPage wizardPage;

	public ImportProjectWizard() {
		setForcePreviousAndNextButtons(false);
		setWindowTitle(Messages.ImportProjectWizard_Title);
	}

	@Override
	public void addPages() {
		super.addPages();
		this.wizardPage = new ImportProjectWizardPage(Messages.ImportProjectWizard_1);
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
		final URI location = this.wizardPage.getLocationHandle();
		if (project != null) {
			// Create project
			try {
				createProject(project, this.wizardPage.shouldCopy() ? null : location);
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

			// Copy files to project if necessary
			if (this.wizardPage.shouldCopy() && location != null) {
				try {
					final File source = new File(location);
					final Set<File> sourceFiles = getAllFilesInFolder(source);
					final File dest = scriptProject.getResource().getLocation().toFile();
					addFilesToFolder(sourceFiles.toArray(new File[sourceFiles.size()]), dest);
				} catch (final Exception e) {
					MessageDialog.openError(getShell(), "Error", e.getMessage());
					super.performCancel();
				}
			}

			return scriptProject;
		} else {
			return null;
		}
	}

	private static Set<File> getAllFilesInFolder(final File folder) throws IOException {
		final Set<File> returned = new LinkedHashSet<>();
		for (final File f : folder.listFiles()) {
			if (!f.isHidden()) {
				returned.add(f);
			}
		}
		return returned;
	}

	private static void addFilesToFolder(final File[] files, final File folder) throws IOException {
		if (!folder.exists()) {
			folder.mkdirs();
		}
		for (final File f : files) {
			if (f.isDirectory()) {
				final File newFolder = new File(folder.getCanonicalPath() + File.separator + f.getName());
				newFolder.mkdir();
				addFilesToFolder(f.listFiles(), newFolder);
			} else {
				final File newFile = new File(folder.getCanonicalPath() + File.separator + f.getName());
				copyFile(f, newFile);
			}
		}
	}

	@SuppressWarnings("resource")
	private static void copyFile(final File sourceFile, final File destFile) throws IOException {
		if (!destFile.exists()) {
			destFile.getParentFile().mkdirs();
			destFile.createNewFile();
		}
		FileChannel source = null;
		FileChannel destination = null;
		try {
			source = new FileInputStream(sourceFile).getChannel();
			destination = new FileOutputStream(destFile).getChannel();
			destination.transferFrom(source, 0, source.size());
		} finally {
			if (source != null) {
				source.close();
			}
			if (destination != null) {
				destination.close();
			}
		}
		final String dest = destFile.getName().trim();
		if (dest.endsWith(".sh") || dest.endsWith(".command")) {
			destFile.setExecutable(true);
		}
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
	protected void createProject(final IProject project, final URI locationURI) throws CoreException {
		BuildpathsBlock.createProject(project, locationURI, null);
		final IEnvironment environment = EnvironmentManager.getLocalEnvironment();
		final IEnvironment pEnv = EnvironmentManager.detectEnvironment(project);
		if (environment.equals(pEnv)) {
			EnvironmentManager.setEnvironmentId(project, null, false);
		} else {
			EnvironmentManager.setEnvironmentId(project, environment.getId(), false);
		}
	}
}