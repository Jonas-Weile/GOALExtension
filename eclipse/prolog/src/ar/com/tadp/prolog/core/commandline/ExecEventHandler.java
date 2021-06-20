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
package ar.com.tadp.prolog.core.commandline;

/**
 * This interface is used to provide a call-back mechanism, so that we can get
 * the output from the process as they happen
 *
 * @author ccancino
 */
public interface ExecEventHandler {
	// This method gets called when the process sent us a new input String.
	public void processNewInput(String input);

	// This method gets called when the process sent us a new error String.
	public void processNewError(String error);

	// This method gets called when the process has ended.
	public void processEnded(int exitValue);
}
