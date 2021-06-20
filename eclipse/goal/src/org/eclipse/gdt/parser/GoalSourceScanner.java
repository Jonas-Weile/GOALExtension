package org.eclipse.gdt.parser;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.Recognizer;
import org.eclipse.core.runtime.IPath;
import org.eclipse.dltk.ui.PreferenceConstants;
import org.eclipse.dltk.ui.text.AbstractScriptScanner;
import org.eclipse.dltk.ui.text.IColorManager;
import org.eclipse.e4.ui.css.swt.theme.ITheme;
import org.eclipse.e4.ui.css.swt.theme.IThemeEngine;
import org.eclipse.e4.ui.css.swt.theme.IThemeManager;
import org.eclipse.gdt.Activator;
import org.eclipse.gdt.lexer.GoalSourceLexer;
import org.eclipse.gdt.lexer.ISourceLexer;
import org.eclipse.gdt.lexer.MASSourceLexer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferenceConverter;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextAttribute;
import org.eclipse.jface.text.rules.IToken;
import org.eclipse.jface.text.rules.ITokenScanner;
import org.eclipse.jface.text.rules.Token;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.texteditor.ITextEditor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;

@SuppressWarnings("restriction")
public class GoalSourceScanner implements ITokenScanner {
	private final static ISourceLexer[] lexers = new ISourceLexer[] { new GoalSourceLexer(), new MASSourceLexer() };
	private final IColorManager colors;
	private final IPreferenceStore prefs;
	private final ITextEditor editor;
	private final boolean dark;
	private IPath input;
	private ISourceLexer lexer;
	private CommonToken current, saved;
	private AbstractScriptScanner external;

	public GoalSourceScanner(final IColorManager colors, final IPreferenceStore prefs, final ITextEditor editor) {
		this.colors = colors;
		this.prefs = prefs;
		this.editor = editor;
		this.dark = (getTheme() != null) && getTheme().getLabel().equals("Dark");
	}

	private ITheme getTheme() {
		final BundleContext context = FrameworkUtil.getBundle(Activator.class).getBundleContext();
		final ServiceReference<IThemeManager> ref = context.getServiceReference(IThemeManager.class);
		final IThemeEngine engine = context.getService(ref).getEngineForDisplay(Display.getCurrent());
		return engine.getActiveTheme();
	}

	@Override
	public void setRange(final IDocument document, final int offset, final int length) {
		if (this.editor != null && this.editor.getEditorInput() instanceof FileEditorInput) {
			String name = "", extension = "";
			try {
				this.input = ((FileEditorInput) this.editor.getEditorInput()).getPath();
				name = this.input.toOSString();
				extension = this.input.getFileExtension();
			} catch (final Exception ignore) {
			}
			for (final ISourceLexer lexer : lexers) {
				if (lexer.getApplicableExtensions().contains(extension)) {
					final CharStream stream = CharStreams.fromString(document.get(), name);
					lexer.createLexer(stream);
					this.lexer = lexer;
					break;
				}
			}
		}
	}

	private CommonToken getNextLexerToken() {
		try {
			return (CommonToken) this.lexer.getLexer().nextToken();
		} catch (Exception e) {
			return new CommonToken(org.antlr.v4.runtime.Token.EOF);
		}
	}

	@Override
	public IToken nextToken() {
		IToken returned = Token.EOF;
		if (this.lexer == null) {
			return returned;
		}

		boolean skip = false;
		if (this.external != null) {
			returned = this.external.nextToken();
			if (returned.isEOF()) {
				skip = true;
				this.external = null;
				this.saved = null;
			} else {
				return returned;
			}
		}

		final CommonToken token = skip ? this.current : getNextLexerToken();
		this.current = token;
		if (this.lexer.getSubTokens().contains(token.getType())) {
			this.saved = this.current;
			final StringBuilder text = new StringBuilder(token.getText());
			while (true) {
				final CommonToken nexttoken = getNextLexerToken();
				this.current = nexttoken;
				if (this.lexer.getSubTokens().contains(nexttoken.getType())) {
					text.append(nexttoken.getText());
				} else {
					break;
				}
			}
			this.external = this.lexer.getSubScanner(this.input, this.colors);
			if (this.external != null) {
				final Document sub = new Document(text.toString());
				this.external.setRange(sub, 0, sub.getLength());
				return nextToken();
			}
		}
		return new IToken() {
			@Override
			public boolean isWhitespace() {
				return token.getType() == GoalSourceScanner.this.lexer.getWhitespaceToken();
			}

			@Override
			public boolean isUndefined() {
				return false;
			}

			@Override
			public boolean isOther() {
				return !isWhitespace() && !isEOF();
			}

			@Override
			public boolean isEOF() {
				return token.getType() == Recognizer.EOF;
			}

			@Override
			public Object getData() {
				final String constant = GoalSourceScanner.this.lexer.getColor(token.getType());
				return getTextAttribute(constant);
			}
		};
	}

	private TextAttribute getTextAttribute(final String constant) {
		Color color = this.colors.getColor(PreferenceConverter.getColor(this.prefs, constant));
		if (this.dark) {
			color = new Color(color.getDevice(), 255 - color.getRed(), 255 - color.getGreen(), 255 - color.getBlue(),
					color.getAlpha());
		}
		final boolean bold = this.prefs.getBoolean(constant + PreferenceConstants.EDITOR_BOLD_SUFFIX);
		final boolean italic = this.prefs.getBoolean(constant + PreferenceConstants.EDITOR_ITALIC_SUFFIX);
		int style = SWT.NORMAL;
		if (bold && italic) {
			style = SWT.BOLD | SWT.ITALIC;
		} else if (bold) {
			style = SWT.BOLD;
		} else if (italic) {
			style = SWT.ITALIC;
		}
		return new TextAttribute(color, null, style);
	}

	@Override
	public int getTokenOffset() {
		if (this.external != null) {
			return this.saved.getStartIndex() + this.external.getTokenOffset();
		} else if (this.current != null) {
			return this.current.getStartIndex();
		} else {
			return 0;
		}
	}

	@Override
	public int getTokenLength() {
		if (this.external != null) {
			return this.external.getTokenLength();
		} else if (this.current != null) {
			return this.current.getStopIndex() + 1 - this.current.getStartIndex();
		} else {
			return 0;
		}
	}
}