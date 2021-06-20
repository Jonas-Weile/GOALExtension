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
 * A KR interface not supported exception should be thrown either if the
 * interface does not support some functionality or the interface is not
 * supported at all. See also package krTools.krFactory.getSupportedKRs for the
 * list of supported KRs at present.
 */
public class KRInterfaceNotSupportedException extends KRException {

	private static final long serialVersionUID = 4881505555016007638L;

	/**
	 * Creates a KRInterfaceNotSupportedException with a message
	 *
	 * @param message
	 *            as a string
	 */
	public KRInterfaceNotSupportedException(String message) {
		super(message);
	}

	/**
	 * Creates a KRInterfaceNotSupportedException with a message and a cause
	 *
	 * @param message
	 *            as a string message
	 * @param cause
	 *            as a throwable cause
	 */
	public KRInterfaceNotSupportedException(String message, Throwable cause) {
		super(message, cause);
	}

}
