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
 * An exception in the management of a database, like creation or deletion.
 */
public class MSTDatabaseException extends MSTException {
	private static final long serialVersionUID = -4057002992724025029L;

	public MSTDatabaseException(String message) {
		super(message);
	}

	public MSTDatabaseException(String message, Throwable cause) {
		super(message, cause);
	}
}
