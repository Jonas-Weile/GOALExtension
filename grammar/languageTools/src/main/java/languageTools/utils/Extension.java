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

import org.apache.commons.io.FilenameUtils;

import krFactory.KRFactory;

/**
 * Extensions of files recognized by GOAL.
 */
public enum Extension {
	/**
	 * A Multi-Agent System file.
	 */
	MAS2G("mas2g", "GOAL"),
	/**
	 * A module file.
	 */
	MOD2G("mod2g", "GOAL"),
	/**
	 * An action specification file.
	 */
	ACT2G("act2g", "GOAL"),
	/**
	 * An planner specification file.
	 */
	PLAN2G("plan2g", "GOAL"),	
	/**
	 * A test file.
	 */
	TEST2G("test2g", "GOAL"),
	/**
	 * A jar file (used for environment interfaces).
	 */
	JAR("jar", "EIS"),
	/**
	 * A learning file.
	 */
	LEARNING("lrn", "GOAL"),
	/**
	 * A Prolog file.
	 */
	PL("pl", KRFactory.SWI_PROLOG);

	/**
	 * OWL file - all possibilities
	 */
	// OWL("owl", KRFactory.OWL_REPO), RDF("rdf", KRFactory.OWL_REPO), N3("n3",
	// KRFactory.OWL_REPO), TTL("ttl",
	// KRFactory.OWL_REPO), NQ("nq", KRFactory.OWL_REPO), JSONLD("jsonld",
	// KRFactory.OWL_REPO), SWRL("swrl",
	// KRFactory.OWL_REPO),

	/** Jason prolog */
	// JSN("jsn", KRFactory.JASON);

	/**
	 * File extension.
	 */
	private String extension;
	/**
	 * Type of file (either a GOAL, EIS, or specific type of KR file).
	 */
	private String type;

	/**
	 *
	 * @param extension
	 * @param type
	 */
	private Extension(String extension, String type) {
		this.extension = extension;
		this.type = type;
	}

	public String getExtension() {
		return this.extension;
	}

	/**
	 * @return The type of file (either GOAL, EIS, or a specific type of KR
	 *         file).
	 */
	public String getType() {
		return this.type;
	}

	/**
	 * Determines the {@link Extension} of a certain file.
	 *
	 * @param filename
	 *            The name or path to a file to get the extension of
	 * @return The {@link Extension} of the given file, or <code>null</code> if
	 *         the file has no known extension.
	 */
	public static Extension getFileExtension(String filename) {
		try {
			return Extension.valueOf(FilenameUtils.getExtension(filename).toUpperCase());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	/**
	 * Determines the extension of a certain file.
	 *
	 * @param file
	 *            The file to get the extension of.
	 * @return The {@link Extension} of the given file, or <code>null</code> if
	 *         the file has no known extension.
	 */
	public static Extension getFileExtension(File file) {
		return Extension.getFileExtension(file.getName());
	}

	public boolean isKR() {
		return this == PL;// || this == OWL || this == RDF || this == N3 || this
							// == TTL || this == NQ || this == JSONLD
		// || this == SWRL || this == JSN;
	}

	@Override
	public String toString() {
		return getExtension();
	}
}
