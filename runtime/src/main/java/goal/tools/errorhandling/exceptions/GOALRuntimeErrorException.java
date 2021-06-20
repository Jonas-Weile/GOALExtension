/**
 * The GOAL Runtime Environment. Copyright (C) 2015 Koen Hindriks.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package goal.tools.errorhandling.exceptions;

public class GOALRuntimeErrorException extends RuntimeException {
	/** Auto-generated serial version UID */
	private static final long serialVersionUID = 2864122461637581518L;

	/**
	 * Creates a {@link GOALRuntimeErrorException}. Should be used only for
	 * reporting errors that occur while running a GOAL agent that are caused by
	 * issues in the agent program that is run.
	 *
	 * @param string
	 *            The error message.
	 * @param e
	 *            The exception ...
	 */
	public GOALRuntimeErrorException(String string, Exception exception) {
		super(string, exception);
	}

	public GOALRuntimeErrorException(String string, Throwable cause) {
		super(string, cause);
	}

	public GOALRuntimeErrorException(Exception exception) {
		super(exception);
	}

	public GOALRuntimeErrorException(String string) {
		super(string);
	}

}
