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

package msFactory;

/**
 * Thrown when we failed to create an instance
 */
public class InstantiationFailedException extends Exception {
	private static final long serialVersionUID = -5486454117227123011L;

	public InstantiationFailedException(String mess) {
		super(mess);
	}

	public InstantiationFailedException(String mess, Exception err) {
		super(mess, err);
	}
}
