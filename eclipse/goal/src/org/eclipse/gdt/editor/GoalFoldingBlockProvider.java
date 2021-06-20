package org.eclipse.gdt.editor;

import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IModelElementVisitor;
import org.eclipse.dltk.core.ISourceRange;
import org.eclipse.dltk.core.ISourceReference;
import org.eclipse.dltk.core.ModelException;
import org.eclipse.dltk.core.SourceRange;
import org.eclipse.dltk.ui.DLTKUIPlugin;
import org.eclipse.dltk.ui.text.folding.AbortFoldingException;
import org.eclipse.dltk.ui.text.folding.IFoldingBlockKind;
import org.eclipse.dltk.ui.text.folding.IFoldingBlockProvider;
import org.eclipse.dltk.ui.text.folding.IFoldingBlockRequestor;
import org.eclipse.dltk.ui.text.folding.IFoldingContent;
import org.eclipse.jface.preference.IPreferenceStore;

public class GoalFoldingBlockProvider implements IFoldingBlockProvider, IModelElementVisitor {
	private IFoldingBlockRequestor requestor;

	@Override
	public void setRequestor(final IFoldingBlockRequestor requestor) {
		this.requestor = requestor;
	}

	@Override
	public void computeFoldableBlocks(final IFoldingContent content) {
		try {
			content.getModelElement().accept(this);
		} catch (final ModelException e) {
			abortFolding(e);
		}
	}

	private void abortFolding(final ModelException e) {
		DLTKUIPlugin.logErrorMessage("Error when computing folding", e);
		throw new AbortFoldingException();
	}

	@Override
	public boolean visit(final IModelElement element) {
		if (element instanceof ISourceReference) {
			try {
				final ISourceRange range = ((ISourceReference) element).getSourceRange();
				if (SourceRange.isAvailable(range) && range.getLength() > 0
						&& element.getElementType() == IModelElement.METHOD) {
					this.requestor.acceptBlock(range.getOffset(), range.getOffset() + range.getLength() - 1,
							new FoldingBlockKind(), element, false);
				}
			} catch (final ModelException e) {
				abortFolding(e);
			}
		}
		return true;
	}

	@Override
	public int getMinimalLineCount() {
		return 0;
	}

	@Override
	public void initializePreferences(final IPreferenceStore preferenceStore) {
	}

	private static class FoldingBlockKind implements IFoldingBlockKind {
		@Override
		public boolean isComment() {
			return false;
		}
	}
}
