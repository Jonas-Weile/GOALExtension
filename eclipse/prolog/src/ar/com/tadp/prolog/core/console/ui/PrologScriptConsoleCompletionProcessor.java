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
package ar.com.tadp.prolog.core.console.ui;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.dltk.console.IScriptConsoleShell;
import org.eclipse.dltk.console.ui.IScriptConsoleViewer;
import org.eclipse.dltk.console.ui.ScriptConsoleCompletionProcessor;
import org.eclipse.dltk.ui.DLTKPluginImages;
import org.eclipse.jface.text.ITextViewer;
import org.eclipse.jface.text.contentassist.ICompletionProposal;
import org.eclipse.jface.text.contentassist.IContextInformation;
import org.eclipse.jface.text.contentassist.IContextInformationValidator;

import ar.com.tadp.prolog.core.PrologSyntax;
import ar.com.tadp.prolog.core.completion.PrologCompletionProposal;
import ar.com.tadp.prolog.core.completion.ProposalRelevanceComparator;

/**
 * @author ccancino
 *
 */
public class PrologScriptConsoleCompletionProcessor extends ScriptConsoleCompletionProcessor {

	public PrologScriptConsoleCompletionProcessor(final IScriptConsoleShell interpreterShell) {
		super(interpreterShell);
	}

	@Override
	protected ICompletionProposal[] computeCompletionProposalsImpl(final IScriptConsoleViewer viewer,
			final int offset) {
		final String commandLine = viewer.getCommandLine();
		final List<ICompletionProposal> proposals = new LinkedList<ICompletionProposal>();
		proposals.addAll(getListingProposals(commandLine, offset - commandLine.length()));
		proposals.addAll(getKeywordsProposals(commandLine, offset - commandLine.length()));

		final ICompletionProposal[] prop = new ICompletionProposal[proposals.size()];
		proposals.toArray(prop);
		Arrays.sort(prop, ProposalRelevanceComparator.getInstance());
		return prop;
	}

	@SuppressWarnings("unchecked")
	private Collection<ICompletionProposal> getListingProposals(final String commandLine, final int offset) {
		try {
			return getInterpreterShell().getCompletions(commandLine, offset);
		} catch (final IOException e) {
			return new LinkedList<ICompletionProposal>();
		}
	}

	private List<ICompletionProposal> getKeywordsProposals(final String commandLine, final int offset) {
		final List<ICompletionProposal> proposals = new LinkedList<ICompletionProposal>();
		for (final String keyword : PrologSyntax.getKeywords()) {
			if (keyword.startsWith(commandLine)) {
				proposals.add(new PrologCompletionProposal(keyword, offset, keyword.length(),
						DLTKPluginImages.get(DLTKPluginImages.IMG_OBJS_KEYWORD), keyword, 20));
			}
		}
		return proposals;
	}

	@Override
	protected IContextInformation[] computeContextInformationImpl(final ITextViewer viewer, final int offset) {
		return null;
	}

	@Override
	public IContextInformationValidator getContextInformationValidator() {
		return null;
	}

}
