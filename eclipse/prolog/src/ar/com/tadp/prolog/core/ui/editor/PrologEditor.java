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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.dltk.core.IDLTKLanguageToolkit;
import org.eclipse.dltk.internal.ui.editor.ScriptEditor;
import org.eclipse.dltk.ui.text.ScriptTextTools;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentExtension3;
import org.eclipse.jface.text.source.DefaultCharacterPairMatcher;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.SourceViewerDecorationSupport;

import ar.com.tadp.prolog.core.PrologCorePlugin;
import ar.com.tadp.prolog.core.PrologLanguageToolkit;
import ar.com.tadp.prolog.core.ui.editor.text.PrologColorConstants;

/**
 * @author ccancino
 *
 */
public class PrologEditor extends ScriptEditor {

	public static final String EDITOR_ID = "ar.com.tadp.prolog.ui.editor";

	public static final String EDITOR_CONTEXT = "#PrologEditorContext";

	@Override
	protected void initializeEditor() {
		super.initializeEditor();
		setEditorContextMenuId(EDITOR_CONTEXT);
	}

	@Override
	public String getEditorId() {
		return EDITOR_ID;
	}

	@Override
	protected void initializeKeyBindingScopes() {
		setKeyBindingScopes(new String[] { EDITOR_ID + ".prologEditorScope" }); //$NON-NLS-1$
	}

	@Override
	public IPreferenceStore getScriptPreferenceStore() {
		return PrologCorePlugin.getDefault().getPreferenceStore();
	}

	@Override
	public IDLTKLanguageToolkit getLanguageToolkit() {
		return PrologLanguageToolkit.getDefault();
	}

	@Override
	public ScriptTextTools getTextTools() {
		return PrologCorePlugin.getDefault().getTextTools();
	}

	@Override
	protected void connectPartitioningToElement(final IEditorInput input, final IDocument document) {
		if (document instanceof IDocumentExtension3) {
			final IDocumentExtension3 extension = (IDocumentExtension3) document;
			if (extension.getDocumentPartitioner(PrologPartitions.PROLOG_PARTITIONING) == null) {
				final PrologTextTools tools = (PrologTextTools) PrologCorePlugin.getDefault().getTextTools();
				tools.setupDocumentPartitioner(document, PrologPartitions.PROLOG_PARTITIONING);
			}
		}
	}

	@Override
	protected void configureSourceViewerDecorationSupport(final SourceViewerDecorationSupport support) {
		super.configureSourceViewerDecorationSupport(support);

		support.setCharacterPairMatcher(
				new DefaultCharacterPairMatcher(new char[] { '(', ')', '[', ']', '{', '}', '\"', '\"', '\'', '\'' }));
		support.setMatchingCharacterPainterPreferenceKeys(PrologColorConstants.EDITOR_MATCHING_BRACKETS,
				PrologColorConstants.EDITOR_MATCHING_BRACKETS_COLOR);
	}

	/**
	 * @return the project for the file that's being edited (or null if not
	 *         available)
	 */
	public IProject getProject() {
		final IEditorInput editorInput = getEditorInput();
		if (editorInput instanceof FileEditorInput) {
			final IFile file = ((FileEditorInput) editorInput).getAdapter(IFile.class);
			return file.getProject();
		}
		return null;
	}

}
