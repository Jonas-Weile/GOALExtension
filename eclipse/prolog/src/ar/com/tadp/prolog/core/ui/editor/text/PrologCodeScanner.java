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
package ar.com.tadp.prolog.core.ui.editor.text;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.dltk.ui.text.AbstractScriptScanner;
import org.eclipse.dltk.ui.text.IColorManager;
import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.css.swt.theme.IThemeManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.EndOfLineRule;
import org.eclipse.jface.text.rules.IRule;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.MultiLineRule;
import org.eclipse.jface.text.rules.SingleLineRule;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.jface.text.rules.WhitespaceRule;
import org.eclipse.jface.text.rules.WordRule;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

import ar.com.tadp.prolog.core.PrologCorePlugin;
import ar.com.tadp.prolog.core.PrologSyntax;
import ar.com.tadp.prolog.core.ui.editor.PrologPartitionScanner;
import ar.com.tadp.prolog.core.ui.editor.PrologSourceViewerConfiguration;
import ar.com.tadp.prolog.core.ui.editor.PrologVarDetector;
import ar.com.tadp.prolog.core.ui.editor.text.detectors.ListWordsRule;
import ar.com.tadp.prolog.core.ui.editor.text.detectors.PrologWhitespaceDetector;
import ar.com.tadp.prolog.core.ui.editor.text.detectors.PrologWordDetector;

/**
 * This scanner will be executed on a single partition of the document
 * (partitions are defined by {@link PrologPartitionScanner}) Inside this
 * partition, it will define which color will be used for each word based on
 * rules defining coloring area usually by defining starting and ending points.
 *
 * @see {@link PrologSourceViewerConfiguration}, {@link PrologPartitionScanner}
 * @author ccancino
 */
@SuppressWarnings("restriction")
public class PrologCodeScanner extends AbstractScriptScanner {
	private static String tokenProperties[] = new String[] { PrologColorConstants.PROLOG_DEFAULT,
			PrologColorConstants.PROLOG_KEYWORD, PrologColorConstants.PROLOG_OPERATOR,
			PrologColorConstants.PROLOG_VARIABLE, PrologColorConstants.PROLOG_STRING,
			PrologColorConstants.PROLOG_COMMENT };
	private final boolean dark;

	public PrologCodeScanner(final IColorManager manager, final IPreferenceStore store) {
		super(manager, store);
		this.dark = getTheme().getLabel().equals("Dark");
		initialize();
	}

	private ITheme getTheme() {
		final BundleContext context = FrameworkUtil.getBundle(PrologCorePlugin.class).getBundleContext();
		final ServiceReference<IThemeManager> ref = context.getServiceReference(IThemeManager.class);
		final IThemeEngine engine = context.getService(ref).getEngineForDisplay(Display.getCurrent());
		return engine.getActiveTheme();
	}

	@Override
	protected String[] getTokenProperties() {
		return tokenProperties;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	protected List createRules() {
		final List<IRule> rules = new LinkedList<>();
		final IToken operator = getToken(PrologColorConstants.PROLOG_OPERATOR);
		final IToken keyword = getToken(PrologColorConstants.PROLOG_KEYWORD);
		final IToken variable = getToken(PrologColorConstants.PROLOG_VARIABLE);
		final IToken string = getToken(PrologColorConstants.PROLOG_STRING);
		final IToken comment = getToken(PrologColorConstants.PROLOG_COMMENT);
		final IToken other = getToken(PrologColorConstants.PROLOG_DEFAULT);

		// Add generic whitespace rule.
		rules.add(new WhitespaceRule(new PrologWhitespaceDetector()));

		// Rules for comments
		rules.add(new EndOfLineRule("%", comment));
		rules.add(new MultiLineRule("/*", "*/", comment));

		// Add a rule for quotes
		rules.add(new SingleLineRule("\"", "\"", string, '\\'));
		rules.add(new SingleLineRule("'", "'", string, '\\'));

		// Rule for variables.
		rules.add(new WordRule(new PrologVarDetector(), variable));

		// Add word rule for Operators.
		rules.add(new ListWordsRule(new PrologOperatorDetector(), PrologSyntax.getOperators(), operator, other));

		// Add word rule for keywords.
		rules.add(new ListWordsRule(new PrologWordDetector(), PrologSyntax.getKeywords(), keyword, other));

		setDefaultReturnToken(other);

		return rules;
	}

	@Override
	public Token getToken(String key) {
		Token token = super.getToken(key);
		TextAttribute source = (TextAttribute) token.getData();
		if (this.dark && source != null) {
			Color original = source.getForeground();
			Color inverse = new Color(original.getDevice(), 255 - original.getRed(), 255 - original.getGreen(),
					255 - original.getBlue(), original.getAlpha());
			TextAttribute replace = new TextAttribute(inverse, source.getBackground(), source.getStyle());
			token.setData(replace);
		}
		return token;
	}
}
