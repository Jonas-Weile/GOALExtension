package jasonkri.language;

import jason.asSyntax.VarTerm;
import jasonkri.JasonSourceInfo;
import krTools.language.Substitution;
import krTools.language.Term;
import krTools.parser.SourceInfo;

public class JasonTerm extends JasonExpression implements Term {

	/**
	 * Only use if this is a plain term, not some variable. See also
	 * {@link #makeTerm(jason.asSyntax.Term, JasonSourceInfo)}
	 * 
	 * @param s
	 * @param i
	 */
	public JasonTerm(jason.asSyntax.Term s, SourceInfo i) {
		super(s, i);
		if (!(this instanceof JasonVar) && s.isVar()) {
			throw new IllegalArgumentException("term is a var:" + s);
		}
	}

	@Override
	public Term applySubst(Substitution substitution) {
		return makeTerm(substitute(substitution),
				(JasonSourceInfo) getSourceInfo());
	}

	/**
	 * Factory method to make terms. Checks what the term contains
	 * 
	 * @param t
	 * @param jasonSourceInfo
	 * @return
	 */
	public static Term makeTerm(jason.asSyntax.Term t, JasonSourceInfo info) {
		if (t.isVar()) {
			return new JasonVar((VarTerm) t, info);
		}
		return new JasonTerm(t, info);
	}
}