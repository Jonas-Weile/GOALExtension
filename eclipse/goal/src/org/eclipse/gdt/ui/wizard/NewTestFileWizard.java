package org.eclipse.gdt.ui.wizard;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.gdt.Messages;
import org.eclipse.gdt.completion.GoalTemplateAccess;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.persistence.TemplateStore;

public class NewTestFileWizard extends NewGoalProjectFileWizard {

	public NewTestFileWizard() {
		setWindowTitle("New Test File Wizard");
		setPageName("New Test File");
		setTitle("Create a new Agent Test File");
		setDescription("Create a new Agent Test File in the workspace");
		setTemplate(Messages.TestTemplateFileID);
		setFileExtension(Messages.TestFileExtension);
	}

	@Override
	public IModelElement getCreatedElement() {
		final IFile testFile = this.wizardPage.getSelectedFile();
		final TemplateStore templateStore = GoalTemplateAccess.getInstance().getTemplateStore();
		final Template template = templateStore.findTemplateById(this.template);
		final String pattern = String.format(template.getPattern(),
				testFile.getName().replace("." + testFile.getFileExtension(), ""));
		final InputStream is = new ByteArrayInputStream(pattern.getBytes());
		try {
			testFile.create(is, false, null);
		} catch (final Exception e) {
			MessageDialog.openError(getShell(), "Error", e.getMessage());
			super.performCancel();
		}
		return DLTKCore.create(testFile);
	}
}