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
 * This exception is thrown when an action on a database (query, insert, delete
 * etc) fails.
 */
public class GOALDatabaseException extends GOALException {
	/** Generated serialVersionUID */
	private static final long serialVersionUID = 953890011844171754L;

	/**
	 * General error indicating a problem with executing a query/insert/delete
	 * to/from a database.
	 *
	 * @param string
	 * @param exception
	 */
	public GOALDatabaseException(String string, Throwable exception) {
		super(string, exception);
	}

	public GOALDatabaseException(String string) {
		super(string);
	}

}
