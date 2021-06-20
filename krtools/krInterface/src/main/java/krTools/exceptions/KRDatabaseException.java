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
 * A KR Database exception should be thrown in case a database could <i>not be
 * created</i>, if content in the database could <i>not be updated</i>, e.g.,
 * could not be added or removed, or something <i>bad happened while
 * destroying</i> the database.
 */
public class KRDatabaseException extends KRException {

	private static final long serialVersionUID = 2132716415540874609L;

	/**
	 * Constructor of KRDatabaseException with a given message
	 *
	 * @param message
	 *            as string for this exception
	 */
	public KRDatabaseException(String message) {
		super(message);
	}

	/**
	 * Creates a KRDatabaseException with a given message and cause.
	 *
	 * @param message
	 *            as string
	 * @param cause
	 *            as Throwable
	 */
	public KRDatabaseException(String message, Throwable cause) {
		super(message, cause);
	}

}
