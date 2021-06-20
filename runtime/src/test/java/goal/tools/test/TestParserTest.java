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
package goal.tools.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Test;

public class TestParserTest extends AbstractTest {
	@Test
	public void testIncorrectMissingMasFile() {
		try {
			setup("src/test/resources/goal/tools/test/incorrectMissingMasFile.test2g");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		assertFalse(this.registry.getAllErrors().isEmpty());
		assertEquals(2, this.registry.getAllErrors().size());
		assertFalse(this.registry.getWarnings().isEmpty());
		assertEquals(1, this.registry.getWarnings().size());
	}

	@Test
	public void testIncorrectInvalidMasFile() {
		try {
			setup("src/test/resources/goal/tools/test/incorrectInvalidMasFile.test2g");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		assertFalse(this.registry.getAllErrors().isEmpty());
		assertEquals(1, this.registry.getAllErrors().size());
		assertFalse(this.registry.getWarnings().isEmpty());
		assertEquals(1, this.registry.getWarnings().size());
	}

	@Test
	public void testIncorrectNonExistingModule() {
		try {
			setup("src/test/resources/goal/tools/test/incorrectNonExistingModule.test2g");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		assertFalse(this.registry.getAllErrors().isEmpty());
		assertEquals(1, this.registry.getAllErrors().size());
		assertFalse(this.registry.getWarnings().isEmpty());
		assertEquals(3, this.registry.getWarnings().size());
	}

	@Test
	public void testIncorrectWrongNumberOfParameters() {
		try {
			setup("src/test/resources/goal/tools/test/incorrectWrongNumberOfParameters.test2g");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		assertFalse(this.registry.getAllErrors().isEmpty());
		assertEquals(1, this.registry.getAllErrors().size());
		assertFalse(this.registry.getWarnings().isEmpty());
		assertEquals(4, this.registry.getWarnings().size());
	}

	@Test
	public void testIncorrectKRLangIncorrect() {
		try {
			setup("src/test/resources/goal/tools/test/incorrectKRLangIncorrect.test2g");
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		assertFalse(this.registry.getAllErrors().isEmpty());
		assertEquals(8, this.registry.getAllErrors().size());
		assertFalse(this.registry.getWarnings().isEmpty());
		assertEquals(3, this.registry.getWarnings().size());
	}
}
