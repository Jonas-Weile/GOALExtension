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
package ar.com.tadp.prolog.core.compiler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * @author Claudio
 *
 */
public class CompilerOutput {
	private int exitStatus;
	private String output;
	private final Collection<CompilerOutputItem> fErrors = new ArrayList<CompilerOutputItem>();
	private List<Exception> exceptions;

	public CompilerOutput(final int exitStatus, final String output, final List<Exception> exceptions) {
		this.exitStatus = exitStatus;
		this.output = output;
		this.exceptions = exceptions;
	}

	public CompilerOutput() {
		// placeholder constructor
	}

	@Override
	public String toString() {
		return "Compiler output [ " + this.exceptions.size() + " Exceptions ]\n" + this.output + "\n" + this.fErrors;
	}

	public int getExitStatus() {
		return this.exitStatus;
	}

	public String getOutput() {
		return this.output;
	}

	public Collection<CompilerOutputItem> getErrors() {
		return new ArrayList<CompilerOutputItem>(this.fErrors);
	}

	public List<Exception> getExceptions() {
		return this.exceptions;
	}

	public void addError(final CompilerOutputItem item) {
		this.fErrors.add(item);
	}

}
