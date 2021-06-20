package org.eclipse.gdt.launch;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.PreferencesLookupDelegate;
import org.eclipse.dltk.debug.ui.launchConfigurations.MainLaunchConfigurationTab;
import org.eclipse.dltk.debug.ui.messages.DLTKLaunchConfigurationsMessages;
import org.eclipse.gdt.GoalNature;
import org.eclipse.gdt.Messages;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.ui.dialogs.ElementTreeSelectionDialog;
import org.eclipse.ui.model.WorkbenchContentProvider;
import org.eclipse.ui.model.WorkbenchLabelProvider;
import org.eclipse.ui.views.navigator.ResourceComparator;

public class GoalLaunchConfigurationTab extends MainLaunchConfigurationTab {

	public GoalLaunchConfigurationTab(final String mode) {
		super(mode);
	}

	@Override
	protected void handleSearchButtonSelected() {
		final ElementTreeSelectionDialog dialog = new ElementTreeSelectionDialog(getShell(),
				new WorkbenchLabelProvider(), new WorkbenchContentProvider());
		dialog.setTitle(DLTKLaunchConfigurationsMessages.mainTab_searchButton_title);
		dialog.setMessage(DLTKLaunchConfigurationsMessages.mainTab_searchButton_message);
		dialog.addFilter(new ViewerFilter() {
			@Override
			public boolean select(final Viewer viewer, final Object parentElement, final Object element) {
				if (element instanceof IFile) {
					final IFile file = (IFile) element;
					final String ext = file.getFileExtension();
					return Messages.MASFileExtension.equalsIgnoreCase(ext)
							|| Messages.TestFileExtension.equalsIgnoreCase(ext);
				}
				return false;
			}
		});
		final IScriptProject proj = getProject();
		if (proj != null) {
			dialog.setInput(proj.getProject());
			dialog.setComparator(new ResourceComparator(ResourceComparator.NAME));
			if (dialog.open() == IDialogConstants.OK_ID) {
				final IResource resource = (IResource) dialog.getFirstResult();
				final String arg = resource.getProjectRelativePath().toPortableString();
				setScriptName(arg);
			}
		}
	}

	@Override
	protected boolean breakOnFirstLinePrefEnabled(final PreferencesLookupDelegate delegate) {
		// return delegate.getBoolean(Activator.PLUGIN_ID,
		// DLTKDebugPreferenceConstants.PREF_DBGP_BREAK_ON_FIRST_LINE);
		return false;
	}

	@Override
	protected boolean dbpgLoggingPrefEnabled(final PreferencesLookupDelegate delegate) {
		// return delegate.getBoolean(Activator.PLUGIN_ID,
		// DLTKDebugPreferenceConstants.PREF_DBGP_ENABLE_LOGGING);
		return false;
	}

	@Override
	public String getNatureID() {
		return GoalNature.GOAL_NATURE;
	}
}
