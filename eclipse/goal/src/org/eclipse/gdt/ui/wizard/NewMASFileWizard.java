package org.eclipse.gdt.ui.wizard;

import org.eclipse.gdt.Messages;

public class NewMASFileWizard extends NewGoalProjectFileWizard {

	public NewMASFileWizard() {
		setWindowTitle("New MAS File Wizard");
		setPageName("New MAS File");
		setTitle("Create a new MAS File");
		setDescription("Create a new MAS File in the workspace");
		setTemplate(Messages.MASFileTemplateID);
		setFileExtension(Messages.MASFileExtension);
	}
}