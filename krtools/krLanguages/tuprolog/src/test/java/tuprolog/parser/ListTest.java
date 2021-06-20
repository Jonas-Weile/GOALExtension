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

package tuprolog.parser;

import static org.junit.Assert.assertEquals;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import tuprolog.parser.Prolog4Parser.ListtermContext;

/**
 * Tests for Prolog4Parser term0
 */
public class ListTest {
	/**
	 * Creates parser to parse given text.
	 *
	 * @return The {@link Parser4} for the text.
	 */
	private Parser4 getParser(String text) throws Exception {
		return new Parser4(new StringReader(text), null);
	}

	private void checkParsesAsList(String... items) throws Exception {
		String text = "[" + list2String(",", items) + "]";
		Parser4 parser = getParser(text);
		ListtermContext tree = parser.listterm();
		System.out.println(text + " -> " + parser.toStringTree(tree));
		assertEquals(parsedList2String(items), parser.toStringTree(tree));
	}

	/**
	 * Turn list of items into a string, using given separator.
	 *
	 * @param separator
	 *            separator to use.
	 * @param items
	 *            items for in the list.
	 */
	private String list2String(String separator, String... items) {
		String listtext = "";
		for (String item : items) {
			if (!listtext.isEmpty()) {
				listtext += separator;
			}
			listtext += item;
		}
		return listtext;
	}

	/**
	 * We should mock this, as now it is really testing too much.
	 *
	 * @param term
	 * @return
	 */
	private String item2String(String term) {
		return "(expression (term900 (term700 (term500 (term400 (term200 (term100 (term50 (term0 " + term + ")))))))))";
	}

	/**
	 * convert a set of items to the expected expression
	 *
	 * @param items
	 *            items that have been put in list and parsed.
	 */
	private String parsedList2String(String... items) {
		String itemstring = "";
		if (items.length > 0) {
			List<String> array = new ArrayList<>(items.length);
			for (String item : items) {
				array.add(item);
			}
			itemstring = items2String(array) + " ";
		}
		return "(listterm [ " + itemstring + "])";
	}

	private String items2String(List<String> items) {
		if (items.size() == 1) {
			return "(items " + item2String(items.get(0)) + ")";
		} else {
			return "(items " + item2String(items.get(0)) + " , " + items2String(items.subList(1, items.size())) + ")";
		}
	}

	@Test
	public void testLisEmptyList() throws Exception {
		checkParsesAsList();
	}

	@Test
	public void testLista() throws Exception {
		checkParsesAsList("a");
	}

	@Test
	public void testListab() throws Exception {
		checkParsesAsList("a", "b");
	}

	@Test
	public void testListabc() throws Exception {
		checkParsesAsList("a", "b", "c");
	}

	// TODO also test lists containing lists and the BAR notation eg [1,2|3]
}
