package org.eclipse.gdt.editor;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IDLTKLanguageToolkit;
import org.eclipse.dltk.internal.ui.editor.ScriptEditor;
import org.eclipse.dltk.ui.text.ScriptTextTools;
import org.eclipse.gdt.Activator;
import org.eclipse.gdt.GoalLanguageToolkit;
import org.eclipse.gdt.debug.GoalConditionalBreakpoint;
import org.eclipse.gdt.debug.GoalLineBreakpoint;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.texteditor.ITextEditorActionConstants;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

public class GoalEditor extends ScriptEditor {
	private static final String EDITOR_ID = "org.eclipse.gdt.editor";
	private static final String EDITOR_CONTEXT = "#GoalEditorContext";

	@Override
	protected void initializeEditor() {
		super.initializeEditor();
		setEditorContextMenuId(EDITOR_CONTEXT);
		setAction(ITextEditorActionConstants.RULER_DOUBLE_CLICK, new Action() {
			@Override
			public void run() {
				try {
					toggleBreakpoint(getEditorInput().getAdapter(IResource.class), getCurrentLine());
				} catch (final Exception e) {
					DLTKCore.error(e);
				}
			}
		});
	}

	private void toggleBreakpoint(final IResource resource, final int line) throws CoreException {
		final GoalLineBreakpoint existing = getBreakpointAt(resource, line);
		if (existing == null) {
			// Nothing there yet? Add regular!
			new GoalLineBreakpoint(resource, line);
		} else if (existing instanceof GoalConditionalBreakpoint) {
			// Was conditional? Then remove!
			existing.delete();
		} else {
			// Was not conditional (regular)? Then remove...
			existing.delete();
			// ... and add conditional!
			new GoalConditionalBreakpoint(resource, line);
		}
	}

	private GoalLineBreakpoint getBreakpointAt(final IResource resource, final int line) throws CoreException {
		for (GoalLineBreakpoint breakpoint : GoalLineBreakpoint.getAll()) {
			if (resource.equals(breakpoint.getMarker().getResource())) {
				if (breakpoint.getLineNumber() == line) {
					return breakpoint;
				}
			}
		}
		return null;
	}

	private int getCurrentLine() {
		final IVerticalRulerInfo ruler = (IVerticalRulerInfo) getAdapter(IVerticalRulerInfo.class);
		return ruler.getLineOfLastMouseButtonActivity() + 1;
	}

	@Override
	public boolean isEditable() {
		// FIXME: doesn't work sometimes
		// final IResource resource =
		// getEditorInput().getAdapter(IResource.class);
		// if (resource == null) {
		return true; // gives a plain text editor at this point. #3162
		// } else {
		// final DebuggerCollection collection =
		// DebuggerCollection.getCollection(resource.getProject().getName());
		// return (collection == null);
		// }
	}

	@Override
	public String getEditorId() {
		return EDITOR_ID;
	}

	@Override
	public IPreferenceStore getScriptPreferenceStore() {
		return Activator.getDefault().getPreferenceStore();
	}

	@Override
	public IDLTKLanguageToolkit getLanguageToolkit() {
		return GoalLanguageToolkit.getDefault();
	}

	@Override
	public ScriptTextTools getTextTools() {
		return Activator.getDefault().getTextTools();
	}

	@Override
	protected void connectPartitioningToElement(final IEditorInput input, final IDocument document) {
		if (document instanceof IDocumentExtension3) {
			final IDocumentExtension3 extension = (IDocumentExtension3) document;
			if (extension.getDocumentPartitioner(IGoalPartitions.GOAL_PARTITIONING) == null) {
				getTextTools().setupDocumentPartitioner(document, IGoalPartitions.GOAL_PARTITIONING);
			}
		}
	}

	@Override
	protected void configureSourceViewerDecorationSupport(final SourceViewerDecorationSupport support) {
		super.configureSourceViewerDecorationSupport(support);
		support.setCharacterPairMatcher(
				new DefaultCharacterPairMatcher(new char[] { '(', ')', '[', ']', '{', '}', '\"', '\"', '\'', '\'' }));
		support.setMatchingCharacterPainterPreferenceKeys(IGoalColorConstants.EDITOR_MATCHING_BRACKETS,
				IGoalColorConstants.EDITOR_MATCHING_BRACKETS_COLOR);
	}
}