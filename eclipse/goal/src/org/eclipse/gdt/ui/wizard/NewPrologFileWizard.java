package org.eclipse.gdt.ui.wizard;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.gdt.Messages;
import org.eclipse.jface.dialogs.MessageDialog;

public class NewPrologFileWizard extends NewGoalProjectFileWizard {

	public NewPrologFileWizard() {
		setWindowTitle("New Prolog File Wizard");
		setPageName("New Prolog File");
		setTitle("Create a new Prolog File");
		setDescription("Create a new SWI Prolog File in the workspace");
		setFileExtension(Messages.PrologFileExtension);
	}

	@Override
	public IModelElement getCreatedElement() {
		final IFile prologFile = this.wizardPage.getSelectedFile();
		final InputStream is = new ByteArrayInputStream(" ".getBytes());
		try {
			prologFile.create(is, false, null);
		} catch (final Exception e) {
			MessageDialog.openError(getShell(), "Error", e.getMessage());
			super.performCancel();
		}
		return DLTKCore.create(prologFile);
	}
}