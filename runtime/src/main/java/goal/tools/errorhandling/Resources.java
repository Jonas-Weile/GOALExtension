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
package goal.tools.errorhandling;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

import goal.tools.errorhandling.exceptions.GOALBug;

/**
 * Util class. Takes an (enumeration) element from a {@link ResourceId}, and
 * finds the resource associated with it.
 */
public class Resources {
	/**
	 * Get the given resource associated with the given resourceId
	 *
	 * @param res
	 * @param args
	 * @return
	 * @throws MissingResourceException
	 */
	public static String get(ResourceId res) throws MissingResourceException {
		ResourceBundle bundle = res.getBundle();
		return bundle.getString(res.toString());
	}

	/**
	 * Check that the given resource is consistent
	 *
	 * @param resources
	 */
	public static void check(Class<? extends Enum<?>> resources) {
		Object[] values = resources.getEnumConstants();
		if (values.length == 0) {
			return; // we can't check empty resources.
		}
		if (!(values[0] instanceof ResourceId)) {
			throw new IllegalArgumentException("value '" + values[0] + "' is not instance of ResourceId.");
		}
		try {
			// check that all enum values are in the resource bundle
			// we iterate so that we can give nice error message if not.
			for (Object v : values) {
				ResourceId val = (ResourceId) v;
				Resources.get(val);
			}
		} catch (MissingResourceException e) {
			throw new GOALBug("missing resource in '" + resources + "'.", e);
		}
	}
}
