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
package ar.com.tadp.prolog.core.parser;

import org.eclipse.dltk.ast.Modifiers;
import org.eclipse.dltk.ast.declarations.FieldDeclaration;
import org.eclipse.dltk.ast.declarations.MethodDeclaration;
import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
import org.eclipse.dltk.ast.declarations.TypeDeclaration;
import org.eclipse.dltk.ast.parser.AbstractSourceParser;
import org.eclipse.dltk.ast.parser.IModuleDeclaration;
import org.eclipse.dltk.ast.statements.Block;
import org.eclipse.dltk.compiler.env.IModuleSource;
import org.eclipse.dltk.compiler.problem.IProblemReporter;

/**
 * @author ccancino
 *
 */
public class PrologSourceParser extends AbstractSourceParser {
	private static String currentLine = null;
	private static boolean commentStarted = false;
	private static boolean implementation = false;

	@Override
	public IModuleDeclaration parse(final IModuleSource input, final IProblemReporter reporter) {
		// TODO Create a ModuleDeclaration containing instances of
		// org.eclipse.dltk.ast.ASTNode
		// return null;
		final String sourceContent = input.getSourceContents();
		String typeName = input.getFileName();
		final ModuleDeclaration module = new ModuleDeclaration(sourceContent.length());
		typeName = typeName.substring(typeName.lastIndexOf("/") + 1, typeName.lastIndexOf("."));
		final TypeDeclaration type = new TypeDeclaration(typeName, 0, 0, 0, 0);
		type.setBody(new Block());
		module.addStatement(type);
		parseProlog(new String(sourceContent), type);
		return module;
	}

	private void parseProlog(final String input, final TypeDeclaration type) {
		String comment = "";
		int offset = 0;
		int lineLenght = 0;
		int cursorPosition = 0;
		final int inputSize = input.length();

		while (cursorPosition < inputSize) {
			currentLine = getLineFrom(cursorPosition, input);
			cursorPosition = cursorPosition + currentLine.length();
			lineLenght = currentLine.length();
			currentLine = currentLine.trim();

			if (isSingleLineComment(currentLine) || isMultiLineComment()) {
				comment += getCleanCommentLine(currentLine);
			}
			if (currentLine.startsWith("/*")) {
				commentStarted = true;
				comment = getCleanCommentLine(currentLine);
			}
			if (currentLine.endsWith("*/") && isComment(currentLine)) {
				commentStarted = false;
			}

			if (!currentLine.isEmpty() && Character.isJavaIdentifierStart(currentLine.charAt(0))) {
				if (isGramaticalRule(currentLine)) {
					addGramaticalRule(type, currentLine, comment, offset);
					updateImplementationState(currentLine);
					comment = "";
				}
				if (isRule(currentLine)) {
					if (!implementation) {
						addRule(type, currentLine, comment, offset);
						comment = "";
					}
					updateImplementationState(currentLine);
				}
				if (isFact(currentLine)) {
					if (!implementation) {
						addFact(type, currentLine, comment, offset);
						comment = "";
					}
					updateImplementationState(currentLine);
				}
			}
			if (isDirective(currentLine)) {
				addDirective(type, currentLine, comment, offset);
				updateImplementationState(currentLine);
				comment = "";
			}

			offset += lineLenght;
		}
	}

	private static void addFact(final TypeDeclaration type, final String predicate, final String comment,
			final int offset) {
		final int end = offset + predicate.length();
		final FieldDeclaration field = new FieldDeclaration(predicate, offset, end, offset, end);
		field.setModifier(Modifiers.AccPublic);
		type.getBody().addStatement(field);
	}

	private static void addRule(final TypeDeclaration type, final String predicate, final String comment,
			final int offset) {
		final int end = offset + predicate.length();
		type.getBody().addStatement(new MethodDeclaration(predicate, offset, end, offset, end));
	}

	private static void addGramaticalRule(final TypeDeclaration type, final String predicate, final String comment,
			final int offset) {
	}

	private static void addDirective(final TypeDeclaration type, final String directive, final String comment,
			final int offset) {
	}

	private static String getLineFrom(final int cursorPosition, final String input) {
		int i = cursorPosition;
		while (i < input.length() && input.charAt(i) != '\n') {
			i++;
		}
		while (i < input.length() && input.charAt(i) == '\n') {
			i++;
		}
		return input.substring(cursorPosition, i);
	}

	private static String getCleanCommentLine(final String line) {
		int i = 0;
		while (i < line.length() && (line.charAt(i) == '%' || line.charAt(i) == '/' || line.charAt(i) == '*')) {
			i++;
		}

		String comment = line.substring(i).trim();

		if (comment.endsWith("*/")) {
			comment = comment.substring(0, comment.indexOf("*/"));
		}

		if (comment.length() > 0) {
			comment += "\n";
		}
		return comment;
	}

	private static boolean isDirective(final String line) {
		return line.startsWith(":-");
	}

	private static boolean isFact(final String line) {
		return !line.contains(":-") && line.endsWith(".") && !isComment(line);
	}

	private static boolean isGramaticalRule(final String line) {
		return line.contains("-->") && !implementation && !isComment(line) && !isRule(line) && !isFact(line);
	}

	private static boolean isRule(final String line) {
		return !line.startsWith(":-") && line.contains(":-") && !isComment(line);
	}

	private static boolean isComment(final String line) {
		return isMultiLineComment() || isSingleLineComment(line);
	}

	private static boolean isMultiLineComment() {
		return commentStarted;
	}

	private static boolean isSingleLineComment(final String line) {
		return line.startsWith("%");
	}

	private static void updateImplementationState(final String line) {
		if (!line.endsWith(".")) {
			implementation = true;
		} else {
			implementation = false;
		}
	}

}
