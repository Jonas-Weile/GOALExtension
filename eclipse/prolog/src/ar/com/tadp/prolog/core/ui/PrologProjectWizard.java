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
package ar.com.tadp.prolog.core.ui;

import java.util.Map;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.dltk.ui.DLTKUIPlugin;
import org.eclipse.dltk.ui.wizards.Messages;
import org.eclipse.dltk.ui.wizards.ProjectWizard;
import org.eclipse.dltk.ui.wizards.ProjectWizardFirstPage;
import org.eclipse.dltk.ui.wizards.ProjectWizardSecondPage;

/**
 * @author ccancino
 *
 */
public class PrologProjectWizard extends ProjectWizard {
	private ProjectWizardFirstPage fFirstPage;
	private ProjectWizardSecondPage fSecondPage;
	private String nature;

	public PrologProjectWizard() {
		setDialogSettings(DLTKUIPlugin.getDefault().getDialogSettings());
		setWindowTitle("New Prolog Project");
	}

	@Override
	public String getScriptNature() {
		return this.nature;
	}

	@Override
	public void addPages() {
		super.addPages();
		this.fFirstPage = new ProjectWizardFirstPage() {

			@Override
			protected boolean interpeterRequired() {
				return false;
			}

			@Override
			public boolean isSrc() {
				return true;
			};
		};

		// First page
		this.fFirstPage.setTitle("Create a Prolog Project");
		this.fFirstPage.setDescription("Create a Prolog project in the workspace or in an external location.");
		addPage(this.fFirstPage);

		// Second page
		this.fSecondPage = new ProjectWizardSecondPage(this.fFirstPage);
		this.fSecondPage.setTitle("Prolog Settings");
		this.fSecondPage.setDescription("Define the Prolog build setting.");
		addPage(this.fSecondPage);
	}

	@Override
	public void setInitializationData(final IConfigurationElement cfig, final String propertyName, final Object data) {
		super.setInitializationData(cfig, propertyName, data);
		if (data instanceof String) {
			this.nature = (String) data;
		} else if (data instanceof Map<?, ?>) {
			this.nature = (String) ((Map<?, ?>) data).get("nature"); //$NON-NLS-1$
		}
		if (this.nature == null || this.nature.length() == 0) {
			throw new RuntimeException(Messages.GenericDLTKProjectWizard_natureMustBeSpecified);
		}
	}

}
