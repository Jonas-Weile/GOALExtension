/**
 * Knowledge Representation Tools. Copyright (C) 2014 Koen Hindriks.
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

package krTools.exceptions;

/**
 * A KR Query Failed exception should be thrown in case something bad happened
 * while a query evaluation is performed by the KR inference engine.
 */
public class KRQueryFailedException extends KRException {

	private static final long serialVersionUID = -7240306206190923813L;

	/**
	 * Creates a KRQueryFailedException with a given message
	 *
	 * @param message
	 *            as a string message
	 */
	public KRQueryFailedException(String message) {
		super(message);
	}

	/**
	 * Creates a KRQueryFailedException with a given message and cause
	 *
	 * @param message
	 *            as a string message
	 * @param cause
	 *            as a throwable cause
	 */
	public KRQueryFailedException(String message, Throwable cause) {
		super(message, cause);
	}

}
