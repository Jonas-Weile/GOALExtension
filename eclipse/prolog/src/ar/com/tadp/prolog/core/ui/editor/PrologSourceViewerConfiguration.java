/*****************************************************************************
 * This file is part of the Prolog Development Tools (ProDT)
 *
 * Author: Claudio Cancinos
 * WWW: https://sourceforge.net/projects/prodevtools
 * Copyright (C): 2008, Claudio Cancinos
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; If not, see <http://www.gnu.org/licenses/>
 ****************************************************************************/
package ar.com.tadp.prolog.core.ui.editor;

import org.eclipse.dltk.ui.text.AbstractScriptScanner;
import org.eclipse.dltk.ui.text.IColorManager;
import org.eclipse.dltk.ui.text.ScriptOutlineInformationControl;
import org.eclipse.dltk.ui.text.ScriptPresentationReconciler;
import org.eclipse.dltk.ui.text.ScriptSourceViewerConfiguration;
import org.eclipse.dltk.ui.text.SingleTokenScriptScanner;
import org.eclipse.dltk.ui.text.completion.ContentAssistPreference;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.DefaultIndentLineAutoEditStrategy;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IInformationControl;
import org.eclipse.jface.text.IInformationControlCreator;
import org.eclipse.jface.text.contentassist.ContentAssistant;
import org.eclipse.jface.text.contentassist.IContentAssistProcessor;
import org.eclipse.jface.text.presentation.IPresentationDamager;
import org.eclipse.jface.text.presentation.IPresentationReconciler;
import org.eclipse.jface.text.presentation.IPresentationRepairer;
import org.eclipse.jface.text.presentation.PresentationReconciler;
import org.eclipse.jface.text.rules.DefaultDamagerRepairer;
import org.eclipse.jface.text.source.ISourceViewer;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.texteditor.ITextEditor;

import ar.com.tadp.prolog.core.completion.PrologCompletionProcessor;
import ar.com.tadp.prolog.core.ui.editor.text.PrologCodeScanner;
import ar.com.tadp.prolog.core.ui.editor.text.PrologColorConstants;

/**
 * @see , {@link PrologCodeScanner}, {@link PrologPartitionScanner}
 * @author ccancino
 */
public class PrologSourceViewerConfiguration extends ScriptSourceViewerConfiguration {
	private AbstractScriptScanner fCodeScanner;
	private AbstractScriptScanner fStringScanner;
	private AbstractScriptScanner fCommentScanner;
	private AbstractScriptScanner fVariableScanner;

	public PrologSourceViewerConfiguration(final IColorManager colorManager, final IPreferenceStore preferenceStore,
			final ITextEditor editor, final String partitioning) {
		super(colorManager, preferenceStore, editor, partitioning);
	}

	@Override
	public IAutoEditStrategy[] getAutoEditStrategies(final ISourceViewer sourceViewer, final String contentType) {
		return new IAutoEditStrategy[] { new DefaultIndentLineAutoEditStrategy() };
	}

	@Override
	public String[] getIndentPrefixes(final ISourceViewer sourceViewer, final String contentType) {
		return new String[] { "\t", "        " };
	}

	@Override
	protected ContentAssistPreference getContentAssistPreference() {
		return PrologContentAssistPreference.getDefault();
	}

	@Override
	protected IInformationControlCreator getOutlinePresenterControlCreator(final ISourceViewer sourceViewer,
			final String commandId) {
		return new IInformationControlCreator() {
			@Override
			public IInformationControl createInformationControl(final Shell parent) {
				final int shellStyle = SWT.RESIZE;
				final int treeStyle = SWT.V_SCROLL | SWT.H_SCROLL;
				return new ScriptOutlineInformationControl(parent, shellStyle, treeStyle, commandId,
						PrologSourceViewerConfiguration.this.fPreferenceStore);
			}
		};
	}

	@Override
	protected void alterContentAssistant(final ContentAssistant assistant) {
		// IDocument.DEFAULT_CONTENT_TYPE
		final IContentAssistProcessor scriptProcessor = new PrologCompletionProcessor(getEditor(), assistant,
				IDocument.DEFAULT_CONTENT_TYPE);
		assistant.setContentAssistProcessor(scriptProcessor, IDocument.DEFAULT_CONTENT_TYPE);
	}

	// This method called from base class.
	@Override
	protected void initializeScanners() {
		// This is our code scanner
		this.fCodeScanner = new PrologCodeScanner(getColorManager(), this.fPreferenceStore);
		// This is default scanners for partitions with same color.
		this.fStringScanner = new SingleTokenScriptScanner(getColorManager(), this.fPreferenceStore,
				PrologColorConstants.PROLOG_STRING);
		this.fCommentScanner = new SingleTokenScriptScanner(getColorManager(), this.fPreferenceStore,
				PrologColorConstants.PROLOG_COMMENT);
		this.fVariableScanner = new SingleTokenScriptScanner(getColorManager(), this.fPreferenceStore,
				PrologColorConstants.PROLOG_VARIABLE);
	}

	private void setDamageRepairer(final PresentationReconciler reconciler, final IPresentationDamager ndr,
			final String partitionName) {
		reconciler.setDamager(ndr, partitionName);
		reconciler.setRepairer((IPresentationRepairer) ndr, partitionName);
	}

	/**
	 * {@link IPresentationDamager} and {@link IPresentationRepairer} are the
	 * ones in charge of keeping up to date the syntax coloring during code
	 * edition. In this method what I do is use those objects to:
	 * <ul>
	 * <li>associate partitions to colors in the case of single color partitions
	 * like comments
	 * <li>associate a scanner to a partition which will be the one to define
	 * the syntax coloring for that partition.
	 * </ul>
	 */
	@Override
	public IPresentationReconciler getPresentationReconciler(final ISourceViewer sourceViewer) {
		final PresentationReconciler reconciler = new ScriptPresentationReconciler();
		reconciler.setDocumentPartitioning(getConfiguredDocumentPartitioning(sourceViewer));

		DefaultDamagerRepairer dr = new DefaultDamagerRepairer(this.fCodeScanner);
		setDamageRepairer(reconciler, dr, IDocument.DEFAULT_CONTENT_TYPE);

		dr = new DefaultDamagerRepairer(this.fStringScanner);
		setDamageRepairer(reconciler, dr, PrologPartitions.PROLOG_STRING);

		dr = new DefaultDamagerRepairer(this.fCommentScanner);
		setDamageRepairer(reconciler, dr, PrologPartitions.PROLOG_COMMENT);

		dr = new DefaultDamagerRepairer(this.fVariableScanner);
		setDamageRepairer(reconciler, dr, PrologPartitions.PROLOG_VARIABLE);

		return reconciler;
	}

	@Override
	public void handlePropertyChangeEvent(final PropertyChangeEvent event) {
		if (this.fCodeScanner.affectsBehavior(event)) {
			this.fCodeScanner.adaptToPreferenceChange(event);
		}
		if (this.fStringScanner.affectsBehavior(event)) {
			this.fStringScanner.adaptToPreferenceChange(event);
		}
	}

	@Override
	public boolean affectsTextPresentation(final PropertyChangeEvent event) {
		return this.fCodeScanner.affectsBehavior(event) || this.fStringScanner.affectsBehavior(event);
	}

}
