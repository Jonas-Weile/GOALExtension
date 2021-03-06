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

import java.util.ResourceBundle;

/**
 * This is a set of Ids that can be converted into (String) objects throught the
 * {@link Resources#get(ResourceId, String...)} call.
 *
 * You should make an enum, and then implement this. The object that holds the
 * actual resources is a properties file.
 *
 * The intention of this code is to make {@link ResourceBundle}s strongly
 * typechecked. Instead of the java {@link ResourceBundle}s, we make an enum
 * that has the keys in the resource on the java level. The values are then to
 * be fetched from a properties file
 *
 * These properties files can be generated with the Eclipse "Source/Externalize
 * String" menu option. But the resulting java file needs to be changed:
 * <ul>
 * <li>the file needs to be changed from a class into an enum
 * <li>all the keys (text before the '=' signs) available in the properties
 * files need to be made elements of the enumeration
 * <li>the enum needs to implement StringResource, and implement <code>
 *   public ResourceBundle getBundle() { return RESOURCE_BUNDLE; }
 *  </code>
 * <li>all calls to <code>yourjavaclass.getString("YOUR_KEY") () </code>need to
 * be changed into <code>Resources.get(YOUR_KEY)</code>
 *
 * It was judged that the OSGI version of 'Externalize String' in Eclipse
 * (accessible with some hacks, #3081) is not good.
 */
public interface ResourceId {
	public ResourceBundle getBundle();
}
