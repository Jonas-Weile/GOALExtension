package org.eclipse.gdt.debug;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.gdt.Messages;
import org.eclipse.ui.texteditor.ITextEditor;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class GoalBreakpointAdapterFactory implements IAdapterFactory {
	@Override
	public Object getAdapter(final Object adaptableObject, final Class adapterType) {
		if (adaptableObject instanceof ITextEditor) {
			final ITextEditor editorPart = (ITextEditor) adaptableObject;
			final IResource resource = editorPart.getEditorInput().getAdapter(IResource.class);
			if (resource != null) {
				final String extension = resource.getFileExtension();
				if (extension != null && (extension.equals(Messages.ActionFileExtension)
						|| extension.equals(Messages.ModuleFileExtension))) {
					return new GoalBreakpointAdapter();
				}
			}
		}
		return null;
	}

	@Override
	public Class[] getAdapterList() {
		return new Class[] { GoalBreakpointAdapter.class };
	}
}
