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

public class NewActionFileWizard extends NewGoalProjectFileWizard {

	public NewActionFileWizard() {
		setWindowTitle("New Action Specification File Wizard");
		setPageName("New Action Specification File");
		setTitle("Create a new Action Specification file");
		setDescription("Create a new Action Specification file in the workspace");
		setTemplate(Messages.ActionFileTemplateID);
		setFileExtension(Messages.ActionFileExtension);
	}

	// @SuppressWarnings("resource")
	@Override
	public IModelElement getCreatedElement() {
		final IFile actionFile = this.wizardPage.getSelectedFile();
		final TemplateStore templateStore = GoalTemplateAccess.getInstance().getTemplateStore();
		final Template template = templateStore.findTemplateById(this.template);
		final String pattern = String.format(template.getPattern(),
				actionFile.getName().replace("." + actionFile.getFileExtension(), ""));
		final InputStream is = new ByteArrayInputStream(pattern.getBytes());
		try {
			actionFile.create(is, false, null);
		} catch (final Exception e) {
			MessageDialog.openError(getShell(), "Error", e.getMessage());
			super.performCancel();
		}
		/*
		 * final IModelElement returned = super.getCreatedElement(); IFile
		 * origin = null; if (this.originator != null &&
		 * this.originator.getResource() instanceof IFile &&
		 * Messages.MASFileExtension.equals(((IFile) this.originator
		 * .getResource()).getFileExtension())) { origin = (IFile)
		 * this.originator.getResource(); } else { final Map<String, IResource>
		 * mas2g = new HashMap<String, IResource>(); try { for (final IResource
		 * child : returned.getScriptProject() .getProject().members()) { if
		 * (Messages.MASFileExtension.equalsIgnoreCase(child
		 * .getFileExtension())) { mas2g.put(child.getName(), child); } } if
		 * (mas2g.size() == 1) { origin = (IFile)
		 * mas2g.values().iterator().next(); } else if (mas2g.size() > 1) {
		 * final ListDialog ld = new ListDialog(new Shell());
		 * ld.setAddCancelButton(true); ld.setBlockOnOpen(true);
		 * ld.setContentProvider(new ArrayContentProvider());
		 * ld.setLabelProvider(new LabelProvider());
		 * ld.setInput(mas2g.keySet()); ld.setTitle(
		 * "Select a MAS2G file to add to (optional)"); ld.open(); if
		 * (ld.getResult() != null && ld.getResult().length > 0) { origin =
		 * (IFile) mas2g.get(ld.getResult()[0]); } } } catch (final Exception e)
		 * { DLTKCore.error(e); } }
		 *
		 * if (origin != null) { try { final Scanner s = new
		 * Scanner(origin.getContents()).useDelimiter("\\A"); final String
		 * source = s.hasNext() ? s.next() : ""; s.close(); final IPath target =
		 * this.wizardPage.getSelectedFile() .getLocation()
		 * .makeRelativeTo(origin.getParent().getLocation());
		 *
		 * final String lookFor = "agentfiles"; final int firstStart =
		 * source.indexOf(lookFor) + lookFor.length(); if ((firstStart -
		 * lookFor.length()) >= 0) { final int realStart = firstStart +
		 * source.substring(firstStart).indexOf("{") + 1; final String newSource
		 * = source.substring(0, realStart) + "\r\n\t\"" + target.toOSString() +
		 * "\"." + source.substring(realStart, source.length());
		 * origin.setContents( new ByteArrayInputStream(newSource.getBytes()),
		 * true, true, null); } } catch (final Exception e) { DLTKCore.error(e);
		 * } } return returned;
		 */
		return DLTKCore.create(actionFile);
	}
}