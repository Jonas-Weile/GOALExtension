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

import org.eclipse.dltk.ui.PreferenceConstants;
import org.eclipse.dltk.ui.text.completion.ScriptCompletionProposal;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.graphics.Image;

import ar.com.tadp.prolog.core.PrologCorePlugin;

/**
 * @author ccancino
 *
 */
public class PrologCompletionProposal extends ScriptCompletionProposal {

	public PrologCompletionProposal(final String replacementString, final int replacementOffset,
			final int replacementLength, final Image image, final String displayString, final int relevance) {
		super(replacementString, replacementOffset, replacementLength, image, displayString, relevance);
	}

	public PrologCompletionProposal(final String replacementString, final int replacementOffset,
			final int replacementLength, final Image image, final String displayString, final int relevance,
			final boolean isInDoc) {
		super(replacementString, replacementOffset, replacementLength, image, displayString, relevance, isInDoc);
	}

	@Override
	protected boolean isSmartTrigger(final char trigger) {
		return (trigger == '.') ? true : false;
	}

	@Override
	protected boolean insertCompletion() {
		final IPreferenceStore preference = PrologCorePlugin.getDefault().getPreferenceStore();
		return preference.getBoolean(PreferenceConstants.CODEASSIST_INSERT_COMPLETION);
	}

}
