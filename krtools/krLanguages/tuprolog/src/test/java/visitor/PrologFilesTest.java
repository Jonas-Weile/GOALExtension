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

package visitor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.util.Arrays;
import java.util.Collection;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;

import tuprolog.parser.Parser4;
import tuprolog.visitor.Visitor4;

/**
 * Tests for {@link Visitor4}, reading in files with prolog "programs". This is
 * a big 'smoke test': we just see if the parser can read all these files. But
 * we do not check if the outcome is all right.
 */
@RunWith(Parameterized.class)
public class PrologFilesTest {

	/**
	 * Parameters are the filenames of the files to test
	 *
	 * @return
	 */
	@Parameters
	public static Collection<String> data() {
		return Arrays.asList(
				new String[] { "/prolog/test.pl", "/prolog/test-1.pl", "/prolog/test-2.pl", "/prolog/test-3.pl" });
	}

	private final Reader stream;

	public PrologFilesTest(String filename) throws Exception {
		URL url = getClass().getResource(filename);
		this.stream = new BufferedReader(new InputStreamReader(url.openStream()));
		System.out.println("running test with file " + url);
	}

	@Test
	public void readFile() throws Exception {
		Visitor4 visitor = new Visitor4(new Parser4(this.stream, null));
		visitor.visitPrologtext();
	}
}
