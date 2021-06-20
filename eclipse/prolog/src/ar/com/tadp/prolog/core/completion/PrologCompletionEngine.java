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
package ar.com.tadp.prolog.core.completion;

import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dltk.codeassist.ICompletionEngine;
import org.eclipse.dltk.compiler.env.IModuleSource;
import org.eclipse.dltk.core.CompletionProposal;
import org.eclipse.dltk.core.CompletionRequestor;
import org.eclipse.dltk.core.DLTKCore;
import org.eclipse.dltk.core.IField;
import org.eclipse.dltk.core.IMethod;
import org.eclipse.dltk.core.IModelElement;
import org.eclipse.dltk.core.IModelElementVisitor;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.IType;
import org.eclipse.dltk.core.ModelException;

import ar.com.tadp.prolog.core.PrologSyntax;

/**
 * @author ccancino
 *
 */
public class PrologCompletionEngine implements ICompletionEngine {
	private CompletionRequestor requestor;
	private int actualCompletionPosition;
	private int offset;

	@Override
	public void setProgressMonitor(final IProgressMonitor progressMonitor) {
	}

	@Override
	public void complete(final IModuleSource module, final int position, final int pos) {
		this.actualCompletionPosition = position;
		this.offset = pos;
		final String[] keywords = PrologSyntax.getKeywords();

		final String prefix = getPrefix(module.getSourceContents(), this.actualCompletionPosition);
		this.actualCompletionPosition -= prefix.length();

		for (final String keyword : keywords) {
			if (keyword.startsWith(prefix)) {
				createProposal(keyword, null);
			}
		}

		// Completion for model elements.
		try {
			module.getModelElement().accept(new IModelElementVisitor() {
				@Override
				public boolean visit(final IModelElement element) {
					if (element.getElementType() > IModelElement.SOURCE_MODULE) {
						if (element.getElementName().startsWith(prefix)) {
							createProposal(element.getElementName(), element);
						}
					}
					return true;
				}
			});
		} catch (final ModelException e) {
			DLTKCore.error(e);
		}
	}

	private void createProposal(final String name, final IModelElement element) {
		CompletionProposal proposal = null;
		try {
			if (element == null) {
				proposal = this.createProposal(CompletionProposal.KEYWORD, this.actualCompletionPosition);
			} else {
				switch (element.getElementType()) {
				case IModelElement.METHOD:
					proposal = this.createProposal(CompletionProposal.METHOD_DECLARATION,
							this.actualCompletionPosition);
					proposal.setFlags(((IMethod) element).getFlags());
					break;
				case IModelElement.FIELD:
					proposal = this.createProposal(CompletionProposal.FIELD_REF, this.actualCompletionPosition);
					proposal.setFlags(((IField) element).getFlags());
					break;
				case IModelElement.TYPE:
					proposal = this.createProposal(CompletionProposal.TYPE_REF, this.actualCompletionPosition);
					proposal.setFlags(((IType) element).getFlags());
					break;
				default:
					proposal = this.createProposal(CompletionProposal.KEYWORD, this.actualCompletionPosition);
					break;
				}
			}
			proposal.setName(name);
			proposal.setCompletion(name);
			proposal.setReplaceRange(this.actualCompletionPosition - this.offset,
					this.actualCompletionPosition - this.offset);
			proposal.setRelevance(20);
			proposal.setModelElement(element);
			this.requestor.accept(proposal);
		} catch (final Exception e) {
		}
	}

	@SuppressWarnings("rawtypes")
	@Override
	public void setOptions(final Map options) {
	}

	@Override
	public void setProject(final IScriptProject project) {
	}

	@Override
	public void setRequestor(final CompletionRequestor requestor) {
		this.requestor = requestor;
	}

	protected CompletionProposal createProposal(final int kind, final int completionOffset) {
		return CompletionProposal.create(kind, completionOffset - this.offset);
	}

	private String getPrefix(final String sourceContents, final int currentOffset) {
		int i = currentOffset;
		if (i > sourceContents.length()) {
			return "";
		}

		while (i > 0) {
			final char ch = sourceContents.substring(i - 1, i).charAt(0);
			if (isInvalidCharacter(ch)) {
				break;
			}
			i--;
		}
		return sourceContents.substring(i, currentOffset).trim();
	}

	private boolean isInvalidCharacter(final char ch) {
		return ch != '\r' && ch != '\n' && !Character.isJavaIdentifierPart(ch) && ch != '(' && ch != ')';
	}

}
