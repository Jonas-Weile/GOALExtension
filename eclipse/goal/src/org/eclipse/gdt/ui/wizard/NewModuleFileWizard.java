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

public class NewModuleFileWizard extends NewGoalProjectFileWizard {

	public NewModuleFileWizard() {
		setWindowTitle("New Module File Wizard");
		setPageName("New Module File");
		setTitle("Create a new Module File");
		setDescription("Create a new Module File in the workspace");
		setTemplate(Messages.ModuleTemplateFileID);
		setFileExtension(Messages.ModuleFileExtension);
	}

	// @SuppressWarnings("resource")
	@Override
	public IModelElement getCreatedElement() {
		final IFile moduleFile = this.wizardPage.getSelectedFile();
		final TemplateStore templateStore = GoalTemplateAccess.getInstance().getTemplateStore();
		final Template template = templateStore.findTemplateById(this.template);
		final String pattern = String.format(template.getPattern(),
				moduleFile.getName().replace("." + moduleFile.getFileExtension(), ""));
		final InputStream is = new ByteArrayInputStream(pattern.getBytes());
		try {
			moduleFile.create(is, false, null);
		} catch (final Exception e) {
			MessageDialog.openError(getShell(), "Error", e.getMessage());
			super.performCancel();
		}

		/*
		 * IFile origin = null; if (this.originator != null &&
		 * this.originator.getResource() instanceof IFile &&
		 * Messages.GOALAgentFileExtension .equals(((IFile)
		 * this.originator.getResource()) .getFileExtension())) { origin =
		 * (IFile) this.originator.getResource(); } else { final Map<String,
		 * IResource> agents = new HashMap<String, IResource>(); try { for
		 * (final IResource child : moduleFile.getProject().members()) { if
		 * (Messages.GOALAgentFileExtension.equalsIgnoreCase(child
		 * .getFileExtension())) { agents.put(child.getName(), child); } } if
		 * (agents.size() == 1) { origin = (IFile) agents.get(0); } else if
		 * (agents.size() > 1) { final ListDialog ld = new ListDialog(new
		 * Shell()); ld.setAddCancelButton(true); ld.setBlockOnOpen(true);
		 * ld.setContentProvider(new ArrayContentProvider());
		 * ld.setLabelProvider(new LabelProvider());
		 * ld.setInput(agents.keySet()); ld.setTitle(
		 * "Select a GOAL file to add to (optional)"); ld.open(); if
		 * (ld.getResult() != null && ld.getResult().length > 0) { origin =
		 * (IFile) agents.get(ld.getResult()[0]); } } } catch (final Exception
		 * e) { DLTKCore.error(e); } }
		 *
		 * if (origin != null) { try { // Automatically add to .goal file if
		 * possible final Scanner s = new Scanner(origin.getContents())
		 * .useDelimiter("\\A"); final String source = s.hasNext() ? s.next() :
		 * ""; s.close(); final IPath target =
		 * moduleFile.getLocation().makeRelativeTo(
		 * origin.getParent().getLocation()); final String toAdd = "#import \""
		 * + target.toOSString() + "\".\n"; origin.setContents(new
		 * ByteArrayInputStream(new String(toAdd + source).getBytes()), true,
		 * true, null); } catch (final Exception e) { DLTKCore.error(e); } }
		 */

		return DLTKCore.create(moduleFile);
	}
}