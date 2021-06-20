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

import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.ui.text.completion.ScriptCompletionProposal;
import org.eclipse.dltk.ui.text.completion.ScriptCompletionProposalCollector;
import org.eclipse.swt.graphics.Image;

import ar.com.tadp.prolog.core.PrologNature;

/**
 * @author ccancino
 *
 */
public class PrologCompletionProposalCollector extends ScriptCompletionProposalCollector {

	protected final static char[] VAR_TRIGGER = { '\t', ' ', '=', ';', '.' };

	@Override
	protected char[] getVarTrigger() {
		return VAR_TRIGGER;
	}

	public PrologCompletionProposalCollector(final ISourceModule module) {
		super(module);
	}

	@Override
	protected String getNatureId() {
		return PrologNature.PROLOG_NATURE;
	}

	// Specific proposals creation. May be use factory?
	@Override
	protected ScriptCompletionProposal createScriptCompletionProposal(final String completion, final int replaceStart,
			final int length, final Image image, final String displayString, final int i) {
		return new PrologCompletionProposal(completion, replaceStart, length, image, displayString, i);
	}

	@Override
	protected ScriptCompletionProposal createScriptCompletionProposal(final String completion, final int replaceStart,
			final int length, final Image image, final String displayString, final int i, final boolean isInDoc) {
		return new PrologCompletionProposal(completion, replaceStart, length, image, displayString, i, isInDoc);
	}

	@Override
	protected ScriptCompletionProposal createOverrideCompletionProposal(final IScriptProject scriptProject,
			final ISourceModule compilationUnit, final String name, final String[] paramTypes, final int start,
			final int length, final String displayName, final String completionProposal) {
		return new PrologOverrideCompletionProposal(scriptProject, compilationUnit, name, paramTypes, start, length,
				displayName, completionProposal);
	}

}
