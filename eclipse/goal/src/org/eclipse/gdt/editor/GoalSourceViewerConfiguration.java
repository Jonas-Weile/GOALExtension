package org.eclipse.gdt.editor;

import org.eclipse.dltk.ui.text.IColorManager;
import org.eclipse.dltk.ui.text.ScriptPresentationReconciler;
import org.eclipse.dltk.ui.text.ScriptSourceViewerConfiguration;
import org.eclipse.dltk.ui.text.completion.ContentAssistPreference;
import org.eclipse.gdt.completion.GoalCompletionProcessor;
import org.eclipse.gdt.parser.GoalSourceScanner;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.texteditor.ITextEditor;

public class GoalSourceViewerConfiguration extends ScriptSourceViewerConfiguration {
	private ITokenScanner fScanner;

	public GoalSourceViewerConfiguration(final IColorManager colorManager, final IPreferenceStore preferenceStore,
			final ITextEditor editor, final String partitioning) {
		super(colorManager, preferenceStore, editor, partitioning);
	}

	@Override
	public String getCommentPrefix() {
		return "%";
	}

	@Override
	public IAutoEditStrategy[] getAutoEditStrategies(final ISourceViewer sourceViewer, final String contentType) {
		return new IAutoEditStrategy[] { new DefaultIndentLineAutoEditStrategy() };
	}

	@Override
	protected ContentAssistPreference getContentAssistPreference() {
		return GoalContentAssistPreference.getDefault();
	}

	@Override
	protected void initializeScanners() {
		this.fScanner = new GoalSourceScanner(getColorManager(), this.fPreferenceStore, getEditor());
	}

	@Override
	public IPresentationReconciler getPresentationReconciler(final ISourceViewer sourceViewer) {
		final PresentationReconciler reconciler = new ScriptPresentationReconciler();
		reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

		final DefaultDamagerRepairer dr = new DefaultDamagerRepairer(this.fScanner);
		reconciler.setDamager(dr, IDocument.DEFAULT_CONTENT_TYPE);
		reconciler.setRepairer(dr, IDocument.DEFAULT_CONTENT_TYPE);

		return reconciler;
	}

	@Override
	public boolean affectsTextPresentation(final PropertyChangeEvent event) {
		return true;
	}

	@Override
	protected void alterContentAssistant(final ContentAssistant assistant) {
		final IContentAssistProcessor scriptProcessor = new GoalCompletionProcessor(getEditor(), assistant,
				IDocument.DEFAULT_CONTENT_TYPE);
		assistant.setContentAssistProcessor(scriptProcessor, IDocument.DEFAULT_CONTENT_TYPE);
	}
}