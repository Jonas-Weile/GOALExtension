/**
 * The GOAL Grammar Tools. Copyright (C) 2014 Koen Hindriks.
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

package languageTools.utils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.filefilter.WildcardFileFilter;

/**
 * Class with some methods for resolving references to files.
 */
public class ReferenceResolver {

	/**
	 * Transforms reference of form 'reference.extension' into relative path to
	 * filename.
	 *
	 * Resolving steps are as follows
	 * <ol>
	 * <li>Extension is checked/added to the reference
	 * <li>If reference (in current directory or as absolute path) exists, it is
	 * used
	 * <li>if not, it is prepended with relativePath and that is used.
	 * <li>if that also does not exist, null is returned
	 * </ol>
	 *
	 * @param reference    Reference that can already have the provided extension
	 * @param extension    Filename extension, or null if it can be ignored. If not
	 *                     null, and the reference has no extension yet, it is
	 *                     added. If the reference has already an extension, it is
	 *                     checked to match with this
	 * @param relativePath a path to a directory. If the reference file can not be
	 *                     found in the current directory nor as absolute path, it
	 *                     is prepended with relativePath for a second attempt at
	 *                     resolving.
	 * @return File if reference could be resolved, {@code null} otherwise.
	 */
	public static List<File> resolveReference(String reference, Extension extension, String relativePath) {
		File path = getPath(reference, relativePath);
		String[] listing = getListing(path);

		List<File> files = new LinkedList<>();
		if (listing != null) {
			String parent = path.getParent();
			for (String found : listing) {
				Extension ext = Extension.getFileExtension(found);
				if (extension == null || (ext != null && ext.equals(extension))) {
					files.add(new File(parent, found));
				}
			}
		}
		return files;
	}

	/**
	 * Resolves references to KR files. Only returns files with known KR extensions.
	 *
	 * @param reference Reference of the form 'id(.id)*'
	 * @return List of files that match reference.
	 */
	public static List<File> resolveKRReference(String reference, String relativePath) {
		File path = getPath(reference, relativePath);
		String[] listing = getListing(path);

		List<File> files = new LinkedList<>();
		if (listing != null) { // Filter for known KR files only.
			String parent = path.getParent();
			for (String found : listing) {
				Extension ext = Extension.getFileExtension(found);
				if (ext != null && ext.isKR()) {
					files.add(new File(parent, found));
				}
			}
		}
		return files;
	}

	private static File getPath(String reference, String relativePath) {
		File path = new File(reference);
		if (path.isFile() && path.exists()) {
			try {
				path = path.getCanonicalFile();
			} catch (IOException ignore) {
			}
		} else {
			path = new File(relativePath, reference);
		}
		return path;
	}

	private static String[] getListing(File path) {
		String filter = FilenameUtils.getBaseName(path.getPath()) + FilenameUtils.EXTENSION_SEPARATOR + "*";
		return (path.getParentFile() == null) ? null : path.getParentFile().list(new WildcardFileFilter(filter));
	}
}
