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
package goal.tools;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import languageTools.analyzer.FileRegistry;
import languageTools.analyzer.mas.MASValidator;
import languageTools.program.mas.MASProgram;
import languageTools.utils.Extension;

public class MASTest {
	@Test
	public void testLoadMASFiles_File() throws Exception {
		String filename = "src/test/resources/goal/agents/fibonaci.mas2g";
		FileRegistry registry = new FileRegistry();
		MASValidator validator = new MASValidator(filename, registry);
		validator.validate();
		MASProgram mas = validator.getProgram();

		assertTrue(mas != null);
		assertEquals(new File(filename).getCanonicalPath(), mas.getSourceFile().getCanonicalPath());
	}

	@Test
	public void testLoadMASFiles_Directory() throws Exception {
		File folder = new File("src/test/resources/goal/agents/");
		List<MASProgram> masFileRegistries = new LinkedList<>();
		for (final File mas2g : Run.getMASFiles(folder, false)) {
			FileRegistry registry = new FileRegistry();
			MASValidator validator = new MASValidator(mas2g.getCanonicalPath(), registry);
			validator.validate();
			final MASProgram mas = validator.getProgram();
			if (mas != null) {
				masFileRegistries.add(mas);
			}
		}

		assertEquals(3, masFileRegistries.size());
	}

	@Test
	public void testCreateNewPL() throws Exception {
		File newFile = File.createTempFile("test", "pl");

		createfile(newFile, Extension.PL);
		assertTrue(newFile.exists());
	}

	@Test
	public void testCreateNewMAS() throws Exception {
		File newFile = File.createTempFile("test", "mas2g");

		createfile(newFile, Extension.MAS2G);
		assertTrue(newFile.exists());
	}

	@Test
	public void testCreateNewModule() throws Exception {
		File newFile = File.createTempFile("test", "mod2g");

		createfile(newFile, Extension.MOD2G);
		assertTrue(newFile.exists());
	}

	/**
	 * Creates a new file and inserts a corresponding template into the file for a
	 * given extension. Overwrites already existing file.
	 *
	 * @param newFile   The file to be created.
	 * @param extension The {@link Extension} the file to be created should have.
	 *                  The extension is also used to select and apply the
	 *                  corresponding template to insert initial content in the
	 *                  file.
	 *
	 * @return The newly created or emptied file (CHECK which is just the same file
	 *         as given as first param?).
	 */
	private File createfile(File newFile, Extension extension) throws Exception {
		/**
		 * do not use f.createNewFile() because that does not overwrite existing files.
		 */

		// make sure the folder to write the file to exists.
		newFile.getParentFile().mkdirs();

		// Copy template if we have an extension.
		if (extension != null) {
			try (FileOutputStream outFile = new FileOutputStream(newFile)) {
				String templatename = "template" //$NON-NLS-1$
						+ extension.toString().toLowerCase();
				try (InputStream template = ClassLoader.getSystemClassLoader()
						.getResourceAsStream("goal/tools/SimpleIDE/files/" + templatename)) { //$NON-NLS-1$
					if (template != null) {
						IOUtils.copy(template, outFile);
					}
				}
			}
		}

		return newFile;
	}
}
