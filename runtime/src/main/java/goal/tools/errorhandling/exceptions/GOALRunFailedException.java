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

/**
 * This exception is thrown when a single Run of a mas failed.
 */
public class GOALRunFailedException extends GOALException {
	private static final long serialVersionUID = -5236339908916550411L;

	/**
	 * @param string
	 * @param exception
	 */
	public GOALRunFailedException(String string, Throwable exception) {
		super(string, exception);
	}

}