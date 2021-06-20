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
package ar.com.tadp.prolog.core.compiler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;

import org.eclipse.dltk.core.DLTKCore;

/**
 * @author jfernandes
 */
public class StreamUtils {

	public static String[] readTokens(final InputStream inputStream) {
		final Set<String> list = readTokensAsSet(inputStream);
		final String[] result = new String[list.size()];
		list.toArray(result);
		return result;
	}

	public static Set<String> readTokensAsSet(final InputStream inputStream) {
		final Set<String> result = new TreeSet<String>();
		try (final BufferedReader br = new BufferedReader(new InputStreamReader(inputStream))) {
			String strLine = "";
			while ((strLine = br.readLine()) != null) {
				if (strLine != null && !"".equals(strLine) && !strLine.startsWith("Na")) {
					final StringTokenizer tokenizer = new StringTokenizer(strLine.trim(), " ");
					while (tokenizer.hasMoreTokens()) {
						result.add(tokenizer.nextToken());
					}
				}
			}
			return result;
		} catch (final IOException e) {
			throw new RuntimeException("Error while reading input stream tokens", e);
		} finally {
			try {
				inputStream.close();
			} catch (final IOException ignore) {
			}
		}
	}

	public static List<String> readLines(final InputStream is) {
		final List<String> lines = new ArrayList<String>();
		try (final BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
			String line;
			while ((line = reader.readLine()) != null) {
				lines.add(line);
			}
			return lines;
		} catch (final IOException e) {
			throw new RuntimeException("Error while reading input stream lines", e);
		} finally {
			try {
				is.close();
			} catch (final IOException ignore) {
			}
		}
	}

	public static String streamToString(final InputStream is) {
		
		final StringBuilder sb = new StringBuilder();
		String line = null;
		try (final BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
			while ((line = reader.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (final IOException e) {
			DLTKCore.error(e);
		} finally {
			try {
				is.close();
			} catch (final IOException ignore) {
			}
		}
		return sb.toString();
	}

}
