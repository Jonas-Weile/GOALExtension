/**
 * The GOAL Mental State. Copyright (C) 2014 Koen Hindriks.
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

package mentalState.error;

/**
 * The super-class for exceptions in this package.
 */
public abstract class MSTException extends Exception {
	private static final long serialVersionUID = -688163115207786945L;

	/**
	 * Creates a MentalState Exception with the given message as exception
	 *
	 * @param message
	 */
	public MSTException(String message) {
		super(message);
	}

	/**
	 * Creates a MentalState exception with the given message and cause
	 *
	 * @param message
	 * @param cause
	 */
	public MSTException(String message, Throwable cause) {
		super(message, cause);
	}

	@Override
	public String toString() {
		return "<" + getClass().getSimpleName() + ": " + getMessage() + ", " + getCause() + ">";
	}
}
