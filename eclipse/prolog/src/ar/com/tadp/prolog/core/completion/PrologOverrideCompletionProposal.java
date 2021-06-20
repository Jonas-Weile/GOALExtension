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

import org.eclipse.core.runtime.Assert;
import org.eclipse.dltk.core.IScriptProject;
import org.eclipse.dltk.core.ISourceModule;
import org.eclipse.dltk.ui.PreferenceConstants;
import org.eclipse.dltk.ui.text.completion.ScriptTypeCompletionProposal;
import org.eclipse.jface.preference.IPreferenceStore;

import ar.com.tadp.prolog.core.PrologCorePlugin;

/**
 * @author ccancino
 *
 */
public class PrologOverrideCompletionProposal extends ScriptTypeCompletionProposal {

	public PrologOverrideCompletionProposal(final IScriptProject scriptProject, final ISourceModule compilationUnit,
			final String name, final String[] paramTypes, final int start, final int length, final String displayName,
			final String completionProposal) {

		super(completionProposal, compilationUnit, start, length, null, displayName, 0);
		Assert.isNotNull(scriptProject);
		Assert.isNotNull(name);
		Assert.isNotNull(paramTypes);
		Assert.isNotNull(compilationUnit);

		final StringBuffer buffer = new StringBuffer();
		buffer.append(completionProposal);

		setReplacementString(buffer.toString());
	}

	@Override
	protected boolean isSmartTrigger(final char trigger) {
		if (trigger == '.') {
			return true;
		}
		return false;
	}

	@Override
	protected boolean insertCompletion() {
		final IPreferenceStore preference = PrologCorePlugin.getDefault().getPreferenceStore();
		return preference.getBoolean(PreferenceConstants.CODEASSIST_INSERT_COMPLETION);
	}

}
