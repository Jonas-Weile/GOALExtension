/*****************************************************************************
 * This file is part of the Prolog Development Tools (ProDT)
 *
 * Author: Claudio Cancinos
 * WWW: https://sourceforge.net/projects/prodevtools
 * Copyright (C): 2008, Claudio Cancinos
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; If not, see <http://www.gnu.org/licenses/>
 ****************************************************************************/
package ar.com.tadp.prolog.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Vector;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Plugin;

/**
 * @author ccancino
 *
 */
public class PrologSyntax {
	private static String swiPredFilename = "syntax" + File.separator + "swiPredicates.txt";
	private static String swiOperFilename = "syntax" + File.separator + "swiOperators.txt";

	private static String[] swiKeywords;
	private static String[] swiOperators;

	private static String[] getDefaultKeywords() {
		if (swiKeywords == null) {
			swiKeywords = loadArray(swiPredFilename);
		}
		return swiKeywords;
	}

	private static String[] getDefaultOperators() {
		if (swiOperators == null) {
			swiOperators = loadArray(swiOperFilename);
		}
		return swiOperators;
	}

	public static String[] getKeywords() {
		return getDefaultKeywords();
	}

	public static String[] getOperators() {
		return getDefaultOperators();
	}

	private static String[] loadArray(final String predFilename) {
		final Vector<String> tmp = new Vector<String>();
		loadFromFile(tmp, predFilename);
		final String[] keywords = new String[tmp.size()];
		tmp.toArray(keywords);
		return keywords;
	}

	/**
	 * Populates the vector 'v' with the keywords inside 'filename'. 'filename'
	 * must be a relative path from the plugin directory instalation.
	 *
	 * @param v
	 *            'Vector' to be populated with the constants in 'filename'.
	 *
	 * @param filename
	 *            Path and filename where we can find the constants to be
	 *            recognized.
	 *
	 */
	private static void loadFromFile(final Vector<String> v, final String filename) {
		String keyword;

		try {
			final Path path = new Path(filename);
			final Plugin plugin = PrologCorePlugin.getDefault();

			InputStream inFile = null;
			try {
				inFile = FileLocator.openStream(plugin.getBundle(), path, false);
			} catch (final IOException ioe) {
				throw (new IOException("Error opening file: " + filename + "."));
			}

			final BufferedReader in = new BufferedReader(new InputStreamReader(inFile));

			while ((keyword = in.readLine()) != null) {
				v.add(keyword.trim());
			}
			in.close();
			inFile.close();
		} catch (final IOException e) {
			logException(e);
		} catch (final Exception e2) {
			logException(e2);
		}
	}

	private static void logException(final Exception e) {
		PrologCorePlugin.log("Problems doing lexical analysis: " + e.getMessage());
	}
}
