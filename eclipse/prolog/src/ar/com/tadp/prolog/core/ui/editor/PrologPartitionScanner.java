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
package ar.com.tadp.prolog.core.ui.editor;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IPredicateRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.RuleBasedPartitionScanner;
import org.eclipse.jface.text.rules.Token;

import ar.com.tadp.prolog.core.ui.editor.text.PrologCodeScanner;

/**
 * This object is in charge to define partition's boundaries in the document.
 * Its the one who says "this is single line comment", "this is code",
 * "this is multiple line comment", etc...
 *
 * @see , {@link PrologCodeScanner}, {@link PrologSourceViewerConfiguration}
 * @author ccancino
 */
public class PrologPartitionScanner extends RuleBasedPartitionScanner {
	public PrologPartitionScanner() {
		final IToken string = new Token(PrologPartitions.PROLOG_STRING);
		final IToken comment = new Token(PrologPartitions.PROLOG_COMMENT);

		final List<IRule> rules = new LinkedList<IRule>();

		// Rules for comments
		rules.add(new EndOfLineRule("%", comment));
		rules.add(new MultiLineRule("/*", "*/", comment));

		// Rule for constants between single quotes.
		rules.add(new MultiLineRule("\'", "\'", string, '\\'));
		// Rule for strings.
		rules.add(new MultiLineRule("\"", "\"", string, '\\'));

		final IPredicateRule[] result = new IPredicateRule[rules.size()];
		rules.toArray(result);
		setPredicateRules(result);
	}
}
