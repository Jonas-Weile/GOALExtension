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
package goal.util;

import java.util.Map;
import java.util.NoSuchElementException;

/**
 * Simple util for some map support functions. Maybe we can use a better library
 * for this.
 */
public class MapUtils {
	/**
	 * Find the first key in the map that maps to the given value. This is a simple
	 * implementation using a for loop. Should be used only where performance is not
	 * critical, or where the map is small.
	 *
	 * @param <K>
	 *            the type of the keys in the map.
	 * @param <V>
	 *            the type of values in the map
	 * @param map
	 *            the map to search
	 * @param value
	 *            the value that is looked for.
	 * @return the first key in the map that maps to given value.
	 * @throws NoSuchElementException
	 *             if no such value in the map.
	 */
	public static <K, V> K keyFromValue(Map<K, V> map, V value) {
		for (K key : map.keySet()) {
			if (map.get(key) == value) {
				return key;
			}
		}
		throw new NoSuchElementException("map '" + map + "' does not contain value '" + value + "'.");
	}
}
